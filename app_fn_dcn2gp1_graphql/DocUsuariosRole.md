# Consulta de Usuarios por Rol con GraphQL

Esta función permite obtener información detallada sobre los usuarios que tienen asignado un rol específico en el sistema, utilizando consultas GraphQL para personalizar la información solicitada.

## Uso de la API

### Punto de Acceso

- Local: `http://localhost:7071/api/usuariosByRole`
- Azure: `https://dcn2gp1graphql.azurewebsites.net/api/usuariosByRole`

### Método

Todas las consultas se realizan mediante peticiones HTTP POST.

### Modos de Uso

#### 1. Modo Simplificado

Solo envía las variables con el rol que deseas consultar:

```json
{
  "variables": {
    "idRol": "1"
  }
}
```

O por nombre del rol:

```json
{
  "variables": {
    "nombreRol": "ADMIN"
  }
}
```

#### 2. Modo Flexible

Envía tu propia consulta GraphQL personalizada:

```json
{
  "query": "query GetBasicUserInfo($nombreRol: String!) { usuariosConRolNombre(nombreRol: $nombreRol) { idUsuario username email nombre apellido } }",
  "variables": {
    "nombreRol": "ADMIN"
  },
  "operationName": "GetBasicUserInfo"
}
```

### Ejemplos de Consultas

#### Obtener usuarios con rol ADMIN (básico)

```json
{
  "variables": {
    "nombreRol": "ADMIN"
  }
}
```

#### Obtener usuarios con rol ADMIN (consulta personalizada sin logs)

```json
{
  "query": "query GetAdminUsers($nombreRol: String!) { usuariosConRolNombre(nombreRol: $nombreRol) { idUsuario username email nombre apellido roles { nombre descripcion } } }",
  "variables": {
    "nombreRol": "ADMIN"
  },
  "operationName": "GetAdminUsers"
}
```

#### Obtener usuarios con rol ID 1 incluyendo logs

```json
{
  "query": "query GetUsersWithLogs($idRol: ID!) { usuariosConRol(idRol: $idRol) { idUsuario username email nombre apellido logs { fechaEvento tipoEvento modulo accion nivel } } }",
  "variables": {
    "idRol": "1"
  },
  "operationName": "GetUsersWithLogs"
}
```

### Campos Disponibles

Puedes solicitar cualquiera de estos campos en tus consultas:

- **Usuario**: idUsuario, username, email, nombre, apellido, fechaCreacion, fechaModificacion, ultimoLogin, activo
- **Roles**: idRol, nombre, descripcion, fechaCreacion, fechaModificacion, activo
- **Logs**: idLog, fechaEvento, tipoEvento, modulo, accion, entidad, nivel, etc.

### IDs de Usuario para Pruebas

Los siguientes IDs están disponibles para pruebas:
- 1 (admin)
- 2
- 4
- 5
- 6

## Respuestas

La función devuelve un objeto JSON con el formato estándar de GraphQL:

```json
{
  "data": {
    "usuariosConRolNombre": [
      {
        "idUsuario": "1",
        "username": "admin",
        "email": "admin@example.com"
      },
      {
        "idUsuario": "5",
        "username": "supervisor",
        "email": "supervisor@example.com"
      }
    ]
  }
}
```

En caso de errores:

```json
{
  "errors": [
    {
      "message": "Error específico",
      "locations": [{"line": 1, "column": 53}]
    }
  ],
  "data": null
}
```