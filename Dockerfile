# Usa una imagen oficial de Java 17
FROM openjdk:17-jdk-slim

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el archivo pom.xml y descargar dependencias (capa cacheable)
COPY pom.xml .
RUN apt-get update && apt-get install -y maven && \
    mvn dependency:go-offline && \
    apt-get remove -y maven && apt-get autoremove -y

# Copiar el resto del código fuente
COPY src ./src

# Compilar y empaquetar la aplicación
RUN mvn clean package -DskipTests

# Exponer el puerto que usará Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/id3-web-0.0.1-SNAPSHOT.jar"]
