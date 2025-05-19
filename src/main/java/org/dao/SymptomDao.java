package org.dao;

import org.model.Symptom;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * SymptomDao arayüzünün implementasyonu.
 */
public class SymptomDao implements ISymptomDao {

    private final DatabaseConnectionManager connectionManager;

    public SymptomDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public Symptom findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM symptoms WHERE symptom_id = ?";
        Symptom symptom = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    symptom = mapResultSetToSymptom(rs);
                }
            }
        }

        return symptom;
    }

    @Override
    public List<Symptom> findAll() throws SQLException {
        String sql = "SELECT * FROM symptoms";
        List<Symptom> symptoms = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Symptom symptom = mapResultSetToSymptom(rs);
                symptoms.add(symptom);
            }
        }

        return symptoms;
    }

    @Override
    public boolean save(Symptom symptom) throws SQLException {
        if (symptom.getSymptom_id() == null) {
            // Insert
            String sql = "INSERT INTO symptoms (symptom_adi, aciklama) VALUES (?, ?) RETURNING symptom_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, symptom.getSymptom_adi());
                stmt.setString(2, symptom.getAciklama());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        symptom.setSymptom_id(rs.getInt(1));
                        return true;
                    }
                }
            }
        } else {
            // Update
            String sql = "UPDATE symptoms SET symptom_adi = ?, aciklama = ? WHERE symptom_id = ?";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, symptom.getSymptom_adi());
                stmt.setString(2, symptom.getAciklama());
                stmt.setInt(3, symptom.getSymptom_id());

                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM symptoms WHERE symptom_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public Symptom findByName(String name) throws SQLException {
        String sql = "SELECT * FROM symptoms WHERE symptom_adi = ?";
        Symptom symptom = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    symptom = mapResultSetToSymptom(rs);
                }
            }
        }

        return symptom;
    }

    @Override
    public List<Symptom> getPatientSymptoms(Integer patientId) throws SQLException {
        String sql = "SELECT s.* FROM symptoms s " +
                "JOIN patient_symptoms ps ON s.symptom_id = ps.symptom_id " +
                "WHERE ps.patient_id = ?";
        List<Symptom> symptoms = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Symptom symptom = mapResultSetToSymptom(rs);
                    symptoms.add(symptom);
                }
            }
        }

        return symptoms;
    }

    @Override
    public boolean addSymptomToPatient(Integer patientId, Integer symptomId) throws SQLException {
        String sql = "INSERT INTO patient_symptoms (patient_id, symptom_id, belirtilme_tarihi) VALUES (?, ?, ?)";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setInt(2, symptomId);
            stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public List<Symptom> getRuleSymptoms(Integer ruleId) throws SQLException {
        String sql = "SELECT s.* FROM symptoms s " +
                "JOIN rule_symptoms rs ON s.symptom_id = rs.symptom_id " +
                "WHERE rs.rule_id = ?";
        List<Symptom> symptoms = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ruleId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Symptom symptom = mapResultSetToSymptom(rs);
                    symptoms.add(symptom);
                }
            }
        }

        return symptoms;
    }

    // ResultSet'ten Symptom nesnesine dönüştürme yardımcı metodu
    private Symptom mapResultSetToSymptom(ResultSet rs) throws SQLException {
        Symptom symptom = new Symptom();
        symptom.setSymptom_id(rs.getInt("symptom_id"));
        symptom.setSymptom_adi(rs.getString("symptom_adi"));
        symptom.setAciklama(rs.getString("aciklama"));
        return symptom;
    }
}