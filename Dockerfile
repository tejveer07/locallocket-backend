# Use OpenJDK 21 for Spring Boot 3.x
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application (using Maven wrapper or preinstalled Maven)
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests

# Expose application port (Render uses 8080 by default)
EXPOSE 8080

# Set environment variable for server port (Spring Boot picks it automatically)
ENV PORT=8080

# Run the built JAR (note: no "-backend" in filename)
ENTRYPOINT ["java", "-jar", "target/locallocket-0.0.1-SNAPSHOT.jar"]
