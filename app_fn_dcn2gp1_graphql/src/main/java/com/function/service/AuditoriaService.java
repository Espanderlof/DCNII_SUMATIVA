package com.function.service;

import com.function.dao.LogEventoDAO;
import com.function.dao.RolDAO;
import com.function.dao.UsuarioDAO;
import com.function.model.LogEvento;
import com.function.model.Rol;
import com.function.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio para gestionar las operaciones de auditoría.
 */
public class AuditoriaService {
    private static final Logger logger = LoggerFactory.getLogger(AuditoriaService.class);
    
    private final UsuarioDAO usuarioDAO;
    private final RolDAO rolDAO;
    private final LogEventoDAO logEventoDAO;
    
    public AuditoriaService() {
        this.usuarioDAO = new UsuarioDAO();
        this.rolDAO = new RolDAO();
        this.logEventoDAO = new LogEventoDAO();
    }
    
    /**
     * Obtiene un usuario completo por su ID (con roles y logs).
     *
     * @param idUsuario ID del usuario
     * @return Usuario con toda su información relacionada
     */
    public Usuario obtenerUsuarioCompleto(Long idUsuario) {
        logger.info("Obteniendo usuario completo con ID: {}", idUsuario);
        return usuarioDAO.obtenerUsuarioCompleto(idUsuario);
    }
    
    /**
     * Obtiene un usuario completo por su nombre de usuario (con roles y logs).
     *
     * @param username Nombre de usuario
     * @return Usuario con toda su información relacionada
     */
    public Usuario obtenerUsuarioCompletoPorUsername(String username) {
        logger.info("Obteniendo usuario completo con username: {}", username);
        Usuario usuario = usuarioDAO.obtenerUsuarioPorUsername(username);
        if (usuario != null) {
            usuario.setRoles(usuarioDAO.obtenerRolesPorUsuario(usuario.getIdUsuario()));
            usuario.setLogs(usuarioDAO.obtenerLogsPorUsuario(usuario.getIdUsuario()));
        }
        return usuario;
    }
    
    /**
     * Obtiene un rol completo por su ID (con usuarios).
     *
     * @param idRol ID del rol
     * @return Rol con sus usuarios
     */
    public Rol obtenerRolCompleto(Long idRol) {
        logger.info("Obteniendo rol completo con ID: {}", idRol);
        return rolDAO.obtenerRolCompleto(idRol);
    }
    
    /**
     * Obtiene un rol completo por su nombre (con usuarios).
     *
     * @param nombreRol Nombre del rol
     * @return Rol con sus usuarios
     */
    public Rol obtenerRolCompletoPorNombre(String nombreRol) {
        logger.info("Obteniendo rol completo con nombre: {}", nombreRol);
        Rol rol = rolDAO.obtenerRolPorNombre(nombreRol);
        if (rol != null) {
            rol.setUsuarios(rolDAO.obtenerUsuariosPorRol(rol.getIdRol()));
        }
        return rol;
    }
    
    /**
     * Obtiene los usuarios que tienen un rol específico, con sus logs.
     *
     * @param idRol ID del rol
     * @return Lista de usuarios con el rol, incluyendo sus logs
     */
    public List<Usuario> obtenerUsuariosPorRolConLogs(Long idRol) {
        logger.info("Obteniendo usuarios con logs para el rol ID: {}", idRol);
        List<Usuario> usuarios = rolDAO.obtenerUsuariosPorRol(idRol);
        
        if (!usuarios.isEmpty()) {
            Map<Long, Usuario> usuariosMap = usuarioDAO.cargarDatosCompletos(usuarios, true);
            return usuariosMap.values().stream().collect(Collectors.toList());
        }
        
        return usuarios;
    }
    
    /**
     * Obtiene los usuarios que tienen un rol específico por nombre, con sus logs.
     *
     * @param nombreRol Nombre del rol
     * @return Lista de usuarios con el rol, incluyendo sus logs
     */
    public List<Usuario> obtenerUsuariosPorNombreRolConLogs(String nombreRol) {
        logger.info("Obteniendo usuarios con logs para el rol: {}", nombreRol);
        Rol rol = rolDAO.obtenerRolPorNombre(nombreRol);
        if (rol != null) {
            return obtenerUsuariosPorRolConLogs(rol.getIdRol());
        }
        return List.of();
    }
    
    /**
     * Busca logs con filtros múltiples.
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
    public List<LogEvento> buscarLogsConFiltros(
            Long idUsuario, String tipoEvento, String modulo, 
            String entidad, String nivel, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        
        logger.info("Buscando logs con filtros - Usuario: {}, Tipo: {}, Módulo: {}, Entidad: {}, Nivel: {}",
                idUsuario, tipoEvento, modulo, entidad, nivel);
        
        return logEventoDAO.obtenerLogsConFiltrosMultiples(
                idUsuario, tipoEvento, modulo, entidad, nivel, fechaInicio, fechaFin);
    }
    
    /**
     * Obtiene estadísticas de logs por usuario.
     * 
     * @return Lista de usuarios con conteo de logs
     */
    public List<Usuario> obtenerEstadisticasLogsPorUsuario() {
        List<Usuario> usuarios = usuarioDAO.obtenerTodosLosUsuarios();
        for (Usuario usuario : usuarios) {
            List<LogEvento> logs = logEventoDAO.obtenerLogsPorUsuario(usuario.getIdUsuario());
            usuario.setLogs(logs);
        }
        return usuarios;
    }
    
    /**
     * Obtiene estadísticas de logs por tipo de evento.
     *
     * @return Lista de eventos de log agrupados por tipo
     */
    public List<LogEvento> obtenerLogsPorTipoEvento(String tipoEvento) {
        return logEventoDAO.obtenerLogsPorTipoEvento(tipoEvento);
    }
    
    /**
     * Obtiene estadísticas de logs por módulo.
     *
     * @return Lista de eventos de log para un módulo específico
     */
    public List<LogEvento> obtenerLogsPorModulo(String modulo) {
        return logEventoDAO.obtenerLogsPorModulo(modulo);
    }
    
    /**
     * Obtiene estadísticas de logs por entidad.
     *
     * @return Lista de eventos de log para una entidad específica
     */
    public List<LogEvento> obtenerLogsPorEntidad(String entidad) {
        return logEventoDAO.obtenerLogsPorEntidad(entidad);
    }
    
    /**
     * Obtiene estadísticas de logs por nivel.
     *
     * @return Lista de eventos de log para un nivel específico
     */
    public List<LogEvento> obtenerLogsPorNivel(String nivel) {
        return logEventoDAO.obtenerLogsPorNivel(nivel);
    }
    
    /**
     * Obtiene estadísticas de logs por rango de fechas.
     *
     * @return Lista de eventos de log en un rango de fechas
     */
    public List<LogEvento> obtenerLogsPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return logEventoDAO.obtenerLogsPorRangoFechas(fechaInicio, fechaFin);
    }
}