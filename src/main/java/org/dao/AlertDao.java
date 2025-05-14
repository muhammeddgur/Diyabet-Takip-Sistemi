package org.dao;

import org.model.Alert;
import org.model.Patient;
import org.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlertDao {
    private PatientDao patientDao = new PatientDao();

    public Alert findById(Integer alertId) throws SQLException {
        String sql = "SELECT * FROM alerts WHERE alert_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, alertId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAlert(rs);
                }
            }
        }

        return null;
    }

    public List<Alert> findAll() throws SQLException {
        String sql = "SELECT * FROM alerts ORDER BY created_at DESC";
        List<Alert> alerts = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                alerts.add(mapResultSetToAlert(rs));
            }
        }

        return alerts;
    }

    public List<Alert> findByPatientId(Integer patientId) throws SQLException {
        String sql = "SELECT * FROM alerts WHERE patient_id = ? ORDER BY created_at DESC";
        List<Alert> alerts = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(mapResultSetToAlert(rs));
                }
            }
        }

        return alerts;
    }

    public List<Alert> findByPatientIdAndDate(Integer patientId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM alerts WHERE patient_id = ? AND DATE(created_at) = ? " +
                "ORDER BY created_at DESC";
        List<Alert> alerts = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(mapResultSetToAlert(rs));
                }
            }
        }

        return alerts;
    }

    public List<Alert> findUnreadByPatientId(Integer patientId) throws SQLException {
        String sql = "SELECT * FROM alerts WHERE patient_id = ? AND is_read = false " +
                "ORDER BY created_at DESC";
        List<Alert> alerts = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(mapResultSetToAlert(rs));
                }
            }
        }

        return alerts;
    }

    public List<Alert> findUrgentByPatientId(Integer patientId) throws SQLException {
        String sql = "SELECT * FROM alerts WHERE patient_id = ? AND is_urgent = true " +
                "ORDER BY created_at DESC";
        List<Alert> alerts = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(mapResultSetToAlert(rs));
                }
            }
        }

        return alerts;
    }

    public List<Alert> findByAlertType(String alertType) throws SQLException {
        String sql = "SELECT * FROM alerts WHERE alert_type = ? ORDER BY created_at DESC";
        List<Alert> alerts = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, alertType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(mapResultSetToAlert(rs));
                }
            }
        }

        return alerts;
    }

    public Integer save(Alert alert) throws SQLException {
        String sql = "INSERT INTO alerts (patient_id, alert_type, alert_message, is_read, is_urgent) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING alert_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, alert.getPatientId());
            stmt.setString(2, alert.getAlertType());
            stmt.setString(3, alert.getAlertMessage());
            stmt.setBoolean(4, alert.isRead());
            stmt.setBoolean(5, alert.isUrgent());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return null;
    }

    public void update(Alert alert) throws SQLException {
        String sql = "UPDATE alerts SET alert_type = ?, alert_message = ?, is_read = ?, " +
                "is_urgent = ? WHERE alert_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, alert.getAlertType());
            stmt.setString(2, alert.getAlertMessage());
            stmt.setBoolean(3, alert.isRead());
            stmt.setBoolean(4, alert.isUrgent());
            stmt.setInt(5, alert.getAlertId());

            stmt.executeUpdate();
        }
    }

    public void delete(Integer alertId) throws SQLException {
        String sql = "DELETE FROM alerts WHERE alert_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, alertId);
            stmt.executeUpdate();
        }
    }

    public void markAsRead(Integer alertId) throws SQLException {
        String sql = "UPDATE alerts SET is_read = true WHERE alert_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, alertId);
            stmt.executeUpdate();
        }
    }

    public void markAllAsRead(Integer patientId) throws SQLException {
        String sql = "UPDATE alerts SET is_read = true WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.executeUpdate();
        }
    }

    public void createMissingMeasurementAlert(Integer patientId) throws SQLException {
        Alert alert = new Alert();
        alert.setPatientId(patientId);
        alert.setAlertType("MISSING_MEASUREMENT");
        alert.setAlertMessage("Hasta gün boyunca kan şekeri ölçümü yapmamıştır. Acil takip önerilir.");
        alert.setRead(false);
        alert.setUrgent(true);

        save(alert);
    }

    public void createInsufficientMeasurementAlert(Integer patientId, int count) throws SQLException {
        Alert alert = new Alert();
        alert.setPatientId(patientId);
        alert.setAlertType("INSUFFICIENT_MEASUREMENTS");
        alert.setAlertMessage("Hastanın günlük kan şekeri ölçüm sayısı yetersiz (<3). Durum izlenmelidir. Mevcut ölçüm sayısı: " + count);
        alert.setRead(false);
        alert.setUrgent(false);

        save(alert);
    }

    public void createCriticalValueAlert(Integer patientId, double value) throws SQLException {
        Alert alert = new Alert();
        alert.setPatientId(patientId);

        if (value < 70) {
            alert.setAlertType("HYPOGLYCEMIA");
            alert.setAlertMessage("Hastanın kan şekeri seviyesi 70 mg/dL'nin altına düştü (" + value +
                    " mg/dL). Hipoglisemi riski! Hızlı müdahale gerekebilir.");
            alert.setUrgent(true);
        } else if (value > 200) {
            alert.setAlertType("HYPERGLYCEMIA");
            alert.setAlertMessage("Hastanın kan şekeri 200 mg/dL'nin üzerinde (" + value +
                    " mg/dL). Hiperglisemi durumu. Acil müdahale gerekebilir.");
            alert.setUrgent(true);
        } else if (value > 150) {
            alert.setAlertType("HIGH_GLUCOSE");
            alert.setAlertMessage("Hastanın kan şekeri 151-200 mg/dL arasında (" + value +
                    " mg/dL). Diyabet kontrolü gereklidir.");
            alert.setUrgent(false);
        }

        alert.setRead(false);
        save(alert);
    }

    private Alert mapResultSetToAlert(ResultSet rs) throws SQLException {
        Alert alert = new Alert();
        alert.setAlertId(rs.getInt("alert_id"));
        alert.setPatientId(rs.getInt("patient_id"));
        alert.setAlertType(rs.getString("alert_type"));
        alert.setAlertMessage(rs.getString("alert_message"));
        alert.setRead(rs.getBoolean("is_read"));
        alert.setUrgent(rs.getBoolean("is_urgent"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            alert.setCreatedAt(createdAt.toLocalDateTime());
        }

        // Hasta bilgilerini yükleme
        try {
            Patient patient = patientDao.findById(alert.getPatientId());
            alert.setPatient(patient);
        } catch (SQLException e) {
            // Hata durumunda sadece uyarı bilgilerini döndürmek için hatayı yut
        }

        return alert;
    }
}