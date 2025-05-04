# Implementación de Event Grid en el Sistema de Gestión de Usuarios y Roles

## Introducción

Este documento describe la implementación de Azure Event Grid en el Sistema de Gestión de Usuarios y Roles, explicando la arquitectura, componentes desarrollados y cómo interactúan entre sí. La solución permite el registro automático de auditoría y el envío de notificaciones cuando ocurren cambios importantes en el sistema.

## Arquitectura

La arquitectura implementada consta de los siguientes componentes:

1. **Funciones productoras de eventos**: Funciones REST existentes que ahora publican eventos en Event Grid
   - Gestión de Usuarios (UsuarioFunction)
   - Gestión de Roles (RolFunction)

2. **Topic de Event Grid**: "usuarios-roles-event"
   - Centraliza todos los eventos del sistema
   - URL: https://usuarios-roles-event.eastus2-1.eventgrid.azure.net/api/events

3. **Funciones consumidoras de eventos**: Nuevas funciones que se suscriben a los eventos
   - AuditEventFunction: Registra eventos en la tabla de auditoría SUM_LOG_EVENTOS
   - NotificationEventFunction: Procesa eventos para enviar notificaciones

4. **Suscripciones de Event Grid**: 
   - AuditoriaEventosSuscripcion: Conecta el topic con la función de auditoría
   - NotificacionesEventosSuscripcion: Conecta el topic con la función de notificaciones

## Componentes Desarrollados

### 1. Utilidad para Publicación de Eventos

Se ha creado la clase `EventGridPublisher.java` que facilita la publicación de eventos desde cualquier parte del código:

```java
public class EventGridPublisher {
    private static final String EVENT_GRID_ENDPOINT = "https://usuarios-roles-event.eastus2-1.eventgrid.azure.net/api/events";
    private static final String EVENT_GRID_KEY = "****************";
    
    public static boolean publishEvent(String source, String eventType, Object data) {
        // Código para publicar eventos en Event Grid
    }
}
```

### 2. Modificaciones a Funciones Existentes

Se ha modificado las funciones `UsuarioFunction.java` y `RolFunction.java` para publicar eventos después de cada operación CRUD:

```java
// Ejemplo de publicación de evento en UsuarioFunction.java
EventGridPublisher.publishEvent(
    "/usuarios/created",
    "user_created",
    usuarioParaEvento
);
```

### 3. Funciones Consumidoras de Eventos

#### AuditEventFunction.java

Función que recibe eventos y los registra en la tabla de auditoría SUM_LOG_EVENTOS:

```java
@FunctionName("auditarEventos")
public void run(
    @EventGridTrigger(name = "eventGridEvent") String content,
    final ExecutionContext context
) {
    // Código para registrar eventos en la tabla de auditoría
}
```

#### NotificationEventFunction.java

Función que recibe eventos y envía notificaciones según el tipo de evento:

```java
@FunctionName("notificarEventos")
public void run(
    @EventGridTrigger(name = "eventGridEvent") String content,
    final ExecutionContext context
) {
    // Código para enviar notificaciones
}
```

## Tipos de Eventos Implementados

El sistema publica y procesa los siguientes tipos de eventos:

| Evento | Origen | Descripción | Datos incluidos |
|--------|--------|-------------|----------------|
| `user_created` | `/usuarios/created` | Se crea un nuevo usuario | ID, username, email, nombre, apellido, fechaCreacion |
| `user_updated` | `/usuarios/updated` | Se actualiza un usuario existente | ID, datos previos, datos nuevos |
| `user_deleted` | `/usuarios/deleted` | Se elimina un usuario (baja lógica) | ID, username, email, datos previos |
| `role_created` | `/roles/created` | Se crea un nuevo rol | ID, nombre, descripción, fechaCreacion |
| `role_updated` | `/roles/updated` | Se actualiza un rol existente | ID, datos previos, datos nuevos |
| `role_deleted` | `/roles/deleted` | Se elimina un rol (baja lógica) | ID, nombre, datos previos |
| `role_assigned` | `/usuarios/roles/assigned` | Se asigna un rol a un usuario | ID usuario, ID rol, nombre rol, username |

## Activación de las Funciones Consumidoras

Las funciones consumidoras se activan automáticamente cuando reciben eventos del topic de Event Grid a través de sus respectivas suscripciones. A continuación se detalla cuándo y por qué se activa cada función:

### AuditEventFunction (Función de Auditoría)

**Suscripción**: AuditoriaEventosSuscripcion

**Se activa cuando**:
- Un usuario es creado, actualizado o eliminado
- Un rol es creado, actualizado o eliminado
- Un rol es asignado a un usuario

**Acciones realizadas**:
1. Recibe el evento a través del trigger de Event Grid
2. Parsea el contenido del evento para extraer la información relevante
3. Identifica el tipo de evento (`user_created`, `role_assigned`, etc.)
4. Extrae los datos específicos del evento según su tipo
5. Formatea la información para el registro de auditoría
6. Inserta un nuevo registro en la tabla `SUM_LOG_EVENTOS` con los siguientes datos:
   - ID_LOG: Generado automáticamente
   - FECHA_EVENTO: Timestamp actual
   - ID_USUARIO: ID del usuario que realizó la acción (si está disponible)
   - USERNAME: Nombre de usuario que realizó la acción
   - TIPO_EVENTO: Tipo de evento (CREATE, UPDATE, DELETE, ASSIGN_ROLE, etc.)
   - MODULO: Módulo del sistema (USUARIOS, ROLES)
   - ACCION: Descripción detallada de la acción
   - ENTIDAD: Tabla afectada (SUM_USUARIOS, SUM_ROLES, SUM_USUARIO_ROL)
   - ID_AFECTADO: ID del registro afectado
   - DATOS_PREVIOS: JSON con estado previo (para updates y deletes)
   - DATOS_NUEVOS: JSON con estado nuevo (para creates y updates)
   - NIVEL: Nivel de importancia (INFO, WARNING, etc.)

### NotificationEventFunction (Función de Notificaciones)

**Suscripción**: NotificacionesEventosSuscripcion

**Se activa cuando**:
- Un usuario es creado (para enviar bienvenida)
- Un usuario es actualizado (para informar de cambios)
- Un usuario es eliminado (para informar desactivación)
- Un rol es asignado a un usuario (para informar nuevos permisos)

**Acciones realizadas**:
1. Recibe el evento a través del trigger de Event Grid
2. Parsea el contenido del evento para extraer la información relevante
3. Identifica el tipo de evento
4. Según el tipo de evento, genera la notificación apropiada:
   - Para `user_created`: Notificación de bienvenida al nuevo usuario
   - Para `user_updated`: Notificación al usuario sobre cambios en su cuenta
   - Para `user_deleted`: Notificación sobre desactivación de cuenta
   - Para `role_created`: Notificación a administradores sobre nuevo rol
   - Para `role_assigned`: Notificación al usuario sobre nuevos permisos

Actualmente, las notificaciones solo se registran en logs, pero el sistema está diseñado para integrarse fácilmente con servicios de correo electrónico, SMS o otros canales de notificación.

## Ejemplos de Eventos Publicados

### Evento de Creación de Usuario

```json
{
  "id": "5d04d6a3-7d24-42b9-b1a8-3c7eb7c2e5a2",
  "source": "/usuarios/created",
  "data": {
    "idUsuario": 8,
    "username": "jperez",
    "email": "juan.perez@example.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "fechaCreacion": "2025-04-30T15:45:22.341Z"
  },
  "eventType": "user_created",
  "eventTime": "2025-04-30T15:45:22.341Z",
  "dataVersion": "1.0"
}
```

### Evento de Asignación de Rol

```json
{
  "id": "7c9e4b1a-5e36-48c3-90ab-d2f8e6b42a5c",
  "source": "/usuarios/roles/assigned",
  "data": {
    "idUsuario": 8,
    "idRol": 2,
    "rolNombre": "USER",
    "username": "jperez"
  },
  "eventType": "role_assigned",
  "eventTime": "2025-04-30T15:47:15.789Z",
  "dataVersion": "1.0"
}
```

## Beneficios de la Implementación

1. **Desacoplamiento**: Las funciones principales se centran en su lógica de negocio mientras que la auditoría y notificaciones se manejan por separado.

2. **Escalabilidad**: Cada componente puede escalarse independientemente según sus necesidades.

3. **Extensibilidad**: Se pueden agregar nuevos consumidores de eventos sin modificar las funciones existentes.

4. **Seguridad**: Se registran automáticamente todas las acciones importantes en el sistema.

5. **Fiabilidad**: Event Grid garantiza la entrega de todos los eventos, incluso en caso de fallos temporales.