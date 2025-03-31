package com.duoc.app_spring.util;

import com.duoc.app_spring.model.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiResponse<Object>> handleWebClientResponseException(WebClientResponseException ex) {
        // Registra información detallada sobre el error
        System.out.println("Error de WebClient: " + ex.getStatusCode());
        System.out.println("Cuerpo de respuesta: " + ex.getResponseBodyAsString());
        
        String errorMessage = "Error al comunicarse con el servicio";
        String errorDetail = ex.getMessage();
        
        // Intenta extraer el mensaje de error del cuerpo de respuesta si está en formato JSON
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(ex.getResponseBodyAsString());
            if (rootNode.has("error")) {
                errorDetail = rootNode.get("error").asText();
            } else if (rootNode.has("message")) {
                errorDetail = rootNode.get("message").asText();
            }
        } catch (Exception e) {
            // Si no se puede parsear como JSON, usa el mensaje original
            System.out.println("No se pudo parsear el cuerpo de respuesta como JSON: " + e.getMessage());
        }
        
        ApiResponse<Object> response = new ApiResponse<>(false, errorMessage, null, errorDetail);
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        ApiResponse<Object> response = new ApiResponse<>(false, "Error interno del servidor", null, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}