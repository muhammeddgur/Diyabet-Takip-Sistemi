package org.service;

import org.dao.MeasurementDao;
import org.dao.IMeasurementDao;
import org.model.BloodSugarMeasurement;
import org.model.Patient;
import org.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ölçüm ve insülin hesaplama işlemleri için servis sınıfı.
 */
public class MeasurementService {

    private final IMeasurementDao measurementDao;
    private final AlertService alertService;

    public MeasurementService() {
        this.measurementDao = new MeasurementDao();
        this.alertService = new AlertService();
    }

    /**
     * Yeni ölçüm ekler.
     *
     * @param measurement Eklenecek ölçüm
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean addMeasurement(BloodSugarMeasurement measurement) {
        try {
            // Ölçüm zamanını kontrol et
            if (measurement.getOlcum_tarihi() == null) {
                measurement.setOlcum_tarihi(LocalDateTime.now());
            }

            // Kan şekeri değerini doğrula
            if (!ValidationUtil.validateBloodSugar(measurement.getOlcum_degeri())) {
                System.err.println("Geçersiz kan şekeri değeri. Değer 30 ile 600 mg/dL arasında olmalıdır.");
                return false;
            }

            // Ölçümü kaydet
            boolean saved = measurementDao.save(measurement);

            if (saved) {
                // Ölçüm eşiklerini kontrol et
                checkMeasurementThresholds(measurement);
                return true;
            }

            return false;
        } catch (SQLException e) {
            System.err.println("Ölçüm ekleme sırasında bir hata oluştu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Günlük ölçümleri getirir.
     *
     * @param patientId Hasta ID'si
     * @param date Tarih
     * @return Ölçümlerin listesi
     */
    public List<BloodSugarMeasurement> getDailyMeasurements(Integer patientId, LocalDate date) {
        try {
            return measurementDao.findByDateRange(patientId, date, date);
        } catch (SQLException e) {
            System.err.println("Günlük ölçümler getirilirken bir hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Haftalık ölçümleri getirir.
     *
     * @param patientId Hasta ID'si
     * @return Ölçümlerin listesi
     */
    public List<BloodSugarMeasurement> getWeeklyMeasurements(Integer patientId) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        try {
            return measurementDao.findByDateRange(patientId, weekAgo, today);
        } catch (SQLException e) {
            System.err.println("Haftalık ölçümler getirilirken bir hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Aylık ölçümleri getirir.
     *
     * @param patientId Hasta ID'si
     * @return Ölçümlerin listesi
     */
    public List<BloodSugarMeasurement> getMonthlyMeasurements(Integer patientId) {
        LocalDate today = LocalDate.now();
        LocalDate monthAgo = today.minusMonths(1);

        try {
            return measurementDao.findByDateRange(patientId, monthAgo, today);
        } catch (SQLException e) {
            System.err.println("Aylık ölçümler getirilirken bir hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Günlük ortalama hesaplar.
     *
     * @param patientId Hasta ID'si
     * @param date Tarih
     * @return Günlük ortalama değer
     */
    public double calculateDailyAverage(Integer patientId, LocalDate date) {
        try {
            return measurementDao.getDailyAverage(patientId, date);
        } catch (SQLException e) {
            System.err.println("Günlük ortalama hesaplanırken bir hata oluştu: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Ölçüm eşiklerini kontrol eder ve gerekli uyarıları oluşturur.
     *
     * @param measurement Kontrol edilecek ölçüm
     */
    public void checkMeasurementThresholds(BloodSugarMeasurement measurement) {
        // Düşük kan şekeri kontrolü
        if (measurement.getOlcum_degeri() < 70) {
            alertService.checkLowBloodSugar(measurement);
        }

        // Yüksek kan şekeri kontrolü
        if (measurement.getOlcum_degeri() > 200) {
            alertService.checkHighBloodSugar(measurement);
        }
    }
}