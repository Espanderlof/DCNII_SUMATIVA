package com.function.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Clase de utilidad para copiar el esquema GraphQL al directorio de recursos.
 * Se debe ejecutar manualmente durante el desarrollo.
 */
public class CopySchemaTask {

    public static void main(String[] args) {
        // Ruta de destino en el directorio de recursos
        Path targetDir = Paths.get("src", "main", "resources");
        Path targetSchemaPath = targetDir.resolve("schema.graphql");

        try {
            // Crear el directorio de recursos si no existe
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
                System.out.println("Directorio de recursos creado: " + targetDir);
            }

            // Copiar el contenido del esquema
            String schemaContent = """
                    type Usuario {
                        idUsuario: ID!
                        username: String!
                        email: String!
                        nombre: String
                        apellido: String
                        fechaCreacion: String
                        fechaModificacion: String
                        ultimoLogin: String
                        activo: Boolean!
                        roles: [Rol!]
                        logs: [LogEvento!]
                    }

                    type Rol {
                        idRol: ID!
                        nombre: String!
                        descripcion: String
                        fechaCreacion: String
                        fechaModificacion: String
                        activo: Boolean!
                        usuarios: [Usuario!]
                    }

                    type LogEvento {
                        idLog: ID!
                        fechaEvento: String!
                        idUsuario: ID
                        username: String
                        tipoEvento: String!
                        modulo: String!
                        accion: String!
                        entidad: String
                        idAfectado: ID
                        datosPrevios: String
                        datosNuevos: String
                        ipOrigen: String
                        userAgent: String
                        nivel: String
                        usuario: Usuario
                    }

                    type Query {
                        # Consultas relacionadas con usuarios
                        usuario(idUsuario: ID!): Usuario
                        usuarioPorUsername(username: String!): Usuario

                        # Consultas relacionadas con roles
                        rol(idRol: ID!): Rol
                        rolPorNombre(nombre: String!): Rol

                        # Consultas relacionadas con logs
                        usuariosConRol(idRol: ID!): [Usuario!]
                        usuariosConRolNombre(nombreRol: String!): [Usuario!]

                        # Consultas específicas para logs
                        logsPorUsuario(idUsuario: ID!): [LogEvento!]
                        logsPorTipoEvento(tipoEvento: String!): [LogEvento!]
                        logsPorModulo(modulo: String!): [LogEvento!]
                        logsPorEntidad(entidad: String!): [LogEvento!]
                        logsPorNivel(nivel: String!): [LogEvento!]
                        logsPorRangoFechas(fechaInicio: String!, fechaFin: String!): [LogEvento!]

                        # Consulta con filtros múltiples
                        logsConFiltros(
                            idUsuario: ID
                            tipoEvento: String
                            modulo: String
                            entidad: String
                            nivel: String
                            fechaInicio: String
                            fechaFin: String
                        ): [LogEvento!]

                        # Estadísticas
                        estadisticasUsuarios: [Usuario!]
                    }
                                """;

            // Escribir el esquema en el directorio de recursos
            try (FileWriter writer = new FileWriter(targetSchemaPath.toFile())) {
                writer.write(schemaContent);
            }

            System.out.println("Esquema GraphQL copiado correctamente a: " + targetSchemaPath);

        } catch (IOException e) {
            System.err.println("Error al copiar el esquema GraphQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}