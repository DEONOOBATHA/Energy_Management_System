FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
COPY --from=builder /app/target/load-balancer-1.0.0.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
