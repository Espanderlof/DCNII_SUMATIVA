package com.function.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa un usuario en el sistema.
 */
public class Usuario {
    private Long idUsuario;
    private String username;
    private String email;
    private String nombre;
    private String apellido;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private LocalDateTime ultimoLogin;
    private Boolean activo;
    
    // Lista de roles del usuario (para relaciones)
    private List<Rol> roles = new ArrayList<>();
    
    // Lista de eventos de log del usuario (para relaciones)
    private List<LogEvento> logs = new ArrayList<>();

    // Constructores
    public Usuario() {
    }

    public Usuario(Long idUsuario, String username, String email, String nombre, String apellido,
                  LocalDateTime fechaCreacion, LocalDateTime fechaModificacion, 
                  LocalDateTime ultimoLogin, Boolean activo) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.email = email;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
        this.ultimoLogin = ultimoLogin;
        this.activo = activo;
    }

    // Getters y Setters
    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public List<Rol> getRoles() {
        return roles;
    }

    public void setRoles(List<Rol> roles) {
        this.roles = roles;
    }
    
    public List<LogEvento> getLogs() {
        return logs;
    }

    public void setLogs(List<LogEvento> logs) {
        this.logs = logs;
    }
    
    public void addRol(Rol rol) {
        this.roles.add(rol);
    }
    
    public void addLogEvento(LogEvento logEvento) {
        this.logs.add(logEvento);
    }
}