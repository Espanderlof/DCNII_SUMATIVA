package com.function.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa un evento de log en el sistema.
 */
public class LogEvento {
    private Long idLog;
    private LocalDateTime fechaEvento;
    private Long idUsuario;
    private String username;
    private String tipoEvento;
    private String modulo;
    private String accion;
    private String entidad;
    private Long idAfectado;
    private String datosPrevios;
    private String datosNuevos;
    private String ipOrigen;
    private String userAgent;
    private String nivel;

    // Referencia al usuario (para relaciones)
    private Usuario usuario;

    // Constructores
    public LogEvento() {
    }

    public LogEvento(Long idLog, LocalDateTime fechaEvento, Long idUsuario, String username,
                    String tipoEvento, String modulo, String accion, String entidad, Long idAfectado,
                    String datosPrevios, String datosNuevos, String ipOrigen, String userAgent, String nivel) {
        this.idLog = idLog;
        this.fechaEvento = fechaEvento;
        this.idUsuario = idUsuario;
        this.username = username;
        this.tipoEvento = tipoEvento;
        this.modulo = modulo;
        this.accion = accion;
        this.entidad = entidad;
        this.idAfectado = idAfectado;
        this.datosPrevios = datosPrevios;
        this.datosNuevos = datosNuevos;
        this.ipOrigen = ipOrigen;
        this.userAgent = userAgent;
        this.nivel = nivel;
    }

    // Getters y Setters
    public Long getIdLog() {
        return idLog;
    }

    public void setIdLog(Long idLog) {
        this.idLog = idLog;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDateTime fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

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

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public Long getIdAfectado() {
        return idAfectado;
    }

    public void setIdAfectado(Long idAfectado) {
        this.idAfectado = idAfectado;
    }

    public String getDatosPrevios() {
        return datosPrevios;
    }

    public void setDatosPrevios(String datosPrevios) {
        this.datosPrevios = datosPrevios;
    }

    public String getDatosNuevos() {
        return datosNuevos;
    }

    public void setDatosNuevos(String datosNuevos) {
        this.datosNuevos = datosNuevos;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}