# Dockerfile
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the jar file
COPY target/digital-wallet-*.jar app.jar

# Expose port
EXPOSE 8080

# Environment variables
ENV SPRING_PROFILES_ACTIVE=prod

#Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]