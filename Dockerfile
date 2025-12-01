# Etapa de construcción
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar solo pom.xml primero para cache de dependencias
COPY pom.xml .
COPY src ./src

# Instalar Maven si no está en el repositorio
RUN apk add --no-cache maven

# Construir la aplicación
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Variables de entorno para Java
ENV JAVA_OPTS=""

# Copiar el jar desde la etapa de construcción
COPY --from=builder /app/target/Inmobiliaria-DC-*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Comando de ejecución
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]