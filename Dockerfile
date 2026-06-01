FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Instalar el JAR local de Weka en el repositorio de Maven
COPY src/main/resources/lib/weka-3-8-0-monolithic.jar /tmp/weka.jar
RUN mvn install:install-file -Dfile=/tmp/weka.jar -DgroupId=nz.ac.waikato.cms.weka -DartifactId=weka-stable -Dversion=3.8.0 -Dpackaging=jar
# Descargar otras dependencias
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/id3-web-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
