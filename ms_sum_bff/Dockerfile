# Primera etapa: Construcción
# Usar una imagen base de JDK para construir la aplicación
# Usamos una imagen base con Java 17
FROM eclipse-temurin:17-jdk-focal AS buildstage 

RUN apt-get update && apt-get install -y maven

# Directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el archivo pom.xml primero para aprovechar la caché de Docker
COPY pom.xml .

# Copiar el código fuente
COPY src /app/src

RUN mvn clean package -DskipTests

# Verificar el contenido del directorio target
RUN ls -la /app/target

# Segunda etapa: Ejecución
FROM eclipse-temurin:17-jdk-focal

# Directorio de trabajo en el contenedor
WORKDIR /app

# Copia el archivo jar al contenedor
COPY --from=buildstage /app/target/*.jar /app/app.jar

# Exponer el puerto 8090 (puerto típico de Spring Boot)
EXPOSE 8090

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "/app/app.jar"]