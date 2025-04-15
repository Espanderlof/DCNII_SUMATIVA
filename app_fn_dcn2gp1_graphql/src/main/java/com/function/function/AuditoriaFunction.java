package com.function.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.function.graphql.GraphQLProvider;
import com.function.util.GsonConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import graphql.ExecutionInput;
import graphql.ExecutionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditoriaFunction {
    private static final Logger logger = LoggerFactory.getLogger(AuditoriaFunction.class);
    private static final Gson gson = GsonConfig.getGson();
    
    private final GraphQLProvider graphQLProvider;
    
    public AuditoriaFunction() {
        // Inicializar el proveedor de GraphQL al instanciar la función
        this.graphQLProvider = new GraphQLProvider();
    }
    
    @FunctionName("auditoria")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) 
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        context.getLogger().info("Función de auditoría GraphQL iniciada");
        
        // Verificar que se haya enviado un cuerpo en la solicitud
        if (!request.getBody().isPresent()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("El cuerpo de la solicitud es requerido")
                    .build();
        }
        
        String requestBody = request.getBody().get();
        
        try {
            // Parsear la solicitud GraphQL
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            
            String query = jsonRequest.has("query") ? jsonRequest.get("query").getAsString() : null;
            String operationName = jsonRequest.has("operationName") ? jsonRequest.get("operationName").getAsString() : null;
            Map<String, Object> variables = new HashMap<>();
            
            if (jsonRequest.has("variables") && !jsonRequest.get("variables").isJsonNull()) {
                // Usar Map.class podría causar problemas, así que usamos HashMap explícitamente
                variables = gson.fromJson(jsonRequest.get("variables"), HashMap.class);
            }
            
            if (query == null || query.trim().isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("La consulta GraphQL es requerida")
                        .build();
            }
            
            // Ejecutar la consulta GraphQL
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                    .query(query)
                    .operationName(operationName)
                    .variables(variables)
                    .build();
            
            ExecutionResult executionResult = GraphQLProvider.getGraphQL().execute(executionInput);
            
            // Preparar la respuesta
            JsonObject responseJson = new JsonObject();
            
            if (!executionResult.getErrors().isEmpty()) {
                responseJson.add("errors", gson.toJsonTree(executionResult.getErrors()));
            }
            
            responseJson.add("data", gson.toJsonTree(executionResult.getData()));
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(responseJson.toString())
                    .build();
            
        } catch (Exception e) {
            logger.error("Error al procesar la consulta GraphQL", e);
            
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la consulta GraphQL: " + e.getMessage())
                    .build();
        }
    }
}