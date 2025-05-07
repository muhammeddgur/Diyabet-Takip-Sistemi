package org.dao;

import org.model.BloodSugarMeasurement;
import org.model.Patient;
import java.sql.*;
import java.time.LocalDateTime;

public class MeasurementDao {
    private final Connection conn;
    public MeasurementDao(Connection conn) { this.conn = conn; }

    public void save(BloodSugarMeasurement m) throws SQLException {
        String sql = "INSERT INTO blood_sugar_measurements(patient_id, value, measured_at) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getPatient().getId());
            ps.setDouble(2, m.getValue());
            ps.setTimestamp(3, Timestamp.valueOf(m.getMeasuredAt()));
            ps.executeUpdate();
        }
    }

    // Listeleme, silme, güncelleme metodları...
}
