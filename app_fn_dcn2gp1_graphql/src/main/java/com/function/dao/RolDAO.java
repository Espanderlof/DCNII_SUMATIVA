package com.function.dao;

import com.function.model.Rol;
import com.function.model.Usuario;
import com.function.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de acceso a datos para la entidad Rol.
 */
public class RolDAO {
    private static final Logger logger = LoggerFactory.getLogger(RolDAO.class);
    
    /**
     * Obtiene un rol por su ID.
     *
     * @param idRol ID del rol a buscar
     * @return Rol encontrado o null si no existe
     */
    public Rol obtenerRolPorId(Long idRol) {
        String sql = "SELECT * FROM sum_roles WHERE id_rol = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idRol);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearRol(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener rol por ID: " + idRol, e);
        }
        
        return null;
    }
    
    /**
     * Obtiene un rol por su nombre.
     *
     * @param nombre Nombre del rol a buscar
     * @return Rol encontrado o null si no existe
     */
    public Rol obtenerRolPorNombre(String nombre) {
        String sql = "SELECT * FROM sum_roles WHERE nombre = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nombre);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearRol(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener rol por nombre: " + nombre, e);
        }
        
        return null;
    }
    
    /**
     * Obtiene todos los roles.
     *
     * @return Lista de todos los roles
     */
    public List<Rol> obtenerTodosLosRoles() {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT * FROM sum_roles";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                roles.add(mapearRol(rs));
            }
        } catch (SQLException e) {
            logger.error("Error al obtener todos los roles", e);
        }
        
        return roles;
    }
    
    /**
     * Obtiene los usuarios que tienen un rol específico.
     *
     * @param idRol ID del rol
     * @return Lista de usuarios con el rol
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
     * Obtiene un rol con sus usuarios.
     *
     * @param idRol ID del rol
     * @return Rol con sus usuarios cargados
     */
    public Rol obtenerRolCompleto(Long idRol) {
        Rol rol = obtenerRolPorId(idRol);
        if (rol != null) {
            rol.setUsuarios(obtenerUsuariosPorRol(idRol));
        }
        return rol;
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
}