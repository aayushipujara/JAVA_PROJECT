package com.signbridge.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static Connection connection = null;
    private static String dbType = "sqlite";
    private static String statusMessage = "Not Connected";

    private DBConnection() {}

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initConnection();
            }
        } catch (SQLException e) {
            System.err.println("[DBConnection] Error checking connection status: " + e.getMessage());
            initConnection();
        }
        return connection;
    }

    private static void initConnection() {
        Properties props = loadProperties();
        dbType = props.getProperty("db.type", "sqlite").trim().toLowerCase();

        try {
            if ("mysql".equals(dbType)) {
                String host = props.getProperty("mysql.host", "localhost");
                String port = props.getProperty("mysql.port", "3306");
                String dbName = props.getProperty("mysql.database", "signbridge_db");
                String user = props.getProperty("mysql.username", "root");
                String pass = props.getProperty("mysql.password", "");

                Class.forName("com.mysql.cj.jdbc.Driver");
                String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                connection = DriverManager.getConnection(url, user, pass);
                statusMessage = "Connected to MySQL (" + dbName + ")";
                System.out.println("[DBConnection] " + statusMessage);
            } else {
                // SQLite default
                String filename = props.getProperty("sqlite.filename", "signbridge.db");
                Class.forName("org.sqlite.JDBC");
                String url = "jdbc:sqlite:" + filename;
                connection = DriverManager.getConnection(url);
                statusMessage = "Connected to SQLite (" + filename + ")";
                System.out.println("[DBConnection] " + statusMessage);
            }
        } catch (ClassNotFoundException e) {
            statusMessage = "Driver Error: " + e.getMessage();
            System.err.println("[DBConnection] JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            statusMessage = "Connection Failed: " + e.getMessage();
            System.err.println("[DBConnection] Database Connection failed: " + e.getMessage());
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        // Check local resources folder or classpath
        File propFile = new File("resources/db.properties");
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                props.load(in);
                return props;
            } catch (Exception e) {
                System.err.println("[DBConnection] Warning: Could not read resources/db.properties: " + e.getMessage());
            }
        }

        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
                return props;
            }
        } catch (Exception ignored) {}

        // Fallback defaults
        props.setProperty("db.type", "sqlite");
        props.setProperty("sqlite.filename", "signbridge.db");
        return props;
    }

    public static String getDbType() {
        return dbType;
    }

    public static String getStatusMessage() {
        return statusMessage;
    }

    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
