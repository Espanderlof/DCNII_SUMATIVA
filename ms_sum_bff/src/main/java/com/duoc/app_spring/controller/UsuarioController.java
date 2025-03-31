package com.duoc.app_spring.controller;

import com.duoc.app_spring.model.ApiResponse;
import com.duoc.app_spring.model.Usuario;
import com.duoc.app_spring.model.UsuarioUpdateDTO;
import com.duoc.app_spring.service.UsuarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {
    
    private final UsuarioService usuarioService;
    
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Usuario>>> getAllUsuarios() {
        ApiResponse<List<Usuario>> response = usuarioService.getAllUsuarios();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Usuario>> getUsuarioById(@PathVariable Long id) {
        ApiResponse<Usuario> response = usuarioService.getUsuarioById(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Usuario>> createUsuario(@RequestBody Usuario usuario) {
        ApiResponse<Usuario> response = usuarioService.createUsuario(usuario);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Usuario>> updateUsuario(@PathVariable Long id, @RequestBody UsuarioUpdateDTO usuario) {
        ApiResponse<Usuario> response = usuarioService.updateUsuario(id, usuario);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUsuario(@PathVariable Long id) {
        ApiResponse<Void> response = usuarioService.deleteUsuario(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @PostMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<List<Long>>> asignarRol(@PathVariable Long id, @RequestBody RolRequest request) {
        ApiResponse<List<Long>> response = usuarioService.asignarRol(id, request.getIdRol());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // Clase interna para la solicitud de asignación de rol
    public static class RolRequest {
        private Long idRol;
        
        public Long getIdRol() {
            return idRol;
        }
        
        public void setIdRol(Long idRol) {
            this.idRol = idRol;
        }
    }
}