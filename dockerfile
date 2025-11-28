# Fase de construcción
FROM maven:3.8.6-eclipse-temurin-17 AS builder
WORKDIR /workspace/app

# Configurar encoding
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

COPY pom.xml .
RUN mvn dependency:go-offline -Dfile.encoding=UTF-8

COPY src src
RUN mvn clean package -DskipTests -Dfile.encoding=UTF-8

# Fase de ejecución
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Variables de entorno
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dspring.profiles.active=prod"
ENV PORT 8080

COPY --from=builder /workspace/app/target/*.jar app.jar

EXPOSE 8080

# Comando para Render
CMD ["java", "-jar", "-Dserver.port=8080", "app.jar"]