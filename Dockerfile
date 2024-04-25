# Utilizar una imagen base oficial de Maven para la compilación
FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /app
# Copiar el archivo pom.xml y los archivos de la aplicación
COPY pom.xml .
COPY src ./src
# Construir la aplicación con Maven
RUN mvn -f pom.xml clean package

# Utilizar una imagen base de OpenJDK para ejecutar la aplicación
FROM openjdk:11-jre-slim
WORKDIR /app
# Copiar el archivo jar desde el contenedor de compilación al contenedor de ejecución
COPY --from=build /app/target/*.war zendeskfive9IVR-0.0.1-SNAPSHOT.war
# Expone el puerto en el que la aplicación se ejecutará
EXPOSE 8080
# Comando para ejecutar la aplicación
CMD ["java", "-jar", "zendeskfive9IVR-0.0.1-SNAPSHOT.war"]