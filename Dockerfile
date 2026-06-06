# ☕ Step 1: Build the application using a stable Maven + Eclipse Temurin Java 17 image
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 🚀 Step 2: Use a clean Eclipse Temurin runtime image to execute the JAR
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]