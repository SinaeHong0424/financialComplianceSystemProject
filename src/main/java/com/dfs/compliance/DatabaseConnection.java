package com.dfs.compliance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {
    
    // [시스템 설정] IPv4 강제 사용 및 OOB 체크 해제 (필수)
    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("oracle.net.disableOob", "true");
        System.setProperty("oracle.jdbc.fanEnabled", "false");
    }

    // [접속 주소] TNS 방식 사용 (가장 안정적)
    private static final String DB_URL = System.getenv().getOrDefault(
        "DFS_DB_URL",
        "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=127.0.0.1)(PORT=1521))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=FREE)))"
    );
    
    private static final String DB_USER = System.getenv().getOrDefault("DFS_DB_USER", "system");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DFS_DB_PASSWORD", "oracle");
    
    // Private constructor to prevent instantiation
    private DatabaseConnection() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            
            Properties props = new Properties();
            props.setProperty("user", DB_USER);
            props.setProperty("password", DB_PASSWORD);
            
            // [수정] 암호화 설정 제거 (드라이버 기본값 사용)
            // props.setProperty("oracle.net.encryption_client", "ACCEPTED");  <-- 삭제함
            // props.setProperty("oracle.net.crypto_checksum_client", "ACCEPTED"); <-- 삭제함
            
            // 타임아웃 설정 (무한 대기 방지)
            props.setProperty("oracle.net.CONNECT_TIMEOUT", "5000"); // 5초
            props.setProperty("oracle.net.READ_TIMEOUT", "5000");    // 5초
            
            Connection conn = DriverManager.getConnection(DB_URL, props);
            
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            
            return conn;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle JDBC Driver not found", e);        
        }
    }
    
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }
    
    public static String getDatabaseVersion() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM v$version WHERE ROWNUM = 1")) {
            if (rs.next()) {
                return rs.getString(1);
            }
            return "Unknown version";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Database Connection Test ===");
        System.out.println("Target URL: " + DB_URL);
        try {
            String version = getDatabaseVersion();
            System.out.println("SUCCESS! Connected to: " + version);
        } catch (Exception e) {
            System.err.println("FAILED!");
            e.printStackTrace();
        }
    }
}