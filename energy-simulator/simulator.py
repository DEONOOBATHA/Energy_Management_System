import pika
import json
import random
import time
from datetime import datetime, timedelta
import logging
import os
import psycopg2

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# RabbitMQ Configuration
RABBITMQ_HOST = os.getenv('RABBITMQ_HOST', 'localhost')
RABBITMQ_PORT = int(os.getenv('RABBITMQ_PORT', '5672'))
RABBITMQ_USER = os.getenv('RABBITMQ_USER', 'root')
RABBITMQ_PASS = os.getenv('RABBITMQ_PASS', 'root')
ENERGY_EXCHANGE = 'energy.events'
ENERGY_ROUTING_KEY = 'energy.hourly'

# Database Configuration
DB_HOST = os.getenv('DB_HOST', 'localhost')
DB_PORT = int(os.getenv('DB_PORT', '5432'))
DB_NAME = os.getenv('DB_NAME', 'device')
DB_USER = os.getenv('DB_USER', 'postgres')
DB_PASSWORD = os.getenv('DB_PASSWORD', 'root')

# Simulation Configuration
INTERVAL_SECONDS = int(os.getenv('INTERVAL_SECONDS', '10'))  # Send data every 10 seconds
DEVICE_ID = os.getenv('DEVICE_ID', '')  # Single device ID from environment
DEVICES = []  # Will be populated from environment or database

def connect_rabbitmq():
    """Connect to RabbitMQ with retry logic"""
    max_retries = 5
    retry_delay = 5
    
    for attempt in range(max_retries):
        try:
            credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
            parameters = pika.ConnectionParameters(
                host=RABBITMQ_HOST,
                port=RABBITMQ_PORT,
                credentials=credentials,
                heartbeat=600,
                blocked_connection_timeout=300
            )
            connection = pika.BlockingConnection(parameters)
            channel = connection.channel()
            
            # Declare exchange (idempotent)
            channel.exchange_declare(exchange=ENERGY_EXCHANGE, exchange_type='topic', durable=True)
            
            logger.info(f"Connected to RabbitMQ at {RABBITMQ_HOST}:{RABBITMQ_PORT}")
            return connection, channel
        except Exception as e:
            logger.error(f"Failed to connect to RabbitMQ (attempt {attempt + 1}/{max_retries}): {e}")
            if attempt < max_retries - 1:
                logger.info(f"Retrying in {retry_delay} seconds...")
                time.sleep(retry_delay)
            else:
                raise

def get_user_id_from_db(device_id):
    """Fetch user_id from database for the given device_id"""
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            database=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD
        )
        cursor = conn.cursor()
        
        # Query user_device table to find user_id for this device
        cursor.execute(
            "SELECT user_id FROM user_device WHERE device_id = %s",
            (device_id,)
        )
        result = cursor.fetchone()
        
        cursor.close()
        conn.close()
        
        if result:
            user_id = str(result[0])
            logger.info(f"Found user_id={user_id} for device_id={device_id}")
            return user_id
        else:
            logger.warning(f"No user association found for device_id={device_id}")
            return None
            
    except Exception as e:
        logger.error(f"Database error while fetching user_id: {e}")
        return None

def load_devices():
    """Load device-user mappings from environment or use defaults"""
    # First, try to load single DEVICE_ID from environment
    if DEVICE_ID:
        logger.info(f"Loading device from DEVICE_ID: {DEVICE_ID}")
        user_id = get_user_id_from_db(DEVICE_ID)
        if user_id:
            DEVICES.append({'deviceId': DEVICE_ID, 'userId': user_id})
            logger.info(f"Successfully loaded device {DEVICE_ID} with user {user_id}")
        else:
            logger.error(f"Could not find user for device {DEVICE_ID}")
        return
    
    # Otherwise, try to load multiple devices from DEVICES env var
    devices_env = os.getenv('DEVICES', '')
    
    if devices_env:
        # Format: "deviceId1:userId1,deviceId2:userId2,..."
        for pair in devices_env.split(','):
            device_id, user_id = pair.strip().split(':')
            DEVICES.append({'deviceId': device_id, 'userId': user_id})
        logger.info(f"Loaded {len(DEVICES)} devices from environment")
    else:
        # Default test devices (replace with actual UUIDs after creating devices)
        DEVICES.extend([
            {'deviceId': '00000000-0000-0000-0000-000000000001', 'userId': '00000000-0000-0000-0000-000000000001'},
            {'deviceId': '00000000-0000-0000-0000-000000000002', 'userId': '00000000-0000-0000-0000-000000000002'}
        ])
        logger.warning("Using default test device UUIDs - update with real devices!")

def generate_energy_data(device):
    """Generate realistic energy consumption data"""
    current_time = datetime.now()
    hour = current_time.hour
    
    # Simulate realistic consumption patterns (higher during day, lower at night)
    base_consumption = random.uniform(0.5, 2.0)
    if 6 <= hour <= 22:  # Daytime
        consumption = base_consumption * random.uniform(1.2, 2.5)
    else:  # Nighttime
        consumption = base_consumption * random.uniform(0.3, 0.8)
    
    return {
        'deviceId': device['deviceId'],
        'userId': device['userId'],
        'energyValue': round(consumption, 2),
        'timestamp': current_time.isoformat(),
        'hour': hour
    }

def publish_energy_data(channel, data):
    """Publish energy consumption data to RabbitMQ"""
    try:
        message = json.dumps(data)
        channel.basic_publish(
            exchange=ENERGY_EXCHANGE,
            routing_key=ENERGY_ROUTING_KEY,
            body=message,
            properties=pika.BasicProperties(
                delivery_mode=2,  # Make message persistent
                content_type='application/json'
            )
        )
        logger.info(f"Published: Device={data['deviceId'][:8]}..., Hour={data['hour']}, Energy={data['energyValue']} kWh")
    except Exception as e:
        logger.error(f"Failed to publish message: {e}")
        raise

def main():
    """Main simulation loop"""
    logger.info("Starting Energy Consumption Simulator...")
    
    load_devices()
    
    if not DEVICES:
        logger.error("No devices configured! Set DEVICES environment variable or update defaults.")
        return
    
    connection, channel = connect_rabbitmq()
    
    try:
        logger.info(f"Simulating energy consumption for {len(DEVICES)} devices every {INTERVAL_SECONDS} seconds")
        logger.info("Press Ctrl+C to stop")
        
        while True:
            for device in DEVICES:
                energy_data = generate_energy_data(device)
                publish_energy_data(channel, energy_data)
            
            time.sleep(INTERVAL_SECONDS)
            
    except KeyboardInterrupt:
        logger.info("Simulator stopped by user")
    except Exception as e:
        logger.error(f"Simulator error: {e}")
    finally:
        if connection and connection.is_open:
            connection.close()
            logger.info("RabbitMQ connection closed")

if __name__ == '__main__':
    main()
