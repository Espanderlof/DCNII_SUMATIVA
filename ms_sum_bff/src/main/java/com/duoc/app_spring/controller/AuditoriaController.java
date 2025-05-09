package com.duoc.app_spring.controller;

import com.duoc.app_spring.service.GraphQLService;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuditoriaController {
    
    private final GraphQLService graphQLService;
    
    public AuditoriaController(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }
    
    /**
     * Endpoint para la API de auditoría - formato original de GraphQL
     */
    @PostMapping(value = "/auditoria", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> forwardGraphQLQuery(@RequestBody String requestBody) {
        JsonNode response = graphQLService.executeAuditoriaQuery(requestBody);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para la API de consulta de usuarios por rol - formato original de GraphQL
     */
    @PostMapping(value = "/usuariosByRole", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> forwardUsuariosByRoleQuery(@RequestBody String requestBody) {
        JsonNode response = graphQLService.executeUsuariosByRoleQuery(requestBody);
        return ResponseEntity.ok(response);
    }
}