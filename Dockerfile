FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy your built JAR file
COPY target/locallocket-0.0.1-SNAPSHOT.jar app.jar

# Expose the correct port
EXPOSE 8080

# Pass Render's PORT dynamically (important)
ENV PORT=8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
