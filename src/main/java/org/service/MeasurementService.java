package org.service;

import org.dao.MeasurementDao;
import org.dao.IMeasurementDao;
import org.dao.AlertDao;
import org.dao.AlertTypeDao;
import org.dao.BloodSugarCategoryDao;
import org.dao.InsulinReferenceDao;
import org.model.Alert;
import org.model.AlertType;
import org.model.BloodSugarCategory;
import org.model.BloodSugarMeasurement;
import org.model.InsulinReference;
import org.model.Patient;
import org.util.DateTimeUtil;
import org.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ölçüm ve insülin hesaplama işlemleri için servis sınıfı.
 */
public class MeasurementService {

    private final IMeasurementDao measurementDao;
    private final AlertService alertService;
    private final AlertDao alertDao;
    private final AlertTypeDao alertTypeDao;
    private final BloodSugarCategoryDao bloodSugarCategoryDao;
    private final InsulinReferenceDao insulinReferenceDao;

    // Ölçüm zamanları ve saatleri için harita
    private static final Map<String, String> TIME_RANGES = new HashMap<>();

    static {
        TIME_RANGES.put("sabah", "07:00-08:00");
        TIME_RANGES.put("ogle", "12:00-13:00");
        TIME_RANGES.put("ikindi", "15:00-16:00");
        TIME_RANGES.put("aksam", "18:00-19:00");
        TIME_RANGES.put("gece", "22:00-23:00");
    }

    public MeasurementService() {
        this.measurementDao = new MeasurementDao();
        this.alertService = new AlertService();
        this.alertDao = new AlertDao();
        this.alertTypeDao = new AlertTypeDao();
        this.bloodSugarCategoryDao = new BloodSugarCategoryDao();
        this.insulinReferenceDao = new InsulinReferenceDao();
    }

    /**
     * Yeni ölçüm ekler, kategori ve insülin miktarını belirler.
     *
     * @param measurement Eklenecek ölçüm
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean addMeasurement(BloodSugarMeasurement measurement) {
        try {
            // Ölçüm zamanını kontrol et
            if (measurement.getOlcum_tarihi() == null) {
                measurement.setOlcum_tarihi(DateTimeUtil.getCurrentDateTime());
            }

            // Kan şekeri değerini doğrula
            if (!ValidationUtil.validateBloodSugar(measurement.getOlcum_degeri())) {
                System.err.println("Geçersiz kan şekeri değeri. Değer 0 ile 1000 mg/dL arasında olmalıdır.");
                return false;
            }

            // Kategori ID belirle
            BloodSugarCategory category = bloodSugarCategoryDao.findByBloodSugarValue(measurement.getOlcum_degeri());
            if (category != null) {
                measurement.setCategory_id(category.getCategory_id());
            }

            // İnsülin miktarını belirle
            if (measurement.getIs_valid_time()) {
                InsulinReference reference = insulinReferenceDao.findByBloodSugarValue(measurement.getOlcum_degeri());
                if (reference != null) {
                    measurement.setInsulin_miktari(reference.getInsulin_dose());
                }
            }

            // Ölçümü kaydet
            boolean saved = measurementDao.save(measurement);

            if (saved) {
                // Ölçüm eşiklerini kontrol et
                checkMeasurementThresholds(measurement);

                // Eğer geçersiz zamanda ölçüm yapıldıysa, uyarı oluştur
                if (!measurement.getIs_valid_time()) {
                    createInvalidTimeAlert(measurement);
                }

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
     * Sadece geçerli saatlerde yapılan günlük ölçümleri getirir.
     *
     * @param patientId Hasta ID'si
     * @param date Tarih
     * @return Geçerli saatlerde yapılan ölçümlerin listesi
     */
    public List<BloodSugarMeasurement> getValidDailyMeasurements(Integer patientId, LocalDate date) {
        try {
            List<BloodSugarMeasurement> allMeasurements = measurementDao.findByDateRange(patientId, date, date);
            return allMeasurements.stream()
                    .filter(m -> m.getIs_valid_time() != null && m.getIs_valid_time())
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            System.err.println("Geçerli günlük ölçümler getirilirken bir hata oluştu: " + e.getMessage());
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
        // DateTimeUtil kullanarak hafta başlangıç ve bitiş tarihlerini al
        List<LocalDate> weekStartAndEnd = DateTimeUtil.getWeekStartAndEnd(null);
        LocalDate startDate = weekStartAndEnd.get(0);
        LocalDate endDate = weekStartAndEnd.get(1);

        try {
            return measurementDao.findByDateRange(patientId, startDate, endDate);
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
        // DateTimeUtil kullanarak ay başlangıç ve bitiş tarihlerini al
        List<LocalDate> monthStartAndEnd = DateTimeUtil.getMonthStartAndEnd(DateTimeUtil.getCurrentDate());
        LocalDate startDate = monthStartAndEnd.get(0);
        LocalDate endDate = monthStartAndEnd.get(1);

        try {
            return measurementDao.findByDateRange(patientId, startDate, endDate);
        } catch (SQLException e) {
            System.err.println("Aylık ölçümler getirilirken bir hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Günlük ortalama hesaplar. Sadece geçerli saatlerde yapılan ölçümleri kullanır.
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

    /**
     * Geçersiz zaman için uyarı oluşturur
     * @param measurement Kan şekeri ölçümü
     */
    private void createInvalidTimeAlert(BloodSugarMeasurement measurement) throws SQLException {
        String olcumZamani = measurement.getOlcum_zamani();
        String zamanAraligi = TIME_RANGES.get(olcumZamani);

        Patient patient = measurement.getPatient();
        if (patient != null) {
            // Uyarı oluştur
            Alert alert = new Alert();
            alert.setPatient(patient);
            alert.setDoctor(patient.getDoctor());

            // Uyarı tipi bul (Ölçüm zamanı uyarısı)
            AlertType alertType = alertTypeDao.findByName("Ölçüm Zamanı Uyarısı");
            if (alertType == null) {
                // Tip yoksa yeni bir uyarı tipi ekle
                alertType = new AlertType();
                alertType.setTip_adi("Ölçüm Zamanı Uyarısı");
                alertType.setAciklama("Ölçüm yapılması gereken zaman aralığı dışında ölçüm yapıldı.");
                alertTypeDao.save(alertType);
            }

            alert.setAlertType(alertType);

            String formattedTime = DateTimeUtil.formatDateTime(measurement.getOlcum_tarihi());

            alert.setMesaj(String.format(
                    "Hasta %s %s, %s ölçümünü belirtilen saat aralığı dışında (%s yerine %s saatinde) gerçekleştirdi. " +
                            "Bu ölçüm ortalamaya dahil edilmeyecektir. Lütfen '%s' ölçümlerini %s saatleri arasında yapınız.",
                    patient.getAd(),
                    patient.getSoyad(),
                    olcumZamani,
                    zamanAraligi,
                    formattedTime,
                    olcumZamani,
                    zamanAraligi
            ));

            alert.setOlusturma_zamani(DateTimeUtil.getCurrentDateTime());
            alertDao.save(alert);
        }
    }

    /**
     * Ölçüm değeri için kategori ID'sini belirler
     * @param bloodSugarValue Kan şekeri değeri
     * @return Kategori ID veya null
     */
    public Integer determineCategoryId(Integer bloodSugarValue) {
        try {
            BloodSugarCategory category = bloodSugarCategoryDao.findByBloodSugarValue(bloodSugarValue);
            return category != null ? category.getCategory_id() : null;
        } catch (SQLException e) {
            System.err.println("Kategori ID belirlenirken bir hata oluştu: " + e.getMessage());
            return null;
        }
    }

    /**
     * Kan şekeri değeri için uygun insülin miktarını belirler
     * @param bloodSugarValue Kan şekeri değeri
     * @return İnsülin miktarı veya null
     */
    public Double determineInsulinAmount(Integer bloodSugarValue) {
        try {
            InsulinReference reference = insulinReferenceDao.findByBloodSugarValue(bloodSugarValue);
            return reference != null ? reference.getInsulin_dose() : null;
        } catch (SQLException e) {
            System.err.println("İnsülin miktarı belirlenirken bir hata oluştu: " + e.getMessage());
            return null;
        }
    }
}