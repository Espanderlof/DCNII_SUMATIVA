## Configuración Previa

- **URL Base**: `http://localhost:8090/api`
- **Tipo de Contenido**: `application/json`

## Gestión de Usuarios

### 1. Obtener todos los usuarios

**Solicitud:**
```
GET /api/usuarios
```

**Ejemplo con cURL:**
```bash
curl -X GET http://localhost:8090/api/usuarios
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Usuarios encontrados",
  "data": [
    {
      "idUsuario": 1,
      "username": "admin",
      "email": "admin@example.com",
      "nombre": "Administrador",
      "apellido": "Sistema",
      "fechaCreacion": "2025-03-01T00:00:00",
      "activo": true
    },
    {
      "idUsuario": 2,
      "username": "jzapata",
      "email": "jaime.zapata.salinas7@gmail.com",
      "nombre": "Jaime",
      "apellido": "Zapata",
      "fechaCreacion": "2025-03-28T10:15:20.123456",
      "activo": true
    }
  ],
  "error": null
}
```

### 2. Obtener usuario por ID

**Solicitud:**
```
GET /api/usuarios/{id}
```

**Ejemplo con cURL:**
```bash
curl -X GET http://localhost:8090/api/usuarios/1
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Usuario encontrado",
  "data": {
    "idUsuario": 1,
    "username": "admin",
    "email": "admin@example.com",
    "nombre": "Administrador",
    "apellido": "Sistema",
    "fechaCreacion": "2025-03-01T00:00:00",
    "activo": true
  },
  "error": null
}
```

### 3. Crear usuario

**Solicitud:**
```
POST /api/usuarios
Content-Type: application/json

{
  "username": "nuevo_usuario",
  "email": "nuevo@example.com",
  "password": "Password123",
  "nombre": "Nuevo",
  "apellido": "Usuario"
}
```

**Ejemplo con cURL:**
```bash
curl -X POST http://localhost:8090/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "username": "nuevo_usuario",
    "email": "nuevo@example.com",
    "password": "Password123",
    "nombre": "Nuevo",
    "apellido": "Usuario"
  }'
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Usuario creado correctamente",
  "data": {
    "idUsuario": 3,
    "username": "nuevo_usuario",
    "email": "nuevo@example.com",
    "nombre": "Nuevo",
    "apellido": "Usuario",
    "fechaCreacion": "2025-03-28T15:30:45.123456",
    "activo": true
  },
  "error": null
}
```

### 4. Actualizar usuario

**Solicitud:**
```
PUT /api/usuarios/{id}
Content-Type: application/json

{
  "username": "usuario_actualizado",
  "email": "actualizado@example.com",
  "nombre": "Usuario",
  "apellido": "Actualizado"
}
```

**Ejemplo con cURL:**
```bash
curl -X PUT http://localhost:8090/api/usuarios/2 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "usuario_actualizado",
    "email": "actualizado@example.com",
    "nombre": "Usuario",
    "apellido": "Actualizado"
  }'
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Usuario actualizado correctamente",
  "data": {
    "idUsuario": 2,
    "username": "usuario_actualizado",
    "email": "actualizado@example.com",
    "nombre": "Usuario",
    "apellido": "Actualizado",
    "fechaCreacion": "2025-03-28T10:15:20.123456",
    "fechaModificacion": "2025-03-28T16:45:10.123456",
    "activo": true
  },
  "error": null
}
```

### 5. Eliminar usuario

**Solicitud:**
```
DELETE /api/usuarios/{id}
```

**Ejemplo con cURL:**
```bash
curl -X DELETE http://localhost:8090/api/usuarios/3
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Usuario eliminado correctamente",
  "data": null,
  "error": null
}
```

### 6. Asignar rol a usuario

**Solicitud:**
```
POST /api/usuarios/{id}/roles
Content-Type: application/json

{
  "idRol": 2
}
```

**Ejemplo con cURL:**
```bash
curl -X POST http://localhost:8090/api/usuarios/1/roles \
  -H "Content-Type: application/json" \
  -d '{
    "idRol": 2
  }'
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Rol asignado correctamente",
  "data": [1, 2],
  "error": null
}
```

## Gestión de Roles

### 1. Obtener todos los roles

**Solicitud:**
```
GET /api/roles
```

**Ejemplo con cURL:**
```bash
curl -X GET http://localhost:8090/api/roles
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Roles encontrados",
  "data": [
    {
      "idRol": 1,
      "nombre": "ADMIN",
      "descripcion": "Administrador con acceso completo al sistema",
      "fechaCreacion": "2025-03-01T00:00:00",
      "activo": true
    },
    {
      "idRol": 2,
      "nombre": "USER",
      "descripcion": "Usuario estándar con acceso limitado",
      "fechaCreacion": "2025-03-01T00:00:00",
      "activo": true
    },
    {
      "idRol": 3,
      "nombre": "GUEST",
      "descripcion": "Usuario invitado con acceso de solo lectura",
      "fechaCreacion": "2025-03-01T00:00:00",
      "activo": true
    }
  ],
  "error": null
}
```

### 2. Obtener rol por ID

**Solicitud:**
```
GET /api/roles/{id}
```

**Ejemplo con cURL:**
```bash
curl -X GET http://localhost:8090/api/roles/1
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Rol encontrado",
  "data": {
    "idRol": 1,
    "nombre": "ADMIN",
    "descripcion": "Administrador con acceso completo al sistema",
    "fechaCreacion": "2025-03-01T00:00:00",
    "activo": true
  },
  "error": null
}
```

### 3. Crear rol

**Solicitud:**
```
POST /api/roles
Content-Type: application/json

{
  "nombre": "EDITOR",
  "descripcion": "Editor con permisos de modificación de contenido"
}
```

**Ejemplo con cURL:**
```bash
curl -X POST http://localhost:8090/api/roles \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "EDITOR",
    "descripcion": "Editor con permisos de modificación de contenido"
  }'
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Rol creado correctamente",
  "data": {
    "idRol": 4,
    "nombre": "EDITOR",
    "descripcion": "Editor con permisos de modificación de contenido",
    "fechaCreacion": "2025-03-28T17:20:30.123456",
    "activo": true
  },
  "error": null
}
```

### 4. Actualizar rol

**Solicitud:**
```
PUT /api/roles/{id}
Content-Type: application/json

{
  "nombre": "EDITOR_JEFE",
  "descripcion": "Editor jefe con permisos adicionales"
}
```

**Ejemplo con cURL:**
```bash
curl -X PUT http://localhost:8090/api/roles/4 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "EDITOR_JEFE",
    "descripcion": "Editor jefe con permisos adicionales"
  }'
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Rol actualizado correctamente",
  "data": {
    "idRol": 4,
    "nombre": "EDITOR_JEFE",
    "descripcion": "Editor jefe con permisos adicionales",
    "fechaCreacion": "2025-03-28T17:20:30.123456",
    "fechaModificacion": "2025-03-28T17:45:15.123456",
    "activo": true
  },
  "error": null
}
```

### 5. Eliminar rol

**Solicitud:**
```
DELETE /api/roles/{id}
```

**Ejemplo con cURL:**
```bash
curl -X DELETE http://localhost:8090/api/roles/4
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Rol eliminado correctamente",
  "data": null,
  "error": null
}
```

### 6. Obtener usuarios por rol

**Solicitud:**
```
GET /api/roles/{id}/usuarios
```

**Ejemplo con cURL:**
```bash
curl -X GET http://localhost:8090/api/roles/1/usuarios
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Usuarios del rol",
  "data": [1, 2],
  "error": null
}
```

## Ejemplos con Postman

### Colección de Postman

Para facilitar las pruebas, puedes importar la siguiente colección en Postman:

1. Crea una nueva colección llamada "BFF - Sistema de Gestión de Usuarios y Roles"
2. Configura la variable de entorno `baseUrl` con el valor `http://localhost:8090/api`
3. Añade las siguientes solicitudes:

#### Gestión de Usuarios

1. **Obtener todos los usuarios**
   - Método: GET
   - URL: `{{baseUrl}}/usuarios`

2. **Obtener un usuario específico**
   - Método: GET
   - URL: `{{baseUrl}}/usuarios/1`

3. **Crear usuario**
   - Método: POST
   - URL: `{{baseUrl}}/usuarios`
   - Body (raw JSON):
     ```json
     {
       "username": "nuevo_usuario",
       "email": "nuevo@example.com",
       "password": "Password123",
       "nombre": "Nuevo",
       "apellido": "Usuario"
     }
     ```

4. **Actualizar usuario**
   - Método: PUT
   - URL: `{{baseUrl}}/usuarios/2`
   - Body (raw JSON):
     ```json
     {
       "username": "usuario_actualizado",
       "email": "actualizado@example.com",
       "nombre": "Usuario",
       "apellido": "Actualizado"
     }
     ```

5. **Eliminar usuario**
   - Método: DELETE
   - URL: `{{baseUrl}}/usuarios/3`

6. **Asignar rol a usuario**
   - Método: POST
   - URL: `{{baseUrl}}/usuarios/1/roles`
   - Body (raw JSON):
     ```json
     {
       "idRol": 2
     }
     ```

#### Gestión de Roles

1. **Obtener todos los roles**
   - Método: GET
   - URL: `{{baseUrl}}/roles`

2. **Obtener un rol específico**
   - Método: GET
   - URL: `{{baseUrl}}/roles/1`

3. **Crear rol**
   - Método: POST
   - URL: `{{baseUrl}}/roles`
   - Body (raw JSON):
     ```json
     {
       "nombre": "EDITOR",
       "descripcion": "Editor con permisos para gestionar contenido"
     }
     ```

4. **Actualizar rol**
   - Método: PUT
   - URL: `{{baseUrl}}/roles/4`
   - Body (raw JSON):
     ```json
     {
       "nombre": "EDITOR_JEFE",
       "descripcion": "Editor jefe con permisos adicionales"
     }
     ```

5. **Eliminar rol**
   - Método: DELETE
   - URL: `{{baseUrl}}/roles/4`

6. **Obtener usuarios por rol**
   - Método: GET
   - URL: `{{baseUrl}}/roles/1/usuarios`

## Manejo de Errores

### Ejemplos de Respuestas de Error

#### Usuario no encontrado

```json
{
  "success": false,
  "message": "Usuario no encontrado",
  "data": null,
  "error": "No se encontró un usuario con el ID proporcionado"
}
```

#### Error de validación

```json
{
  "success": false,
  "message": "Datos inválidos",
  "data": null,
  "error": "Los campos username, email y password son obligatorios"
}
```

#### Error de conflicto

```json
{
  "success": false,
  "message": "Conflicto",
  "data": null,
  "error": "Ya existe un usuario con ese nombre de usuario o email"
}
```

#### Error interno del servidor

```json
{
  "success": false,
  "message": "Error interno del servidor",
  "data": null,
  "error": "Error al comunicarse con el servicio: Connection timed out"
}
```