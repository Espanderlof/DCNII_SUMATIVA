# Documentación del Sistema de Gestión de Usuarios y Roles

## Introducción

Este documento describe la implementación del Sistema de Gestión de Usuarios y Roles desarrollado como parte del proyecto Cloud Native. El sistema está compuesto por funciones serverless implementadas en Azure Functions usando Java, que permiten realizar operaciones CRUD sobre usuarios y roles en una base de datos Oracle Cloud.

## Arquitectura del Sistema

El sistema implementa una arquitectura serverless basada en Azure Functions con una base de datos Oracle Cloud. Los componentes principales son:

1. **Azure Functions:** Implementan la lógica de negocio para gestionar usuarios y roles.
2. **Base de datos Oracle Cloud:** Almacena la información de usuarios, roles y sus relaciones.

## Funciones Serverless Implementadas

Se han implementado las siguientes funciones HTTP en Azure Functions:

1. **Usuarios** (`/api/usuarios`): Gestión CRUD de usuarios
2. **Roles** (`/api/roles`): Gestión CRUD de roles
3. **AsignarRol** (`/api/usuarios/{id}/roles`): Asignación de roles a usuarios
4. **UsuariosRol** (`/api/roles/{id}/usuarios`): Obtención de usuarios asignados a un rol
5. **Debug** (`/api/debug`): Función auxiliar para depuración

## Estructura de la Base de Datos

La base de datos Oracle Cloud contiene las siguientes tablas:

1. **sum_usuarios**: Almacena información de los usuarios
2. **sum_roles**: Almacena información de los roles
3. **sum_usuario_rol**: Tabla de relación entre usuarios y roles

## Detalles de las API

### 1. Gestión de Usuarios

#### 1.1 Obtener todos los usuarios

- **Endpoint:** `GET /api/usuarios`
- **Descripción:** Devuelve todos los usuarios registrados
- **Respuesta exitosa:**
  - **Código:** 200 OK
  - **Cuerpo:**
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
      // más usuarios...
    ]
  }
  ```

#### 1.2 Obtener usuario por ID

- **Endpoint:** `GET /api/usuarios/{id}`
- **Descripción:** Devuelve un usuario específico por su ID
- **Respuesta exitosa:**
  - **Código:** 200 OK
  - **Cuerpo:**
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
    }
  }
  ```
- **Respuesta de error:**
  - **Código:** 404 Not Found
  - **Cuerpo:**
  ```json
  {
    "success": false,
    "message": "Usuario no encontrado",
    "error": "No se encontró un usuario con el ID proporcionado"
  }
  ```

#### 1.3 Crear usuario

- **Endpoint:** `POST /api/usuarios`
- **Descripción:** Crea un nuevo usuario
- **Cuerpo de la solicitud:**
  ```json
  {
    "username": "nuevo_usuario",
    "email": "usuario@example.com",
    "password": "contrasena123",
    "nombre": "Nuevo",
    "apellido": "Usuario"
  }
  ```
- **Respuesta exitosa:**
  - **Código:** 201 Created
  - **Cuerpo:**
  ```json
  {
    "success": true,
    "message": "Usuario creado correctamente",
    "data": {
      "idUsuario": 2,
      "username": "nuevo_usuario",
      "email": "usuario@example.com",
      "nombre": "Nuevo",
      "apellido": "Usuario",
      "fechaCreacion": "2025-03-27T01:45:20.123456",
      "activo": true
    }
  }
  ```

#### 1.4 Actualizar usuario

- **Endpoint:** `PUT /api/usuarios/{id}`
- **Descripción:** Actualiza la información de un usuario existente
- **Cuerpo de la solicitud:**
  ```json
  {
    "username": "usuario_actualizado",
    "email": "usuario_act@example.com",
    "nombre": "Usuario",
    "apellido": "Actualizado"
  }
  ```
- **Respuesta exitosa:**
  - **Código:** 200 OK
  - **Cuerpo:**
  ```json
  {
    "success": true,
    "message": "Usuario actualizado correctamente",
    "data": {
      "idUsuario": 2,
      "username": "usuario_actualizado",
      "email": "usuario_act@example.com",
      "nombre": "Usuario",
      "apellido": "Actualizado",
      "fechaCreacion": "2025-03-27T01:45:20.123456",
      "fechaModificacion": "2025-03-27T01:50:10.123456",
      "activo": true
    }
  }
  ```

#### 1.5 Eliminar usuario

- **Endpoint:** `DELETE /api/usuarios/{id}`
- **Descripción:** Desactiva un usuario (borrado lógico)
- **Respuesta exitosa:**
  - **Código:** 200 OK
  - **Cuerpo:**
  ```json
  {
    "success": true,
    "message": "Usuario eliminado correctamente",
    "data": null
  }
  ```

#### 1.6 Asignar rol a usuario

- **Endpoint:** `POST /api/usuarios/{id}/roles`
- **Descripción:** Asigna un rol a un usuario
- **Cuerpo de la solicitud:**
  ```json
  {
    "idRol": 1
  }
  ```
- **Respuesta exitosa:**
  - **Código:** 200 OK
  - **Cuerpo:**
  ```json
  {
    "success": true,
    "message": "Rol asignado correctamente",
    "data": [1, 2] // IDs de los roles asignados al usuario
  }
  ```

### 2. Gestión de Roles

#### 2.1 Obtener todos los roles

- **Endpoint:** `GET /api/roles`
- **Descripción:** Devuelve todos los roles disponibles
- **Respuesta exitosa:**
  - **Código:** 200 OK
  - **Cuerpo:**
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
      // más roles...
    ]
  }
  ```

#### 2.2 Obtener rol por ID

- **Endpoint:** `GET /api/roles/{id}`
- **Descripción:** Devuelve un rol específico por su ID
- **Respuesta exitosa:**
  - **Código:** 200 OK
  - **Cuerpo:**
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
    }
  }
  ```

#### 2.3 Crear rol

- **Endpoint:** `POST /api/roles`
- **Descripción:** Crea un nuevo rol
- **Cuerpo de la solicitud:**
  ```json
  {
    "nombre": "EDITOR",
    "descripcion": "Editor con permisos de modificación de contenido"
  }
  ```
- **Respuesta exitosa:**
  - **Código:** 201 Created
  - **Cuerpo:**
  ```json
  {
    "success": true,
    "message": "Rol creado correctamente",
    "data": {
      "idRol": 4,
      "nombre": "EDITOR",
      "descripcion": "Editor con permisos de modificación de contenido",
      "fechaCreacion": "2025-03-27T01:55:30.123456",
      "activo": true
    }
  }
  ```

#### 2.4 Actualizar rol

- **Endpoint:** `PUT /api/roles/{id}`
- **Descripción:** Actualiza la información de un rol existente
- **Cuerpo de la solicitud:**
  ```json
  {
    "nombre": "EDITOR_JEFE",
    "descripcion": "Editor jefe con permisos adicionales"
  }
  ```
- **Respuesta exitosa:**
  - **Código:** 200 OK
  - **Cuerpo:**
  ```json
  {
    "success": true,
    "message": "Rol actualizado correctamente",
    "data": {
      "idRol": 4,
      "nombre": "EDITOR_JEFE",
      "descripcion": "Editor jefe con permisos adicionales",
      "fechaCreacion": "2025-03-27T01:55:30.123456",
      "fechaModificacion": "2025-03-27T02:00:15.123456",
      "activo": true
    }
  }
  ```

#### 2.5 Eliminar rol

- **Endpoint:** `DELETE /api/roles/{id}`
- **Descripción:** Desactiva un rol (borrado lógico)
- **Respuesta exitosa:**
  - **Código:** 200 OK
  - **Cuerpo:**
  ```json
  {
    "success": true,
    "message": "Rol eliminado correctamente",
    "data": null
  }
  ```

#### 2.6 Obtener usuarios por rol

- **Endpoint:** `GET /api/roles/{id}/usuarios`
- **Descripción:** Obtiene los IDs de usuarios que tienen asignado un rol específico
- **Respuesta exitosa:**
  - **Código:** 200 OK
  - **Cuerpo:**
  ```json
  {
    "success": true,
    "message": "Usuarios del rol",
    "data": [1, 3, 5] // IDs de los usuarios con este rol
  }
  ```

### 3. Depuración

#### 3.1 Información de depuración

- **Endpoint:** `GET /api/debug`
- **Descripción:** Proporciona información detallada sobre la configuración y el estado del sistema
- **Respuesta exitosa:**
  - **Código:** 200 OK
  - **Cuerpo:** Información en formato texto plano sobre variables de entorno, directorios, wallet y estado de la conexión a la base de datos