# Imagen base con Java 17
FROM eclipse-temurin:17-jdk-alpine

# Directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el archivo .jar al contenedor
COPY target/algoritmo-prediccion-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto que usa Spring Boot
EXPOSE 8080

# Ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
