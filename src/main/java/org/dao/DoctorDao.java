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
        String sql = "SELECT d.id, d.specialty, u.id as user_id, u.tc_kimlik, u.email, u.password_hash, u.role " +
                "FROM doctors d JOIN users u ON d.user_id = u.id WHERE d.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getInt("user_id"),
                            rs.getString("tc_kimlik"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("role")
                    );
                    return new Doctor(rs.getInt("id"), user, rs.getString("specialty"));
                }
            }
        }
        return null;
    }
}
