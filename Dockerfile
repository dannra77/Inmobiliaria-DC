# Etapa de construcción
FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el JAR
COPY --from=builder /app/target/Inmobiliaria-DC-*.jar app.jar

# Exponer el puerto (Render usa 10000 pero Spring leerá $PORT)
EXPOSE 10000

# Usar la variable PORT de Render
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]