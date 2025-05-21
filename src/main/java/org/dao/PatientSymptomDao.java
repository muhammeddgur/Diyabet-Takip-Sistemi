package org.dao;

import org.model.Patient;
import org.model.PatientSymptom;
import org.model.Symptom;
import org.util.DateTimeUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Hasta belirtileri için DAO sınıfı
 */
public class PatientSymptomDao implements IPatientSymptomDao {

    private final DatabaseConnectionManager connectionManager;
    private final PatientDao patientDao;
    private final SymptomDao symptomDao;

    public PatientSymptomDao() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
        this.patientDao = new PatientDao();
        this.symptomDao = new SymptomDao();
    }

    @Override
    public PatientSymptom findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM patient_symptoms WHERE patient_symptom_id = ?";
        PatientSymptom patientSymptom = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    patientSymptom = mapResultSetToPatientSymptom(rs);
                }
            }
        }

        return patientSymptom;
    }

    @Override
    public boolean save(PatientSymptom patientSymptom) throws SQLException {
        if (patientSymptom.getPatient_symptom_id() == null) {
            // Insert
            String sql = "INSERT INTO patient_symptoms (patient_id, symptom_id, belirtilme_tarihi) " +
                    "VALUES (?, ?, ?) RETURNING patient_symptom_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, patientSymptom.getPatient_id());
                stmt.setInt(2, patientSymptom.getSymptom_id());
                stmt.setDate(3, java.sql.Date.valueOf(patientSymptom.getBelirtilme_tarihi()));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        patientSymptom.setPatient_symptom_id(rs.getInt(1));
                        return true;
                    }
                }
            }
        } else {
            // Update
            String sql = "UPDATE patient_symptoms SET patient_id = ?, symptom_id = ?, " +
                    "belirtilme_tarihi = ? WHERE patient_symptom_id = ?";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, patientSymptom.getPatient_id());
                stmt.setInt(2, patientSymptom.getSymptom_id());
                stmt.setDate(3, java.sql.Date.valueOf(patientSymptom.getBelirtilme_tarihi()));
                stmt.setInt(4, patientSymptom.getPatient_symptom_id());

                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM patient_symptoms WHERE patient_symptom_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public List<PatientSymptom> findByPatientId(Integer patientId) throws SQLException {
        String sql = "SELECT * FROM patient_symptoms WHERE patient_id = ? ORDER BY belirtilme_tarihi DESC";
        List<PatientSymptom> patientSymptoms = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PatientSymptom patientSymptom = mapResultSetToPatientSymptom(rs);
                    patientSymptoms.add(patientSymptom);
                }
            }
        }

        return patientSymptoms;
    }

    @Override
    public List<PatientSymptom> findByPatientIdAndDate(Integer patientId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM patient_symptoms WHERE patient_id = ? AND belirtilme_tarihi = ? ORDER BY belirtilme_tarihi DESC";
        List<PatientSymptom> patientSymptoms = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setDate(2, java.sql.Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PatientSymptom patientSymptom = mapResultSetToPatientSymptom(rs);
                    patientSymptoms.add(patientSymptom);
                }
            }
        }

        return patientSymptoms;
    }

    @Override
    public List<PatientSymptom> findByDateRange(Integer patientId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM patient_symptoms WHERE patient_id = ? AND belirtilme_tarihi BETWEEN ? AND ? ORDER BY belirtilme_tarihi DESC";
        List<PatientSymptom> patientSymptoms = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setDate(2, java.sql.Date.valueOf(startDate));
            stmt.setDate(3, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PatientSymptom patientSymptom = mapResultSetToPatientSymptom(rs);
                    patientSymptoms.add(patientSymptom);
                }
            }
        }

        return patientSymptoms;
    }

    // ResultSet'ten PatientSymptom nesnesine dönüştürme yardımcı metodu
    private PatientSymptom mapResultSetToPatientSymptom(ResultSet rs) throws SQLException {
        PatientSymptom patientSymptom = new PatientSymptom();
        patientSymptom.setPatient_symptom_id(rs.getInt("patient_symptom_id"));
        patientSymptom.setPatient_id(rs.getInt("patient_id"));
        patientSymptom.setSymptom_id(rs.getInt("symptom_id"));

        Date belirtilmeTarihi = rs.getDate("belirtilme_tarihi");
        if (belirtilmeTarihi != null) {
            patientSymptom.setBelirtilme_tarihi(belirtilmeTarihi.toLocalDate());
        } else {
            patientSymptom.setBelirtilme_tarihi(DateTimeUtil.getCurrentDate()); // Null ise güncel tarih
        }

        // Lazy loading için gerekirse Patient ve Symptom nesnelerini yükle
        try {
            Patient patient = patientDao.findById(patientSymptom.getPatient_id());
            patientSymptom.setPatient(patient);

            Symptom symptom = symptomDao.findById(patientSymptom.getSymptom_id());
            patientSymptom.setSymptom(symptom);
        } catch (SQLException e) {
            System.err.println("Patient veya Symptom yüklenirken hata: " + e.getMessage());
        }

        return patientSymptom;
    }
}