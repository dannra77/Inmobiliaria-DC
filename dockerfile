# Fase de construcción
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace/app

# Copiar archivos de construcción
COPY pom.xml .
COPY src src

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Fase de ejecución
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=builder /workspace/app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]