package com.duoc.app_spring.controller;

import com.duoc.app_spring.model.ApiResponse;
import com.duoc.app_spring.model.Rol;
import com.duoc.app_spring.service.RolService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RolController {
    
    private final RolService rolService;
    
    public RolController(RolService rolService) {
        this.rolService = rolService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Rol>>> getAllRoles() {
        ApiResponse<List<Rol>> response = rolService.getAllRoles();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Rol>> getRolById(@PathVariable Long id) {
        ApiResponse<Rol> response = rolService.getRolById(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Rol>> createRol(@RequestBody Rol rol) {
        ApiResponse<Rol> response = rolService.createRol(rol);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Rol>> updateRol(@PathVariable Long id, @RequestBody Rol rol) {
        ApiResponse<Rol> response = rolService.updateRol(id, rol);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRol(@PathVariable Long id) {
        ApiResponse<Void> response = rolService.deleteRol(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @GetMapping("/{id}/usuarios")
    public ResponseEntity<ApiResponse<List<Long>>> getUsuariosByRol(@PathVariable Long id) {
        ApiResponse<List<Long>> response = rolService.getUsuariosByRol(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}