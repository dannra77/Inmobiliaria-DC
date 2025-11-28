# Fase de construcción
FROM maven:3.8.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiar archivos del proyecto
COPY pom.xml .
COPY src src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Fase de ejecución
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copiar el JAR desde la fase de construcción
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]