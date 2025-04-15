package com.function.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.function.graphql.GraphQLProvider;
import com.function.graphql.GraphQLQueries;
import com.function.util.GsonConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import graphql.ExecutionInput;
import graphql.ExecutionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsuariosByRoleFunction {
    private static final Logger logger = LoggerFactory.getLogger(UsuariosByRoleFunction.class);
    private static final Gson gson = GsonConfig.getGson();
    
    @FunctionName("usuariosByRole")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) 
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        context.getLogger().info("Función usuariosByRole iniciada (GraphQL)");
        
        // Verificar que se haya enviado un cuerpo en la solicitud
        if (!request.getBody().isPresent()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("El cuerpo de la solicitud es requerido")
                    .build();
        }
        
        String requestBody = request.getBody().get();
        
        try {
            // Parsear la solicitud
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            
            // Variables que pueden venir en la solicitud
            String rolId = null;
            String rolNombre = null;
            String queryPersonalizada = null;
            
            // Verificar si el cliente está enviando una consulta GraphQL personalizada
            if (jsonRequest.has("query") && !jsonRequest.get("query").isJsonNull()) {
                queryPersonalizada = jsonRequest.get("query").getAsString();
            }
            
            // Obtener las variables
            Map<String, Object> variables = new HashMap<>();
            if (jsonRequest.has("variables") && !jsonRequest.get("variables").isJsonNull()) {
                JsonObject varsJson = jsonRequest.getAsJsonObject("variables");
                
                if (varsJson.has("rolId") && !varsJson.get("rolId").isJsonNull()) {
                    rolId = varsJson.get("rolId").getAsString();
                    variables.put("idRol", rolId);
                }
                
                if (varsJson.has("rolNombre") && !varsJson.get("rolNombre").isJsonNull()) {
                    rolNombre = varsJson.get("rolNombre").getAsString();
                    variables.put("nombreRol", rolNombre);
                }
                
                // Copiar el resto de variables que puedan existir
                for (String key : varsJson.keySet()) {
                    if (!key.equals("rolId") && !key.equals("rolNombre") && !varsJson.get(key).isJsonNull()) {
                        variables.put(key, gson.fromJson(varsJson.get(key), Object.class));
                    }
                }
            }
            
            // Validar parámetros si no es una consulta personalizada
            if (queryPersonalizada == null && rolId == null && rolNombre == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Se debe proporcionar un ID de rol (rolId) o nombre de rol (rolNombre) en las variables")
                        .build();
            }
            
            // Determinar qué query usar
            String query;
            String operationName;
            
            if (queryPersonalizada != null) {
                // Usar la consulta personalizada proporcionada por el cliente
                query = queryPersonalizada;
                operationName = jsonRequest.has("operationName") ? 
                        jsonRequest.get("operationName").getAsString() : null;
            } else if (rolId != null) {
                // Usar la consulta predefinida por ID de rol desde la clase GraphQLQueries
                query = GraphQLQueries.USUARIOS_POR_ROL_ID;
                operationName = "ObtenerUsuariosPorRol";
            } else {
                // Usar la consulta predefinida por nombre de rol desde la clase GraphQLQueries
                query = GraphQLQueries.USUARIOS_POR_ROL_NOMBRE;
                operationName = "ObtenerUsuariosPorNombreRol";
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
            logger.error("Error al procesar la consulta GraphQL para usuarios por rol", e);
            
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la consulta: " + e.getMessage())
                    .build();
        }
    }
}