package com.function.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.function.util.DBConnectionManager;
import com.function.util.GsonConfig;

/**
 * Función que recibe eventos de Event Grid y los registra en la tabla de auditoría.
 */
public class AuditEventFunction {

    private final Gson gson = GsonConfig.getGson();

    @FunctionName("auditarEventos")
    public void run(
        @EventGridTrigger(name = "eventGridEvent") String content,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("Función de auditoría de eventos ejecutada.");

        try {
            // Parsear el evento recibido
            JsonObject eventGridEvent = JsonParser.parseString(content).getAsJsonObject();
            
            logger.info("============== EVENTO DE AUDITORÍA ==============");
            logger.info("Evento recibido: " + eventGridEvent.toString());
            
            String eventType = eventGridEvent.get("eventType").getAsString();
            String dataStr = eventGridEvent.get("data").toString();
            JsonObject data = JsonParser.parseString(dataStr).getAsJsonObject();
            
            logger.info("Tipo de evento: " + eventType);
            logger.info("Datos del evento: " + dataStr);
            
            // Registrar el evento en la tabla de auditoría
            registrarEventoAuditoria(eventType, data, logger);
            
            logger.info("Evento registrado correctamente en el sistema de auditoría");
            logger.info("==================================================");
            
        } catch (Exception e) {
            // Captura y loguea cualquier excepción
            logger.severe("Error al procesar el evento de auditoría: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Registra un evento en la tabla de auditoría SUM_LOG_EVENTOS.
     * 
     * @param eventType Tipo de evento
     * @param data Datos del evento en formato JSON
     * @param logger Logger para registrar información
     * @throws SQLException Si ocurre un error en la base de datos
     */
    private void registrarEventoAuditoria(String eventType, JsonObject data, Logger logger) throws SQLException {
        // SQL para insertar en la tabla de auditoría
        String sql = "INSERT INTO SUM_LOG_EVENTOS (ID_LOG, ID_USUARIO, USERNAME, TIPO_EVENTO, " +
                     "MODULO, ACCION, ENTIDAD, ID_AFECTADO, DATOS_PREVIOS, DATOS_NUEVOS, IP_ORIGEN, NIVEL) " +
                     "VALUES (SUM_LOG_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Valores predeterminados
            Long idUsuario = null;
            String username = "sistema";
            String modulo = "SYSTEM";
            String accion = eventType;
            String entidad = null;
            Long idAfectado = null;
            String datosPrevios = null;
            String datosNuevos = null;
            String ipOrigen = null;
            String nivel = "INFO";
            
            // Extraer información del evento según su tipo
            switch (eventType) {
                case "user_created":
                    modulo = "USUARIOS";
                    accion = "Creación de usuario";
                    entidad = "SUM_USUARIOS";
                    if (data.has("idUsuario")) {
                        idAfectado = data.get("idUsuario").getAsLong();
                    }
                    if (data.has("username")) {
                        username = data.get("username").getAsString();
                    }
                    datosNuevos = data.toString();
                    break;
                    
                case "user_updated":
                    modulo = "USUARIOS";
                    accion = "Actualización de usuario";
                    entidad = "SUM_USUARIOS";
                    if (data.has("idUsuario")) {
                        idAfectado = data.get("idUsuario").getAsLong();
                    }
                    if (data.has("username")) {
                        username = data.get("username").getAsString();
                    }
                    if (data.has("datosPrevios")) {
                        datosPrevios = data.get("datosPrevios").toString();
                    }
                    if (data.has("datosNuevos")) {
                        datosNuevos = data.get("datosNuevos").toString();
                    }
                    break;
                    
                case "user_deleted":
                    modulo = "USUARIOS";
                    accion = "Eliminación de usuario";
                    entidad = "SUM_USUARIOS";
                    if (data.has("idUsuario")) {
                        idAfectado = data.get("idUsuario").getAsLong();
                    }
                    if (data.has("username")) {
                        username = data.get("username").getAsString();
                    }
                    if (data.has("datosPrevios")) {
                        datosPrevios = data.get("datosPrevios").toString();
                    }
                    break;
                    
                case "role_created":
                    modulo = "ROLES";
                    accion = "Creación de rol";
                    entidad = "SUM_ROLES";
                    if (data.has("idRol")) {
                        idAfectado = data.get("idRol").getAsLong();
                    }
                    if (data.has("idUsuario")) {
                        idUsuario = data.get("idUsuario").getAsLong();
                    }
                    if (data.has("username")) {
                        username = data.get("username").getAsString();
                    }
                    datosNuevos = data.toString();
                    break;
                    
                case "role_updated":
                    modulo = "ROLES";
                    accion = "Actualización de rol";
                    entidad = "SUM_ROLES";
                    if (data.has("idRol")) {
                        idAfectado = data.get("idRol").getAsLong();
                    }
                    if (data.has("idUsuario")) {
                        idUsuario = data.get("idUsuario").getAsLong();
                    }
                    if (data.has("username")) {
                        username = data.get("username").getAsString();
                    }
                    if (data.has("datosPrevios")) {
                        datosPrevios = data.get("datosPrevios").toString();
                    }
                    if (data.has("datosNuevos")) {
                        datosNuevos = data.get("datosNuevos").toString();
                    }
                    break;
                    
                case "role_deleted":
                    modulo = "ROLES";
                    accion = "Eliminación de rol";
                    entidad = "SUM_ROLES";
                    if (data.has("idRol")) {
                        idAfectado = data.get("idRol").getAsLong();
                    }
                    if (data.has("idUsuario")) {
                        idUsuario = data.get("idUsuario").getAsLong();
                    }
                    if (data.has("username")) {
                        username = data.get("username").getAsString();
                    }
                    if (data.has("datosPrevios")) {
                        datosPrevios = data.get("datosPrevios").toString();
                    }
                    break;
                    
                case "role_assigned":
                    modulo = "ROLES";
                    accion = "Asignación de rol a usuario";
                    entidad = "SUM_USUARIO_ROL";
                    if (data.has("idUsuario")) {
                        idAfectado = data.get("idUsuario").getAsLong();
                        idUsuario = data.get("idUsuario").getAsLong();
                    }
                    if (data.has("username")) {
                        username = data.get("username").getAsString();
                    }
                    if (data.has("idRol")) {
                        datosNuevos = "{\"idRol\": " + data.get("idRol").getAsString() + "}";
                    }
                    break;
                    
                default:
                    logger.warning("Tipo de evento no reconocido: " + eventType);
                    break;
            }
            
            // Preparar los parámetros de la consulta
            stmt.setObject(1, idUsuario);
            stmt.setString(2, username);
            stmt.setString(3, eventType);
            stmt.setString(4, modulo);
            stmt.setString(5, accion);
            stmt.setString(6, entidad);
            stmt.setObject(7, idAfectado);
            stmt.setString(8, datosPrevios);
            stmt.setString(9, datosNuevos);
            stmt.setString(10, ipOrigen);
            stmt.setString(11, nivel);
            
            // Ejecutar la inserción
            int filasAfectadas = stmt.executeUpdate();
            logger.info("Registro de auditoría insertado. Filas afectadas: " + filasAfectadas);
            
        } catch (SQLException e) {
            logger.severe("Error al insertar en la tabla de auditoría: " + e.getMessage());
            throw e;
        }
    }
}