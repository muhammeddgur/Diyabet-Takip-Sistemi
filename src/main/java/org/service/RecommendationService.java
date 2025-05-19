package org.service;

import org.dao.RecommendationRuleDao;
import org.dao.IRecommendationRuleDao;
import org.model.Diet;
import org.model.Exercise;
import org.model.Patient;
import org.model.RecommendationRule;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Diyet/egzersiz önerileri için servis sınıfı.
 */
public class RecommendationService {

    private final IRecommendationRuleDao ruleDao;
    private final MeasurementService measurementService;
    private final PatientService patientService;

    public RecommendationService() {
        this.ruleDao = new RecommendationRuleDao();
        this.measurementService = new MeasurementService();
        this.patientService = new PatientService();
    }

    /**
     * Kan şekerine göre öneri bulur.
     *
     * @param bloodSugar Kan şekeri değeri
     * @return Öneriler listesi
     */
    public List<RecommendationRule> findRecommendationByBloodSugar(Integer bloodSugar) {
        try {
            return ruleDao.findByBloodSugarLevel(bloodSugar);
        } catch (SQLException e) {
            System.err.println("Kan şekerine göre öneri bulunurken bir hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Belirtilere göre öneri bulur.
     *
     * @param symptomIds Belirti ID'leri
     * @return Öneriler listesi
     */
    public List<RecommendationRule> findRecommendationBySymptoms(List<Integer> symptomIds) {
        try {
            return ruleDao.findBySymptoms(symptomIds);
        } catch (SQLException e) {
            System.err.println("Belirtilere göre öneri bulunurken bir hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Diyet önerisi oluşturur.
     *
     * @param patientId Hasta ID'si
     * @return Önerilen diyet veya null
     */
    public Diet generateDietRecommendation(Integer patientId) {
        try {
            // Hastanın kan şekeri ortalamasını al
            double avgBloodSugar = measurementService.calculateDailyAverage(patientId, java.time.LocalDate.now());

            if (avgBloodSugar <= 0) {
                System.err.println("Kan şekeri ortalaması hesaplanamadı.");
                return null;
            }

            // Kan şekerine göre öneri bul
            List<RecommendationRule> rules = findRecommendationByBloodSugar((int) avgBloodSugar);

            if (rules.isEmpty()) {
                System.err.println("Uygun öneri bulunamadı.");
                return null;
            }

            // İlk öneriyi kullan
            return rules.get(0).getRecommendedDiet();
        } catch (Exception e) {
            System.err.println("Diyet önerisi oluşturulurken bir hata oluştu: " + e.getMessage());
            return null;
        }
    }

    /**
     * Egzersiz önerisi oluşturur.
     *
     * @param patientId Hasta ID'si
     * @return Önerilen egzersiz veya null
     */
    public Exercise generateExerciseRecommendation(Integer patientId) {
        try {
            // Hastanın kan şekeri ortalamasını al
            double avgBloodSugar = measurementService.calculateDailyAverage(patientId, java.time.LocalDate.now());

            if (avgBloodSugar <= 0) {
                System.err.println("Kan şekeri ortalaması hesaplanamadı.");
                return null;
            }

            // Kan şekerine göre öneri bul
            List<RecommendationRule> rules = findRecommendationByBloodSugar((int) avgBloodSugar);

            if (rules.isEmpty()) {
                System.err.println("Uygun öneri bulunamadı.");
                return null;
            }

            // İlk öneriyi kullan
            return rules.get(0).getRecommendedExercise();
        } catch (Exception e) {
            System.err.println("Egzersiz önerisi oluşturulurken bir hata oluştu: " + e.getMessage());
            return null;
        }
    }
}