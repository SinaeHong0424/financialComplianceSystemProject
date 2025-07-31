package com.dfs.compliance.util;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection utility using Oracle Universal Connection Pool (UCP).
 * 
 * <p>This class manages database connections using connection pooling for
 * optimal performance and resource utilization. It follows the Singleton
 * pattern to ensure only one connection pool exists per application.
 * 
 * <p>Configuration is loaded from application.properties file.
 * 
 * @author DFS Technology Bureau
 * @version 1.0
 * @since 2025-08-04
 */
public class DatabaseConnection {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    
    private static DatabaseConnection instance;
    private PoolDataSource poolDataSource;
    private Properties properties;
    
    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the connection pool with properties from configuration file.
     * 
     * @throws SQLException if connection pool initialization fails
     * @throws IOException if properties file cannot be loaded
     */
    private DatabaseConnection() throws SQLException, IOException {
        loadProperties();
        initializeConnectionPool();
    }
    
    /**
     * Gets the singleton instance of DatabaseConnection.
     * Creates a new instance if one doesn't exist.
     * 
     * @return the singleton DatabaseConnection instance
     * @throws SQLException if connection pool initialization fails
     * @throws IOException if properties file cannot be loaded
     */
    public static synchronized DatabaseConnection getInstance() throws SQLException, IOException {
        if (instance == null) {
            instance = new DatabaseConnection();
            logger.info("DatabaseConnection instance created successfully");
        }
        return instance;
    }
    
    /**
     * Loads database configuration from application.properties file.
     * 
     * @throws IOException if properties file cannot be found or read
     */
    private void loadProperties() throws IOException {
        properties = new Properties();
        
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            
            if (input == null) {
                logger.error("Unable to find application.properties");
                throw new IOException("application.properties file not found in classpath");
            }
            
            properties.load(input);
            logger.info("Database properties loaded successfully");
            
        } catch (IOException e) {
            logger.error("Error loading application.properties: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Initializes Oracle Universal Connection Pool (UCP) with configured parameters.
     * 
     * @throws SQLException if pool initialization fails
     */
    private void initializeConnectionPool() throws SQLException {
        try {
            poolDataSource = PoolDataSourceFactory.getPoolDataSource();
            
            // Connection properties
            poolDataSource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
            poolDataSource.setURL(properties.getProperty("db.url"));
            poolDataSource.setUser(properties.getProperty("db.username"));
            poolDataSource.setPassword(properties.getProperty("db.password"));
            
            // Pool configuration
            poolDataSource.setInitialPoolSize(
                Integer.parseInt(properties.getProperty("db.pool.initialSize", "5"))
            );
            poolDataSource.setMinPoolSize(
                Integer.parseInt(properties.getProperty("db.pool.minSize", "5"))
            );
            poolDataSource.setMaxPoolSize(
                Integer.parseInt(properties.getProperty("db.pool.maxSize", "20"))
            );
            
            // Connection timeout (30 seconds)
            poolDataSource.setConnectionWaitTimeout(
                Integer.parseInt(properties.getProperty("db.pool.connectionWaitTimeout", "30"))
            );
            
            // Inactive connection timeout (5 minutes)
            poolDataSource.setInactiveConnectionTimeout(
                Integer.parseInt(properties.getProperty("db.pool.inactiveConnectionTimeout", "300"))
            );
            
            // Validate connection on borrow
            poolDataSource.setValidateConnectionOnBorrow(true);
            
            // Connection pool name
            poolDataSource.setConnectionPoolName("DFS_COMPLIANCE_POOL");
            
            logger.info("Connection pool initialized successfully");
            logger.info("Pool configuration - Initial: {}, Min: {}, Max: {}", 
                poolDataSource.getInitialPoolSize(),
                poolDataSource.getMinPoolSize(),
                poolDataSource.getMaxPoolSize()
            );
            
        } catch (SQLException e) {
            logger.error("Failed to initialize connection pool: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Gets a connection from the connection pool.
     * 
     * <p>Important: Connections must be closed in a finally block or
     * try-with-resources statement to return them to the pool.
     * 
     * @return a database connection from the pool
     * @throws SQLException if unable to get connection
     */
    public Connection getConnection() throws SQLException {
        if (poolDataSource == null) {
            logger.error("Connection pool not initialized");
            throw new SQLException("Connection pool not initialized");
        }
        
        try {
            Connection connection = poolDataSource.getConnection();
            logger.debug("Connection obtained from pool");
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to get connection from pool: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Gets current connection pool statistics.
     * Useful for monitoring and debugging.
     * 
     * @return formatted string with pool statistics
     * @throws SQLException if unable to retrieve statistics
     */
    public String getPoolStatistics() throws SQLException {
        if (poolDataSource == null) {
            return "Connection pool not initialized";
        }
        
        return String.format(
            "Pool Statistics - Available: %d, Borrowed: %d, Total: %d",
            poolDataSource.getAvailableConnectionsCount(),
            poolDataSource.getBorrowedConnectionsCount(),
            poolDataSource.getTotalConnectionsCount()
        );
    }
    
    /**
     * Tests database connectivity.
     * 
     * @return true if connection successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean isValid = conn.isValid(5); // 5 second timeout
            
            if (isValid) {
                logger.info("Database connection test successful");
            } else {
                logger.warn("Database connection test failed");
            }
            
            return isValid;
            
        } catch (SQLException e) {
            logger.error("Database connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Closes the connection pool and releases all resources.
     * Should be called during application shutdown.
     * 
     * @throws SQLException if error occurs while closing pool
     */
    public void closePool() throws SQLException {
        if (poolDataSource != null) {
            try {
                // Note: UCP doesn't have explicit close method
                // Connections will be closed when they return to pool
                logger.info("Connection pool shutdown initiated");
                poolDataSource = null;
                instance = null;
            } catch (Exception e) {
                logger.error("Error during pool shutdown: {}", e.getMessage());
                throw new SQLException("Error closing connection pool", e);
            }
        }
    }
    
    /**
     * Gets a property value from configuration.
     * 
     * @param key property key
     * @return property value or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}