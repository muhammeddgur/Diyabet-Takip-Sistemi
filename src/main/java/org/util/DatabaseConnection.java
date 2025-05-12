package org.util;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://100.91.94.35:5432/diyabettakip";
    private static final String USER = "emirhan";
    private static final String PASS = "karabulut_0704";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
