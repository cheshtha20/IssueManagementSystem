# Stage 1: Build stage with Maven and Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY frontend ./frontend
COPY src ./src

# Build frontend and backend into a single executable JAR file
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime stage with Java 17 JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy executable jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Render sets PORT dynamically, default to 8080
ENV PORT=8080
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
