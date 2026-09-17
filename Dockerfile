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

# JVM optimizations for Render's 512MB memory limit and fast startup
ENV JAVA_OPTS="-Xmx300m \
  -Xms128m \
  -Xss512k \
  -XX:+UseSerialGC \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=60.0 \
  -XX:TieredStopAtLevel=1 \
  -XX:+OptimizeStringConcat \
  -Djava.awt.headless=true \
  -Dspring.jmx.enabled=false \
  -Dlogging.level.root=WARN"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
