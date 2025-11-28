# Fase de construcción
FROM maven:3.8.6-eclipse-temurin-17 AS builder
WORKDIR /workspace/app

# Copiar archivo de dependencias primero (para cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar código fuente y compilar
COPY src src
RUN mvn clean package -DskipTests

# Fase de ejecución
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Instalar wait-for-it para esperar por la base de datos
RUN apt-get update && apt-get install -y wait-for-it

COPY --from=builder /workspace/app/target/*.jar app.jar

EXPOSE 8080

# Usar forma exec para mejor manejo de señales
ENTRYPOINT ["java", "-jar", "app.jar"]