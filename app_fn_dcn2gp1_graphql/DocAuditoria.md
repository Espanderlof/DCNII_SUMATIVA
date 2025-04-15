# API de Auditoría con GraphQL y Azure Functions

Este proyecto implementa una API de auditoría utilizando GraphQL y Azure Functions, que permite consultar información detallada sobre la actividad de los usuarios en el sistema, incluyendo eventos de log, roles asignados y toda la información relacionada con la seguridad y auditoría.

## Uso de la API

### Punto de Acceso

- Local: `http://localhost:7071/api/auditoria`
- Azure: `https://dcn2gp1graphql.azurewebsites.net/api/auditoria`

### Método

Todas las consultas se realizan mediante peticiones HTTP POST.

### Ejemplos de Consultas

Consultar un usuario por ID:

```json
{
  "query": "query { usuario(idUsuario: \"1\") { idUsuario username email roles { nombre } logs { fechaEvento tipoEvento accion } } }"
}
```

## Función de Depuración

Se incluye una función de depuración para verificar la conexión a la base de datos Oracle:

- Local: `http://localhost:7071/api/debug`
- Azure: `https://dcn2gp1graphql.azurewebsites.net/api/debug`