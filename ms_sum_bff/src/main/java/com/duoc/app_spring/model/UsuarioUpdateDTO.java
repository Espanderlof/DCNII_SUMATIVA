package com.duoc.app_spring.model;

import lombok.Data;

@Data
public class UsuarioUpdateDTO {
    private String username;
    private String email;
    private String nombre;
    private String apellido;
}