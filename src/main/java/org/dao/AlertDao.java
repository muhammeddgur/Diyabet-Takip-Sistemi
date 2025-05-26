package org.dao;

import org.model.Alert;
import org.model.AlertType;
import org.model.Doctor;
import org.model.Patient;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AlertDao arayüzünün implementasyonu.
 */
public class AlertDao implements IAlertDao {

    private final DatabaseConnectionManager connectionManager;
    private final PatientDao patientDao;
    private final DoctorDao doctorDao;
    private final AlertTypeDao alertTypeDao;

    public AlertDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
        patientDao = new PatientDao();
        doctorDao = new DoctorDao();
        alertTypeDao = new AlertTypeDao();
    }

    @Override
    public Alert findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM alerts WHERE alert_id = ?";
        Alert alert = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    alert = mapResultSetToAlert(rs);
                }
            }
        }

        return alert;
    }

    @Override
    public List<Alert> findAll() throws SQLException {
        String sql = "SELECT * FROM alerts ORDER BY olusturma_zamani DESC";
        List<Alert> alerts = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Alert alert = mapResultSetToAlert(rs);
                if (alert != null) {
                    alerts.add(alert);
                }
            }
        }

        return alerts;
    }

    @Override
    public boolean save(Alert alert) throws SQLException {
        if (alert.getAlert_id() == null) {
            // Insert
            String sql = "INSERT INTO alerts (patient_id, doctor_id, alert_type_id, mesaj, olusturma_zamani) " +
                    "VALUES (?, ?, ?, ?, ?) RETURNING alert_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, alert.getPatient().getPatient_id());
                stmt.setInt(2, alert.getDoctor().getDoctor_id());
                stmt.setInt(3, alert.getAlertType().getAlert_type_id());
                stmt.setString(4, alert.getMesaj());
                stmt.setTimestamp(5, Timestamp.valueOf(alert.getOlusturma_zamani()));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        alert.setAlert_id(rs.getInt(1));
                        return true;
                    }
                }
            }
        } else {
            // Update
            String sql = "UPDATE alerts SET patient_id = ?, doctor_id = ?, alert_type_id = ?, mesaj = ? WHERE alert_id = ?";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, alert.getPatient().getPatient_id());
                stmt.setInt(2, alert.getDoctor().getDoctor_id());
                stmt.setInt(3, alert.getAlertType().getAlert_type_id());
                stmt.setString(4, alert.getMesaj());

                stmt.setInt(5, alert.getAlert_id());

                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM alerts WHERE alert_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public List<Alert> findByPatientId(Integer patientId) throws SQLException {
        String sql = "SELECT * FROM alerts WHERE patient_id = ? ORDER BY olusturma_zamani DESC";
        List<Alert> alerts = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Alert alert = mapResultSetToAlert(rs);
                    if (alert != null) {
                        alerts.add(alert);
                    }
                }
            }
        }

        return alerts;
    }

    @Override
    public List<Alert> findByDoctorId(Integer doctorId) throws SQLException {
        String sql = "SELECT * FROM alerts WHERE doctor_id = ? ORDER BY olusturma_zamani DESC";
        List<Alert> alerts = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Alert alert = mapResultSetToAlert(rs);
                    if (alert != null) {
                        alerts.add(alert);
                    }
                }
            }
        }

        return alerts;
    }

    @Override
    public List<Alert> findUnreadAlerts(Integer doctorId) throws SQLException {
        String sql = "SELECT * FROM alerts WHERE doctor_id = ? AND okundu_mu = false ORDER BY olusturma_zamani DESC";
        List<Alert> alerts = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, doctorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Alert alert = mapResultSetToAlert(rs);
                    if (alert != null) {
                        alerts.add(alert);
                    }
                }
            }
        }

        return alerts;
    }

    @Override
    public boolean markAsRead(Integer alertId) throws SQLException {
        String sql = "UPDATE alerts SET okundu_mu = true, okunma_zamani = ? WHERE alert_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, alertId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // ResultSet'ten Alert nesnesine dönüştürme yardımcı metodu
    private Alert mapResultSetToAlert(ResultSet rs) throws SQLException {
        Alert alert = new Alert();
        alert.setAlert_id(rs.getInt("alert_id"));
        alert.setMesaj(rs.getString("mesaj"));

        Timestamp createdAt = rs.getTimestamp("olusturma_zamani");
        if (createdAt != null) {
            alert.setOlusturma_zamani(createdAt.toLocalDateTime());
        }

        // Hasta bilgisini ekle
        int patientId = rs.getInt("patient_id");
        Patient patient = patientDao.findById(patientId);
        if (patient != null) {
            alert.setPatient(patient);
        }

        // Doktor bilgisini ekle
        int doctorId = rs.getInt("doctor_id");
        Doctor doctor = doctorDao.findById(doctorId);
        if (doctor != null) {
            alert.setDoctor(doctor);
        }

        // Uyarı tipi bilgisini ekle
        int alertTypeId = rs.getInt("alert_type_id");
        AlertType alertType = alertTypeDao.findById(alertTypeId);
        if (alertType != null) {
            alert.setAlertType(alertType);
        }

        return alert;
    }
}