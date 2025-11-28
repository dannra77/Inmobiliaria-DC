# Fase de construcción
FROM maven:3.8.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY . .
RUN mvn clean package -DskipTests -Dfile.encoding=UTF-8

# Fase de ejecución
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]