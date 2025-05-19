package org.dao;

import org.model.Diet;
import org.model.Exercise;
import org.model.RecommendationRule;
import org.model.Symptom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * RecommendationRuleDao arayüzünün implementasyonu.
 */
public class RecommendationRuleDao implements IRecommendationRuleDao {

    private final DatabaseConnectionManager connectionManager;
    private final DietDao dietDao;
    private final ExerciseDao exerciseDao;
    private final SymptomDao symptomDao;

    public RecommendationRuleDao() {
        connectionManager = DatabaseConnectionManager.getInstance();
        dietDao = new DietDao();
        exerciseDao = new ExerciseDao();
        symptomDao = new SymptomDao();
    }

    @Override
    public RecommendationRule findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM recommendation_rules WHERE rule_id = ?";
        RecommendationRule rule = null;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    rule = mapResultSetToRule(rs);

                    // Belirtileri ekle
                    List<Symptom> symptoms = symptomDao.getRuleSymptoms(id);
                    rule.setSymptoms(symptoms);
                }
            }
        }

        return rule;
    }

    @Override
    public List<RecommendationRule> findAll() throws SQLException {
        String sql = "SELECT * FROM recommendation_rules";
        List<RecommendationRule> rules = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                RecommendationRule rule = mapResultSetToRule(rs);

                // Belirtileri ekle
                List<Symptom> symptoms = symptomDao.getRuleSymptoms(rule.getRule_id());
                rule.setSymptoms(symptoms);

                rules.add(rule);
            }
        }

        return rules;
    }

    @Override
    public boolean save(RecommendationRule rule) throws SQLException {
        if (rule.getRule_id() == null) {
            // Insert
            String sql = "INSERT INTO recommendation_rules (min_blood_sugar, max_blood_sugar, recommended_diet_id, recommended_exercise_id) " +
                    "VALUES (?, ?, ?, ?) RETURNING rule_id";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, rule.getMinBloodSugar());
                stmt.setInt(2, rule.getMaxBloodSugar());
                stmt.setInt(3, rule.getRecommendedDiet().getDiet_id());
                stmt.setInt(4, rule.getRecommendedExercise().getExercise_id());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        rule.setRule_id(rs.getInt(1));

                        // Belirtileri ekle
                        for (Symptom symptom : rule.getSymptoms()) {
                            addSymptomToRule(rule.getRule_id(), symptom.getSymptom_id());
                        }

                        return true;
                    }
                }
            }
        } else {
            // Update
            String sql = "UPDATE recommendation_rules SET min_blood_sugar = ?, max_blood_sugar = ?, " +
                    "recommended_diet_id = ?, recommended_exercise_id = ? WHERE rule_id = ?";

            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, rule.getMinBloodSugar());
                stmt.setInt(2, rule.getMaxBloodSugar());
                stmt.setInt(3, rule.getRecommendedDiet().getDiet_id());
                stmt.setInt(4, rule.getRecommendedExercise().getExercise_id());
                stmt.setInt(5, rule.getRule_id());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    // Mevcut belirti ilişkilerini sil
                    deleteRuleSymptoms(rule.getRule_id());

                    // Belirtileri yeniden ekle
                    for (Symptom symptom : rule.getSymptoms()) {
                        addSymptomToRule(rule.getRule_id(), symptom.getSymptom_id());
                    }

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        // Önce rule_symptoms tablosundan ilişkili kayıtları sil
        deleteRuleSymptoms(id);

        // Sonra kuralı sil
        String sql = "DELETE FROM recommendation_rules WHERE rule_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public List<RecommendationRule> findByBloodSugarLevel(int level) throws SQLException {
        String sql = "SELECT * FROM recommendation_rules WHERE min_blood_sugar <= ? AND max_blood_sugar >= ?";
        List<RecommendationRule> rules = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, level);
            stmt.setInt(2, level);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RecommendationRule rule = mapResultSetToRule(rs);

                    // Belirtileri ekle
                    List<Symptom> symptoms = symptomDao.getRuleSymptoms(rule.getRule_id());
                    rule.setSymptoms(symptoms);

                    rules.add(rule);
                }
            }
        }

        return rules;
    }

    @Override
    public List<RecommendationRule> findBySymptoms(List<Integer> symptomIds) throws SQLException {
        if (symptomIds.isEmpty()) {
            return new ArrayList<>();
        }

        // SQL sorgusu için symptom_id değerlerini birleştir
        StringJoiner idJoiner = new StringJoiner(",");
        for (Integer id : symptomIds) {
            idJoiner.add(id.toString());
        }

        String sql = "SELECT r.*, COUNT(rs.symptom_id) as matching_symptoms FROM recommendation_rules r " +
                "JOIN rule_symptoms rs ON r.rule_id = rs.rule_id " +
                "WHERE rs.symptom_id IN (" + idJoiner.toString() + ") " +
                "GROUP BY r.rule_id " +
                "HAVING COUNT(rs.symptom_id) > 0 " +
                "ORDER BY matching_symptoms DESC";

        List<RecommendationRule> rules = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                RecommendationRule rule = mapResultSetToRule(rs);

                // Belirtileri ekle
                List<Symptom> symptoms = symptomDao.getRuleSymptoms(rule.getRule_id());
                rule.setSymptoms(symptoms);

                rules.add(rule);
            }
        }

        return rules;
    }

    @Override
    public boolean addSymptomToRule(Integer ruleId, Integer symptomId) throws SQLException {
        String sql = "INSERT INTO rule_symptoms (rule_id, symptom_id) VALUES (?, ?)";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ruleId);
            stmt.setInt(2, symptomId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Kural ile ilişkili tüm belirti kayıtlarını silme yardımcı metodu
    private boolean deleteRuleSymptoms(Integer ruleId) throws SQLException {
        String sql = "DELETE FROM rule_symptoms WHERE rule_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ruleId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // ResultSet'ten RecommendationRule nesnesine dönüştürme yardımcı metodu
    private RecommendationRule mapResultSetToRule(ResultSet rs) throws SQLException {
        RecommendationRule rule = new RecommendationRule();
        rule.setRule_id(rs.getInt("rule_id"));
        rule.setMinBloodSugar(rs.getInt("min_blood_sugar"));
        rule.setMaxBloodSugar(rs.getInt("max_blood_sugar"));

        // Önerilen diyet
        int dietId = rs.getInt("recommended_diet_id");
        Diet diet = dietDao.findById(dietId);
        rule.setRecommendedDiet(diet);

        // Önerilen egzersiz
        int exerciseId = rs.getInt("recommended_exercise_id");
        Exercise exercise = exerciseDao.findById(exerciseId);
        rule.setRecommendedExercise(exercise);

        return rule;
    }
}