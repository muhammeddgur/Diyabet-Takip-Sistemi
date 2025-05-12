package org.dao;

import org.model.BloodSugarMeasurement;
import org.model.Patient;
import java.sql.*;
import java.time.LocalDateTime;

public class MeasurementDao {
    private final Connection conn;

    public MeasurementDao(Connection conn) {
        this.conn = conn;
    }

    public void save(BloodSugarMeasurement m) throws SQLException {
        String sql = "INSERT INTO blood_sugar_measurements(patient_id, value, measured_at) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getPatient().getId());
            ps.setDouble(2, m.getValue());
            ps.setTimestamp(3, Timestamp.valueOf(m.getMeasuredAt()));
            ps.executeUpdate();
        }
    }

    public BloodSugarMeasurement findById(int id) throws SQLException {
        String sql = "SELECT bsm.id, bsm.patient_id, bsm.value, bsm.measured_at FROM blood_sugar_measurements bsm WHERE bsm.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Patient patient = new PatientDao(conn).findById(rs.getInt("patient_id"));
                    double value = rs.getDouble("value");
                    LocalDateTime measuredAt = rs.getTimestamp("measured_at").toLocalDateTime();
                    return new BloodSugarMeasurement(id, patient, value, measuredAt);
                }
            }
        }
        return null;
    }
}