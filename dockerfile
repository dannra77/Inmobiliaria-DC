FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copiar archivos de Maven primero
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copiar código fuente
COPY src src

# Dar permisos y construir
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Ejecutar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/*.jar"]