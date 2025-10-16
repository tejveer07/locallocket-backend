# --------------------------
# Stage 1: Build the JAR
# --------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy Maven files and source code
COPY pom.xml .
COPY src ./src

# Build the Spring Boot JAR without running tests
RUN mvn clean package -DskipTests

# --------------------------
# Stage 2: Create runtime image
# --------------------------
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy the built JAR from the build stage and rename it to app.jar
COPY --from=build /app/target/*.jar app.jar

# Expose default Spring Boot port
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]



#This was working for Back4app
## Stage 1: Build JAR using Maven
#FROM maven:3.9.6-eclipse-temurin-21 AS build
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src
#RUN mvn clean package -DskipTests
#
## Stage 2: Run the built JAR
#FROM eclipse-temurin:21-jdk
#WORKDIR /app
#COPY --from=build /app/target/*.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","app.jar"]



## Use Java 21 JDK for building
#FROM eclipse-temurin:21-jdk-jammy
#
#WORKDIR /app
#
## Copy everything
#COPY . .
#
## Make Maven wrapper executable (if using mvnw)
#RUN chmod +x mvnw || true
#
## Build with Maven
#RUN ./mvnw clean package -DskipTests -B || mvn clean package -DskipTests -B
#
## Expose port
#EXPOSE 8080
#
## Run application - use the actual JAR location
#ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","target/locallocket-0.0.1-SNAPSHOT.jar"]
