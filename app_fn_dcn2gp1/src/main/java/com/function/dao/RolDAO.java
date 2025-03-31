package com.function.dao;

import com.function.model.Rol;
import com.function.util.DBConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Clase de acceso a datos para la entidad Rol.
 */
public class RolDAO {
    private static final Logger logger = LoggerFactory.getLogger(RolDAO.class);

    /**
     * Obtiene todos los roles.
     * @return Lista de roles
     */
    public List<Rol> findAll() {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT id_rol, nombre, descripcion, fecha_creacion, fecha_modificacion, activo " +
                    "FROM sum_roles";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Rol rol = mapResultSetToRol(rs);
                roles.add(rol);
            }
        } catch (SQLException e) {
            logger.error("Error al obtener todos los roles", e);
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
        
        return roles;
    }

    /**
     * Busca un rol por su ID.
     * @param id ID del rol
     * @return Optional conteniendo el rol si existe
     */
    public Optional<Rol> findById(Long id) {
        String sql = "SELECT id_rol, nombre, descripcion, fecha_creacion, fecha_modificacion, activo " +
                    "FROM sum_roles WHERE id_rol = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Rol rol = mapResultSetToRol(rs);
                    return Optional.of(rol);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar rol por ID: " + id, e);
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
        
        return Optional.empty();
    }

    /**
     * Busca un rol por su nombre.
     * @param nombre Nombre del rol
     * @return Optional conteniendo el rol si existe
     */
    public Optional<Rol> findByNombre(String nombre) {
        String sql = "SELECT id_rol, nombre, descripcion, fecha_creacion, fecha_modificacion, activo " +
                    "FROM sum_roles WHERE nombre = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nombre);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Rol rol = mapResultSetToRol(rs);
                    return Optional.of(rol);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar rol por nombre: " + nombre, e);
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
        
        return Optional.empty();
    }

    /**
     * Crea un nuevo rol en la base de datos.
     * @param rol Rol a crear
     * @return Rol creado con su ID asignado
     */
    public Rol create(Rol rol) {
        String sql = "INSERT INTO sum_roles (id_rol, nombre, descripcion) " +
                    "VALUES (sum_seq_rol.NEXTVAL, ?, ?)";
        
        String sqlGetId = "SELECT sum_seq_rol.CURRVAL FROM DUAL";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             PreparedStatement stmtGetId = conn.prepareStatement(sqlGetId)) {
            
            stmt.setString(1, rol.getNombre());
            stmt.setString(2, rol.getDescripcion());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmtGetId.executeQuery()) {
                    if (rs.next()) {
                        Long newRolId = rs.getLong(1);
                        rol.setIdRol(newRolId);
                        logger.info("Rol creado con ID: " + newRolId);
                        return rol;
                    }
                }
            }
            
            throw new RuntimeException("No se pudo crear el rol");
            
        } catch (SQLException e) {
            logger.error("Error al crear rol: " + rol.getNombre(), e);
            if (e.getMessage().contains("unique constraint")) {
                throw new RuntimeException("Ya existe un rol con ese nombre", e);
            }
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Actualiza un rol existente.
     * @param rol Rol con datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean update(Rol rol) {
        String sql = "UPDATE sum_roles SET " +
                    "nombre = ?, " +
                    "descripcion = ? " +
                    "WHERE id_rol = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, rol.getNombre());
            stmt.setString(2, rol.getDescripcion());
            stmt.setLong(3, rol.getIdRol());
            
            int rowsAffected = stmt.executeUpdate();
            logger.info("Rol actualizado con ID: " + rol.getIdRol() + ", filas afectadas: " + rowsAffected);
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.error("Error al actualizar rol con ID: " + rol.getIdRol(), e);
            if (e.getMessage().contains("unique constraint")) {
                throw new RuntimeException("Ya existe un rol con ese nombre", e);
            }
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Elimina un rol (baja lógica).
     * @param id ID del rol a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean delete(Long id) {
        String sql = "UPDATE sum_roles SET activo = 0 WHERE id_rol = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            logger.info("Rol eliminado (lógicamente) con ID: " + id + ", filas afectadas: " + rowsAffected);
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.error("Error al eliminar rol con ID: " + id, e);
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Obtiene los usuarios que tienen un rol específico.
     * @param idRol ID del rol
     * @return Lista de IDs de usuarios
     */
    public List<Long> getUsuarios(Long idRol) {
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
        } catch (SQLException e) {
            logger.error("Error al obtener usuarios del rol con ID: " + idRol, e);
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
        
        return usuarios;
    }

    /**
     * Convierte un ResultSet en un objeto Rol.
     * @param rs ResultSet con datos de rol
     * @return Rol
     * @throws SQLException si hay error en la conversión
     */
    private Rol mapResultSetToRol(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        rol.setIdRol(rs.getLong("id_rol"));
        rol.setNombre(rs.getString("nombre"));
        rol.setDescripcion(rs.getString("descripcion"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            rol.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        Timestamp fechaModificacion = rs.getTimestamp("fecha_modificacion");
        if (fechaModificacion != null) {
            rol.setFechaModificacion(fechaModificacion.toLocalDateTime());
        }
        
        rol.setActivo(rs.getBoolean("activo"));
        
        return rol;
    }
}