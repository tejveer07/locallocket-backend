# ---------- Build stage (Maven + JDK) ----------
# Use a known-working Maven image with Temurin JDK 21 on Docker Hub
FROM maven:3.9.3-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom first to cache dependencies
COPY pom.xml .

# Pre-download dependencies for faster builds
RUN mvn -B dependency:go-offline

# Copy source and package
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Runtime stage (smaller image) ----------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the fat jar produced by Spring Boot (wildcard to avoid name issues)
COPY --from=build /app/target/*.jar app.jar

# Important: Back4App uses a PORT env variable for health checks; we expose 8080
ENV PORT=8080
EXPOSE 8080

# Entrypoint runs the jar
ENTRYPOINT ["sh","-c","java -jar /app/app.jar"]
