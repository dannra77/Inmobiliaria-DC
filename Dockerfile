FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
# Desactivar el filtrado de recursos
RUN mvn clean package -DskipTests -Dmaven.resources.filtering=false