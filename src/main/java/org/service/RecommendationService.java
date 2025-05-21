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
    private final PatientService patientService;
    private final SymptomDao symptomDao;

    public RecommendationService() {
        this.ruleDao = new RecommendationRuleDao();
        this.measurementService = new MeasurementService();
        this.patientService = new PatientService();
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
            List<RecommendationRule> rules = ruleDao.findByBloodSugarLevel(bloodSugar);
            System.out.println("Kan şekeri değeri " + bloodSugar + " için bulunan toplam kural sayısı: " + rules.size());
            // Kuralların detaylarını göster
            for (RecommendationRule rule : rules) {
                System.out.println("Kural ID: " + rule.getRule_id() +
                        ", Kan şekeri aralığı: " + rule.getMinBloodSugar() +
                        "-" + rule.getMaxBloodSugar() +
                        ", Diyet: " + rule.getRecommendedDiet().getDiet_adi() +
                        ", Egzersiz: " + rule.getRecommendedExercise().getExercise_adi());
                System.out.println("  Belirtiler: " + getSymptomNames(rule));
            }
            return rules;
        } catch (SQLException e) {
            System.err.println("Kan şekerine göre öneri bulunurken bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Kuralın belirti adlarını string olarak döndürür
     */
    private String getSymptomNames(RecommendationRule rule) {
        if (rule.getSymptoms() == null || rule.getSymptoms().isEmpty()) {
            return "Belirti yok";
        }
        return rule.getSymptoms().stream()
                .map(Symptom::getSymptom_adi)
                .collect(Collectors.joining(", "));
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

            List<Integer> symptomIds = patientSymptoms.stream()
                    .map(PatientSymptom::getSymptom_id)
                    .collect(Collectors.toList());

            System.out.println("Hasta ID " + patientId + " için veritabanından çekilen belirti ID'leri: " + symptomIds);

            // Belirti adlarını da göster
            List<String> symptomNames = new ArrayList<>();
            for (Integer symptomId : symptomIds) {
                try {
                    Symptom symptom = symptomDao.findById(symptomId);
                    if (symptom != null) {
                        symptomNames.add(symptom.getSymptom_adi() + " (ID: " + symptomId + ")");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Belirti adları: " + String.join(", ", symptomNames));

            return symptomIds;
        } catch (Exception e) {
            System.err.println("Hasta belirtileri alınırken hata: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Hastanın durumuna göre önerilen diyeti belirler
     *
     * @param patientId Hasta ID
     * @param bloodSugar Kan şekeri değeri (null olabilir, o zaman hastanın son ölçümü kullanılır)
     * @param symptomIds Belirti ID listesi (null olabilir, o zaman hastanın belirtileri alınır)
     * @return Önerilen diyet
     */
    public Diet recommendDiet(Integer patientId, Integer bloodSugar, List<Integer> symptomIds) {
        try {
            System.out.println("\n--- Diyet Öneri Sistemi Başlatılıyor ---");

            // Kan şekeri değeri verilmemişse son ölçümü al
            if (bloodSugar == null) {
                BloodSugarMeasurement lastMeasurement = measurementService.getLatestMeasurement(patientId);
                bloodSugar = lastMeasurement != null ? lastMeasurement.getOlcum_degeri() : 100; // Varsayılan değer
                System.out.println("Hastanın son kan şekeri değeri: " + bloodSugar);
            }

            // Belirtiler verilmemişse, hastanın belirtilerini veritabanından al
            if (symptomIds == null || symptomIds.isEmpty()) {
                symptomIds = getPatientSymptomIds(patientId);
                System.out.println("Veritabanından alınan hasta belirti ID'leri: " + symptomIds);
            }

            // En iyi eşleşen kuralı bul
            List<RecommendationRule> matchingRules = findRecommendationByBloodSugar(bloodSugar);

            if (matchingRules.isEmpty()) {
                System.out.println("Kan şekeri seviyesine uygun kural bulunamadı, varsayılan diyet öneriliyor");
                return getDefaultDietByBloodSugar(bloodSugar);
            }

            // Belirtiler verilmişse, tam eşleşme kontrolü yap
            if (symptomIds != null && !symptomIds.isEmpty()) {
                // Önce tam eşleşen kuralları kontrol et
                System.out.println("Tam eşleşme kontrolü yapılıyor...");
                for (RecommendationRule rule : matchingRules) {
                    if (hasExactSymptomMatch(rule, symptomIds)) {
                        System.out.println("TAM EŞLEŞME BULUNDU! Kural ID: " + rule.getRule_id());
                        System.out.println("Önerilen diyet: " + rule.getRecommendedDiet().getDiet_adi());
                        return rule.getRecommendedDiet();
                    }
                }

                // Tam eşleşme yoksa, kısmi eşleşmeleri kontrol et
                System.out.println("Tam eşleşme bulunamadı. Kısmi eşleşme kontrolü yapılıyor...");
                int bestMatchCount = 0;
                RecommendationRule bestRule = null;

                for (RecommendationRule rule : matchingRules) {
                    int matchCount = getSymptomMatchCount(rule, symptomIds);
                    System.out.println("Kural ID: " + rule.getRule_id() + ", Eşleşen belirti sayısı: " + matchCount);

                    if (matchCount > bestMatchCount) {
                        bestMatchCount = matchCount;
                        bestRule = rule;
                    }
                }

                if (bestRule != null && bestMatchCount > 0) {
                    System.out.println("EN İYİ KISMI EŞLEŞME: Kural ID: " + bestRule.getRule_id() +
                            ", Eşleşen belirti sayısı: " + bestMatchCount);
                    System.out.println("Önerilen diyet: " + bestRule.getRecommendedDiet().getDiet_adi());
                    return bestRule.getRecommendedDiet();
                }
            }

            // Eşleşme yoksa ilk kuralı kullan
            System.out.println("Belirti eşleşmesi bulunamadı, ilk kural kullanılıyor. Kural ID: " + matchingRules.get(0).getRule_id());
            System.out.println("Önerilen diyet: " + matchingRules.get(0).getRecommendedDiet().getDiet_adi());
            return matchingRules.get(0).getRecommendedDiet();

        } catch (Exception e) {
            System.err.println("Diyet önerisi oluşturulurken hata: " + e.getMessage());
            e.printStackTrace();
            return getDefaultDietByBloodSugar(100); // Hata durumunda varsayılan diyet
        }
    }

    /**
     * Hastanın durumuna göre önerilen egzersizi belirler
     *
     * @param patientId Hasta ID
     * @param bloodSugar Kan şekeri değeri (null olabilir, o zaman hastanın son ölçümü kullanılır)
     * @param symptomIds Belirti ID listesi (null olabilir, o zaman hastanın belirtileri alınır)
     * @return Önerilen egzersiz
     */
    public Exercise recommendExercise(Integer patientId, Integer bloodSugar, List<Integer> symptomIds) {
        try {
            System.out.println("\n--- Egzersiz Öneri Sistemi Başlatılıyor ---");

            // Kan şekeri değeri verilmemişse son ölçümü al
            if (bloodSugar == null) {
                BloodSugarMeasurement lastMeasurement = measurementService.getLatestMeasurement(patientId);
                bloodSugar = lastMeasurement != null ? lastMeasurement.getOlcum_degeri() : 100; // Varsayılan değer
                System.out.println("Hastanın son kan şekeri değeri: " + bloodSugar);
            }

            // Belirtiler verilmemişse, hastanın belirtilerini veritabanından al
            if (symptomIds == null || symptomIds.isEmpty()) {
                symptomIds = getPatientSymptomIds(patientId);
                System.out.println("Veritabanından alınan hasta belirti ID'leri: " + symptomIds);
            }

            // En iyi eşleşen kuralı bul
            List<RecommendationRule> matchingRules = findRecommendationByBloodSugar(bloodSugar);

            if (matchingRules.isEmpty()) {
                System.out.println("Kan şekeri seviyesine uygun kural bulunamadı, varsayılan egzersiz öneriliyor");
                return getDefaultExerciseByBloodSugar(bloodSugar);
            }

            // Belirtiler verilmişse, tam eşleşme kontrolü yap
            if (symptomIds != null && !symptomIds.isEmpty()) {
                // Önce tam eşleşen kuralları kontrol et
                System.out.println("Tam eşleşme kontrolü yapılıyor...");
                for (RecommendationRule rule : matchingRules) {
                    if (hasExactSymptomMatch(rule, symptomIds)) {
                        System.out.println("TAM EŞLEŞME BULUNDU! Kural ID: " + rule.getRule_id());
                        System.out.println("Önerilen egzersiz: " + rule.getRecommendedExercise().getExercise_adi());
                        return rule.getRecommendedExercise();
                    }
                }

                // Tam eşleşme yoksa, kısmi eşleşmeleri kontrol et
                System.out.println("Tam eşleşme bulunamadı. Kısmi eşleşme kontrolü yapılıyor...");
                int bestMatchCount = 0;
                RecommendationRule bestRule = null;

                for (RecommendationRule rule : matchingRules) {
                    int matchCount = getSymptomMatchCount(rule, symptomIds);
                    System.out.println("Kural ID: " + rule.getRule_id() + ", Eşleşen belirti sayısı: " + matchCount);

                    if (matchCount > bestMatchCount) {
                        bestMatchCount = matchCount;
                        bestRule = rule;
                    }
                }

                if (bestRule != null && bestMatchCount > 0) {
                    System.out.println("EN İYİ KISMI EŞLEŞME: Kural ID: " + bestRule.getRule_id() +
                            ", Eşleşen belirti sayısı: " + bestMatchCount);
                    System.out.println("Önerilen egzersiz: " + bestRule.getRecommendedExercise().getExercise_adi());
                    return bestRule.getRecommendedExercise();
                }
            }

            // Eşleşme yoksa ilk kuralı kullan
            System.out.println("Belirti eşleşmesi bulunamadı, ilk kural kullanılıyor. Kural ID: " + matchingRules.get(0).getRule_id());
            System.out.println("Önerilen egzersiz: " + matchingRules.get(0).getRecommendedExercise().getExercise_adi());
            return matchingRules.get(0).getRecommendedExercise();

        } catch (Exception e) {
            System.err.println("Egzersiz önerisi oluşturulurken hata: " + e.getMessage());
            e.printStackTrace();
            return getDefaultExerciseByBloodSugar(100); // Hata durumunda varsayılan egzersiz
        }
    }

    /**
     * Belirtilen kuralın belirli bir belirtiyi içerip içermediğini kontrol eder
     */
    private boolean ruleHasSymptom(RecommendationRule rule, Integer symptomId) {
        try {
            if (rule.getSymptoms() == null || rule.getSymptoms().isEmpty()) {
                return false;
            }

            for (Symptom symptom : rule.getSymptoms()) {
                if (symptom.getSymptom_id().equals(symptomId)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kural ve hasta belirtileri tam eşleşiyor mu kontrol eder
     * Hem sayı hem de içerik bakımından uyuşmalı
     */
    private boolean hasExactSymptomMatch(RecommendationRule rule, List<Integer> symptomIds) {
        try {
            if (rule.getSymptoms() == null || rule.getSymptoms().isEmpty()) {
                return symptomIds.isEmpty(); // İkisi de boşsa eşleşme var
            }

            if (rule.getSymptoms().size() != symptomIds.size()) {
                System.out.println("Kural ID " + rule.getRule_id() + ": Belirti sayısı uyuşmuyor" +
                        " (Kural: " + rule.getSymptoms().size() + ", Hasta: " + symptomIds.size() + ")");
                return false; // Belirti sayısı uyuşmuyor
            }

            // Her iki tarafta da aynı belirtilerin olup olmadığını kontrol et
            Set<Integer> ruleSymptomIdsSet = new HashSet<>();
            for (Symptom symptom : rule.getSymptoms()) {
                ruleSymptomIdsSet.add(symptom.getSymptom_id());
            }

            Set<Integer> patientSymptomIdsSet = new HashSet<>(symptomIds);

            // İki set tam olarak aynı mı?
            boolean exactMatch = ruleSymptomIdsSet.equals(patientSymptomIdsSet);

            System.out.println("Kural ID " + rule.getRule_id() + ": Tam eşleşme kontrolü: " +
                    (exactMatch ? "EVET" : "HAYIR"));
            System.out.println("  Kural belirtileri: " + ruleSymptomIdsSet);
            System.out.println("  Hasta belirtileri: " + patientSymptomIdsSet);

            return exactMatch;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kaç belirti eşleşiyor sayar
     */
    private int getSymptomMatchCount(RecommendationRule rule, List<Integer> symptomIds) {
        try {
            if (rule.getSymptoms() == null || rule.getSymptoms().isEmpty()) {
                return 0;
            }

            int matchCount = 0;
            List<Integer> ruleSymptomIds = rule.getSymptoms().stream()
                    .map(Symptom::getSymptom_id)
                    .collect(Collectors.toList());

            System.out.println("  Kural ID " + rule.getRule_id() + " belirtileri: " + ruleSymptomIds);
            System.out.println("  Hasta belirtileri: " + symptomIds);

            // Kesişim kümesinin boyutu
            for (Integer symptomId : symptomIds) {
                if (ruleSymptomIds.contains(symptomId)) {
                    matchCount++;
                    System.out.println("    Eşleşen belirti: ID " + symptomId);
                }
            }

            return matchCount;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Kan şekeri seviyesine göre varsayılan diyet önerisi
     */
    private Diet getDefaultDietByBloodSugar(int bloodSugar) {
        // Tabloya göre kan şekeri seviyesine uygun varsayılan diyeti döndür
        Diet diet = new Diet();

        if (bloodSugar < 70) {
            // Hipoglisemi
            diet.setDiet_adi("Dengeli Beslenme");
            diet.setAciklama("Hipoglisemi için dengeli beslenme önerilir.");
        } else if (bloodSugar <= 110) {
            // Normal - Alt Düzey
            diet.setDiet_adi("Az Şekerli Diyet");
            diet.setAciklama("Normal alt seviye kan şekeri için az şekerli diyet önerilir.");
        } else if (bloodSugar <= 180) {
            // Normal - Üst Düzey / Hafif Yüksek
            diet.setDiet_adi("Az Şekerli Diyet");
            diet.setAciklama("Hafif yüksek kan şekeri için az şekerli diyet önerilir.");
        } else {
            // Hiperglisemi
            diet.setDiet_adi("Şekersiz Diyet");
            diet.setAciklama("Yüksek kan şekeri için şekersiz diyet önerilir.");
        }

        System.out.println("Kan şekeri: " + bloodSugar + " için varsayılan diyet: " + diet.getDiet_adi());
        return diet;
    }

    /**
     * Kan şekeri seviyesine göre varsayılan egzersiz önerisi
     */
    private Exercise getDefaultExerciseByBloodSugar(int bloodSugar) {
        // Tabloya göre kan şekeri seviyesine uygun varsayılan egzersizi döndür
        Exercise exercise = new Exercise();

        if (bloodSugar < 70) {
            // Hipoglisemi
            exercise.setExercise_adi("Yok");
            exercise.setAciklama("Hipoglisemide egzersiz önerilmez, istirahat edilmelidir.");
        } else if (bloodSugar <= 110) {
            // Normal - Alt Düzey
            exercise.setExercise_adi("Yürüyüş");
            exercise.setAciklama("Normal kan şekeri için günlük yürüyüş önerilir.");
        } else if (bloodSugar <= 180) {
            // Normal - Üst Düzey / Hafif Yüksek
            exercise.setExercise_adi("Klinik Egzersiz");
            exercise.setAciklama("Hafif yüksek kan şekerinde klinik egzersiz önerilir.");
        } else {
            // Hiperglisemi
            exercise.setExercise_adi("Klinik Egzersiz");
            exercise.setAciklama("Yüksek kan şekerinde doktor kontrolünde klinik egzersiz önerilir.");
        }

        System.out.println("Kan şekeri: " + bloodSugar + " için varsayılan egzersiz: " + exercise.getExercise_adi());
        return exercise;
    }
}