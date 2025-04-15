package com.function.dao;

import com.function.model.LogEvento;
import com.function.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de acceso a datos para la entidad LogEvento.
 */
public class LogEventoDAO {
    private static final Logger logger = LoggerFactory.getLogger(LogEventoDAO.class);
    
    /**
     * Obtiene un log por su ID.
     *
     * @param idLog ID del log a buscar
     * @return LogEvento encontrado o null si no existe
     */
    public LogEvento obtenerLogPorId(Long idLog) {
        String sql = "SELECT * FROM sum_log_eventos WHERE id_log = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idLog);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearLogEvento(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener log por ID: " + idLog, e);
        }
        
        return null;
    }
    
    /**
     * Obtiene logs por usuario.
     *
     * @param idUsuario ID del usuario
     * @return Lista de logs del usuario
     */
    public List<LogEvento> obtenerLogsPorUsuario(Long idUsuario) {
        return obtenerLogsConFiltro("id_usuario = ?", idUsuario);
    }
    
    /**
     * Obtiene logs por tipo de evento.
     *
     * @param tipoEvento Tipo de evento a filtrar
     * @return Lista de logs del tipo especificado
     */
    public List<LogEvento> obtenerLogsPorTipoEvento(String tipoEvento) {
        return obtenerLogsConFiltro("tipo_evento = ?", tipoEvento);
    }
    
    /**
     * Obtiene logs por módulo.
     *
     * @param modulo Módulo a filtrar
     * @return Lista de logs del módulo especificado
     */
    public List<LogEvento> obtenerLogsPorModulo(String modulo) {
        return obtenerLogsConFiltro("modulo = ?", modulo);
    }
    
    /**
     * Obtiene logs por entidad.
     *
     * @param entidad Entidad a filtrar
     * @return Lista de logs de la entidad especificada
     */
    public List<LogEvento> obtenerLogsPorEntidad(String entidad) {
        return obtenerLogsConFiltro("entidad = ?", entidad);
    }
    
    /**
     * Obtiene logs por nivel.
     *
     * @param nivel Nivel a filtrar
     * @return Lista de logs del nivel especificado
     */
    public List<LogEvento> obtenerLogsPorNivel(String nivel) {
        return obtenerLogsConFiltro("nivel = ?", nivel);
    }
    
    /**
     * Obtiene logs por rango de fechas.
     *
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de logs en el rango especificado
     */
    public List<LogEvento> obtenerLogsPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<LogEvento> logs = new ArrayList<>();
        String sql = "SELECT * FROM sum_log_eventos WHERE fecha_evento BETWEEN ? AND ? ORDER BY fecha_evento DESC";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(fechaInicio));
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(fechaFin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapearLogEvento(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener logs por rango de fechas", e);
        }
        
        return logs;
    }
    
    /**
     * Obtiene logs por ID afectado.
     *
     * @param idAfectado ID del registro afectado
     * @return Lista de logs relacionados con el ID afectado
     */
    public List<LogEvento> obtenerLogsPorIdAfectado(Long idAfectado) {
        return obtenerLogsConFiltro("id_afectado = ?", idAfectado);
    }
    
    /**
     * Obtiene logs con una condición de filtro.
     *
     * @param condicion Condición SQL WHERE
     * @param parametro Valor del parámetro
     * @return Lista de logs que cumplen la condición
     */
    private List<LogEvento> obtenerLogsConFiltro(String condicion, Object parametro) {
        List<LogEvento> logs = new ArrayList<>();
        String sql = "SELECT * FROM sum_log_eventos WHERE " + condicion + " ORDER BY fecha_evento DESC";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (parametro instanceof String) {
                stmt.setString(1, (String) parametro);
            } else if (parametro instanceof Long) {
                stmt.setLong(1, (Long) parametro);
            } else if (parametro instanceof Integer) {
                stmt.setInt(1, (Integer) parametro);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapearLogEvento(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener logs con filtro: " + condicion, e);
        }
        
        return logs;
    }
    
    /**
     * Obtiene logs combinando múltiples filtros.
     *
     * @param idUsuario ID del usuario (opcional)
     * @param tipoEvento Tipo de evento (opcional)
     * @param modulo Módulo (opcional)
     * @param entidad Entidad (opcional)
     * @param nivel Nivel (opcional)
     * @param fechaInicio Fecha inicio (opcional)
     * @param fechaFin Fecha fin (opcional)
     * @return Lista de logs que cumplen todos los filtros aplicados
     */
    public List<LogEvento> obtenerLogsConFiltrosMultiples(
            Long idUsuario, String tipoEvento, String modulo, 
            String entidad, String nivel, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        
        List<LogEvento> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM sum_log_eventos WHERE 1=1");
        List<Object> parametros = new ArrayList<>();
        
        if (idUsuario != null) {
            sql.append(" AND id_usuario = ?");
            parametros.add(idUsuario);
        }
        
        if (tipoEvento != null && !tipoEvento.isEmpty()) {
            sql.append(" AND tipo_evento = ?");
            parametros.add(tipoEvento);
        }
        
        if (modulo != null && !modulo.isEmpty()) {
            sql.append(" AND modulo = ?");
            parametros.add(modulo);
        }
        
        if (entidad != null && !entidad.isEmpty()) {
            sql.append(" AND entidad = ?");
            parametros.add(entidad);
        }
        
        if (nivel != null && !nivel.isEmpty()) {
            sql.append(" AND nivel = ?");
            parametros.add(nivel);
        }
        
        if (fechaInicio != null && fechaFin != null) {
            sql.append(" AND fecha_evento BETWEEN ? AND ?");
            parametros.add(fechaInicio);
            parametros.add(fechaFin);
        } else if (fechaInicio != null) {
            sql.append(" AND fecha_evento >= ?");
            parametros.add(fechaInicio);
        } else if (fechaFin != null) {
            sql.append(" AND fecha_evento <= ?");
            parametros.add(fechaFin);
        }
        
        sql.append(" ORDER BY fecha_evento DESC");
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // Establecer los parámetros
            for (int i = 0; i < parametros.size(); i++) {
                Object param = parametros.get(i);
                if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof Long) {
                    stmt.setLong(i + 1, (Long) param);
                } else if (param instanceof LocalDateTime) {
                    stmt.setTimestamp(i + 1, java.sql.Timestamp.valueOf((LocalDateTime) param));
                }
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapearLogEvento(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener logs con filtros múltiples", e);
        }
        
        return logs;
    }
    
    /**
     * Mapea un ResultSet a un objeto LogEvento.
     */
    private LogEvento mapearLogEvento(ResultSet rs) throws SQLException {
        LogEvento log = new LogEvento();
        log.setIdLog(rs.getLong("id_log"));
        
        if (rs.getTimestamp("fecha_evento") != null) {
            log.setFechaEvento(rs.getTimestamp("fecha_evento").toLocalDateTime());
        }
        
        log.setIdUsuario(rs.getLong("id_usuario"));
        log.setUsername(rs.getString("username"));
        log.setTipoEvento(rs.getString("tipo_evento"));
        log.setModulo(rs.getString("modulo"));
        log.setAccion(rs.getString("accion"));
        log.setEntidad(rs.getString("entidad"));
        
        // El id_afectado puede ser NULL
        Long idAfectado = rs.getLong("id_afectado");
        if (!rs.wasNull()) {
            log.setIdAfectado(idAfectado);
        }
        
        log.setDatosPrevios(rs.getString("datos_previos"));
        log.setDatosNuevos(rs.getString("datos_nuevos"));
        log.setIpOrigen(rs.getString("ip_origen"));
        log.setUserAgent(rs.getString("user_agent"));
        log.setNivel(rs.getString("nivel"));
        
        return log;
    }
}