# syntax=docker/dockerfile:1
# Enable BuildKit

# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Create empty .env if it doesn't exist
RUN touch .env
COPY .env .
# Download all required dependencies into one layer
RUN mvn dependency:go-offline -B
COPY src ./src
# Build with limited memory usage for more stability
RUN mvn clean package -DskipTests -Dmaven.test.skip=true -Dmaven.compiler.fork=true -Dmaven.compiler.maxmem=512m

# Production stage
FROM eclipse-temurin:21-jre
WORKDIR /app
# Create empty .env if it doesn't exist
RUN touch .env
COPY .env .
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
