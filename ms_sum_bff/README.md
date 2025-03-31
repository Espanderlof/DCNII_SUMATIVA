# APP_SPRING

## Limpiar proyecto e instalar
mvn clean install

## Iniciar aplicacion
mvn spring-boot:run

## Construir imagen
docker build -t ms_sum_bff .

## Levantar contendor docker primera vez
docker run -d -p 8090:8090 --name ms_sum_bff ms_sum_bff

## Levantar contenedor docker
sudo docker start ms_sum_bff

## Ver logs del contenedor
docker logs ms_sum_bff

## Ver todos los contenedores
docker ps -a

## Detener contenedor
docker stop ms_sum_bff

## eliminar contenedor
docker rm ms_sum_bff

## DockerHub
1. Crear repo en https://hub.docker.com/
2. Primero, asegúrate de estar logueado en Docker Hub desde tu terminal docker login
3. Identifica tu imagen local. Puedes ver tus imágenes locales con: docker images
4. Etiqueta tu imagen local con el formato requerido por Docker Hub: Por ejemplo, si tu imagen local se llama "backend-app:1.0", los comandos serían: docker tag ms_sum_bff espanderlof/dcn2_sum_bff:latest 
5. Para subir la imagen al repositorio seria: docker push espanderlof/dcn2_sum_bff:latest

## Cargar contenedor en Azure MV
1. Traer el contenedor: docker pull espanderlof/dcn2_sum_bff:latest
2. Levantar el contenedor: docker run -d -p 8090:8090 --name ms_sum_bff espanderlof/dcn2_sum_bff:latest