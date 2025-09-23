FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven wrapper files (only if they exist)
COPY mvnw* ./
COPY .mvn .mvn/
COPY pom.xml .

# Make mvnw executable (in case it's not)
RUN chmod +x mvnw

# Download dependencies first (for better Docker layer caching)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src/

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "target/locallocket-backend-0.0.1-SNAPSHOT.jar"]
