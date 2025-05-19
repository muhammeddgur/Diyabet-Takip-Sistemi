package org.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Veritabanı bağlantılarını yönetmek için kullanılan sınıf.
 */
public class DatabaseConnectionManager {
    private static final String DB_URL = "jdbc:postgresql://100.91.94.35:5432/diyabettakip";
    private static final String USER = "emirhan";
    private static final String PASSWORD = "karabulut_0704";

    private static DatabaseConnectionManager instance;

    // Singleton tasarım deseni için private constructor
    private DatabaseConnectionManager() {
        try {
            // PostgreSQL JDBC sürücüsünü yükle
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC sürücüsü yüklenemedi: " + e.getMessage());
        }
    }

    /**
     * Singleton örneği döndürür.
     */
    public static DatabaseConnectionManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionManager.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Veritabanına bir bağlantı oluşturur ve döndürür.
     *
     * @return Veritabanı bağlantısı
     * @throws SQLException Bağlantı hatası durumunda
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    /**
     * Veritabanı bağlantısını kapatır.
     *
     * @param connection Kapatılacak bağlantı
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Bağlantı kapatılamadı: " + e.getMessage());
            }
        }
    }
}