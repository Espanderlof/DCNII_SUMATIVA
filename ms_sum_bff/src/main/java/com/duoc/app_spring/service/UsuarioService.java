package com.duoc.app_spring.service;

import com.duoc.app_spring.model.ApiResponse;
import com.duoc.app_spring.model.Usuario;
import com.duoc.app_spring.model.UsuarioUpdateDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
public class UsuarioService {
    
    private final WebClient webClient;
    
    public UsuarioService(WebClient.Builder webClientBuilder, 
                          @Value("${azure.functions.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }
    
    public ApiResponse<List<Usuario>> getAllUsuarios() {
        return webClient.get()
                .uri("/usuarios")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Usuario>>>() {})
                .block();
    }
    
    public ApiResponse<Usuario> getUsuarioById(Long id) {
        return webClient.get()
                .uri("/usuarios/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Usuario>>() {})
                .block();
    }
    
    public ApiResponse<Usuario> createUsuario(Usuario usuario) {
        return webClient.post()
                .uri("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(usuario)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Usuario>>() {})
                .block();
    }
    
    public ApiResponse<Usuario> updateUsuario(Long id, UsuarioUpdateDTO usuario) {
        try {
            return webClient.put()
                    .uri("/usuarios/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(usuario)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Usuario>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            System.out.println("Error en la solicitud: " + e.getResponseBodyAsString());
            throw e;
        }
    }
    
    public ApiResponse<Void> deleteUsuario(Long id) {
        return webClient.delete()
                .uri("/usuarios/" + id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                .block();
    }
    
    public ApiResponse<List<Long>> asignarRol(Long idUsuario, Long idRol) {
        return webClient.post()
                .uri("/usuarios/" + idUsuario + "/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RolRequest(idRol))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Long>>>() {})
                .block();
    }
    
    // Clase interna para la solicitud de asignación de rol
    private static class RolRequest {
        private Long idRol;
        
        public RolRequest(Long idRol) {
            this.idRol = idRol;
        }
        
        public Long getIdRol() {
            return idRol;
        }
        
        public void setIdRol(Long idRol) {
            this.idRol = idRol;
        }
    }
}