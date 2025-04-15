package com.function.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.function.util.DBConnectionManager;

import java.io.File;
import java.util.*;
import java.sql.Connection;

public class DebugFunction {
    @FunctionName("debug")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) 
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        context.getLogger().info("Funcion de depuracion iniciada");
        
        StringBuilder resultado = new StringBuilder();
        resultado.append("=== INFORMACION DE depuracion ===\n\n");
        
        // Variables de entorno
        resultado.append("VARIABLES DE ENTORNO:\n");
        resultado.append("ORACLE_USERNAME: ").append(System.getenv("ORACLE_USERNAME")).append("\n");
        resultado.append("ORACLE_PASSWORD: ").append(System.getenv("ORACLE_PASSWORD") != null ? "***configurada***" : "no configurada").append("\n");
        resultado.append("ORACLE_TNS_NAME: ").append(System.getenv("ORACLE_TNS_NAME")).append("\n");
        resultado.append("ORACLE_WALLET_PATH: ").append(System.getenv("ORACLE_WALLET_PATH")).append("\n\n");
        
        // Directorio actual
        resultado.append("INFORMACION DE DIRECTORIOS:\n");
        String currentDir = new File(".").getAbsolutePath();
        resultado.append("Directorio actual: ").append(currentDir).append("\n");
        
        // Verificar wallet
        String walletPath = System.getenv("ORACLE_WALLET_PATH");
        if (walletPath != null) {
            File walletDir = new File(walletPath);
            resultado.append("Wallet existe: ").append(walletDir.exists()).append("\n");
            
            if (walletDir.exists()) {
                resultado.append("Es directorio: ").append(walletDir.isDirectory()).append("\n");
                resultado.append("Archivos en wallet:\n");
                
                File[] files = walletDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        resultado.append("  - ").append(file.getName()).append("\n");
                    }
                } else {
                    resultado.append("  (No se pudieron listar los archivos)\n");
                }
            }
        } else {
            resultado.append("ORACLE_WALLET_PATH no esta definido\n");
        }
        
        resultado.append("\n");
        
        // Probar CONEXION
        resultado.append("PRUEBA DE CONEXION:\n");
        try {
            boolean dbAvailable = DBConnectionManager.isDatabaseAvailable();
            resultado.append("Base de datos disponible: ").append(dbAvailable).append("\n");
            
            if (dbAvailable) {
                try (Connection conn = DBConnectionManager.getConnection()) {
                    resultado.append("CONEXION establecida correctamente\n");
                }
            }
        } catch (Exception e) {
            resultado.append("Error al conectar: ").append(e.getMessage()).append("\n");
            if (e.getCause() != null) {
                resultado.append("Causa: ").append(e.getCause().getMessage()).append("\n");
            }
        }
        
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "text/plain")
                .body(resultado.toString())
                .build();
    }
}