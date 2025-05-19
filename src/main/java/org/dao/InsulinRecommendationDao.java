package org.dao;

import org.model.InsulinRecommendation;
import org.model.Patient;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * InsulinRecommendationDao arayüzünün implementasyonu.
 */
public class InsulinRecommendationDao implements IInsulinRecommendationDao {

    private final DatabaseConnectionManager connectionManager;
    private final PatientDao patientDao;

    public InsulinRecommendationDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
        patientDao = new PatientDao();
    }

    @Override
    public InsulinRecommendation findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM insulin_recommendations WHERE recommendation_id = ?";
        InsulinRecommendation recommendation = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    recommendation = mapResultSetToRecommendation(rs);
                }
            }
        }

        return recommendation;
    }

    @Override
    public List<InsulinRecommendation> findAll() throws SQLException {
        String sql = "SELECT * FROM insulin_recommendations ORDER BY created_at DESC";
        List<InsulinRecommendation> recommendations = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                InsulinRecommendation recommendation = mapResultSetToRecommendation(rs);
                if (recommendation != null) {
                    recommendations.add(recommendation);
                }
            }
        }

        return recommendations;
    }

    @Override
    public boolean save(InsulinRecommendation recommendation) throws SQLException {
        if (recommendation.getRecommendation_id() == null) {
            // Insert
            String sql = "INSERT INTO insulin_recommendations (patient_id, recommendation_date, average_value, measured_count, " +
                    "reference_id, applied, created_at) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING recommendation_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, recommendation.getPatient().getPatient_id());
                stmt.setDate(2, java.sql.Date.valueOf(recommendation.getRecommendation_date()));
                stmt.setDouble(3, recommendation.getAverageValue());
                stmt.setInt(4, recommendation.getMeasuredCount());

                // reference_id belirlenmeli, burada sadece sabit bir değer ekliyoruz.
                // Gerçek uygulamada kan şekeri değerine göre insulin_reference tablosundan uygun referansı seçmelisiniz.
                stmt.setInt(5, 1);

                stmt.setBoolean(6, recommendation.getApplied());
                stmt.setTimestamp(7, Timestamp.valueOf(recommendation.getCreated_at()));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        recommendation.setRecommendation_id(rs.getInt(1));
                        return true;
                    }
                }
            }
        } else {
            // Update
            String sql = "UPDATE insulin_recommendations SET patient_id = ?, recommendation_date = ?, average_value = ?, " +
                    "measured_count = ?, reference_id = ?, applied = ? WHERE recommendation_id = ?";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, recommendation.getPatient().getPatient_id());
                stmt.setDate(2, java.sql.Date.valueOf(recommendation.getRecommendation_date()));
                stmt.setDouble(3, recommendation.getAverageValue());
                stmt.setInt(4, recommendation.getMeasuredCount());

                // reference_id belirlenmeli
                stmt.setInt(5, 1);

                stmt.setBoolean(6, recommendation.getApplied());
                stmt.setInt(7, recommendation.getRecommendation_id());

                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM insulin_recommendations WHERE recommendation_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public List<InsulinRecommendation> findByPatientId(Integer patientId) throws SQLException {
        String sql = "SELECT * FROM insulin_recommendations WHERE patient_id = ? ORDER BY recommendation_date DESC";
        List<InsulinRecommendation> recommendations = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InsulinRecommendation recommendation = mapResultSetToRecommendation(rs);
                    if (recommendation != null) {
                        recommendations.add(recommendation);
                    }
                }
            }
        }

        return recommendations;
    }

    @Override
    public InsulinRecommendation findByDate(Integer patientId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM insulin_recommendations WHERE patient_id = ? AND recommendation_date = ?";
        InsulinRecommendation recommendation = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setDate(2, java.sql.Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    recommendation = mapResultSetToRecommendation(rs);
                }
            }
        }

        return recommendation;
    }

    @Override
    public List<InsulinRecommendation> findByDateRange(Integer patientId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM insulin_recommendations WHERE patient_id = ? AND recommendation_date >= ? AND recommendation_date <= ? " +
                "ORDER BY recommendation_date";
        List<InsulinRecommendation> recommendations = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setDate(2, java.sql.Date.valueOf(startDate));
            stmt.setDate(3, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InsulinRecommendation recommendation = mapResultSetToRecommendation(rs);
                    if (recommendation != null) {
                        recommendations.add(recommendation);
                    }
                }
            }
        }

        return recommendations;
    }

    @Override
    public boolean markAsApplied(Integer recommendationId) throws SQLException {
        String sql = "UPDATE insulin_recommendations SET applied = true WHERE recommendation_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, recommendationId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // ResultSet'ten InsulinRecommendation nesnesine dönüştürme yardımcı metodu
    private InsulinRecommendation mapResultSetToRecommendation(ResultSet rs) throws SQLException {
        InsulinRecommendation recommendation = new InsulinRecommendation();
        recommendation.setRecommendation_id(rs.getInt("recommendation_id"));
        recommendation.setRecommendation_date(rs.getDate("recommendation_date").toLocalDate());
        recommendation.setAverageValue(rs.getDouble("average_value"));
        recommendation.setMeasuredCount(rs.getInt("measured_count"));
        recommendation.setApplied(rs.getBoolean("applied"));

        // Önerilen insülin miktarını reference_id üzerinden belirlenmelidir
        // Gerçek uygulamada insulin_reference tablosundan çekilmelidir
        recommendation.setRecommendedInsulin(2.5); // Örnek değer

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            recommendation.setCreated_at(createdAt.toLocalDateTime());
        }

        // Hasta bilgisini ekle
        int patientId = rs.getInt("patient_id");
        Patient patient = patientDao.findById(patientId);
        if (patient != null) {
            recommendation.setPatient(patient);
        }

        return recommendation;
    }
}