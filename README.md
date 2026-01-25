# Energy Management System

Distributed microservices architecture for real-time energy consumption monitoring with Docker Swarm, RabbitMQ, and WebSocket notifications.

## Architecture

**Services:** 7 microservices (auth, user, device, monitoring x3, chat, websocket) + load balancer + frontend  
**Message Brokers:** 2 RabbitMQ instances (sync + data collection)  
**Databases:** 6 PostgreSQL databases  
**Gateway:** Traefik reverse proxy  
**AI:** Google Gemini chatbot integration

### Communication Flow

Client → Traefik → Services → PostgreSQL  
Simulators → RabbitMQ → Load Balancer → Monitoring (3 replicas)  
Monitoring → RabbitMQ → WebSocket → Frontend

## Tech Stack

**Backend:** Java 17, Spring Boot 3.2, Spring Security, JPA  
**Frontend:** React 18, Vite, Nginx  
**Infrastructure:** Docker Swarm, Traefik 2.10, RabbitMQ 3, PostgreSQL 15

## Quick Start

### Prerequisites
- Docker Desktop with Swarm mode
- 8GB+ RAM
- Ports: 80, 5672-5673, 8080-8090, 15672-15673

### Installation

1. **Clone and configure**
```bash
git clone https://github.com/your-username/energy-management-system.git
cd energy-management-system
echo "GEMINI_API_KEY=your-key-here" > .env
```

2. **Initialize Swarm**
```powershell
docker swarm init
```

3. **Build images**
```powershell
docker build -t auth-service ./auth-service
docker build -t user_microservice ./user_microservice
docker build -t device_microservice ./device_microservice
docker build -t monitoring_microservice ./monitoring_microservice
docker build -t chat-service ./chat-service
docker build -t websocket ./websocket
docker build -t load-balancer ./load-balancer
docker build -t frontend ./frontend
docker build -t synch-rabbitmq ./synch-rabbitmq
```

4. **Deploy**
```powershell
docker stack deploy -c main-stack.yml sd
```

5. **Verify**
```powershell
docker stack ps sd --filter "desired-state=running"
```

## Service Endpoints

| Service | Port | Access |
|---------|------|--------|
| Frontend | 80 | http://localhost |
| Auth API | 8080 | via Traefik |
| User API | 8081 | via Traefik |
| Device API | 8082 | via Traefik |
| Monitoring | 8083 | Load Balancer |
| Chat API | 8085 | via Traefik |
| WebSocket | 8086 | ws://localhost:8086 |
| RabbitMQ UI | 15672 | http://localhost:15672 (root/root) |

## Configuration

**Load Balancing:** `load-balancer/src/main/resources/application.properties`
```properties
load-balancer.strategy=consistent-hashing
load-balancer.replicas=3
```

**Monitoring Replicas:** `main-stack.yml`
```yaml
monitoring-service:
  replicas: 3
```

## Testing

**Run energy simulator:**
```powershell
docker run -d --network sd_default `
  -e DEVICE_ID=test-device-001 `
  -e RABBITMQ_HOST=data-collection-broker `
  energy-simulator:latest
```

**View logs:**
```powershell
docker service logs sd_monitoring-service --follow
```

## Key Features

- **High Availability:** 3 monitoring replicas with consistent hashing
- **Real-Time:** WebSocket overconsumption notifications
- **AI Support:** Rule-based + Gemini AI chatbot
- **Scalability:** Event-driven with RabbitMQ, horizontal scaling
- **Security:** JWT authentication, role-based access

## Troubleshooting

**Services not starting:** Check `docker service logs sd_<service-name>`  
**WebSocket issues:** Verify websocket service: `docker service ps sd_websocket`  
**No data flow:** Check RabbitMQ queues at http://localhost:15673

## Project Structure

```
SD/
├── auth-service/          # JWT authentication
├── user_microservice/     # User CRUD
├── device_microservice/   # Device management
├── monitoring_microservice/ # Energy data (3 replicas)
├── chat-service/          # AI chatbot
├── websocket/             # Real-time notifications
├── load-balancer/         # Consistent hashing
├── frontend/              # React UI
├── synch-rabbitmq/        # Event sync
├── data-collection-broker/ # Data ingestion
├── energy-simulator/      # Python simulator
├── gateway/               # Traefik config
└── main-stack.yml         # Swarm deployment
```
## License

MIT License - Free to use for personal and educational projects.

## Contact

Available for collaboration opportunities.
