# Simple Dockerfile for Spring Boot runtime
FROM eclipse-temurin:17-jre-alpine AS runtime

# Create app directory
WORKDIR /app

# Copy the jar from build context (expecting app.jar at repository root)
COPY app.jar /app/app.jar

# Expose default Spring port
EXPOSE 8080

# Use a non-root user for security
RUN addgroup -S app && adduser -S app -G app
USER app

ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-XX:InitialRAMPercentage=50.0","-jar","/app/app.jar"]
