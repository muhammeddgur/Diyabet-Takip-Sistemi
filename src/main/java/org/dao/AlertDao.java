package org.dao;

import org.model.Alert;
import java.sql.*;
import java.time.LocalDateTime;

public class AlertDao {
    private final Connection conn;
    public AlertDao(Connection conn) { this.conn = conn; }

    public void save(Alert a) throws SQLException {
        String sql = "INSERT INTO alerts(patient_id, type, message, created_at) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getPatient().getId());
            ps.setString(2, a.getType());
            ps.setString(3, a.getMessage());
            ps.setTimestamp(4, Timestamp.valueOf(a.getCreatedAt()));
            ps.executeUpdate();
        }
    }

}
