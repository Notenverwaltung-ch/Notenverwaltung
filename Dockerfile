# Simple multi-stage Dockerfile for Spring Boot (Gradle)
FROM eclipse-temurin:17-jre-alpine AS runtime

# The JAR will be copied in at build time by the workflow/build context
ARG JAR_FILE=build/libs/*.jar

# Create app directory
WORKDIR /app

# Copy the jar from build context
COPY ${JAR_FILE} app.jar

# Expose default Spring port
EXPOSE 8080

# Use a non-root user for security
RUN addgroup -S app && adduser -S app -G app
USER app

ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-XX:InitialRAMPercentage=50.0","-jar","/app/app.jar"]
