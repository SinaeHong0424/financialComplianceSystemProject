package com.dfs.compliance.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Database connection utility using Oracle Universal Connection Pool (UCP).
 * 
 * <p>Implements singleton pattern to ensure single connection pool instance.
 * Provides connection pooling for improved performance and resource management.
 */
public class DatabaseConnection {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    
    private static DatabaseConnection instance;
    private static PoolDataSource poolDataSource;
    private static Properties properties;
    
    /**
     * Private constructor to prevent instantiation.
     * Initializes the connection pool with configuration from properties file.
     * 
     * @throws SQLException if pool initialization fails
     */
    private DatabaseConnection() throws SQLException {
        try {
            initializePool();
            logger.info("Database connection pool initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool: {}", e.getMessage());
            throw new SQLException("Database pool initialization failed", e);
        }
    }
    
    /**
     * Gets singleton instance of DatabaseConnection.
     * 
     * @return DatabaseConnection instance
     * @throws SQLException if initialization fails
     */
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Initializes Oracle UCP connection pool.
     * 
     * @throws SQLException if pool setup fails
     * @throws IOException if properties file cannot be read
     */
    private void initializePool() throws SQLException, IOException {
        loadProperties();
        
        poolDataSource = PoolDataSourceFactory.getPoolDataSource();
        
        // Basic connection properties
        poolDataSource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        poolDataSource.setURL(getProperty("db.url"));
        poolDataSource.setUser(getProperty("db.username"));
        poolDataSource.setPassword(getProperty("db.password"));
        
        // Pool size configuration
        poolDataSource.setInitialPoolSize(
            Integer.parseInt(getProperty("db.pool.initialSize", "5"))
        );
        poolDataSource.setMinPoolSize(
            Integer.parseInt(getProperty("db.pool.minSize", "5"))
        );
        poolDataSource.setMaxPoolSize(
            Integer.parseInt(getProperty("db.pool.maxSize", "20"))
        );
        
        // Connection timeout configuration (in seconds)
        poolDataSource.setConnectionWaitTimeout(
            Integer.parseInt(getProperty("db.pool.connectionWaitTimeout", "30"))
        );
        
        // Inactive connection timeout (in seconds)
        poolDataSource.setInactiveConnectionTimeout(
            Integer.parseInt(getProperty("db.pool.inactiveConnectionTimeout", "300"))
        );
        
        // Validate connections on borrow
        poolDataSource.setValidateConnectionOnBorrow(true);
        
        logger.info("Connection pool configured: min={}, max={}, initial={}", 
                    poolDataSource.getMinPoolSize(),
                    poolDataSource.getMaxPoolSize(),
                    poolDataSource.getInitialPoolSize());
    }
    
    /**
     * Loads database properties from application.properties file.
     * 
     * @throws IOException if properties file cannot be read
     */
    private void loadProperties() throws IOException {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.warn("application.properties not found, using defaults");
                setDefaultProperties();
                return;
            }
            properties.load(input);
            logger.debug("Loaded properties from application.properties");
        }
    }
    
    /**
     * Sets default database properties.
     */
    private void setDefaultProperties() {
        properties.setProperty("db.url", "jdbc:oracle:thin:@localhost:1521/FREE");
        properties.setProperty("db.username", "dfs_compliance");
        properties.setProperty("db.password", "SecurePassword123");
        properties.setProperty("db.pool.initialSize", "5");
        properties.setProperty("db.pool.minSize", "5");
        properties.setProperty("db.pool.maxSize", "20");
        properties.setProperty("db.pool.connectionWaitTimeout", "30");
        properties.setProperty("db.pool.inactiveConnectionTimeout", "300");
    }
    
    /**
     * Gets a property value with optional default.
     * 
     * @param key property key
     * @param defaultValue default value if property not found
     * @return property value or default
     */
    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Gets a property value.
     * 
     * @param key property key
     * @return property value
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Gets a connection from the pool.
     * 
     * @return database connection
     * @throws SQLException if connection cannot be obtained
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = poolDataSource.getConnection();
            logger.debug("Connection obtained from pool");
            return conn;
        } catch (SQLException e) {
            logger.error("Failed to get connection from pool: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Tests database connectivity.
     * 
     * @return true if connection is successful
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean valid = conn.isValid(5);
            logger.info("Database connection test: {}", valid ? "SUCCESS" : "FAILED");
            return valid;
        } catch (SQLException e) {
            logger.error("Database connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets connection pool statistics.
     * 
     * @return formatted statistics string
     */
    public String getPoolStatistics() {
        try {
            return String.format(
                "Pool Statistics - Available: %d, Borrowed: %d, Total: %d",
                poolDataSource.getAvailableConnectionsCount(),
                poolDataSource.getBorrowedConnectionsCount(),
                poolDataSource.getAvailableConnectionsCount() + 
                    poolDataSource.getBorrowedConnectionsCount()
            );
        } catch (SQLException e) {
            logger.error("Failed to get pool statistics: {}", e.getMessage());
            return "Pool statistics unavailable";
        }
    }
    
    /**
     * Closes the connection pool and releases all resources.
     * Should be called during application shutdown.
     */
    public void closePool() {
        if (poolDataSource != null) {
            try {
                logger.info("Closing connection pool...");
                // UCP doesn't have explicit close, connections are released automatically
                poolDataSource = null;
                instance = null;
                logger.info("Connection pool closed successfully");
            } catch (Exception e) {
                logger.error("Error closing connection pool: {}", e.getMessage());
            }
        }
    }
}