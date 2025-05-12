package org.dao;

import org.model.Doctor;
import org.model.User;
import java.sql.*;

public class DoctorDao {
    private final Connection conn;

    public DoctorDao(Connection conn) {
        this.conn = conn;
    }

    public Doctor findById(int id) throws SQLException {
        String sql = "SELECT d.id, u.tc_kimlik, u.email, u.password_hash, u.role " +
                "FROM doctors d JOIN users u ON d.user_tc = u.tc_kimlik WHERE d.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getString("tc_kimlik"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("role")
                    );
                    return new Doctor(rs.getInt("id"), user);
                }
            }
        }
        return null;
    }

    public Doctor findByUserTc(String tcKimlik) throws SQLException {
        String sql = "SELECT d.id FROM doctors d WHERE d.user_tc = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tcKimlik);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new UserDao(conn).findByTc(tcKimlik);
                    return new Doctor(rs.getInt("id"), user);
                }
            }
        }
        return null;
    }
}