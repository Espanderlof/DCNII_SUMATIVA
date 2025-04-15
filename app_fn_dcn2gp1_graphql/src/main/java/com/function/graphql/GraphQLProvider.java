package com.function.graphql;

import com.function.service.AuditoriaService;
import com.function.model.LogEvento;
import com.function.model.Rol;
import com.function.model.Usuario;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

public class GraphQLProvider {
    private static final Logger logger = LoggerFactory.getLogger(GraphQLProvider.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    // Schema GraphQL embebido como respaldo en caso de que no se pueda cargar del archivo
    private static final String EMBEDDED_SCHEMA = 
            "type Usuario {\n" +
            "    idUsuario: ID!\n" +
            "    username: String!\n" +
            "    email: String!\n" +
            "    nombre: String\n" +
            "    apellido: String\n" +
            "    fechaCreacion: String\n" +
            "    fechaModificacion: String\n" +
            "    ultimoLogin: String\n" +
            "    activo: Boolean!\n" +
            "    roles: [Rol!]\n" +
            "    logs: [LogEvento!]\n" +
            "}\n" +
            "\n" +
            "type Rol {\n" +
            "    idRol: ID!\n" +
            "    nombre: String!\n" +
            "    descripcion: String\n" +
            "    fechaCreacion: String\n" +
            "    fechaModificacion: String\n" +
            "    activo: Boolean!\n" +
            "    usuarios: [Usuario!]\n" +
            "}\n" +
            "\n" +
            "type LogEvento {\n" +
            "    idLog: ID!\n" +
            "    fechaEvento: String!\n" +
            "    idUsuario: ID\n" +
            "    username: String\n" +
            "    tipoEvento: String!\n" +
            "    modulo: String!\n" +
            "    accion: String!\n" +
            "    entidad: String\n" +
            "    idAfectado: ID\n" +
            "    datosPrevios: String\n" +
            "    datosNuevos: String\n" +
            "    ipOrigen: String\n" +
            "    userAgent: String\n" +
            "    nivel: String\n" +
            "    usuario: Usuario\n" +
            "}\n" +
            "\n" +
            "type Query {\n" +
            "    # Consultas relacionadas con usuarios\n" +
            "    usuario(idUsuario: ID!): Usuario\n" +
            "    usuarioPorUsername(username: String!): Usuario\n" +
            "    \n" +
            "    # Consultas relacionadas con roles\n" +
            "    rol(idRol: ID!): Rol\n" +
            "    rolPorNombre(nombre: String!): Rol\n" +
            "    \n" +
            "    # Consultas relacionadas con logs\n" +
            "    usuariosConRol(idRol: ID!): [Usuario!]\n" +
            "    usuariosConRolNombre(nombreRol: String!): [Usuario!]\n" +
            "    \n" +
            "    # Consultas específicas para logs\n" +
            "    logsPorUsuario(idUsuario: ID!): [LogEvento!]\n" +
            "    logsPorTipoEvento(tipoEvento: String!): [LogEvento!]\n" +
            "    logsPorModulo(modulo: String!): [LogEvento!]\n" +
            "    logsPorEntidad(entidad: String!): [LogEvento!]\n" +
            "    logsPorNivel(nivel: String!): [LogEvento!]\n" +
            "    logsPorRangoFechas(fechaInicio: String!, fechaFin: String!): [LogEvento!]\n" +
            "    \n" +
            "    # Consulta con filtros múltiples\n" +
            "    logsConFiltros(\n" +
            "        idUsuario: ID\n" +
            "        tipoEvento: String\n" +
            "        modulo: String\n" +
            "        entidad: String\n" +
            "        nivel: String\n" +
            "        fechaInicio: String\n" +
            "        fechaFin: String\n" +
            "    ): [LogEvento!]\n" +
            "    \n" +
            "    # Estadísticas\n" +
            "    estadisticasUsuarios: [Usuario!]\n" +
            "}";
    
    private static GraphQL graphQL;
    private final AuditoriaService auditoriaService;
    
    // Inicialización estática para garantizar que graphQL siempre esté disponible
    static {
        try {
            // Intentar inicializar con un esquema mínimo por defecto para evitar null
            SchemaParser schemaParser = new SchemaParser();
            TypeDefinitionRegistry typeRegistry = schemaParser.parse(new StringReader(EMBEDDED_SCHEMA));
            
            RuntimeWiring defaultWiring = RuntimeWiring.newRuntimeWiring().build();
            SchemaGenerator schemaGenerator = new SchemaGenerator();
            GraphQLSchema schema = schemaGenerator.makeExecutableSchema(typeRegistry, defaultWiring);
            
            graphQL = GraphQL.newGraphQL(schema).build();
            
            logger.info("GraphQL inicializado con esquema por defecto");
        } catch (Exception e) {
            logger.error("Error en la inicialización estática de GraphQL", e);
        }
    }
    
    public GraphQLProvider() {
        this.auditoriaService = new AuditoriaService();
        init();
    }
    
    /**
     * Convierte un String a LocalDateTime.
     * 
     * @param dateTimeStr String en formato ISO
     * @return LocalDateTime parseado
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.error("Error al parsear fecha: {}", dateTimeStr, e);
            return LocalDateTime.now();
        }
    }
    
    /**
     * Obtiene la instancia de GraphQL.
     * @return Instancia de GraphQL configurada
     */
    public static GraphQL getGraphQL() {
        // Garantizar que nunca se devuelva null
        if (graphQL == null) {
            logger.error("La instancia de GraphQL es nula. Se intentará reinicializar.");
            try {
                SchemaParser schemaParser = new SchemaParser();
                TypeDefinitionRegistry typeRegistry = schemaParser.parse(new StringReader(EMBEDDED_SCHEMA));
                
                RuntimeWiring defaultWiring = RuntimeWiring.newRuntimeWiring().build();
                SchemaGenerator schemaGenerator = new SchemaGenerator();
                GraphQLSchema schema = schemaGenerator.makeExecutableSchema(typeRegistry, defaultWiring);
                
                graphQL = GraphQL.newGraphQL(schema).build();
                logger.info("GraphQL reinicializado con esquema por defecto");
            } catch (Exception e) {
                logger.error("Error al reinicializar GraphQL", e);
                throw new RuntimeException("No se pudo inicializar GraphQL", e);
            }
        }
        return graphQL;
    }
    
    private void init() {
        try {
            // Cargar el esquema desde el archivo de recursos
            InputStream schemaStream = getClass().getClassLoader().getResourceAsStream("schema.graphql");
            TypeDefinitionRegistry typeRegistry;
            
            if (schemaStream == null) {
                logger.warn("No se pudo cargar el archivo schema.graphql. Usando esquema embebido.");
                typeRegistry = new SchemaParser().parse(new StringReader(EMBEDDED_SCHEMA));
            } else {
                logger.info("Archivo schema.graphql encontrado, cargando...");
                typeRegistry = new SchemaParser().parse(new InputStreamReader(schemaStream));
            }
            
            // Configurar el cableado en tiempo de ejecución con los resolvers
            RuntimeWiring runtimeWiring = buildRuntimeWiring();
            
            // Generar el esquema GraphQL
            SchemaGenerator schemaGenerator = new SchemaGenerator();
            GraphQLSchema schema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
            
            // Construir la instancia de GraphQL
            graphQL = GraphQL.newGraphQL(schema).build();
            
            logger.info("GraphQL inicializado correctamente con esquema completo");
        } catch (Exception e) {
            logger.error("Error al inicializar GraphQL con esquema completo", e);
            logger.info("El proveedor seguirá usando el esquema por defecto");
        }
    }
    
    private RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
            .type(TypeRuntimeWiring.newTypeWiring("Query")
                // Consultas de usuario
                .dataFetcher("usuario", environment -> {
                    Long idUsuario = Long.parseLong(environment.getArgument("idUsuario"));
                    return auditoriaService.obtenerUsuarioCompleto(idUsuario);
                })
                .dataFetcher("usuarioPorUsername", environment -> {
                    String username = environment.getArgument("username");
                    return auditoriaService.obtenerUsuarioCompletoPorUsername(username);
                })
                
                // Consultas de rol
                .dataFetcher("rol", environment -> {
                    Long idRol = Long.parseLong(environment.getArgument("idRol"));
                    return auditoriaService.obtenerRolCompleto(idRol);
                })
                .dataFetcher("rolPorNombre", environment -> {
                    String nombre = environment.getArgument("nombre");
                    return auditoriaService.obtenerRolCompletoPorNombre(nombre);
                })
                
                // Consultas combinadas
                .dataFetcher("usuariosConRol", environment -> {
                    Long idRol = Long.parseLong(environment.getArgument("idRol"));
                    return auditoriaService.obtenerUsuariosPorRolConLogs(idRol);
                })
                .dataFetcher("usuariosConRolNombre", environment -> {
                    String nombreRol = environment.getArgument("nombreRol");
                    return auditoriaService.obtenerUsuariosPorNombreRolConLogs(nombreRol);
                })
                
                // Consultas de logs específicas
                .dataFetcher("logsPorUsuario", environment -> {
                    Long idUsuario = Long.parseLong(environment.getArgument("idUsuario"));
                    return auditoriaService.buscarLogsConFiltros(idUsuario, null, null, null, null, null, null);
                })
                .dataFetcher("logsPorTipoEvento", environment -> {
                    String tipoEvento = environment.getArgument("tipoEvento");
                    return auditoriaService.obtenerLogsPorTipoEvento(tipoEvento);
                })
                .dataFetcher("logsPorModulo", environment -> {
                    String modulo = environment.getArgument("modulo");
                    return auditoriaService.obtenerLogsPorModulo(modulo);
                })
                .dataFetcher("logsPorEntidad", environment -> {
                    String entidad = environment.getArgument("entidad");
                    return auditoriaService.obtenerLogsPorEntidad(entidad);
                })
                .dataFetcher("logsPorNivel", environment -> {
                    String nivel = environment.getArgument("nivel");
                    return auditoriaService.obtenerLogsPorNivel(nivel);
                })
                .dataFetcher("logsPorRangoFechas", environment -> {
                    LocalDateTime fechaInicio = parseDateTime(environment.getArgument("fechaInicio"));
                    LocalDateTime fechaFin = parseDateTime(environment.getArgument("fechaFin"));
                    return auditoriaService.obtenerLogsPorRangoFechas(fechaInicio, fechaFin);
                })
                
                // Consulta con filtros múltiples
                .dataFetcher("logsConFiltros", environment -> {
                    // Obtener todas las variables como un mapa
                    Map<String, Object> arguments = environment.getArguments();
                    
                    Long idUsuario = null;
                    if (arguments.containsKey("idUsuario") && arguments.get("idUsuario") != null) {
                        idUsuario = Long.parseLong((String) arguments.get("idUsuario"));
                    }
                    
                    String tipoEvento = (String) arguments.getOrDefault("tipoEvento", null);
                    String modulo = (String) arguments.getOrDefault("modulo", null);
                    String entidad = (String) arguments.getOrDefault("entidad", null);
                    String nivel = (String) arguments.getOrDefault("nivel", null);
                    
                    LocalDateTime fechaInicio = null;
                    if (arguments.containsKey("fechaInicio") && arguments.get("fechaInicio") != null) {
                        fechaInicio = parseDateTime((String) arguments.get("fechaInicio"));
                    }
                    
                    LocalDateTime fechaFin = null;
                    if (arguments.containsKey("fechaFin") && arguments.get("fechaFin") != null) {
                        fechaFin = parseDateTime((String) arguments.get("fechaFin"));
                    }
                    
                    return auditoriaService.buscarLogsConFiltros(
                            idUsuario, tipoEvento, modulo, entidad, nivel, fechaInicio, fechaFin);
                })
                
                // Estadísticas
                .dataFetcher("estadisticasUsuarios", environment -> {
                    return auditoriaService.obtenerEstadisticasLogsPorUsuario();
                })
            )
            .build();
    }
}