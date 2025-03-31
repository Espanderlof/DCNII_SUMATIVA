package com.function.dao;

import com.function.model.Usuario;
import com.function.util.DBConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Clase de acceso a datos para la entidad Usuario.
 */
public class UsuarioDAO {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioDAO.class);

    /**
     * Obtiene todos los usuarios activos.
     * @return Lista de usuarios
     */
    public List<Usuario> findAll() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id_usuario, username, email, nombre, apellido, " +
                    "password_hash, fecha_creacion, fecha_modificacion, ultimo_login, activo " +
                    "FROM sum_usuarios";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Usuario usuario = mapResultSetToUsuario(rs);
                usuarios.add(usuario);
            }
        } catch (SQLException e) {
            logger.error("Error al obtener todos los usuarios", e);
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
        
        return usuarios;
    }

    /**
     * Busca un usuario por su ID.
     * @param id ID del usuario
     * @return Optional conteniendo el usuario si existe
     */
    public Optional<Usuario> findById(Long id) {
        String sql = "SELECT id_usuario, username, email, nombre, apellido, " +
                    "password_hash, fecha_creacion, fecha_modificacion, ultimo_login, activo " +
                    "FROM sum_usuarios WHERE id_usuario = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapResultSetToUsuario(rs);
                    return Optional.of(usuario);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar usuario por ID: " + id, e);
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
        
        return Optional.empty();
    }

    /**
     * Busca un usuario por su nombre de usuario.
     * @param username Nombre de usuario
     * @return Optional conteniendo el usuario si existe
     */
    public Optional<Usuario> findByUsername(String username) {
        String sql = "SELECT id_usuario, username, email, nombre, apellido, " +
                    "password_hash, fecha_creacion, fecha_modificacion, ultimo_login, activo " +
                    "FROM sum_usuarios WHERE username = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapResultSetToUsuario(rs);
                    return Optional.of(usuario);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar usuario por username: " + username, e);
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
        
        return Optional.empty();
    }

    /**
     * Crea un nuevo usuario en la base de datos.
     * @param usuario Usuario a crear
     * @return Usuario creado con su ID asignado
     */
    public Usuario create(Usuario usuario) {
        String sql = "BEGIN sum_crear_usuario(?, ?, ?, ?, ?, ?); END;";
        
        try (Connection conn = DBConnectionManager.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, usuario.getUsername());
            cstmt.setString(2, usuario.getEmail());
            cstmt.setString(3, usuario.getNombre());
            cstmt.setString(4, usuario.getApellido());
            cstmt.setString(5, usuario.getPasswordHash());
            cstmt.registerOutParameter(6, Types.NUMERIC);
            
            cstmt.execute();
            
            Long newUserId = cstmt.getLong(6);
            usuario.setIdUsuario(newUserId);
            
            logger.info("Usuario creado con ID: " + newUserId);
            return usuario;
            
        } catch (SQLException e) {
            logger.error("Error al crear usuario: " + usuario.getUsername(), e);
            if (e.getMessage().contains("unique constraint")) {
                throw new RuntimeException("Ya existe un usuario con ese nombre de usuario o email", e);
            }
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Actualiza un usuario existente.
     * @param usuario Usuario con datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean update(Usuario usuario) {
        String sql = "UPDATE sum_usuarios SET " +
                    "nombre = ?, " +
                    "apellido = ?, " +
                    "email = ?, " +
                    "username = ?, " +
                    "password_hash = ? " +
                    "WHERE id_usuario = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getUsername());
            stmt.setString(5, usuario.getPasswordHash());
            stmt.setLong(6, usuario.getIdUsuario());
            
            int rowsAffected = stmt.executeUpdate();
            logger.info("Usuario actualizado con ID: " + usuario.getIdUsuario() + ", filas afectadas: " + rowsAffected);
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.error("Error al actualizar usuario con ID: " + usuario.getIdUsuario(), e);
            if (e.getMessage().contains("unique constraint")) {
                throw new RuntimeException("Ya existe un usuario con ese nombre de usuario o email", e);
            }
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Elimina un usuario (baja lógica).
     * @param id ID del usuario a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean delete(Long id) {
        String sql = "UPDATE sum_usuarios SET activo = 0 WHERE id_usuario = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            logger.info("Usuario eliminado (lógicamente) con ID: " + id + ", filas afectadas: " + rowsAffected);
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.error("Error al eliminar usuario con ID: " + id, e);
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Actualiza el último login de un usuario.
     * @param id ID del usuario
     * @return true si se actualizó correctamente
     */
    public boolean updateLastLogin(Long id) {
        String sql = "UPDATE sum_usuarios SET ultimo_login = CURRENT_TIMESTAMP WHERE id_usuario = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            logger.info("Último login actualizado para usuario con ID: " + id);
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.error("Error al actualizar último login del usuario con ID: " + id, e);
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Asigna un rol a un usuario.
     * @param idUsuario ID del usuario
     * @param idRol ID del rol
     * @return true si se asignó correctamente
     */
    public boolean asignarRol(Long idUsuario, Long idRol) {
        String sql = "BEGIN sum_asignar_rol(?, ?); END;";
        
        try (Connection conn = DBConnectionManager.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setLong(1, idUsuario);
            cstmt.setLong(2, idRol);
            
            cstmt.execute();
            logger.info("Rol " + idRol + " asignado al usuario " + idUsuario);
            
            return true;
            
        } catch (SQLException e) {
            logger.error("Error al asignar rol " + idRol + " al usuario " + idUsuario, e);
            if (e.getMessage().contains("20001")) {
                throw new RuntimeException("El usuario no existe", e);
            }
            if (e.getMessage().contains("20002")) {
                throw new RuntimeException("El rol no existe", e);
            }
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Quita un rol a un usuario.
     * @param idUsuario ID del usuario
     * @param idRol ID del rol
     * @return true si se quitó correctamente
     */
    public boolean quitarRol(Long idUsuario, Long idRol) {
        String sql = "BEGIN sum_quitar_rol(?, ?); END;";
        
        try (Connection conn = DBConnectionManager.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setLong(1, idUsuario);
            cstmt.setLong(2, idRol);
            
            cstmt.execute();
            logger.info("Rol " + idRol + " quitado al usuario " + idUsuario);
            
            return true;
            
        } catch (SQLException e) {
            logger.error("Error al quitar rol " + idRol + " al usuario " + idUsuario, e);
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Obtiene los roles de un usuario.
     * @param idUsuario ID del usuario
     * @return Lista de IDs de roles
     */
    public List<Long> getRoles(Long idUsuario) {
        List<Long> roles = new ArrayList<>();
        String sql = "SELECT id_rol FROM sum_usuario_rol WHERE id_usuario = ?";
        
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getLong("id_rol"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener roles del usuario con ID: " + idUsuario, e);
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
        
        return roles;
    }

    /**
     * Convierte un ResultSet en un objeto Usuario.
     * @param rs ResultSet con datos de usuario
     * @return Usuario
     * @throws SQLException si hay error en la conversión
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getLong("id_usuario"));
        usuario.setUsername(rs.getString("username"));
        usuario.setEmail(rs.getString("email"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            usuario.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        Timestamp fechaModificacion = rs.getTimestamp("fecha_modificacion");
        if (fechaModificacion != null) {
            usuario.setFechaModificacion(fechaModificacion.toLocalDateTime());
        }
        
        Timestamp ultimoLogin = rs.getTimestamp("ultimo_login");
        if (ultimoLogin != null) {
            usuario.setUltimoLogin(ultimoLogin.toLocalDateTime());
        }
        
        usuario.setActivo(rs.getBoolean("activo"));
        
        return usuario;
    }
}