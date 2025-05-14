package org.service;

import org.dao.MeasurementDao;
import org.model.BloodSugarMeasurement;
import org.model.Patient;
import org.util.DateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kan şekeri ölçümlerinin yönetimi için servis sınıfı.
 * Ölçüm ekleme, listeleme, analiz ve raporlama işlemlerini yönetir.
 */
public class MeasurementService {
    private static final Logger LOGGER = Logger.getLogger(MeasurementService.class.getName());

    private final MeasurementDao measurementDao;
    private final AlertService alertService;

    // Kan şekeri seviyelerine ait sabitler (mg/dL)
    private static final BigDecimal HYPOGLYCEMIA_THRESHOLD = new BigDecimal("70.0");
    private static final BigDecimal NORMAL_UPPER_THRESHOLD = new BigDecimal("110.0");
    private static final BigDecimal HIGH_THRESHOLD = new BigDecimal("180.0");
    private static final BigDecimal VERY_HIGH_THRESHOLD = new BigDecimal("200.0");

    public MeasurementService() {
        this.measurementDao = new MeasurementDao();
        this.alertService = new AlertService();
    }

    /**
     * Yeni bir kan şekeri ölçümü ekler ve gerekirse uyarılar oluşturur.
     *
     * @param measurement Yeni kan şekeri ölçümü
     * @return Eklenen ölçümün ID'si, başarısızsa null
     */
    public Integer addMeasurement(BloodSugarMeasurement measurement) {
        try {
            // Ölçüm zamanını kontrol et
            if (measurement.getMeasurementTime() == null) {
                measurement.setMeasurementTime(DateTime.getCurrentDateTime());
            }

            // Ölçüm tipini belirle
            if (measurement.getMeasurementType() == null || measurement.getMeasurementType().isEmpty()) {
                String period = DateTime.getMeasurementPeriod(measurement.getMeasurementTime().toLocalTime());
                measurement.setMeasurementType(period);
            }

            // Veritabanına kaydet
            Integer measurementId = measurementDao.save(measurement);

            if (measurementId != null) {
                // Ölçümü işle ve gerekirse uyarı oluştur
                alertService.processBloodSugarMeasurement(measurement);
                LOGGER.info("Yeni ölçüm eklendi. ID: " + measurementId);
            }

            return measurementId;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ölçüm eklenirken hata oluştu", e);
            return null;
        }
    }

    /**
     * Bir ölçümü günceller.
     *
     * @param measurement Güncellenecek ölçüm
     * @return İşlem başarılıysa true, değilse false
     */
    public boolean updateMeasurement(BloodSugarMeasurement measurement) {
        try {
            measurementDao.update(measurement);
            LOGGER.info("Ölçüm güncellendi. ID: " + measurement.getMeasurementId());
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ölçüm güncellenirken hata oluştu", e);
            return false;
        }
    }

    /**
     * Bir ölçümü siler.
     *
     * @param measurementId Silinecek ölçümün ID'si
     * @return İşlem başarılıysa true, değilse false
     */
    public boolean deleteMeasurement(Integer measurementId) {
        try {
            measurementDao.delete(measurementId);
            LOGGER.info("Ölçüm silindi. ID: " + measurementId);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ölçüm silinirken hata oluştu", e);
            return false;
        }
    }

    /**
     * Belirli bir ID'ye sahip ölçümü getirir.
     *
     * @param measurementId Ölçüm ID'si
     * @return Ölçüm nesnesi, bulunamazsa null
     */
    public BloodSugarMeasurement getMeasurementById(Integer measurementId) {
        try {
            return measurementDao.findById(measurementId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ölçüm bulunurken hata oluştu", e);
            return null;
        }
    }

    /**
     * Belirli bir hastaya ait tüm ölçümleri getirir.
     *
     * @param patientId Hasta ID'si
     * @return Ölçüm listesi
     */
    public List<BloodSugarMeasurement> getAllMeasurementsByPatient(Integer patientId) {
        try {
            return measurementDao.findByPatientId(patientId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Hasta ölçümleri listelenirken hata oluştu", e);
            return new ArrayList<>();
        }
    }

    /**
     * Belirli bir hastaya ait belirli bir tarih aralığındaki ölçümleri getirir.
     *
     * @param patientId Hasta ID'si
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Belirtilen tarih aralığındaki ölçüm listesi
     */
    public List<BloodSugarMeasurement> getMeasurementsByDateRange(Integer patientId, LocalDate startDate, LocalDate endDate) {
        try {
            return measurementDao.findByPatientIdAndDateRange(patientId, startDate, endDate);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Tarih aralığına göre ölçümler listelenirken hata oluştu", e);
            return new ArrayList<>();
        }
    }

    /**
     * Belirli bir hastaya ait belirli bir tarihteki ölçümleri getirir.
     *
     * @param patientId Hasta ID'si
     * @param date Tarih
     * @return Belirtilen tarihteki ölçüm listesi
     */
    public List<BloodSugarMeasurement> getMeasurementsByDate(Integer patientId, LocalDate date) {
        try {
            return measurementDao.findByPatientIdAndDate(patientId, date);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Tarihe göre ölçümler listelenirken hata oluştu", e);
            return new ArrayList<>();
        }
    }

    /**
     * Belirli bir hastaya ait belirli tipteki ölçümleri getirir.
     *
     * @param patientId Hasta ID'si
     * @param measurementType Ölçüm tipi (ör. MORNING, NOON, EVENING)
     * @return Belirtilen tipteki ölçüm listesi
     */
    public List<BloodSugarMeasurement> getMeasurementsByType(Integer patientId, String measurementType) {
        try {
            return measurementDao.findByPatientIdAndType(patientId, measurementType);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Tipe göre ölçümler listelenirken hata oluştu", e);
            return new ArrayList<>();
        }
    }

    /**
     * Belirli bir hastanın günlük ortalama kan şekeri değerlerini hesaplar.
     *
     * @param patientId Hasta ID'si
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Tarih -> Ortalama değer eşlemesi
     */
    public Map<LocalDate, BigDecimal> getDailyAverages(Integer patientId, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> dailyAverages = new HashMap<>();

        try {
            List<BloodSugarMeasurement> measurements = measurementDao.findByPatientIdAndDateRange(patientId, startDate, endDate);
            Map<LocalDate, List<BloodSugarMeasurement>> groupByDate = new HashMap<>();

            // Ölçümleri tarihe göre gruplandır
            for (BloodSugarMeasurement measurement : measurements) {
                LocalDate date = measurement.getMeasurementTime().toLocalDate();
                groupByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(measurement);
            }

            // Her tarih için ortalama hesapla
            for (Map.Entry<LocalDate, List<BloodSugarMeasurement>> entry : groupByDate.entrySet()) {
                LocalDate date = entry.getKey();
                List<BloodSugarMeasurement> dailyMeasurements = entry.getValue();

                BigDecimal total = BigDecimal.ZERO;
                for (BloodSugarMeasurement measurement : dailyMeasurements) {
                    total = total.add(measurement.getMeasurementValue());
                }

                BigDecimal average = total.divide(new BigDecimal(dailyMeasurements.size()), 2, RoundingMode.HALF_UP);
                dailyAverages.put(date, average);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Günlük ortalama hesaplanırken hata oluştu", e);
        }

        return dailyAverages;
    }

    /**
     * Belirli bir hastanın ölçüm tiplerine göre ortalama değerlerini hesaplar.
     *
     * @param patientId Hasta ID'si
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Ölçüm tipi -> Ortalama değer eşlemesi
     */
    public Map<String, BigDecimal> getAveragesByType(Integer patientId, LocalDate startDate, LocalDate endDate) {
        Map<String, BigDecimal> typeAverages = new HashMap<>();

        try {
            List<BloodSugarMeasurement> measurements = measurementDao.findByPatientIdAndDateRange(patientId, startDate, endDate);
            Map<String, List<BloodSugarMeasurement>> groupByType = new HashMap<>();

            // Ölçümleri tipine göre gruplandır
            for (BloodSugarMeasurement measurement : measurements) {
                String type = measurement.getMeasurementType();
                groupByType.computeIfAbsent(type, k -> new ArrayList<>()).add(measurement);
            }

            // Her tip için ortalama hesapla
            for (Map.Entry<String, List<BloodSugarMeasurement>> entry : groupByType.entrySet()) {
                String type = entry.getKey();
                List<BloodSugarMeasurement> typeMeasurements = entry.getValue();

                BigDecimal total = BigDecimal.ZERO;
                for (BloodSugarMeasurement measurement : typeMeasurements) {
                    total = total.add(measurement.getMeasurementValue());
                }

                BigDecimal average = total.divide(new BigDecimal(typeMeasurements.size()), 2, RoundingMode.HALF_UP);
                typeAverages.put(type, average);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Tip bazlı ortalama hesaplanırken hata oluştu", e);
        }

        return typeAverages;
    }

    /**
     * Hastanın belirli bir tarih aralığındaki genel ortalama kan şekeri değerini hesaplar.
     *
     * @param patientId Hasta ID'si
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Ortalama kan şekeri değeri
     */
    public BigDecimal getOverallAverage(Integer patientId, LocalDate startDate, LocalDate endDate) {
        try {
            List<BloodSugarMeasurement> measurements = measurementDao.findByPatientIdAndDateRange(patientId, startDate, endDate);

            if (measurements.isEmpty()) {
                return BigDecimal.ZERO;
            }

            BigDecimal total = BigDecimal.ZERO;
            for (BloodSugarMeasurement measurement : measurements) {
                total = total.add(measurement.getMeasurementValue());
            }

            return total.divide(new BigDecimal(measurements.size()), 2, RoundingMode.HALF_UP);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Genel ortalama hesaplanırken hata oluştu", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Hastanın kan şekeri durumunu değerlendirir.
     *
     * @param patientId Hasta ID'si
     * @param days Son kaç gün değerlendirilecek
     * @return Değerlendirme mesajı
     */
    public String evaluateBloodSugarStatus(Integer patientId, int days) {
        try {
            LocalDate endDate = DateTime.getCurrentDate();
            LocalDate startDate = endDate.minusDays(days);

            List<BloodSugarMeasurement> measurements = measurementDao.findByPatientIdAndDateRange(patientId, startDate, endDate);

            if (measurements.isEmpty()) {
                return "Değerlendirme için yeterli ölçüm bulunmamaktadır.";
            }

            int totalCount = measurements.size();
            int lowCount = 0;
            int normalCount = 0;
            int highCount = 0;
            int veryHighCount = 0;

            for (BloodSugarMeasurement measurement : measurements) {
                BigDecimal value = measurement.getMeasurementValue();

                if (value.compareTo(HYPOGLYCEMIA_THRESHOLD) < 0) {
                    lowCount++;
                } else if (value.compareTo(NORMAL_UPPER_THRESHOLD) <= 0) {
                    normalCount++;
                } else if (value.compareTo(HIGH_THRESHOLD) <= 0) {
                    highCount++;
                } else {
                    veryHighCount++;
                }
            }

            // Yüzdeleri hesapla
            double lowPercent = (double) lowCount / totalCount * 100;
            double normalPercent = (double) normalCount / totalCount * 100;
            double highPercent = (double) highCount / totalCount * 100;
            double veryHighPercent = (double) veryHighCount / totalCount * 100;

            // Değerlendirme mesajını oluştur
            StringBuilder report = new StringBuilder();
            report.append("Son ").append(days).append(" günlük kan şekeri değerlendirmesi:\n");
            report.append("Toplam ölçüm: ").append(totalCount).append("\n");
            report.append(String.format("Düşük (<%s mg/dL): %d (%.1f%%)\n", HYPOGLYCEMIA_THRESHOLD, lowCount, lowPercent));
            report.append(String.format("Normal (%s-%s mg/dL): %d (%.1f%%)\n",
                    HYPOGLYCEMIA_THRESHOLD, NORMAL_UPPER_THRESHOLD, normalCount, normalPercent));
            report.append(String.format("Yüksek (%s-%s mg/dL): %d (%.1f%%)\n",
                    NORMAL_UPPER_THRESHOLD, HIGH_THRESHOLD, highCount, highPercent));
            report.append(String.format("Çok Yüksek (>%s mg/dL): %d (%.1f%%)\n", HIGH_THRESHOLD, veryHighCount, veryHighPercent));

            // Genel durumu değerlendir
            if (normalPercent >= 70) {
                report.append("\nGenel Durum: İYİ - Kan şekeri değerleriniz çoğunlukla normal aralıkta.");
            } else if (normalPercent >= 50) {
                report.append("\nGenel Durum: ORTA - Kan şekeri değerleriniz kısmen kontrol altında, ancak iyileştirme gerekebilir.");
            } else {
                report.append("\nGenel Durum: DİKKAT - Kan şekeri değerleriniz çoğunlukla normal aralık dışında. Doktorunuzla görüşmeniz önerilir.");
            }

            return report.toString();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Kan şekeri durumu değerlendirilirken hata oluştu", e);
            return "Değerlendirme sırasında bir hata oluştu.";
        }
    }

    /**
     * Hastanın eksik ölçümlerini kontrol eder.
     *
     * @param patientId Hasta ID'si
     * @param date Kontrol edilecek tarih
     * @return Eksik ölçüm tipleri listesi
     */
    public List<String> checkMissingMeasurements(Integer patientId, LocalDate date) {
        List<String> missingTypes = new ArrayList<>();
        missingTypes.add("MORNING");
        missingTypes.add("NOON");
        missingTypes.add("EVENING");

        try {
            List<BloodSugarMeasurement> measurements = measurementDao.findByPatientIdAndDate(patientId, date);

            for (BloodSugarMeasurement measurement : measurements) {
                missingTypes.remove(measurement.getMeasurementType());
            }

            return missingTypes;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Eksik ölçümler kontrol edilirken hata oluştu", e);
            return missingTypes;
        }
    }

    /**
     * Belirli bir tarihte hastanın ölçüm sayısını döndürür.
     *
     * @param patientId Hasta ID'si
     * @param date Tarih
     * @return Ölçüm sayısı
     */
    public int getMeasurementCountByDate(Integer patientId, LocalDate date) {
        try {
            return measurementDao.countMeasurementsByPatientAndDate(patientId, date);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ölçüm sayısı hesaplanırken hata oluştu", e);
            return 0;
        }
    }

    /**
     * Hastanın son ölçümünü döndürür.
     *
     * @param patientId Hasta ID'si
     * @return Son ölçüm, yoksa null
     */
    public BloodSugarMeasurement getLastMeasurement(Integer patientId) {
        try {
            List<BloodSugarMeasurement> measurements = measurementDao.findByPatientId(patientId);

            if (measurements.isEmpty()) {
                return null;
            }

            // Ölçümler tarih ve zamana göre sıralı olduğundan, listenin ilk elemanı en son ölçümdür
            return measurements.get(0);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Son ölçüm alınırken hata oluştu", e);
            return null;
        }
    }

    /**
     * Belirli bir hastanın ölçüm uyumluluğunu kontrol eder.
     * Son 7 günde olması gereken ve gerçekleşen ölçüm oranını döndürür.
     *
     * @param patientId Hasta ID'si
     * @return Uyum yüzdesi (0-100)
     */
    public int checkComplianceRate(Integer patientId) {
        try {
            LocalDate today = DateTime.getCurrentDate();
            LocalDate startDate = today.minusDays(7);

            int expectedTotal = 21; // 7 gün x 3 ölçüm (sabah, öğle, akşam)
            int actualTotal = 0;

            for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
                actualTotal += measurementDao.countMeasurementsByPatientAndDate(patientId, date);
            }

            return Math.min(100, (int) (((double) actualTotal / expectedTotal) * 100));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Uyum oranı hesaplanırken hata oluştu", e);
            return 0;
        }
    }

    /**
     * Belirli bir hasta için ölçüm önerileri sunar.
     *
     * @param patientId Hasta ID'si
     * @return Öneriler metni
     */
    public String getNextMeasurementRecommendation(Integer patientId) {
        try {
            BloodSugarMeasurement lastMeasurement = getLastMeasurement(patientId);
            LocalDateTime now = DateTime.getCurrentDateTime();
            String currentPeriod = DateTime.getMeasurementPeriod(now.toLocalTime());
            String nextPeriod;

            if (lastMeasurement != null) {
                LocalDate lastDate = lastMeasurement.getMeasurementTime().toLocalDate();

                // Bugünün ölçümleri kontrol et
                if (lastDate.equals(now.toLocalDate())) {
                    List<String> missingTypes = checkMissingMeasurements(patientId, now.toLocalDate());

                    if (!missingTypes.isEmpty()) {
                        StringBuilder recommendation = new StringBuilder();
                        recommendation.append("Bugün için önerilen ölçümler:\n");

                        for (String type : missingTypes) {
                            recommendation.append("- ").append(getMeasurementTypeDescription(type)).append("\n");
                        }

                        return recommendation.toString();
                    } else {
                        return "Bugün için tüm gerekli ölçümlerinizi tamamladınız. Tebrikler!";
                    }
                }
            }

            // Mevcut zamana göre öneri
            if ("OTHER".equals(currentPeriod)) {
                // Bir sonraki ölçüm periyodu bulunamadı
                if (now.getHour() < 7) {
                    nextPeriod = "MORNING";
                } else if (now.getHour() < 12) {
                    nextPeriod = "NOON";
                } else if (now.getHour() < 18) {
                    nextPeriod = "EVENING";
                } else {
                    return "Bir sonraki ölçümünüzü yarın sabah (07:00-09:00) yapmanızı öneririz.";
                }

                return "Bir sonraki ölçümünüzü " + getMeasurementTypeDescription(nextPeriod) +
                        " saatleri arasında yapmanızı öneririz.";
            } else {
                return "Şu an ölçüm yapmanız için uygun bir zaman: " + getMeasurementTypeDescription(currentPeriod);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ölçüm önerisi oluşturulurken hata oluştu", e);
            return "Ölçüm önerisi oluşturulamadı.";
        }
    }

    /**
     * Ölçüm tipi için açıklama döndürür.
     *
     * @param measurementType Ölçüm tipi
     * @return Açıklama
     */
    private String getMeasurementTypeDescription(String measurementType) {
        switch (measurementType) {
            case "MORNING":
                return "Sabah (07:00-09:00)";
            case "NOON":
                return "Öğle (12:00-14:00)";
            case "AFTERNOON":
                return "İkindi (15:00-17:00)";
            case "EVENING":
                return "Akşam (18:00-20:00)";
            case "NIGHT":
                return "Gece (22:00-00:00)";
            default:
                return "Diğer";
        }
    }
}