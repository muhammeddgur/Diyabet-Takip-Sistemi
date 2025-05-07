package dao;

import model.User;
import util.Password;
import java.sql.*;

public class UserDao {
    private final Connection conn;
    public UserDao(Connection conn) {
        this.conn = conn;
    }

    public User findByTcAndPassword(String tcKimlik, String password) throws SQLException {
        String sql = "SELECT id, tc_kimlik, email, password_hash, role FROM users WHERE tc_kimlik = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tcKimlik);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (Password.checkPassword(password, storedHash)) {
                        return new User(
                                rs.getInt("id"),
                                rs.getString("tc_kimlik"),
                                rs.getString("email"),
                                storedHash,
                                rs.getString("role")
                        );
                    }
                }
            }
        }
        return null;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
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
