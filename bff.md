# Desarrollo del Microservicio BFF para el Sistema de Gestión de Usuarios y Roles

## Introducción

Este documento describe el diseño e implementación del microservicio BFF (Backend For Frontend) que orquestará las llamadas a las funciones serverless del Sistema de Gestión de Usuarios y Roles.

## Arquitectura del BFF

El BFF será implementado como un microservicio usando Spring Boot, que actuará como intermediario entre los clientes (frontend o sistemas externos) y las funciones serverless desplegadas en Azure Functions.

## Objetivos del BFF

1. Proporcionar un punto de entrada único para los clientes
2. Orquestar llamadas a múltiples funciones serverless
3. Transformar y adaptar datos según sea necesario
4. Simplificar la interfaz para los clientes
5. Implementar lógica de negocio adicional si es necesario

## Tecnologías a utilizar

- Java 17
- Spring Boot 3.x
- Spring WebClient para comunicación con las funciones
- Spring Security para autenticación

## Endpoints del BFF

El BFF expondrá endpoints RESTful que reflejen la funcionalidad de las funciones serverless subyacentes:

### Gestión de Usuarios

- `GET /api/usuarios` - Obtener todos los usuarios
- `GET /api/usuarios/{id}` - Obtener usuario por ID
- `POST /api/usuarios` - Crear usuario
- `PUT /api/usuarios/{id}` - Actualizar usuario
- `DELETE /api/usuarios/{id}` - Eliminar usuario
- `POST /api/usuarios/{id}/roles` - Asignar rol a usuario

### Gestión de Roles

- `GET /api/roles` - Obtener todos los roles
- `GET /api/roles/{id}` - Obtener rol por ID
- `POST /api/roles` - Crear rol
- `PUT /api/roles/{id}` - Actualizar rol
- `DELETE /api/roles/{id}` - Eliminar rol
- `GET /api/roles/{id}/usuarios` - Obtener usuarios por rol

## Estructura del Proyecto

```
com.duoc.bff/
├── config/
│   ├── WebClientConfig.java
│   └── SecurityConfig.java (opcional)
├── controller/
│   ├── UsuarioController.java
│   └── RolController.java
├── service/
│   ├── UsuarioService.java
│   └── RolService.java
├── model/
│   ├── Usuario.java
│   ├── Rol.java
│   └── Response.java
├── exception/
│   └── GlobalExceptionHandler.java
└── BffApplication.java
```

## Comunicación con Azure Functions

El BFF se comunicará con las funciones serverless mediante solicitudes HTTP usando WebClient de Spring. Ejemplo:

```java
@Service
public class UsuarioService {
    
    private final WebClient webClient;
    private final String functionBaseUrl = "https://dcn2gp1.azurewebsites.net/api";
    
    public UsuarioService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(functionBaseUrl).build();
    }
    
    public Mono<ResponseEntity<Object>> getAllUsuarios() {
        return webClient.get()
                .uri("/usuarios")
                .retrieve()
                .toEntity(Object.class);
    }
    
    // Otros métodos para interactuar con las funciones de usuarios
}
```

## Despliegue

El BFF será desplegado como un contenedor Docker, siguiendo las prácticas de CI/CD para entornos cloud native.