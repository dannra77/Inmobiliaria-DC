# Fase de construcción
FROM maven:3.8.6-eclipse-temurin-17 AS builder
WORKDIR /workspace/app

# Configurar encoding UTF-8 desde el inicio
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

# Copiar archivo de dependencias primero
COPY pom.xml .
RUN mvn dependency:go-offline -Dfile.encoding=UTF-8

# Copiar código fuente
COPY src src

# Compilar con encoding explícito
RUN mvn clean package -DskipTests -Dfile.encoding=UTF-8

# Fase de ejecución
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Configurar encoding también en runtime
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

COPY --from=builder /workspace/app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]