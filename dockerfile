FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copiar archivos del proyecto
COPY . .

# Instalar Maven y construir la aplicación
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

# Exponer puerto y ejecutar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/*.jar"]