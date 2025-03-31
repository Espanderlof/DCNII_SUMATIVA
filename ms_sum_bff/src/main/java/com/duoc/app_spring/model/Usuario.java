package com.duoc.app_spring.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    private Long idUsuario;
    private String username;
    private String email;
    private String nombre;
    private String apellido;
    private String password; // Solo para crear/actualizar
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private LocalDateTime ultimoLogin;
    private boolean activo;
}