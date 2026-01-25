FROM maven:3.9-eclipse-temurin-21 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY ./pom.xml /simulator/pom.xml
WORKDIR /simulator
RUN mvn dependency:go-offline

COPY ./src/ /simulator/src
WORKDIR /simulator
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /simulator
COPY --from=build /simulator/target/energy-simulator-1.0.0.jar /simulator/energy-simulator.jar

ENTRYPOINT ["java", "-jar", "/simulator/energy-simulator.jar"]
