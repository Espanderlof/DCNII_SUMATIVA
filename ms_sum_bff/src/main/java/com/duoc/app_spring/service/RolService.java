package com.duoc.app_spring.service;

import com.duoc.app_spring.model.ApiResponse;
import com.duoc.app_spring.model.Rol;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class RolService {
    
    private final WebClient webClient;
    
    public RolService(WebClient.Builder webClientBuilder, 
                     @Value("${azure.functions.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }
    
    public ApiResponse<List<Rol>> getAllRoles() {
        return webClient.get()
                .uri("/roles")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Rol>>>() {})
                .block();
    }
    
    public ApiResponse<Rol> getRolById(Long id) {
        return webClient.get()
                .uri("/roles/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Rol>>() {})
                .block();
    }
    
    public ApiResponse<Rol> createRol(Rol rol) {
        return webClient.post()
                .uri("/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rol)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Rol>>() {})
                .block();
    }
    
    public ApiResponse<Rol> updateRol(Long id, Rol rol) {
        return webClient.put()
                .uri("/roles/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rol)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Rol>>() {})
                .block();
    }
    
    public ApiResponse<Void> deleteRol(Long id) {
        return webClient.delete()
                .uri("/roles/" + id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                .block();
    }
    
    public ApiResponse<List<Long>> getUsuariosByRol(Long idRol) {
        return webClient.get()
                .uri("/roles/" + idRol + "/usuarios")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Long>>>() {})
                .block();
    }
}