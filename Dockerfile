# Multi-stage Dockerfile for Go Nature Farms Backend
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.compiler.fork=false

# Stage 2: Run with JVM optimizations for fast startup
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/gonaturefarms-backend.jar app.jar
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
EXPOSE 8080

# JVM optimizations for fast startup and low memory footprint
ENV JAVA_OPTS="-XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseG1GC \
  -XX:+UseStringDeduplication \
  -XX:+OptimizeStringConcat \
  -Djava.awt.headless=true \
  -Dspring.jmx.enabled=false"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
