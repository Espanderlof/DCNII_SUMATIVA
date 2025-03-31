package com.duoc.app_spring.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rol {
    private Long idRol;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private boolean activo;
}