FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
# Especificar codificación UTF-8
RUN mvn clean package -DskipTests -Dproject.build.sourceEncoding=UTF-8