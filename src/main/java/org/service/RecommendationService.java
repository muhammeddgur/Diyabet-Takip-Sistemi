package org.service;

import org.dao.RecommendationRuleDao;
import org.dao.IRecommendationRuleDao;
import org.dao.SymptomDao;
import org.model.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;

/**
 * Diyet/egzersiz önerileri için servis sınıfı.
 */
public class RecommendationService {

    private final IRecommendationRuleDao ruleDao;
    private final MeasurementService measurementService;
    private final SymptomDao symptomDao;

    public RecommendationService() {
        this.ruleDao = new RecommendationRuleDao();
        this.measurementService = new MeasurementService();
        this.symptomDao = new SymptomDao();
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
            System.err.println("Kan şekerine göre öneri bulunurken hata: " + e.getMessage());
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
            System.err.println("Belirtilere göre öneri bulunurken hata: " + e.getMessage());
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
        return recommendDiet(patientId, null, null);
    }

    /**
     * Egzersiz önerisi oluşturur.
     *
     * @param patientId Hasta ID'si
     * @return Önerilen egzersiz veya null
     */
    public Exercise generateExerciseRecommendation(Integer patientId) {
        return recommendExercise(patientId, null, null);
    }

    /**
     * Hastanın belirti ID'lerini veritabanından alır
     */
    private List<Integer> getPatientSymptomIds(Integer patientId) {
        try {
            List<PatientSymptom> patientSymptoms = symptomDao.getPatientSymptomsByPatientId(patientId);
            return patientSymptoms.stream()
                    .map(PatientSymptom::getSymptom_id)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Hasta belirtileri alınırken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Belirtilere ve kan şekeri değerine göre diyet önerisi oluşturur
     *
     * @param symptomNames Belirti adları listesi
     * @param bloodSugar Kan şekeri değeri
     * @return Önerilen diyet veya null (eşleşme yoksa)
     */
    public Diet recommendDietBySymptoms(List<String> symptomNames, int bloodSugar) {
        try {
            // Belirtilerin ID'lerini bul ya da yeni oluştur
            List<Integer> symptomIds = resolveSymptomIds(symptomNames);

            // Kan şekeri seviyesine uygun kuralları bul
            List<RecommendationRule> matchingRules = findRecommendationByBloodSugar(bloodSugar);

            if (matchingRules.isEmpty()) {
                return null;
            }

            // Kurallarda tam eşleşme ara
            for (RecommendationRule rule : matchingRules) {
                if (hasExactSymptomMatch(rule, symptomIds)) {
                    return rule.getRecommendedDiet();
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("Diyet önerisi oluşturulurken hata: " + e.getMessage());
            return null;
        }
    }

    /**
     * Belirtilere ve kan şekeri değerine göre egzersiz önerisi oluşturur
     *
     * @param symptomNames Belirti adları listesi
     * @param bloodSugar Kan şekeri değeri
     * @return Önerilen egzersiz veya null (eşleşme yoksa)
     */
    public Exercise recommendExerciseBySymptoms(List<String> symptomNames, int bloodSugar) {
        try {
            // Belirtilerin ID'lerini bul ya da yeni oluştur
            List<Integer> symptomIds = resolveSymptomIds(symptomNames);

            // Kan şekeri seviyesine uygun kuralları bul
            List<RecommendationRule> matchingRules = findRecommendationByBloodSugar(bloodSugar);

            if (matchingRules.isEmpty()) {
                return null;
            }

            // Kurallarda tam eşleşme ara
            for (RecommendationRule rule : matchingRules) {
                if (hasExactSymptomMatch(rule, symptomIds)) {
                    return rule.getRecommendedExercise();
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("Egzersiz önerisi oluşturulurken hata: " + e.getMessage());
            return null;
        }
    }

    /**
     * Belirti adlarını ID'lere çevirir, yoksa yeni belirti oluşturur
     */
    private List<Integer> resolveSymptomIds(List<String> symptomNames) throws SQLException {
        List<Integer> symptomIds = new ArrayList<>();
        for (String symptomName : symptomNames) {
            Symptom symptom = symptomDao.findByName(symptomName);
            if (symptom != null) {
                symptomIds.add(symptom.getSymptom_id());
            } else {
                // Yeni belirti oluştur
                Symptom newSymptom = new Symptom();
                newSymptom.setSymptom_adi(symptomName);
                newSymptom.setAciklama("Otomatik eklenen belirti");

                boolean success = symptomDao.save(newSymptom);
                if (success && newSymptom.getSymptom_id() != null) {
                    symptomIds.add(newSymptom.getSymptom_id());
                }
            }
        }
        return symptomIds;
    }

    /**
     * Hastanın durumuna göre önerilen diyeti belirler
     */
    public Diet recommendDiet(Integer patientId, Integer bloodSugar, List<Integer> symptomIds) {
        try {
            // Kan şekeri değeri verilmemişse son ölçümü al
            if (bloodSugar == null) {
                BloodSugarMeasurement lastMeasurement = measurementService.getLatestMeasurement(patientId);
                bloodSugar = lastMeasurement != null ? lastMeasurement.getOlcum_degeri() : null;
                if (bloodSugar == null) {
                    return null;
                }
            }

            // Belirtiler verilmemişse, hastanın belirtilerini veritabanından al
            if (symptomIds == null || symptomIds.isEmpty()) {
                symptomIds = getPatientSymptomIds(patientId);
            }

            // Kan şekeri seviyesine uygun kuralları bul
            List<RecommendationRule> matchingRules = findRecommendationByBloodSugar(bloodSugar);

            if (matchingRules.isEmpty()) {
                return null;
            }

            // Tam eşleşme ara
            for (RecommendationRule rule : matchingRules) {
                if (hasExactSymptomMatch(rule, symptomIds)) {
                    return rule.getRecommendedDiet();
                }
            }

            return null;

        } catch (Exception e) {
            System.err.println("Diyet önerisi oluşturulurken hata: " + e.getMessage());
            return null;
        }
    }

    /**
     * Hastanın durumuna göre önerilen egzersizi belirler
     */
    public Exercise recommendExercise(Integer patientId, Integer bloodSugar, List<Integer> symptomIds) {
        try {
            // Kan şekeri değeri verilmemişse son ölçümü al
            if (bloodSugar == null) {
                BloodSugarMeasurement lastMeasurement = measurementService.getLatestMeasurement(patientId);
                bloodSugar = lastMeasurement != null ? lastMeasurement.getOlcum_degeri() : null;
                if (bloodSugar == null) {
                    return null;
                }
            }

            // Belirtiler verilmemişse, hastanın belirtilerini veritabanından al
            if (symptomIds == null || symptomIds.isEmpty()) {
                symptomIds = getPatientSymptomIds(patientId);
            }

            // Kan şekeri seviyesine uygun kuralları bul
            List<RecommendationRule> matchingRules = findRecommendationByBloodSugar(bloodSugar);

            if (matchingRules.isEmpty()) {
                return null;
            }

            // Tam eşleşme ara
            for (RecommendationRule rule : matchingRules) {
                if (hasExactSymptomMatch(rule, symptomIds)) {
                    return rule.getRecommendedExercise();
                }
            }

            return null;

        } catch (Exception e) {
            System.err.println("Egzersiz önerisi oluşturulurken hata: " + e.getMessage());
            return null;
        }
    }

    /**
     * Kural ve hasta belirtileri tam eşleşiyor mu kontrol eder
     */
    private boolean hasExactSymptomMatch(RecommendationRule rule, List<Integer> symptomIds) {
        try {
            // Kural belirtileri
            List<Integer> ruleSymptomIds = new ArrayList<>();
            if (rule.getSymptoms() != null && !rule.getSymptoms().isEmpty()) {
                ruleSymptomIds = rule.getSymptoms().stream()
                        .map(Symptom::getSymptom_id)
                        .collect(Collectors.toList());
            }

            // Hasta belirtileri
            List<Integer> patientSymptomIds = symptomIds != null ? symptomIds : new ArrayList<>();

            // Belirti sayısı farklıysa eşleşme yok
            if (ruleSymptomIds.size() != patientSymptomIds.size()) {
                return false;
            }

            // Her iki tarafta da aynı belirtilerin olup olmadığını kontrol et
            Set<Integer> ruleSymptomIdsSet = new HashSet<>(ruleSymptomIds);
            Set<Integer> patientSymptomIdsSet = new HashSet<>(patientSymptomIds);

            // İki set tam olarak aynı mı?
            return ruleSymptomIdsSet.equals(patientSymptomIdsSet);

        } catch (Exception e) {
            System.err.println("Belirti eşleşme kontrolünde hata: " + e.getMessage());
            return false;
        }
    }
}