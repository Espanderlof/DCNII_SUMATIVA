package com.function.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Administrador de conexiones a la base de datos utilizando HikariCP.
 */
public class DBConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(DBConnectionManager.class);
    
    // Verificamos si estamos en entorno de prueba
    private static final boolean IS_TEST_ENV = System.getProperty("test.environment") != null;
    
    private static HikariDataSource dataSource;
    
    // Inicialización del pool de conexiones
    static {
        try {
            if (IS_TEST_ENV) {
                initTestDataSource();
            } else {
                initOracleDataSource();
            }
        } catch (Exception e) {
            logger.error("Error al inicializar el pool de conexiones", e);
            // No lanzamos excepción para permitir que la aplicación continúe funcionando
            // Las funciones que no requieren base de datos seguirán operando
        }
    }
    
    /**
     * Inicializa un datasource para pruebas con H2
     */
    private static void initTestDataSource() {
        logger.info("Iniciando datasource para entorno de pruebas con H2");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        
        dataSource = new HikariDataSource(config);
        logger.info("Pool de conexiones para pruebas inicializado correctamente");
    }
    
    /**
     * Inicializa un datasource para Oracle Cloud
     */
    private static void initOracleDataSource() {
        logger.info("Iniciando datasource para Oracle Cloud con Wallet");
        
        try {
            // Obtener credenciales desde variables de entorno
            String dbUser = System.getenv("ORACLE_USERNAME");
            String dbPassword = System.getenv("ORACLE_PASSWORD");
            String tnsName = System.getenv("ORACLE_TNS_NAME");
            String walletPath = System.getenv("ORACLE_WALLET_PATH");
            
            if (dbUser == null || dbPassword == null || tnsName == null || walletPath == null) {
                logger.error("Error: Variables de entorno para Oracle no estan definidas correctamente");
                logger.error("ORACLE_USERNAME: {}, ORACLE_TNS_NAME: {}, ORACLE_WALLET_PATH: {}", 
                           dbUser != null ? "definido" : "no definido",
                           tnsName != null ? "definido" : "no definido", 
                           walletPath != null ? "definido" : "no definido");
                return;
            }
            
            // Verificar si existe el wallet
            File walletDir = new File(walletPath);
            
            if (!walletDir.exists()) {
                logger.error("Error: Directorio del Wallet no encontrado en: {}", walletPath);
                logger.error("Directorio actual: {}", new File(".").getAbsolutePath());
                // Listar archivos en el directorio actual para debugging
                File[] files = new File(".").listFiles();
                if (files != null) {
                    logger.info("Archivos en el directorio actual:");
                    for (File file : files) {
                        logger.info(file.getName() + (file.isDirectory() ? " (directorio)" : ""));
                    }
                }
                return;
            }
            
            logger.info("Wallet encontrado en: {}", walletPath);
            // Listar archivos en el directorio del wallet para debugging
            File[] walletFiles = walletDir.listFiles();
            if (walletFiles != null) {
                logger.info("Archivos en el wallet:");
                for (File file : walletFiles) {
                    logger.info(file.getName());
                }
            }
            
            // JDBC URL usando el wallet
            String jdbcUrl = "jdbc:oracle:thin:@" + tnsName + "?TNS_ADMIN=" + walletPath;
            
            logger.info("Configuracion de conexión con Wallet: URL={}, Usuario={}", jdbcUrl, dbUser);
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setDriverClassName("oracle.jdbc.driver.OracleDriver");
            
            // Propiedades adicionales para la conexión TLS
            Properties props = new Properties();
            props.setProperty("oracle.net.ssl_version", "1.2");
            props.setProperty("oracle.net.ssl_server_dn_match", "true");
            config.setDataSourceProperties(props);
            
            // Configuración avanzada
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setMinimumIdle(1);
            config.setMaximumPoolSize(5);
            
            dataSource = new HikariDataSource(config);
            logger.info("Pool de conexiones inicializado correctamente");
            
            // Validar conexión inmediatamente
            try (Connection conn = dataSource.getConnection()) {
                logger.info("¡Conexion a Oracle validada correctamente!");
            }
        } catch (Exception e) {
            logger.error("Error al configurar la conexion Oracle", e);
            logger.error("Mensaje de error: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Causa: {}", e.getCause().getMessage());
            }
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
            dataSource = null;
        }
    }
    
    /**
     * Obtiene una conexión del pool de conexiones.
     * @return Conexión a la base de datos
     * @throws SQLException si ocurre un error al obtener la conexión
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("El pool de conexiones no ha sido inicializado correctamente");
        }
        
        try {
            Connection connection = dataSource.getConnection();
            logger.debug("Conexion obtenida del pool correctamente");
            return connection;
        } catch (SQLException e) {
            logger.error("Error al obtener conexion del pool", e);
            throw e;
        }
    }
    
    /**
     * Comprueba si la base de datos está disponible.
     * @return true si está disponible, false en caso contrario
     */
    public static boolean isDatabaseAvailable() {
        if (dataSource == null) {
            return false;
        }
        
        try (Connection conn = dataSource.getConnection()) {
            return true;
        } catch (SQLException e) {
            logger.error("Base de datos no disponible", e);
            return false;
        }
    }
}