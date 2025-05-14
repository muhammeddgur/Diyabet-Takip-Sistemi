package org.dao;

import org.model.BloodSugarMeasurement;
import org.model.Patient;
import org.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): 2025-05-14 15:44:55
 * Current User's Login: Emirhan-Karabulut
 *
 * Kan şekeri ölçümleri için veritabanı işlemlerini yöneten DAO sınıfı
 */
public class MeasurementDao {
    private PatientDao patientDao = new PatientDao();

    public BloodSugarMeasurement findById(Integer measurementId) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements WHERE measurement_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, measurementId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMeasurement(rs);
                }
            }
        }

        return null;
    }

    public List<BloodSugarMeasurement> findAll() throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements ORDER BY measurement_time DESC";
        List<BloodSugarMeasurement> measurements = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                measurements.add(mapResultSetToMeasurement(rs));
            }
        }

        return measurements;
    }

    public List<BloodSugarMeasurement> findByPatientId(Integer patientId) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements WHERE patient_id = ? ORDER BY measurement_time DESC";
        List<BloodSugarMeasurement> measurements = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    measurements.add(mapResultSetToMeasurement(rs));
                }
            }
        }

        return measurements;
    }

    /**
     * Belirli bir hastaya ait belirli bir tarihteki ölçümleri getirir
     */
    public List<BloodSugarMeasurement> findByPatientIdAndDate(Integer patientId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements WHERE patient_id = ? AND " +
                "DATE(measurement_time) = ? ORDER BY measurement_time";
        List<BloodSugarMeasurement> measurements = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    measurements.add(mapResultSetToMeasurement(rs));
                }
            }
        }

        return measurements;
    }

    /**
     * Belirli bir hastaya ait belirli bir tarih aralığındaki ölçümleri getirir
     */
    public List<BloodSugarMeasurement> findByPatientIdAndDateRange(Integer patientId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements WHERE patient_id = ? AND " +
                "DATE(measurement_time) BETWEEN ? AND ? ORDER BY measurement_time";
        List<BloodSugarMeasurement> measurements = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    measurements.add(mapResultSetToMeasurement(rs));
                }
            }
        }

        return measurements;
    }

    /**
     * Belirli bir hastaya ait belirli tipteki ölçümleri getirir
     */
    public List<BloodSugarMeasurement> findByPatientIdAndType(Integer patientId, String measurementType) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements WHERE patient_id = ? AND measurement_type = ? " +
                "ORDER BY measurement_time DESC";
        List<BloodSugarMeasurement> measurements = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setString(2, measurementType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    measurements.add(mapResultSetToMeasurement(rs));
                }
            }
        }

        return measurements;
    }

    /**
     * Belirli değer aralığındaki ölçümleri getirir
     */
    public List<BloodSugarMeasurement> findByValueRange(Integer patientId, BigDecimal minValue, BigDecimal maxValue) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_measurements WHERE patient_id = ? AND " +
                "measurement_value BETWEEN ? AND ? ORDER BY measurement_time DESC";
        List<BloodSugarMeasurement> measurements = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setBigDecimal(2, minValue);
            stmt.setBigDecimal(3, maxValue);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    measurements.add(mapResultSetToMeasurement(rs));
                }
            }
        }

        return measurements;
    }

    public Integer save(BloodSugarMeasurement measurement) throws SQLException {
        String sql = "INSERT INTO blood_sugar_measurements (patient_id, measurement_value, measurement_time, " +
                "measurement_type, is_valid_time, notes) VALUES (?, ?, ?, ?, ?, ?) RETURNING measurement_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, measurement.getPatientId());
            stmt.setBigDecimal(2, measurement.getMeasurementValue());
            stmt.setTimestamp(3, Timestamp.valueOf(measurement.getMeasurementTime()));
            stmt.setString(4, measurement.getMeasurementType());
            stmt.setBoolean(5, measurement.isValidTime());
            stmt.setString(6, measurement.getNotes());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return null;
    }

    public void update(BloodSugarMeasurement measurement) throws SQLException {
        String sql = "UPDATE blood_sugar_measurements SET measurement_value = ?, measurement_time = ?, " +
                "measurement_type = ?, is_valid_time = ?, notes = ? WHERE measurement_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, measurement.getMeasurementValue());
            stmt.setTimestamp(2, Timestamp.valueOf(measurement.getMeasurementTime()));
            stmt.setString(3, measurement.getMeasurementType());
            stmt.setBoolean(4, measurement.isValidTime());
            stmt.setString(5, measurement.getNotes());
            stmt.setInt(6, measurement.getMeasurementId());

            stmt.executeUpdate();
        }
    }

    public void delete(Integer measurementId) throws SQLException {
        String sql = "DELETE FROM blood_sugar_measurements WHERE measurement_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, measurementId);
            stmt.executeUpdate();
        }
    }

    /**
     * Belirli bir tarihte hastanın ölçüm sayısını döndürür
     */
    public int countMeasurementsByPatientAndDate(Integer patientId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM blood_sugar_measurements " +
                "WHERE patient_id = ? AND DATE(measurement_time) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }

        return 0;
    }

    /**
     * Ölçüm zamanının uygun saat aralığında olup olmadığını kontrol eder
     *
     * @param measurementType Ölçüm tipi (MORNING, NOON, AFTERNOON, EVENING, NIGHT)
     * @param time Ölçüm zamanı
     * @return Uygun aralıkta ise true
     */
    public boolean isValidTimeForMeasurementType(String measurementType, LocalTime time) {
        switch (measurementType) {
            case "MORNING":
                return time.isAfter(LocalTime.of(7, 0)) && time.isBefore(LocalTime.of(8, 0));
            case "NOON":
                return time.isAfter(LocalTime.of(12, 0)) && time.isBefore(LocalTime.of(13, 0));
            case "AFTERNOON":
                return time.isAfter(LocalTime.of(15, 0)) && time.isBefore(LocalTime.of(16, 0));
            case "EVENING":
                return time.isAfter(LocalTime.of(18, 0)) && time.isBefore(LocalTime.of(19, 0));
            case "NIGHT":
                return time.isAfter(LocalTime.of(22, 0)) && time.isBefore(LocalTime.of(23, 0));
            default:
                return false;
        }
    }

    private BloodSugarMeasurement mapResultSetToMeasurement(ResultSet rs) throws SQLException {
        BloodSugarMeasurement measurement = new BloodSugarMeasurement();
        measurement.setMeasurementId(rs.getInt("measurement_id"));
        measurement.setPatientId(rs.getInt("patient_id"));
        measurement.setMeasurementValue(rs.getBigDecimal("measurement_value"));
        measurement.setMeasurementTime(rs.getTimestamp("measurement_time").toLocalDateTime());
        measurement.setMeasurementType(rs.getString("measurement_type"));
        measurement.setValidTime(rs.getBoolean("is_valid_time"));
        measurement.setNotes(rs.getString("notes"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            measurement.setCreatedAt(createdAt.toLocalDateTime());
        }

        // Hasta bilgilerini yükleme
        try {
            Patient patient = patientDao.findById(measurement.getPatientId());
            measurement.setPatient(patient);
        } catch (SQLException e) {
            // Hata durumunda sadece ölçüm bilgilerini döndürmek için hatayı yut
        }

        return measurement;
    }
}