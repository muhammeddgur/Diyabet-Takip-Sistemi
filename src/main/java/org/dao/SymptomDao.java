package org.dao;

import org.model.PatientSymptom;
import org.model.Symptom;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymptomDao {

    private final DatabaseConnectionManager connectionManager;

    public SymptomDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
    }

    /**
     * Tüm belirti türlerini getirir
     *
     * @return Belirti listesi
     * @throws SQLException Veritabanı hatası durumunda
     */
    public List<Symptom> findAll() throws SQLException {
        String sql = "SELECT * FROM symptoms ORDER BY symptom_adi";
        List<Symptom> symptoms = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Symptom symptom = new Symptom();
                symptom.setSymptom_id(rs.getInt("symptom_id"));
                symptom.setSymptom_adi(rs.getString("symptom_adi"));
                symptom.setAciklama(rs.getString("aciklama"));
                symptoms.add(symptom);
            }
        }

        return symptoms;
    }

    /**
     * ID'ye göre belirti getirir
     *
     * @param id Belirti ID'si
     * @return Belirti veya null
     * @throws SQLException Veritabanı hatası durumunda
     */
    public Symptom findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM symptoms WHERE symptom_id = ?";
        Symptom symptom = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    symptom = new Symptom();
                    symptom.setSymptom_id(rs.getInt("symptom_id"));
                    symptom.setSymptom_adi(rs.getString("symptom_adi"));
                    symptom.setAciklama(rs.getString("aciklama"));
                }
            }
        }

        return symptom;
    }

    /**
     * İsme göre belirti getirir
     *
     * @param name Belirti adı
     * @return Belirti veya null
     * @throws SQLException Veritabanı hatası durumunda
     */
    public Symptom findByName(String name) throws SQLException {
        String sql = "SELECT * FROM symptoms WHERE symptom_adi = ?";
        Symptom symptom = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    symptom = new Symptom();
                    symptom.setSymptom_id(rs.getInt("symptom_id"));
                    symptom.setSymptom_adi(rs.getString("symptom_adi"));
                    symptom.setAciklama(rs.getString("aciklama"));
                }
            }
        }

        return symptom;
    }

    /**
     * Hasta ID'sine göre hasta belirtilerini getirir
     *
     * @param patientId Hasta ID'si
     * @return Hasta belirtileri listesi
     * @throws SQLException Veritabanı hatası durumunda
     */
    public List<PatientSymptom> getPatientSymptomsByPatientId(Integer patientId) throws SQLException {
        String sql = "SELECT * FROM patient_symptoms WHERE patient_id = ? ORDER BY belirtilme_tarihi DESC";
        List<PatientSymptom> patientSymptoms = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PatientSymptom patientSymptom = new PatientSymptom();
                    patientSymptom.setPatient_symptom_id(rs.getInt("patient_symptom_id"));
                    patientSymptom.setPatient_id(rs.getInt("patient_id"));
                    patientSymptom.setSymptom_id(rs.getInt("symptom_id"));

                    // LocalDate olarak tarih al
                    Date belirtilmeTarihi = rs.getDate("belirtilme_tarihi");
                    if (belirtilmeTarihi != null) {
                        patientSymptom.setBelirtilme_tarihi(belirtilmeTarihi.toLocalDate());
                    }

                    patientSymptoms.add(patientSymptom);
                }
            }
        }

        return patientSymptoms;
    }

    /**
     * Hasta ID ve belirti ID'lerine göre hasta belirtilerini getirir
     *
     * @param patientId Hasta ID
     * @param symptomIds Belirti ID listesi
     * @return Hasta belirtileri listesi
     * @throws SQLException Veritabanı hatası durumunda
     */
    public List<PatientSymptom> getPatientSymptomsBySymptomIds(Integer patientId, List<Integer> symptomIds) throws SQLException {
        if (symptomIds == null || symptomIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Parametre sayısına göre ? işaretleri hazırlanıyor
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < symptomIds.size(); i++) {
            placeholders.append(i > 0 ? ",?" : "?");
        }

        String sql = "SELECT * FROM patient_symptoms WHERE patient_id = ? AND symptom_id IN (" +
                placeholders.toString() + ") ORDER BY belirtilme_tarihi DESC";

        List<PatientSymptom> patientSymptoms = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            // Belirti ID'lerini parametre olarak ekle
            for (int i = 0; i < symptomIds.size(); i++) {
                stmt.setInt(i + 2, symptomIds.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PatientSymptom patientSymptom = new PatientSymptom();
                    patientSymptom.setPatient_symptom_id(rs.getInt("patient_symptom_id"));
                    patientSymptom.setPatient_id(rs.getInt("patient_id"));
                    patientSymptom.setSymptom_id(rs.getInt("symptom_id"));

                    // LocalDate olarak tarih al
                    Date belirtilmeTarihi = rs.getDate("belirtilme_tarihi");
                    if (belirtilmeTarihi != null) {
                        patientSymptom.setBelirtilme_tarihi(belirtilmeTarihi.toLocalDate());
                    }

                    patientSymptoms.add(patientSymptom);
                }
            }
        }

        return patientSymptoms;
    }

    /**
     * Hasta ID'sine göre detaylı belirti bilgilerini getirir
     * (Semptom adı, açıklama gibi bilgileri de içeren liste)
     *
     * @param patientId Hasta ID'si
     * @return Detaylı belirti bilgileri listesi
     * @throws SQLException Veritabanı hatası durumunda
     */
    public List<Map<String, Object>> getPatientSymptomDetails(Integer patientId) throws SQLException {
        String sql = "SELECT ps.patient_symptom_id, ps.patient_id, ps.symptom_id, ps.belirtilme_tarihi, " +
                "s.symptom_adi, s.aciklama " +
                "FROM patient_symptoms ps " +
                "JOIN symptoms s ON ps.symptom_id = s.symptom_id " +
                "WHERE ps.patient_id = ? " +
                "ORDER BY ps.belirtilme_tarihi DESC";

        List<Map<String, Object>> symptomDetails = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("patientSymptomId", rs.getInt("patient_symptom_id"));
                    detail.put("patientId", rs.getInt("patient_id"));
                    detail.put("symptomId", rs.getInt("symptom_id"));

                    // LocalDate olarak tarih al
                    Date belirtilmeTarihi = rs.getDate("belirtilme_tarihi");
                    if (belirtilmeTarihi != null) {
                        detail.put("reportDate", belirtilmeTarihi.toLocalDate());
                    }

                    detail.put("symptomName", rs.getString("symptom_adi"));
                    detail.put("description", rs.getString("aciklama"));

                    symptomDetails.add(detail);
                }
            }
        }

        return symptomDetails;
    }

    /**
     * Belirti ekler veya günceller
     *
     * @param symptom Belirti nesnesi
     * @return İşlem başarılı ise true
     * @throws SQLException Veritabanı hatası durumunda
     */
    public boolean save(Symptom symptom) throws SQLException {
        if (symptom.getSymptom_id() == null) {
            // Yeni ekle
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
            // Güncelle
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

    /**
     * Hasta belirtisi ekler
     *
     * @param patientSymptom Hasta belirti nesnesi
     * @return İşlem başarılı ise true
     * @throws SQLException Veritabanı hatası durumunda
     */
    public boolean savePatientSymptom(PatientSymptom patientSymptom) throws SQLException {
        if (patientSymptom.getPatient_symptom_id() == null) {
            // Yeni ekle
            String sql = "INSERT INTO patient_symptoms (patient_id, symptom_id, belirtilme_tarihi) " +
                    "VALUES (?, ?, ?) RETURNING patient_symptom_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, patientSymptom.getPatient_id());
                stmt.setInt(2, patientSymptom.getSymptom_id());
                stmt.setDate(3, Date.valueOf(patientSymptom.getBelirtilme_tarihi()));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        patientSymptom.setPatient_symptom_id(rs.getInt(1));
                        return true;
                    }
                }
            }
        } else {
            // Güncelle
            String sql = "UPDATE patient_symptoms SET patient_id = ?, symptom_id = ?, belirtilme_tarihi = ? " +
                    "WHERE patient_symptom_id = ?";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, patientSymptom.getPatient_id());
                stmt.setInt(2, patientSymptom.getSymptom_id());
                stmt.setDate(3, Date.valueOf(patientSymptom.getBelirtilme_tarihi()));
                stmt.setInt(4, patientSymptom.getPatient_symptom_id());

                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        }

        return false;
    }

    /**
     * Belirli bir kuralın belirtilerini getirir
     *
     * @param ruleId Kural ID'si
     * @return Belirtiler listesi
     * @throws SQLException Veritabanı hatası durumunda
     */
    public List<Symptom> getRuleSymptoms(Integer ruleId) throws SQLException {
        String sql = "SELECT s.* FROM symptoms s " +
                "JOIN rule_symptoms rs ON s.symptom_id = rs.symptom_id " +
                "WHERE rs.rule_id = ? " +
                "ORDER BY s.symptom_adi";

        List<Symptom> symptoms = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ruleId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Symptom symptom = new Symptom();
                    symptom.setSymptom_id(rs.getInt("symptom_id"));
                    symptom.setSymptom_adi(rs.getString("symptom_adi"));
                    symptom.setAciklama(rs.getString("aciklama"));
                    symptoms.add(symptom);
                }
            }
        }

        return symptoms;
    }

    /**
     * Hasta belirtisi siler
     *
     * @param patientSymptomId Hasta belirti ID'si
     * @return İşlem başarılı ise true
     * @throws SQLException Veritabanı hatası durumunda
     */
    public boolean deletePatientSymptom(Integer patientSymptomId) throws SQLException {
        String sql = "DELETE FROM patient_symptoms WHERE patient_symptom_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientSymptomId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}