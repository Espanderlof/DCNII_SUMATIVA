package com.function.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.function.dao.UsuarioDAO;
import com.function.util.DBConnectionManager;
import com.function.util.EventGridPublisher;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Función que asigna automáticamente roles a usuarios nuevos y elimina roles cuando estos son eliminados.
 */
public class RolAutomaticoFunction {

    private static final Long ROL_POR_DEFECTO = 2L;
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FunctionName("rolAutomatico")
    public void run(
        @EventGridTrigger(name = "eventGridEvent") String content,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("Función de asignación automática de roles ejecutada.");

        try {
            // Parsear el evento recibido
            JsonObject eventGridEvent = JsonParser.parseString(content).getAsJsonObject();
            
            logger.info("============== EVENTO DE ROL AUTOMÁTICO ==============");
            logger.info("Evento recibido: " + eventGridEvent.toString());
            
            String eventType = eventGridEvent.get("eventType").getAsString();
            String dataStr = eventGridEvent.get("data").toString();
            JsonObject data = JsonParser.parseString(dataStr).getAsJsonObject();
            
            logger.info("Tipo de evento: " + eventType);
            logger.info("Datos del evento: " + dataStr);
            
            // Procesar según el tipo de evento
            switch (eventType) {
                case "user_created":
                    asignarRolPorDefecto(data, logger);
                    break;
                case "role_deleted":
                    eliminarRolAsignado(data, logger);
                    break;
                default:
                    logger.info("Tipo de evento no procesado por esta función: " + eventType);
                    break;
            }
            
            logger.info("Evento procesado correctamente");
            logger.info("==================================================");
            
        } catch (Exception e) {
            logger.severe("Error al procesar el evento: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Asigna el rol por defecto a un usuario recién creado.
     * 
     * @param data Datos del evento
     * @param logger Logger para registrar información
     */
    private void asignarRolPorDefecto(JsonObject data, Logger logger) {
        try {
            // Extraer idUsuario considerando diferentes estructuras posibles
            Long idUsuario = null;
            if (data.has("idUsuario")) {
                idUsuario = data.get("idUsuario").getAsLong();
            } else if (data.has("members") && data.getAsJsonObject("members").has("idUsuario")) {
                JsonObject idUsuarioObj = data.getAsJsonObject("members").getAsJsonObject("idUsuario");
                if (idUsuarioObj.has("value")) {
                    idUsuario = idUsuarioObj.get("value").getAsLong();
                }
            }
            
            if (idUsuario == null) {
                logger.warning("El evento no contiene el ID del usuario en un formato reconocible");
                return;
            }
            
            // Extraer username considerando diferentes estructuras posibles
            String username = "desconocido";
            if (data.has("username")) {
                username = data.get("username").getAsString();
            } else if (data.has("members") && data.getAsJsonObject("members").has("username")) {
                JsonObject usernameObj = data.getAsJsonObject("members").getAsJsonObject("username");
                if (usernameObj.has("value")) {
                    username = usernameObj.get("value").getAsString();
                }
            }
            
            logger.info("Asignando rol por defecto " + ROL_POR_DEFECTO + " al usuario " + idUsuario);
            
            // Verificar si el usuario ya tiene el rol asignado
            List<Long> rolesActuales = usuarioDAO.getRoles(idUsuario);
            if (rolesActuales.contains(ROL_POR_DEFECTO)) {
                logger.info("El usuario ya tiene el rol por defecto asignado");
                return;
            }
            
            // Asignar el rol por defecto
            boolean resultado = usuarioDAO.asignarRol(idUsuario, ROL_POR_DEFECTO);
            
            if (resultado) {
                logger.info("Rol por defecto asignado correctamente al usuario " + idUsuario);
                
                // Publicar evento de asignación de rol
                JsonObject dataEvento = new JsonObject();
                dataEvento.addProperty("idUsuario", idUsuario);
                dataEvento.addProperty("idRol", ROL_POR_DEFECTO);
                dataEvento.addProperty("rolNombre", "USER"); // Asumiendo que el rol 2 es "USER"
                dataEvento.addProperty("username", username);
                dataEvento.addProperty("asignacionAutomatica", true);

                EventGridPublisher.publishEvent(
                    "/usuarios/roles/asignacion_automatica",
                    "role_assigned_auto",
                    dataEvento
                );
            } else {
                logger.warning("No se pudo asignar el rol por defecto al usuario " + idUsuario);
            }
            
        } catch (Exception e) {
            logger.severe("Error al asignar rol por defecto: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Elimina un rol de todos los usuarios que lo tengan asignado cuando el rol es eliminado.
     * 
     * @param data Datos del evento
     * @param logger Logger para registrar información
     */
    private void eliminarRolAsignado(JsonObject data, Logger logger) {
        try {
            // Extraer idRol considerando diferentes estructuras posibles
            Long idRol = null;
            String nombreRol = "desconocido";
            
            // Intenta extraer de la estructura anidada con members
            if (data.has("members")) {
                JsonObject members = data.getAsJsonObject("members");
                
                // Obtener idRol
                if (members.has("idRol")) {
                    JsonObject idRolObj = members.getAsJsonObject("idRol");
                    if (idRolObj.has("value")) {
                        idRol = idRolObj.get("value").getAsLong();
                    }
                }
                
                // Obtener nombre del rol
                if (members.has("nombre")) {
                    JsonObject nombreObj = members.getAsJsonObject("nombre");
                    if (nombreObj.has("value")) {
                        nombreRol = nombreObj.get("value").getAsString();
                    }
                }
            } 
            // Estructura simple
            else if (data.has("idRol")) {
                idRol = data.get("idRol").getAsLong();
                if (data.has("nombre")) {
                    nombreRol = data.get("nombre").getAsString();
                }
            }
            
            if (idRol == null) {
                logger.warning("El evento no contiene el ID del rol en un formato reconocible");
                return;
            }
            
            logger.info("Eliminando asignaciones del rol " + idRol + " para todos los usuarios");
            
            // Obtener todos los usuarios que tienen este rol
            List<Long> usuariosAfectados = obtenerUsuariosConRol(idRol);
            
            if (usuariosAfectados.isEmpty()) {
                logger.info("No hay usuarios con el rol " + idRol + " asignado");
                return;
            }
            
            logger.info("Se encontraron " + usuariosAfectados.size() + " usuarios con el rol " + idRol);
            
            // Eliminar el rol de cada usuario
            int usuariosActualizados = eliminarRolDeUsuarios(idRol);
            
            logger.info("Se eliminó el rol " + idRol + " de " + usuariosActualizados + " usuarios");
            
            // Publicar evento informativo
            for (Long idUsuario : usuariosAfectados) {
                JsonObject dataEvento = new JsonObject();
                dataEvento.addProperty("idUsuario", idUsuario);
                dataEvento.addProperty("idRol", idRol);
                dataEvento.addProperty("rolNombre", nombreRol);
                dataEvento.addProperty("eliminacionAutomatica", true);

                EventGridPublisher.publishEvent(
                    "/usuarios/roles/eliminacion_automatica",
                    "role_removed_auto",
                    dataEvento
                );
            }
            
        } catch (Exception e) {
            logger.severe("Error al eliminar rol asignado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obtiene la lista de usuarios que tienen un rol específico.
     * 
     * @param idRol ID del rol
     * @return Lista de IDs de usuarios
     * @throws SQLException Si ocurre un error en la base de datos
     */
    private List<Long> obtenerUsuariosConRol(Long idRol) throws SQLException {
        List<Long> usuarios = new ArrayList<>();
        String sql = "SELECT id_usuario FROM sum_usuario_rol WHERE id_rol = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idRol);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(rs.getLong("id_usuario"));
                }
            }
        }
        
        return usuarios;
    }
    
    /**
     * Elimina un rol de todos los usuarios que lo tengan asignado.
     * 
     * @param idRol ID del rol a eliminar
     * @return Número de usuarios actualizados
     * @throws SQLException Si ocurre un error en la base de datos
     */
    private int eliminarRolDeUsuarios(Long idRol) throws SQLException {
        String sql = "DELETE FROM sum_usuario_rol WHERE id_rol = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idRol);
            
            return stmt.executeUpdate();
        }
    }
}