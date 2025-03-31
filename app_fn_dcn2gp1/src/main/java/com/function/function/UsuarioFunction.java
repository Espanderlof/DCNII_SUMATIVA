package com.function.function;

import com.function.dao.UsuarioDAO;
import com.function.model.Response;
import com.function.model.Usuario;
import com.function.util.GsonConfig;
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

import java.util.*;

/**
 * funcion de Azure para la gestión de usuarios.
 */
public class UsuarioFunction {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioFunction.class);
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final Gson gson = GsonConfig.getGson();

    /**
     * Este método se ejecuta cuando se recibe una solicitud HTTP en el endpoint /api/usuarios.
     * Maneja operaciones CRUD para usuarios.
     * 
     * @param request Solicitud HTTP
     * @param context Contexto de ejecución
     * @return Respuesta HTTP
     */
    @FunctionName("usuarios")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "usuarios/{id=none}")
                    HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {
        
        logger.info("Solicitud HTTP recibida en la funcion de Usuarios");
        
        try {
            // Obtener el ID del usuario de la ruta, si existe
            // final String id = request.getQueryParameters().get("id");
            // final String id = request.getHeaders().get("x-ms-url-path-params") != null ?
            //     request.getHeaders().get("x-ms-url-path-params").get("id") :
            //     "none";
            
            // Manejar la solicitud según el método HTTP
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
                            .body(gson.toJson(Response.error("Metodo no soportado", "Este metodo HTTP no esta soportado")))
                            .header("Content-Type", "application/json")
                            .build();
            }
        } catch (Exception e) {
            logger.error("Error en la funcion de Usuarios", e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Response.error("Error interno del servidor", e.getMessage())))
                    .header("Content-Type", "application/json")
                    .build();
        }
    }

    /**
     * Maneja solicitudes GET para obtener usuarios.
     * 
     * @param request Solicitud HTTP
     * @param id ID del usuario (opcional)
     * @return Respuesta HTTP
     */
    private HttpResponseMessage handleGetRequest(HttpRequestMessage<Optional<String>> request, String id) {
        // Si se proporciona un ID, devuelve un usuario específico
        if (id != null && !id.equals("none")) {
            try {
                Long userId = Long.parseLong(id);
                Optional<Usuario> usuario = usuarioDAO.findById(userId);
                
                if (usuario.isPresent()) {
                    // No devolver el hash de contraseña en la respuesta
                    usuario.get().setPasswordHash(null);
                    return request.createResponseBuilder(HttpStatus.OK)
                            .body(gson.toJson(Response.success("Usuario encontrado", usuario.get())))
                            .header("Content-Type", "application/json")
                            .build();
                } else {
                    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .body(gson.toJson(Response.error("Usuario no encontrado", "No se encontro un usuario con el ID proporcionado")))
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
        // Si no se proporciona un ID, devuelve todos los usuarios
        else {
            List<Usuario> usuarios = usuarioDAO.findAll();
            
            // No devolver el hash de contraseña en la respuesta
            usuarios.forEach(u -> u.setPasswordHash(null));
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .body(gson.toJson(Response.success("Usuarios encontrados", usuarios)))
                    .header("Content-Type", "application/json")
                    .build();
        }
    }

    /**
     * Maneja solicitudes POST para crear usuarios.
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
            JsonObject jsonUsuario = JsonParser.parseString(request.getBody().get()).getAsJsonObject();
            
            // Validar campos obligatorios
            if (!jsonUsuario.has("username") || !jsonUsuario.has("email") || !jsonUsuario.has("password")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body(gson.toJson(Response.error("Datos invalidos", "Los campos username, email y password son obligatorios")))
                        .header("Content-Type", "application/json")
                        .build();
            }
            
            // Crear objeto Usuario con los datos proporcionados
            Usuario usuario = new Usuario();
            usuario.setUsername(jsonUsuario.get("username").getAsString());
            usuario.setEmail(jsonUsuario.get("email").getAsString());
            
            // Aplicar un hash a la contraseña (en un entorno real, usar un algoritmo seguro)
            String passwordHash = jsonUsuario.get("password").getAsString();
            // En producción, aquí iría el código para hashear la contraseña
            usuario.setPasswordHash(passwordHash);
            
            // Asignar otros campos si estan presentes
            if (jsonUsuario.has("nombre")) {
                usuario.setNombre(jsonUsuario.get("nombre").getAsString());
            }
            if (jsonUsuario.has("apellido")) {
                usuario.setApellido(jsonUsuario.get("apellido").getAsString());
            }
            
            // Crear el usuario en la base de datos
            Usuario usuarioCreado = usuarioDAO.create(usuario);
            
            // No devolver el hash de contraseña en la respuesta
            usuarioCreado.setPasswordHash(null);
            
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .body(gson.toJson(Response.success("Usuario creado correctamente", usuarioCreado)))
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
     * Maneja solicitudes PUT para actualizar usuarios.
     * 
     * @param request Solicitud HTTP
     * @param id ID del usuario a actualizar
     * @return Respuesta HTTP
     */
    private HttpResponseMessage handlePutRequest(HttpRequestMessage<Optional<String>> request, String id) {
        // Verificar si se proporciona un ID
        if (id == null || id.equals("none")) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(Response.error("ID no proporcionado", "Debe proporcionar un ID de usuario para actualizar")))
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
            Long userId = Long.parseLong(id);
            
            // Verificar si el usuario existe
            Optional<Usuario> usuarioExistente = usuarioDAO.findById(userId);
            if (!usuarioExistente.isPresent()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Response.error("Usuario no encontrado", "No se encontro un usuario con el ID proporcionado")))
                        .header("Content-Type", "application/json")
                        .build();
            }
            
            // Parsear el JSON de la solicitud
            JsonObject jsonUsuario = JsonParser.parseString(request.getBody().get()).getAsJsonObject();
            
            // Actualizar objeto Usuario con los datos proporcionados
            Usuario usuario = usuarioExistente.get();
            
            if (jsonUsuario.has("username")) {
                usuario.setUsername(jsonUsuario.get("username").getAsString());
            }
            if (jsonUsuario.has("email")) {
                usuario.setEmail(jsonUsuario.get("email").getAsString());
            }
            if (jsonUsuario.has("nombre")) {
                usuario.setNombre(jsonUsuario.get("nombre").getAsString());
            }
            if (jsonUsuario.has("apellido")) {
                usuario.setApellido(jsonUsuario.get("apellido").getAsString());
            }
            if (jsonUsuario.has("password")) {
                // En producción, aquí iría el código para hashear la contraseña
                usuario.setPasswordHash(jsonUsuario.get("password").getAsString());
            }
            
            // Actualizar el usuario en la base de datos
            if (usuarioDAO.update(usuario)) {
                // No devolver el hash de contraseña en la respuesta
                usuario.setPasswordHash(null);
                
                return request.createResponseBuilder(HttpStatus.OK)
                        .body(gson.toJson(Response.success("Usuario actualizado correctamente", usuario)))
                        .header("Content-Type", "application/json")
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Response.error("Error al actualizar", "No se pudo actualizar el usuario")))
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
     * Maneja solicitudes DELETE para eliminar usuarios.
     * 
     * @param request Solicitud HTTP
     * @param id ID del usuario a eliminar
     * @return Respuesta HTTP
     */
    private HttpResponseMessage handleDeleteRequest(HttpRequestMessage<Optional<String>> request, String id) {
        // Verificar si se proporciona un ID
        if (id == null || id.equals("none")) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(Response.error("ID no proporcionado", "Debe proporcionar un ID de usuario para eliminar")))
                    .header("Content-Type", "application/json")
                    .build();
        }
        
        try {
            Long userId = Long.parseLong(id);
            
            // Verificar si el usuario existe
            Optional<Usuario> usuarioExistente = usuarioDAO.findById(userId);
            if (!usuarioExistente.isPresent()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Response.error("Usuario no encontrado", "No se encontro un usuario con el ID proporcionado")))
                        .header("Content-Type", "application/json")
                        .build();
            }
            
            // Eliminar el usuario (baja lógica)
            if (usuarioDAO.delete(userId)) {
                return request.createResponseBuilder(HttpStatus.OK)
                        .body(gson.toJson(Response.success("Usuario eliminado correctamente", null)))
                        .header("Content-Type", "application/json")
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(gson.toJson(Response.error("Error al eliminar", "No se pudo eliminar el usuario")))
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
     * Este endpoint adicional permite asignar roles a usuarios.
     * 
     * @param request Solicitud HTTP
     * @param context Contexto de ejecución
     * @return Respuesta HTTP
     */
    @FunctionName("asignarRol")
    public HttpResponseMessage asignarRol(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "usuarios/{id}/roles")
                    HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {
        
        logger.info("Solicitud HTTP recibida para asignar rol a usuario");
        
        // Obtener el ID del usuario de la ruta
        // final String id = request.getQueryParameters().get("id");
        
        // Verificar si se proporciona un ID
        if (id == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(gson.toJson(Response.error("ID no proporcionado", "Debe proporcionar un ID de usuario")))
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
            Long userId = Long.parseLong(id);
            
            // Parsear el JSON de la solicitud
            JsonObject jsonRol = JsonParser.parseString(request.getBody().get()).getAsJsonObject();
            
            // Validar campos obligatorios
            if (!jsonRol.has("idRol")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body(gson.toJson(Response.error("Datos invalidos", "El campo idRol es obligatorio")))
                        .header("Content-Type", "application/json")
                        .build();
            }
            
            Long rolId = jsonRol.get("idRol").getAsLong();
            
            // Asignar el rol al usuario
            usuarioDAO.asignarRol(userId, rolId);
            
            // Obtener todos los roles del usuario
            List<Long> roles = usuarioDAO.getRoles(userId);
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .body(gson.toJson(Response.success("Rol asignado correctamente", roles)))
                    .header("Content-Type", "application/json")
                    .build();
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("El usuario no existe")) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Response.error("Usuario no encontrado", e.getMessage())))
                        .header("Content-Type", "application/json")
                        .build();
            } else if (e.getMessage().contains("El rol no existe")) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body(gson.toJson(Response.error("Rol no encontrado", e.getMessage())))
                        .header("Content-Type", "application/json")
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(gson.toJson(Response.error("Error al asignar rol", e.getMessage())))
                        .header("Content-Type", "application/json")
                        .build();
            }
        }
    }
}