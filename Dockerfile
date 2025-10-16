# Use Java 21 JDK for building
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy everything
COPY . .

# Make Maven wrapper executable (if using mvnw)
RUN chmod +x mvnw || true

# Build with Maven
RUN ./mvnw clean package -DskipTests -B || mvn clean package -DskipTests -B

# Expose port
EXPOSE 8080

# Run application - use the actual JAR location
ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","target/locallocket-0.0.1-SNAPSHOT.jar"]
