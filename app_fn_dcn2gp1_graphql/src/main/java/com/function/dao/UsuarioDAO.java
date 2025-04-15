package com.function.dao;

import com.function.model.Usuario;
import com.function.model.Rol;
import com.function.model.LogEvento;
import com.function.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de acceso a datos para la entidad Usuario.
 */
public class UsuarioDAO {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioDAO.class);

    /**
     * Obtiene un usuario por su ID.
     *
     * @param idUsuario ID del usuario a buscar
     * @return Usuario encontrado o null si no existe
     */
    public Usuario obtenerUsuarioPorId(Long idUsuario) {
        String sql = "SELECT * FROM sum_usuarios WHERE id_usuario = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener usuario por ID: " + idUsuario, e);
        }
        
        return null;
    }
    
    /**
     * Obtiene un usuario por su nombre de usuario.
     *
     * @param username Nombre de usuario a buscar
     * @return Usuario encontrado o null si no existe
     */
    public Usuario obtenerUsuarioPorUsername(String username) {
        String sql = "SELECT * FROM sum_usuarios WHERE username = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener usuario por username: " + username, e);
        }
        
        return null;
    }
    
    /**
     * Obtiene todos los usuarios.
     *
     * @return Lista de todos los usuarios
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM sum_usuarios";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            logger.error("Error al obtener todos los usuarios", e);
        }
        
        return usuarios;
    }
    
    /**
     * Obtiene todos los usuarios con un rol específico.
     *
     * @param idRol ID del rol a filtrar
     * @return Lista de usuarios con el rol especificado
     */
    public List<Usuario> obtenerUsuariosPorRol(Long idRol) {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.* FROM sum_usuarios u " +
                     "JOIN sum_usuario_rol ur ON u.id_usuario = ur.id_usuario " +
                     "WHERE ur.id_rol = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idRol);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapearUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener usuarios por rol: " + idRol, e);
        }
        
        return usuarios;
    }

    /**
     * Obtiene los roles de un usuario.
     *
     * @param idUsuario ID del usuario
     * @return Lista de roles del usuario
     */
    public List<Rol> obtenerRolesPorUsuario(Long idUsuario) {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT r.* FROM sum_roles r " +
                     "JOIN sum_usuario_rol ur ON r.id_rol = ur.id_rol " +
                     "WHERE ur.id_usuario = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(mapearRol(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener roles por usuario: " + idUsuario, e);
        }
        
        return roles;
    }
    
    /**
     * Obtiene los logs de un usuario.
     *
     * @param idUsuario ID del usuario
     * @return Lista de logs del usuario
     */
    public List<LogEvento> obtenerLogsPorUsuario(Long idUsuario) {
        List<LogEvento> logs = new ArrayList<>();
        String sql = "SELECT * FROM sum_log_eventos WHERE id_usuario = ? ORDER BY fecha_evento DESC";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapearLogEvento(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener logs por usuario: " + idUsuario, e);
        }
        
        return logs;
    }
    
    /**
     * Obtiene un usuario con sus roles y logs.
     *
     * @param idUsuario ID del usuario
     * @return Usuario con sus relaciones cargadas
     */
    public Usuario obtenerUsuarioCompleto(Long idUsuario) {
        Usuario usuario = obtenerUsuarioPorId(idUsuario);
        if (usuario != null) {
            usuario.setRoles(obtenerRolesPorUsuario(idUsuario));
            usuario.setLogs(obtenerLogsPorUsuario(idUsuario));
        }
        return usuario;
    }
    
    /**
     * Carga los datos completos para una lista de usuarios.
     *
     * @param usuarios Lista de usuarios a cargar
     * @param incluirLogs Si se deben incluir los logs
     * @return Mapa de usuarios por ID con datos completos
     */
    public Map<Long, Usuario> cargarDatosCompletos(List<Usuario> usuarios, boolean incluirLogs) {
        Map<Long, Usuario> usuariosMap = new HashMap<>();
        
        if (usuarios.isEmpty()) {
            return usuariosMap;
        }
        
        // Crear mapa por ID
        for (Usuario u : usuarios) {
            usuariosMap.put(u.getIdUsuario(), u);
        }
        
        try (Connection conn = DBConnectionManager.getConnection()) {
            // Cargar roles para todos los usuarios
            StringBuilder inClause = new StringBuilder();
            for (int i = 0; i < usuarios.size(); i++) {
                if (i > 0) inClause.append(",");
                inClause.append("?");
            }
            
            String sqlRoles = "SELECT ur.id_usuario, r.* FROM sum_roles r " +
                              "JOIN sum_usuario_rol ur ON r.id_rol = ur.id_rol " +
                              "WHERE ur.id_usuario IN (" + inClause + ")";
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlRoles)) {
                int index = 1;
                for (Usuario u : usuarios) {
                    stmt.setLong(index++, u.getIdUsuario());
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Long idUsuario = rs.getLong("id_usuario");
                        Rol rol = mapearRol(rs);
                        
                        Usuario u = usuariosMap.get(idUsuario);
                        if (u != null) {
                            u.addRol(rol);
                        }
                    }
                }
            }
            
            // Cargar logs si se solicitan
            if (incluirLogs) {
                String sqlLogs = "SELECT * FROM sum_log_eventos " +
                                "WHERE id_usuario IN (" + inClause + ") " +
                                "ORDER BY fecha_evento DESC";
                
                try (PreparedStatement stmt = conn.prepareStatement(sqlLogs)) {
                    int index = 1;
                    for (Usuario u : usuarios) {
                        stmt.setLong(index++, u.getIdUsuario());
                    }
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Long idUsuario = rs.getLong("id_usuario");
                            LogEvento log = mapearLogEvento(rs);
                            
                            Usuario u = usuariosMap.get(idUsuario);
                            if (u != null) {
                                u.addLogEvento(log);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error al cargar datos completos para usuarios", e);
        }
        
        return usuariosMap;
    }
    
    /**
     * Mapea un ResultSet a un objeto Usuario.
     */
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getLong("id_usuario"));
        usuario.setUsername(rs.getString("username"));
        usuario.setEmail(rs.getString("email"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        
        // Manejar fechas que pueden ser nulas
        if (rs.getTimestamp("fecha_creacion") != null) {
            usuario.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        }
        if (rs.getTimestamp("fecha_modificacion") != null) {
            usuario.setFechaModificacion(rs.getTimestamp("fecha_modificacion").toLocalDateTime());
        }
        if (rs.getTimestamp("ultimo_login") != null) {
            usuario.setUltimoLogin(rs.getTimestamp("ultimo_login").toLocalDateTime());
        }
        
        usuario.setActivo(rs.getInt("activo") == 1);
        
        return usuario;
    }
    
    /**
     * Mapea un ResultSet a un objeto Rol.
     */
    private Rol mapearRol(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        rol.setIdRol(rs.getLong("id_rol"));
        rol.setNombre(rs.getString("nombre"));
        rol.setDescripcion(rs.getString("descripcion"));
        
        // Manejar fechas que pueden ser nulas
        if (rs.getTimestamp("fecha_creacion") != null) {
            rol.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        }
        if (rs.getTimestamp("fecha_modificacion") != null) {
            rol.setFechaModificacion(rs.getTimestamp("fecha_modificacion").toLocalDateTime());
        }
        
        rol.setActivo(rs.getInt("activo") == 1);
        
        return rol;
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