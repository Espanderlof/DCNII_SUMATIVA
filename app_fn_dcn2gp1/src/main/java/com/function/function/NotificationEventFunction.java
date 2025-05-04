package com.function.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

import java.util.logging.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.function.util.GsonConfig;

/**
 * Función que recibe eventos de Event Grid y envía notificaciones.
 */
public class NotificationEventFunction {

    private final Gson gson = GsonConfig.getGson();

    @FunctionName("notificarEventos")
    public void run(
        @EventGridTrigger(name = "eventGridEvent") String content,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("Función de notificación de eventos ejecutada.");

        try {
            // Parsear el evento recibido
            JsonObject eventGridEvent = JsonParser.parseString(content).getAsJsonObject();
            
            logger.info("============== NOTIFICACIÓN DE EVENTO ==============");
            logger.info("Evento recibido: " + eventGridEvent.toString());
            
            String eventType = eventGridEvent.get("eventType").getAsString();
            String dataStr = eventGridEvent.get("data").toString();
            JsonObject data = JsonParser.parseString(dataStr).getAsJsonObject();
            
            logger.info("Tipo de evento: " + eventType);
            logger.info("Datos del evento: " + dataStr);
            
            // Procesar el evento según su tipo
            switch (eventType) {
                case "user_created":
                    notificarUsuarioCreado(data, logger);
                    break;
                case "user_updated":
                    notificarUsuarioActualizado(data, logger);
                    break;
                case "user_deleted":
                    notificarUsuarioEliminado(data, logger);
                    break;
                case "role_created":
                    notificarRolCreado(data, logger);
                    break;
                case "role_updated":
                    notificarRolActualizado(data, logger);
                    break;
                case "role_deleted":
                    notificarRolEliminado(data, logger);
                    break;
                case "role_assigned":
                    notificarRolAsignado(data, logger);
                    break;
                default:
                    logger.warning("Tipo de evento no reconocido: " + eventType);
                    break;
            }
            
            logger.info("Notificación enviada correctamente");
            logger.info("==================================================");
            
        } catch (Exception e) {
            // Captura y loguea cualquier excepción
            logger.severe("Error al procesar la notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Envía una notificación cuando se crea un usuario.
     */
    private void notificarUsuarioCreado(JsonObject data, Logger logger) {
        // Extraer información relevante
        String username = data.has("username") ? data.get("username").getAsString() : "desconocido";
        String email = data.has("email") ? data.get("email").getAsString() : "desconocido";
        
        // En un sistema real, aquí enviaríamos un correo o notificación
        // Por ahora, solo registramos en el log
        logger.info("NOTIFICACIÓN: Nuevo usuario creado - " + username + " (" + email + ")");
        logger.info("Se enviaría un correo de bienvenida al usuario");
        
        // Aquí iría el código para enviar un correo electrónico
        // Por ejemplo:
        // emailService.sendEmail(email, "Bienvenido a nuestro sistema", "Contenido del mensaje...");
    }
    
    /**
     * Envía una notificación cuando se actualiza un usuario.
     */
    private void notificarUsuarioActualizado(JsonObject data, Logger logger) {
        String username = data.has("username") ? data.get("username").getAsString() : "desconocido";
        logger.info("NOTIFICACIÓN: Usuario actualizado - " + username);
        logger.info("Se enviaría un correo notificando los cambios en la cuenta");
    }
    
    /**
     * Envía una notificación cuando se elimina un usuario.
     */
    private void notificarUsuarioEliminado(JsonObject data, Logger logger) {
        String username = data.has("username") ? data.get("username").getAsString() : "desconocido";
        logger.info("NOTIFICACIÓN: Usuario eliminado - " + username);
        logger.info("Se enviaría un correo notificando la desactivación de la cuenta");
    }
    
    /**
     * Envía una notificación cuando se crea un rol.
     */
    private void notificarRolCreado(JsonObject data, Logger logger) {
        String nombre = data.has("nombre") ? data.get("nombre").getAsString() : "desconocido";
        logger.info("NOTIFICACIÓN: Nuevo rol creado - " + nombre);
        logger.info("Se notificaría a los administradores sobre el nuevo rol");
    }
    
    /**
     * Envía una notificación cuando se actualiza un rol.
     */
    private void notificarRolActualizado(JsonObject data, Logger logger) {
        String nombre = data.has("nombre") ? data.get("nombre").getAsString() : "desconocido";
        logger.info("NOTIFICACIÓN: Rol actualizado - " + nombre);
        logger.info("Se notificaría a los usuarios afectados por el cambio de rol");
    }
    
    /**
     * Envía una notificación cuando se elimina un rol.
     */
    private void notificarRolEliminado(JsonObject data, Logger logger) {
        String nombre = data.has("nombre") ? data.get("nombre").getAsString() : "desconocido";
        logger.info("NOTIFICACIÓN: Rol eliminado - " + nombre);
        logger.info("Se notificaría a los usuarios afectados por la eliminación del rol");
    }
    
    /**
     * Envía una notificación cuando se asigna un rol a un usuario.
     */
    private void notificarRolAsignado(JsonObject data, Logger logger) {
        String username = data.has("username") ? data.get("username").getAsString() : "desconocido";
        String rolNombre = data.has("rolNombre") ? data.get("rolNombre").getAsString() : "desconocido";
        
        logger.info("NOTIFICACIÓN: Rol asignado - Usuario: " + username + ", Rol: " + rolNombre);
        logger.info("Se notificaría al usuario sobre sus nuevos permisos");
    }
}