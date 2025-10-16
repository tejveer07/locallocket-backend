# Use Java 21 JDK
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy everything
COPY . .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Build with Maven wrapper (uses Java 21 from container)
RUN ./mvnw clean package -DskipTests -B

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "target/locallocket-backend-0.0.1-SNAPSHOT.jar"]
