FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copiar archivos del proyecto
COPY . .

# Construir la aplicación
RUN ./mvnw clean package -DskipTests

# Ejecutar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/*.jar"]