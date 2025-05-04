package com.function.function;

import com.function.dao.RolDAO;
import com.function.model.Response;
import com.function.model.Rol;
import com.function.util.GsonConfig;
import com.function.util.EventGridPublisher;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Funcion de Azure para la gestion de roles.
 */
public class RolFunction {
    private static final Logger logger = LoggerFactory.getLogger(RolFunction.class);
    private final RolDAO rolDAO = new RolDAO();
    private final Gson gson = GsonConfig.getGson();

    /**
     * Este Metodo se ejecuta cuando se recibe una solicitud HTTP en el endpoint /api/roles.
     * Maneja operaciones CRUD para roles.
     * 
     * @param request Solicitud HTTP
     * @param context Contexto de ejecución
     * @return Respuesta HTTP
     */
    @FunctionName("roles")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "roles/{id=none}")
                    HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {
        
        logger.info("Solicitud HTTP recibida en la Funcion de Roles");
        
        try {
            // Manejar la solicitud según el Metodo HTTP
            switch (request.getHttpMethod()) {
                case GET:
                    return handleGetRequest(request, id);
                case POST:
                    return handlePostRequest(request);
                case PUT:
                    return handlePutRequest(request, id);
                case DELETE:
                    return handleDeleteRequest(request, id);
                default:
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                            .body(gson.toJson(Response.error("Metodo no soportado", "Este Metodo HTTP no esta soportado")))
                            .header("Content-Type", "application/json")
                            .build();
            }
        } catch (Exception e) {
            logger.error("Error en la Funcion de Roles", e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Response.error("Error interno del servidor", e.getMessage())))
                    .header("Content-Type", "application/json")
                    .build();
        }
    }
    /**
     * Maneja solicitudes GET para obtener roles.
     * 
     * @param request Solicitud HTTP
     * @param id ID del rol (opcional)
     * @return Respuesta HTTP
     */
    private HttpResponseMessage handleGetRequest(HttpRequestMessage<Optional<String>> request, String id) {
        // Si se proporciona un ID, devuelve un rol específico
        if (id != null && !id.equals("none")) {
            try {
                Long rolId = Long.parseLong(id);
                Optional<Rol> rol = rolDAO.findById(rolId);
                
                if (rol.isPresent()) {
                    return request.createResponseBuilder(HttpStatus.OK)
                            .body(gson.toJson(Response.success("Rol encontrado", rol.get())))
                            .header("Content-Type", "application/json")
                            .build();
                } else {
                    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .body(gson.toJson(Response.error("Rol no encontrado", "No se encontro un rol con el ID proporcionado")))
                            .header("Content-Type", "application/json")
                            .build();
                }
            } catch (NumberFormatException e) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body(gson.toJson(Response.error("ID invalido", "El ID proporcionado no es un numero valido")))
                        .header("Content-Type", "application/json")
                        .build();
            }
        } 
        // Si no se proporciona un ID, devuelve todos los roles
        else {
            List<Rol> roles = rolDAO.findAll();
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .body(gson.toJson(Response.success("Roles encontrados", roles)))
                    .header("Content-Type", "application/json")
                    .build();
        }
    }

    /**
     * Maneja solicitudes POST para crear roles.
     * 
     * @param request Solicitud HTTP
     * @return Respuesta HTTP
     */
    private HttpResponseMessage handlePostRequest(HttpRequestMessage<Optional<String>> request) {
        // Verificar si el cuerpo de la solicitud esta presente
        if (!request.getBody().isPresent() || StringUtils.isBlank(request.getBody().get())) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(Response.error("Datos invalidos", "El cuerpo de la solicitud esta vacio")))
                    .header("Content-Type", "application/json")
                    .build();
        }
        
        try {
            // Parsear el JSON de la solicitud
            JsonObject jsonRol = JsonParser.parseString(request.getBody().get()).getAsJsonObject();
            
            // Validar campos obligatorios
            if (!jsonRol.has("nombre")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body(gson.toJson(Response.error("Datos invalidos", "El campo nombre es obligatorio")))
                        .header("Content-Type", "application/json")
                        .build();
            }
            
            // Crear objeto Rol con los datos proporcionados
            Rol rol = new Rol();
            rol.setNombre(jsonRol.get("nombre").getAsString());
            
            if (jsonRol.has("descripcion")) {
                rol.setDescripcion(jsonRol.get("descripcion").getAsString());
            }
            
            // Crear el rol en la base de datos
            Rol rolCreado = rolDAO.create(rol);
            
            // Publicar evento de creación de rol
            Rol rolParaEvento = new Rol();
            rolParaEvento.setIdRol(rolCreado.getIdRol());
            rolParaEvento.setNombre(rolCreado.getNombre());
            rolParaEvento.setDescripcion(rolCreado.getDescripcion());
            rolParaEvento.setFechaCreacion(rolCreado.getFechaCreacion());

            // En un entorno real, aquí obtendríamos la información del usuario que está realizando la acción
            // Por ahora, enviamos un evento simplificado
            EventGridPublisher.publishEvent(
                "/roles/created",
                "role_created",
                rolParaEvento
            );
            
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .body(gson.toJson(Response.success("Rol creado correctamente", rolCreado)))
                    .header("Content-Type", "application/json")
                    .build();
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Ya existe")) {
                return request.createResponseBuilder(HttpStatus.CONFLICT)
                        .body(gson.toJson(Response.error("Conflicto", e.getMessage())))
                        .header("Content-Type", "application/json")
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body(gson.toJson(Response.error("Error", e.getMessage())))
                        .header("Content-Type", "application/json")
                        .build();
            }
        }
    }

    /**
     * Maneja solicitudes PUT para actualizar roles.
     * 
     * @param request Solicitud HTTP
     * @param id ID del rol a actualizar
     * @return Respuesta HTTP
     */
    private HttpResponseMessage handlePutRequest(HttpRequestMessage<Optional<String>> request, String id) {
        // Verificar si se proporciona un ID
        if (id == null || id.equals("none")) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(Response.error("ID no proporcionado", "Debe proporcionar un ID de rol para actualizar")))
                    .header("Content-Type", "application/json")
                    .build();
        }
        
        // Verificar si el cuerpo de la solicitud esta presente
        if (!request.getBody().isPresent() || StringUtils.isBlank(request.getBody().get())) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(Response.error("Datos invalidos", "El cuerpo de la solicitud esta vacio")))
                    .header("Content-Type", "application/json")
                    .build();
        }
        
        try {
            Long rolId = Long.parseLong(id);
            
            // Verificar si el rol existe
            Optional<Rol> rolExistente = rolDAO.findById(rolId);
            if (!rolExistente.isPresent()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Response.error("Rol no encontrado", "No se encontro un rol con el ID proporcionado")))
                        .header("Content-Type", "application/json")
                        .build();
            }
            
            // Guardar datos previos para el evento
            Rol datosAnteriores = new Rol();
            datosAnteriores.setIdRol(rolExistente.get().getIdRol());
            datosAnteriores.setNombre(rolExistente.get().getNombre());
            datosAnteriores.setDescripcion(rolExistente.get().getDescripcion());
            
            // Parsear el JSON de la solicitud
            JsonObject jsonRol = JsonParser.parseString(request.getBody().get()).getAsJsonObject();
            
            // Actualizar objeto Rol con los datos proporcionados
            Rol rol = rolExistente.get();
            
            if (jsonRol.has("nombre")) {
                rol.setNombre(jsonRol.get("nombre").getAsString());
            }
            if (jsonRol.has("descripcion")) {
                rol.setDescripcion(jsonRol.get("descripcion").getAsString());
            }
            
            // Actualizar el rol en la base de datos
            if (rolDAO.update(rol)) {
                // Publicar evento de actualización de rol
                JsonObject dataEvento = new JsonObject();
                dataEvento.addProperty("idRol", rol.getIdRol());
                dataEvento.addProperty("nombre", rol.getNombre());
                dataEvento.addProperty("descripcion", rol.getDescripcion());
                
                // Agregar datos previos
                JsonObject datosPreviosJson = gson.toJsonTree(datosAnteriores).getAsJsonObject();
                dataEvento.add("datosPrevios", datosPreviosJson);
                
                // Agregar datos nuevos
                JsonObject datosNuevosJson = gson.toJsonTree(rol).getAsJsonObject();
                dataEvento.add("datosNuevos", datosNuevosJson);

                // En un entorno real, aquí incluiríamos datos del usuario que realiza la acción
                EventGridPublisher.publishEvent(
                    "/roles/updated",
                    "role_updated",
                    dataEvento
                );
                
                return request.createResponseBuilder(HttpStatus.OK)
                        .body(gson.toJson(Response.success("Rol actualizado correctamente", rol)))
                        .header("Content-Type", "application/json")
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Response.error("Error al actualizar", "No se pudo actualizar el rol")))
                        .header("Content-Type", "application/json")
                        .build();
            }
            
        } catch (NumberFormatException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(Response.error("ID invalido", "El ID proporcionado no es un numero valido")))
                    .header("Content-Type", "application/json")
                    .build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Ya existe")) {
                return request.createResponseBuilder(HttpStatus.CONFLICT)
                        .body(gson.toJson(Response.error("Conflicto", e.getMessage())))
                        .header("Content-Type", "application/json")
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body(gson.toJson(Response.error("Error", e.getMessage())))
                        .header("Content-Type", "application/json")
                        .build();
            }
        }
    }
    /**
     * Maneja solicitudes DELETE para eliminar roles.
     * 
     * @param request Solicitud HTTP
     * @param id ID del rol a eliminar
     * @return Respuesta HTTP
     */
    private HttpResponseMessage handleDeleteRequest(HttpRequestMessage<Optional<String>> request, String id) {
        // Verificar si se proporciona un ID
        if (id == null || id.equals("none")) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(Response.error("ID no proporcionado", "Debe proporcionar un ID de rol para eliminar")))
                    .header("Content-Type", "application/json")
                    .build();
        }
        
        try {
            Long rolId = Long.parseLong(id);
            
            // Verificar si el rol existe
            Optional<Rol> rolExistente = rolDAO.findById(rolId);
            if (!rolExistente.isPresent()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Response.error("Rol no encontrado", "No se encontro un rol con el ID proporcionado")))
                        .header("Content-Type", "application/json")
                        .build();
            }
            
            // Eliminar el rol (baja lógica)
            if (rolDAO.delete(rolId)) {
                // Publicar evento de eliminación de rol
                JsonObject dataEvento = new JsonObject();
                dataEvento.addProperty("idRol", rolId);
                dataEvento.addProperty("nombre", rolExistente.get().getNombre());
                
                // Agregar datos previos
                JsonObject datosPreviosJson = gson.toJsonTree(rolExistente.get()).getAsJsonObject();
                dataEvento.add("datosPrevios", datosPreviosJson);

                // En un entorno real, aquí incluiríamos datos del usuario que realiza la acción
                EventGridPublisher.publishEvent(
                    "/roles/deleted",
                    "role_deleted",
                    dataEvento
                );
                
                return request.createResponseBuilder(HttpStatus.OK)
                        .body(gson.toJson(Response.success("Rol eliminado correctamente", null)))
                        .header("Content-Type", "application/json")
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(gson.toJson(Response.error("Error al eliminar", "No se pudo eliminar el rol")))
                        .header("Content-Type", "application/json")
                        .build();
            }
            
        } catch (NumberFormatException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(Response.error("ID invalido", "El ID proporcionado no es un numero valido")))
                    .header("Content-Type", "application/json")
                    .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Response.error("Error al eliminar", e.getMessage())))
                    .header("Content-Type", "application/json")
                    .build();
        }
    }
    
    /**
     * Este endpoint adicional permite listar los usuarios de un rol.
     * 
     * @param request Solicitud HTTP
     * @param context Contexto de ejecución
     * @return Respuesta HTTP
     */
    @FunctionName("usuariosRol")
    public HttpResponseMessage getUsuarios(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "roles/{id}/usuarios")
                    HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {
        
        logger.info("Solicitud HTTP recibida para obtener usuarios de un rol");
        
        // Verificar si se proporciona un ID
        if (id == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(Response.error("ID no proporcionado", "Debe proporcionar un ID de rol")))
                    .header("Content-Type", "application/json")
                    .build();
        }
        
        try {
            Long rolId = Long.parseLong(id);
            
            // Verificar si el rol existe
            Optional<Rol> rolExistente = rolDAO.findById(rolId);
            if (!rolExistente.isPresent()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Response.error("Rol no encontrado", "No se encontro un rol con el ID proporcionado")))
                        .header("Content-Type", "application/json")
                        .build();
            }
            
            // Obtener los usuarios que tienen este rol
            List<Long> usuarios = rolDAO.getUsuarios(rolId);
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .body(gson.toJson(Response.success("Usuarios del rol", usuarios)))
                    .header("Content-Type", "application/json")
                    .build();
            
        } catch (NumberFormatException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(Response.error("ID invalido", "El ID proporcionado no es un numero valido")))
                    .header("Content-Type", "application/json")
                    .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Response.error("Error", e.getMessage())))
                    .header("Content-Type", "application/json")
                    .build();
        }
    }
}