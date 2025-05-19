package org.service;

import org.dao.InsulinRecommendationDao;
import org.dao.IInsulinRecommendationDao;
import org.model.InsulinRecommendation;
import org.model.Patient;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * İnsülin önerileri için servis sınıfı.
 */
public class InsulinRecommendationService {

    private final IInsulinRecommendationDao recommendationDao;
    private final MeasurementService measurementService;
    private final PatientService patientService;
    private final NotificationService notificationService;

    public InsulinRecommendationService() {
        this.recommendationDao = new InsulinRecommendationDao();
        this.measurementService = new MeasurementService();
        this.patientService = new PatientService();
        this.notificationService = new NotificationService();
    }

    /**
     * İnsülin dozu hesaplar.
     *
     * @param patientId Hasta ID'si
     * @return Hesaplanan insülin dozu veya -1 (hesaplanamadı)
     */
    public double calculateInsulinDose(Integer patientId) {
        // Bugünün ortalamasını al
        double dailyAverage = measurementService.calculateDailyAverage(patientId, LocalDate.now());

        if (dailyAverage <= 0) {
            System.err.println("Günlük ortalama hesaplanamadı.");
            return -1;
        }

        // İnsülin dozunu belirle
        // Bu hesaplama gerçek tıbbi bir hesaplama değildir, gerçek uygulamada doktorların belirlediği kurallara göre hesaplanmalıdır
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
                notificationService.sendEmail(
                        patient.getEmail(),
                        "Diyabet Takip Sistemi - İnsülin Önerisi",
                        "Sayın " + patient.getAd() + " " + patient.getSoyad() + ",\n\n" +
                                "Bugünkü kan şekeri ortalama değeriniz: " + recommendation.getAverageValue() + " mg/dL\n" +
                                "Önerilen insülin miktarı: " + recommendation.getRecommendedInsulin() + " ünite"
                );

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