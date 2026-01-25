FROM maven:3.9-eclipse-temurin-21 AS build
RUN ln -snf /usr/share/zoneinfo/UTC /etc/localtime && echo UTC > /etc/timezone
COPY ./pom.xml /demo/pom.xml
WORKDIR /demo
RUN mvn dependency:go-offline
COPY ./src/ /demo/src
WORKDIR /demo
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /demo
COPY --from=build /demo/target/monitoring-0.0.1-SNAPSHOT.jar /demo/monitoring.jar
ENTRYPOINT ["java", "-jar", "/demo/monitoring.jar"]
