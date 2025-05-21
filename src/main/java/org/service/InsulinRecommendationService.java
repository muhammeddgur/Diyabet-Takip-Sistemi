package org.service;

import org.dao.InsulinRecommendationDao;
import org.dao.IInsulinRecommendationDao;
import org.dao.MeasurementDao;
import org.model.BloodSugarMeasurement;
import org.model.InsulinRecommendation;
import org.model.Patient;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * İnsülin önerileri için servis sınıfı.
 */
public class InsulinRecommendationService {

    private final IInsulinRecommendationDao recommendationDao;
    private final MeasurementService measurementService;
    private final PatientService patientService;
    private final NotificationService notificationService;
    private final MeasurementDao measurementDao;

    public InsulinRecommendationService() {
        this.recommendationDao = new InsulinRecommendationDao();
        this.measurementService = new MeasurementService();
        this.patientService = new PatientService();
        this.notificationService = new NotificationService();
        this.measurementDao = new MeasurementDao();
    }

    /**
     * İnsülin dozu hesaplar. Sadece geçerli saatlerde yapılan ölçümleri kullanır.
     *
     * @param patientId Hasta ID'si
     * @return Hesaplanan insülin dozu veya -1 (hesaplanamadı)
     */
    public double calculateInsulinDose(Integer patientId) {
        try {
            // Günlük ölçümleri al
            List<BloodSugarMeasurement> dailyMeasurements = measurementService.getDailyMeasurements(patientId, LocalDate.now());

            // Sadece geçerli zamanda yapılan ölçümleri filtrele
            List<BloodSugarMeasurement> validMeasurements = dailyMeasurements.stream()
                    .filter(m -> m.getIs_valid_time() != null && m.getIs_valid_time())
                    .collect(Collectors.toList());

            if (validMeasurements.isEmpty()) {
                System.err.println("Geçerli saatlerde yapılan ölçüm bulunamadı.");
                return -1;
            }

            // Geçerli ölçümlerin ortalamasını hesapla
            double sum = validMeasurements.stream()
                    .mapToInt(BloodSugarMeasurement::getOlcum_degeri)
                    .sum();

            double dailyAverage = sum / validMeasurements.size();

            if (dailyAverage <= 0) {
                System.err.println("Günlük ortalama hesaplanamadı.");
                return -1;
            }

            // İnsülin dozunu belirle
            if (dailyAverage < 70) {
                return 0; // Hipoglisemi durumu, insülin önerilmez
            } else if (dailyAverage <= 110) {
                return 0; // Normal seviye, insülin önerilmez
            } else if (dailyAverage <= 150) {
                return 1; // Hafif yüksek
            } else if (dailyAverage <= 200) {
                return 2; // Orta yüksek
            } else {
                return 3; // Çok yüksek
            }
        } catch (Exception e) {
            System.err.println("İnsülin dozu hesaplanırken bir hata oluştu: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Sadece geçerli saatlerde yapılan ölçümleri kullanarak günlük ortalama hesaplar
     *
     * @param patientId Hasta ID'si
     * @param date Tarih
     * @return Ortalama değer
     */
    public double calculateValidTimeDailyAverage(Integer patientId, LocalDate date) {
        try {
            // Günlük ölçümleri al
            List<BloodSugarMeasurement> dailyMeasurements = measurementDao.findByDateRange(patientId, date, date);

            // Sadece geçerli zamanda yapılan ölçümleri filtrele
            List<BloodSugarMeasurement> validMeasurements = dailyMeasurements.stream()
                    .filter(m -> m.getIs_valid_time() != null && m.getIs_valid_time())
                    .collect(Collectors.toList());

            if (validMeasurements.isEmpty()) {
                return 0.0;
            }

            // Geçerli ölçümlerin ortalamasını hesapla
            double sum = validMeasurements.stream()
                    .mapToInt(BloodSugarMeasurement::getOlcum_degeri)
                    .sum();

            return sum / validMeasurements.size();
        } catch (SQLException e) {
            System.err.println("Günlük ortalama hesaplanırken bir hata oluştu: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Öneri oluşturur. Sadece geçerli saatlerde yapılan ölçümleri kullanır.
     *
     * @param patientId Hasta ID'si
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean createDailyRecommendation(Integer patientId) {
        try {
            // Hastayı bul - getPatient metodunu kullanıyoruz
            Patient patient = patientService.getPatient(patientId);
            if (patient == null) {
                System.err.println("Hasta bulunamadı.");
                return false;
            }

            // Günlük ölçümleri al
            List<BloodSugarMeasurement> dailyMeasurements = measurementService.getDailyMeasurements(patientId, LocalDate.now());

            // Sadece geçerli zamanda yapılan ölçümleri filtrele
            List<BloodSugarMeasurement> validMeasurements = dailyMeasurements.stream()
                    .filter(m -> m.getIs_valid_time() != null && m.getIs_valid_time())
                    .collect(Collectors.toList());

            if (validMeasurements.isEmpty()) {
                System.err.println("Geçerli saatlerde yapılan ölçüm bulunamadı.");
                return false;
            }

            // Geçerli ölçümlerin ortalamasını hesapla
            double sum = validMeasurements.stream()
                    .mapToInt(BloodSugarMeasurement::getOlcum_degeri)
                    .sum();

            double dailyAverage = sum / validMeasurements.size();

            // İnsülin dozunu hesapla
            double recommendedDose = calculateInsulinDose(patientId);
            if (recommendedDose < 0) {
                return false;
            }

            // Öneri oluştur
            InsulinRecommendation recommendation = new InsulinRecommendation();
            recommendation.setPatient(patient);
            recommendation.setPatient_id(patientId);
            recommendation.setRecommendation_date(LocalDate.now());
            recommendation.setAverageValue(dailyAverage);
            recommendation.setMeasuredCount(validMeasurements.size());
            recommendation.setRecommendedInsulin(recommendedDose);
            recommendation.setCreated_at(LocalDateTime.now());
            recommendation.setApplied(false);

            return createRecommendation(recommendation);
        } catch (Exception e) {
            System.err.println("Öneri oluşturulurken bir hata oluştu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Öneri oluşturur.
     *
     * @param recommendation Oluşturulacak öneri
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean createRecommendation(InsulinRecommendation recommendation) {
        try {
            // Oluşturulma zamanını ayarla
            if (recommendation.getCreated_at() == null) {
                recommendation.setCreated_at(LocalDateTime.now());
            }

            // Öneriyi kaydet
            boolean saved = recommendationDao.save(recommendation);

            if (saved) {
                // Hastaya e-posta gönder
                Patient patient = recommendation.getPatient();
                if (patient != null) {
                    notificationService.sendEmail(
                            patient.getEmail(),
                            "Diyabet Takip Sistemi - İnsülin Önerisi",
                            "Sayın " + patient.getAd() + " " + patient.getSoyad() + ",\n\n" +
                                    "Bugünkü kan şekeri ortalama değeriniz: " + recommendation.getAverageValue() + " mg/dL\n" +
                                    "Önerilen insülin miktarı: " + recommendation.getRecommendedInsulin() + " ünite\n\n" +
                                    "Not: Bu öneri sadece belirtilen saat aralıklarında yapılan " +
                                    recommendation.getMeasuredCount() + " adet ölçüm dikkate alınarak hesaplanmıştır."
                    );
                }
                return true;
            }

            return false;
        } catch (SQLException e) {
            System.err.println("Öneri oluşturulurken bir hata oluştu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Öneri geçmişini getirir.
     *
     * @param patientId Hasta ID'si
     * @return Önerilerin listesi
     */
    public List<InsulinRecommendation> getRecommendationHistory(Integer patientId) {
        try {
            return recommendationDao.findByPatientId(patientId);
        } catch (SQLException e) {
            System.err.println("Öneri geçmişi getirilirken bir hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Öneriyi uygulandı olarak işaretler.
     *
     * @param recommendationId Öneri ID'si
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean markRecommendationAsApplied(Integer recommendationId) {
        try {
            return recommendationDao.markAsApplied(recommendationId);
        } catch (SQLException e) {
            System.err.println("Öneri uygulandı olarak işaretlenirken bir hata oluştu: " + e.getMessage());
            return false;
        }
    }
}