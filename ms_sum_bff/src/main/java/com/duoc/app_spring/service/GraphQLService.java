package com.duoc.app_spring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GraphQLService {
    
    private final WebClient.Builder webClientBuilder;
    private final String auditoriaUrl;
    private final String usuariosByRoleUrl;
    private final ObjectMapper objectMapper;
    
    public GraphQLService(WebClient.Builder webClientBuilder,
                          @Value("${azure.functions.graphql-url}") String auditoriaUrl,
                          @Value("${azure.functions.usuarios-by-role-url}") String usuariosByRoleUrl) {
        this.webClientBuilder = webClientBuilder;
        this.auditoriaUrl = auditoriaUrl;
        this.usuariosByRoleUrl = usuariosByRoleUrl;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Ejecuta una consulta GraphQL para el endpoint de auditoría
     */
    public JsonNode executeAuditoriaQuery(String requestBody) {
        return executeGraphQLQuery(requestBody, auditoriaUrl);
    }
    
    /**
     * Ejecuta una consulta GraphQL para el endpoint de usuarios por rol
     */
    public JsonNode executeUsuariosByRoleQuery(String requestBody) {
        return executeGraphQLQuery(requestBody, usuariosByRoleUrl);
    }
    
    /**
     * Método privado compartido para ejecutar consultas GraphQL a la URL especificada
     */
    private JsonNode executeGraphQLQuery(String requestBody, String url) {
        try {
            // Verificar que el cuerpo de la solicitud sea JSON válido
            JsonNode requestNode = objectMapper.readTree(requestBody);
            
            // Crear un cliente específico para la URL solicitada
            WebClient client = webClientBuilder
                    .baseUrl(url)
                    .build();
            
            // Realizar la solicitud HTTP POST al servicio GraphQL
            String response = client.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestNode)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // Convertir la respuesta a JsonNode para preservar el formato exacto
            if (response != null) {
                return objectMapper.readTree(response);
            } else {
                JsonNode errorNode = objectMapper.createObjectNode()
                        .put("error", "No se recibió respuesta del servidor GraphQL");
                return errorNode;
            }
        } catch (Exception e) {
            // Registrar el error para depuración
            System.err.println("Error al ejecutar consulta GraphQL a " + url + ": " + e.getMessage());
            e.printStackTrace();
            
            // En caso de error, devolver un objeto JSON con el mensaje de error
            try {
                return objectMapper.createObjectNode()
                        .put("error", "Error al procesar la consulta GraphQL: " + e.getMessage());
            } catch (Exception ex) {
                // Esto no debería ocurrir, pero por seguridad
                throw new RuntimeException("Error grave al procesar la consulta", ex);
            }
        }
    }
}