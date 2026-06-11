##Build
#FROM maven:3.9.6-eclipse-temurin-21 AS builder
#
#WORKDIR /app
#
## Copy pom.xml and download dependencies first (layer caching)
#COPY pom.xml .
#RUN mvn dependency:go-offline -B
#
## Copy source code and build
#COPY src ./src
#RUN mvn clean package -DskipTests -B
#
## Stage 2: Run
#FROM eclipse-temurin:21-jre-alpine
#
#WORKDIR /app
#
## Copy the built JAR from builder stage
#COPY --from=builder /app/target/*.jar app.jar
#
## Expose the application port
#EXPOSE 8080
#
## Run the application
#ENTRYPOINT ["java", "-jar", "app.jar"]
#



FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
