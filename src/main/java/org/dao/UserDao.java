package org.dao;

import org.model.User;
import org.util.Password;
import java.sql.*;

public class UserDao {
    private final Connection conn;

    public UserDao(Connection conn) {
        this.conn = conn;
    }

    public User findByTcAndPassword(String tcKimlik, String password) throws SQLException {
        System.out.println("UserDao - TC kontrolü: " + tcKimlik);
        String sql = "SELECT tc_kimlik, email, password_hash, role FROM users WHERE tc_kimlik = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tcKimlik);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    System.out.println("UserDao - Kayıt bulundu! Hash: " + storedHash);
                    System.out.println("UserDao - Girilen şifre: " + password);

                    boolean passwordMatch = Password.checkPassword(password, storedHash);
                    System.out.println("UserDao - Şifre eşleşiyor mu? " + passwordMatch);

                    if (passwordMatch) {
                        return new User(
                                rs.getString("tc_kimlik"),
                                rs.getString("email"),
                                storedHash,
                                rs.getString("role")
                        );
                    }
                } else {
                    System.out.println("UserDao - TC ile kayıt bulunamadı!");
                }
            }
        } catch (Exception e) {
            System.out.println("UserDao - HATA: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public User findByTc(String tcKimlik) throws SQLException {
        String sql = "SELECT tc_kimlik, email, password_hash, role FROM users WHERE tc_kimlik = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tcKimlik);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("tc_kimlik"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("role")
                    );
                }
            }
        }
        return null;
    }
}