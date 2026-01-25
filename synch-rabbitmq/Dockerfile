# Dockerfile for RabbitMQ deployment on Heroku
# Uses official RabbitMQ image with management plugin

FROM rabbitmq:3-management

# Copy custom configuration files
COPY config/rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
COPY config/rabbitmq-env.conf /etc/rabbitmq/rabbitmq-env.conf

# Create startup script that handles Heroku's dynamic PORT
RUN echo '#!/bin/bash\n\
set -e\n\
\n\
# If PORT environment variable is set (Heroku), use it for RabbitMQ\n\
if [ -n "$PORT" ]; then\n\
  echo "Heroku PORT detected: $PORT"\n\
  export RABBITMQ_NODE_PORT=$PORT\n\
  echo "RabbitMQ will listen on port $PORT"\n\
else\n\
  echo "No PORT env variable, using default port 5672"\n\
fi\n\
\n\
# Start RabbitMQ server\n\
exec docker-entrypoint.sh rabbitmq-server' > /usr/local/bin/start-rabbitmq.sh \
  && chmod +x /usr/local/bin/start-rabbitmq.sh

# Expose ports
# Note: On Heroku, only $PORT is used and dynamically assigned
EXPOSE 5672 15672

# Use custom startup script
CMD ["/usr/local/bin/start-rabbitmq.sh"]
