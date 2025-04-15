package com.function.graphql;

public final class GraphQLQueries {
    
    //Consulta para obtener usuarios por ID de rol.
    public static final String USUARIOS_POR_ROL_ID = 
            "query ObtenerUsuariosPorRol($idRol: ID!) { " +
            "  usuariosConRol(idRol: $idRol) { " +
            "    idUsuario " +
            "    username " +
            "    email " +
            "    nombre " +
            "    apellido " +
            "    fechaCreacion " +
            "    fechaModificacion " +
            "    ultimoLogin " +
            "    activo " +
            "    roles { " +
            "      idRol " +
            "      nombre " +
            "      descripcion " +
            "    } " +
            "    logs { " +
            "      idLog " +
            "      fechaEvento " +
            "      tipoEvento " +
            "      modulo " +
            "      accion " +
            "      entidad " +
            "      nivel " +
            "    } " +
            "  } " +
            "}";
    
    //Consulta para obtener usuarios por nombre de rol.
    public static final String USUARIOS_POR_ROL_NOMBRE = 
            "query ObtenerUsuariosPorNombreRol($nombreRol: String!) { " +
            "  usuariosConRolNombre(nombreRol: $nombreRol) { " +
            "    idUsuario " +
            "    username " +
            "    email " +
            "    nombre " +
            "    apellido " +
            "    fechaCreacion " +
            "    fechaModificacion " +
            "    ultimoLogin " +
            "    activo " +
            "    roles { " +
            "      idRol " +
            "      nombre " +
            "      descripcion " +
            "    } " +
            "    logs { " +
            "      idLog " +
            "      fechaEvento " +
            "      tipoEvento " +
            "      modulo " +
            "      accion " +
            "      entidad " +
            "      nivel " +
            "    } " +
            "  } " +
            "}";
    
    
    //Consulta para obtener un usuario por ID.
    public static final String USUARIO_POR_ID = 
            "query ObtenerUsuarioPorId($idUsuario: ID!) { " +
            "  usuario(idUsuario: $idUsuario) { " +
            "    idUsuario " +
            "    username " +
            "    email " +
            "    nombre " +
            "    apellido " +
            "    fechaCreacion " +
            "    fechaModificacion " +
            "    ultimoLogin " +
            "    activo " +
            "    roles { " +
            "      idRol " +
            "      nombre " +
            "      descripcion " +
            "    } " +
            "    logs { " +
            "      idLog " +
            "      fechaEvento " +
            "      tipoEvento " +
            "      modulo " +
            "      accion " +
            "    } " +
            "  } " +
            "}";
    
    //Consulta para obtener logs por filtros múltiples.
    public static final String LOGS_CON_FILTROS = 
            "query ObtenerLogsConFiltros(" +
            "  $idUsuario: ID, " +
            "  $tipoEvento: String, " +
            "  $modulo: String, " +
            "  $entidad: String, " +
            "  $nivel: String, " +
            "  $fechaInicio: String, " +
            "  $fechaFin: String " +
            ") { " +
            "  logsConFiltros(" +
            "    idUsuario: $idUsuario, " +
            "    tipoEvento: $tipoEvento, " +
            "    modulo: $modulo, " +
            "    entidad: $entidad, " +
            "    nivel: $nivel, " +
            "    fechaInicio: $fechaInicio, " +
            "    fechaFin: $fechaFin " +
            "  ) { " +
            "    idLog " +
            "    fechaEvento " +
            "    username " +
            "    tipoEvento " +
            "    modulo " +
            "    accion " +
            "    entidad " +
            "    nivel " +
            "    datosPrevios " +
            "    datosNuevos " +
            "  } " +
            "}";
    
    //Constructor privado para evitar instanciación
    private GraphQLQueries() {
        throw new AssertionError("La clase GraphQLQueries no debe ser instanciada");
    }
}