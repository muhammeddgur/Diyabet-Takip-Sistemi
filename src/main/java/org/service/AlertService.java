package org.service;

import org.dao.AlertDao;
import org.dao.MeasurementDao;
import org.dao.PatientDao;
import org.model.Alert;
import org.model.BloodSugarMeasurement;
import org.model.Patient;
import org.util.DateTime;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hasta uyarılarının yönetimi için servis sınıfı.
 * Uyarı oluşturma, listeleme, güncelleme, silme ve uyarı durumlarının
 * yönetimini sağlar.
 */
public class AlertService {
    private static final Logger LOGGER = Logger.getLogger(AlertService.class.getName());

    private final AlertDao alertDao;
    private final MeasurementDao measurementDao;
    private final PatientDao patientDao;

    // Kan şekeri seviyeleri için eşik değerleri
    private static final BigDecimal HYPOGLYCEMIA_THRESHOLD = new BigDecimal("70.0");
    private static final BigDecimal NORMAL_UPPER_THRESHOLD = new BigDecimal("110.0");
    private static final BigDecimal MILD_HIGH_THRESHOLD = new BigDecimal("150.0");
    private static final BigDecimal HIGH_THRESHOLD = new BigDecimal("180.0");
    private static final BigDecimal VERY_HIGH_THRESHOLD = new BigDecimal("200.0");

    public AlertService() {
        this.alertDao = new AlertDao();
        this.measurementDao = new MeasurementDao();
        this.patientDao = new PatientDao();
    }

    /**
     * Belirli bir uyarı ID'sine göre uyarıyı getirir.
     *
     * @param alertId Uyarı ID'si
     * @return Uyarı nesnesi, bulunamazsa null döner
     */
    public Alert getAlertById(Integer alertId) {
        try {
            return alertDao.findById(alertId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Uyarı bulunamadı: " + alertId, e);
            return null;
        }
    }

    /**
     * Tüm uyarıları listeler.
     *
     * @return Uyarı listesi
     */
    public List<Alert> getAllAlerts() {
        try {
            return alertDao.findAll();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Uyarılar listelenirken hata oluştu", e);
            return new ArrayList<>();
        }
    }

    /**
     * Belirli bir hastaya ait tüm uyarıları listeler.
     *
     * @param patientId Hasta ID'si
     * @return Hastaya ait uyarı listesi
     */
    public List<Alert> getAlertsByPatientId(Integer patientId) {
        try {
            return alertDao.findByPatientId(patientId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Hasta uyarıları listelenirken hata oluştu: " + patientId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Belirli bir hastaya ait, belirli bir tarihteki uyarıları listeler.
     *
     * @param patientId Hasta ID'si
     * @param date Tarih
     * @return Hastaya ait belirli tarihteki uyarı listesi
     */
    public List<Alert> getAlertsByPatientIdAndDate(Integer patientId, LocalDate date) {
        try {
            return alertDao.findByPatientIdAndDate(patientId, date);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Hasta için tarih bazlı uyarılar listelenirken hata oluştu: " + patientId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Belirli bir hastaya ait okunmamış uyarıları listeler.
     *
     * @param patientId Hasta ID'si
     * @return Hastaya ait okunmamış uyarı listesi
     */
    public List<Alert> getUnreadAlertsByPatientId(Integer patientId) {
        try {
            return alertDao.findUnreadByPatientId(patientId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Hasta için okunmamış uyarılar listelenirken hata oluştu: " + patientId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Belirli bir hastaya ait acil uyarıları listeler.
     *
     * @param patientId Hasta ID'si
     * @return Hastaya ait acil uyarı listesi
     */
    public List<Alert> getUrgentAlertsByPatientId(Integer patientId) {
        try {
            return alertDao.findUrgentByPatientId(patientId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Hasta için acil uyarılar listelenirken hata oluştu: " + patientId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Belirli bir uyarı tipine göre uyarıları listeler.
     *
     * @param alertType Uyarı tipi
     * @return Belirli tipteki uyarı listesi
     */
    public List<Alert> getAlertsByType(String alertType) {
        try {
            return alertDao.findByAlertType(alertType);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Uyarı tipine göre listeleme hatası: " + alertType, e);
            return new ArrayList<>();
        }
    }

    /**
     * Yeni bir uyarı oluşturur.
     *
     * @param alert Oluşturulacak uyarı
     * @return Oluşturulan uyarının ID'si, başarısız olursa null
     */
    public Integer createAlert(Alert alert) {
        try {
            return alertDao.save(alert);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Uyarı oluşturma hatası", e);
            return null;
        }
    }

    /**
     * Mevcut bir uyarıyı günceller.
     *
     * @param alert Güncellenecek uyarı
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean updateAlert(Alert alert) {
        try {
            alertDao.update(alert);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Uyarı güncelleme hatası: " + alert.getAlertId(), e);
            return false;
        }
    }

    /**
     * Bir uyarıyı siler.
     *
     * @param alertId Silinecek uyarının ID'si
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean deleteAlert(Integer alertId) {
        try {
            alertDao.delete(alertId);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Uyarı silme hatası: " + alertId, e);
            return false;
        }
    }

    /**
     * Bir uyarıyı okundu olarak işaretler.
     *
     * @param alertId Okundu işaretlenecek uyarının ID'si
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean markAlertAsRead(Integer alertId) {
        try {
            alertDao.markAsRead(alertId);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Uyarı okundu işaretleme hatası: " + alertId, e);
            return false;
        }
    }

    /**
     * Bir hastanın tüm uyarılarını okundu olarak işaretler.
     *
     * @param patientId Uyarıları okundu işaretlenecek hastanın ID'si
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean markAllAlertsAsRead(Integer patientId) {
        try {
            alertDao.markAllAsRead(patientId);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Tüm uyarıları okundu işaretleme hatası (PatientID: " + patientId + ")", e);
            return false;
        }
    }

    /**
     * Ölçüm eksikliği uyarısı oluşturur.
     *
     * @param patientId Uyarı oluşturulacak hastanın ID'si
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean createMissingMeasurementAlert(Integer patientId) {
        try {
            alertDao.createMissingMeasurementAlert(patientId);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ölçüm eksikliği uyarısı oluşturma hatası: " + patientId, e);
            return false;
        }
    }

    /**
     * Yetersiz ölçüm uyarısı oluşturur.
     *
     * @param patientId Uyarı oluşturulacak hastanın ID'si
     * @param count Mevcut ölçüm sayısı
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean createInsufficientMeasurementAlert(Integer patientId, int count) {
        try {
            alertDao.createInsufficientMeasurementAlert(patientId, count);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Yetersiz ölçüm uyarısı oluşturma hatası: " + patientId, e);
            return false;
        }
    }

    /**
     * Kritik değer uyarısı oluşturur.
     *
     * @param patientId Uyarı oluşturulacak hastanın ID'si
     * @param value Kan şekeri değeri
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean createCriticalValueAlert(Integer patientId, double value) {
        try {
            alertDao.createCriticalValueAlert(patientId, value);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Kritik değer uyarısı oluşturma hatası: " + patientId, e);
            return false;
        }
    }

    /**
     * Yeni bir kan şekeri ölçümünü işler ve gerekirse uyarı oluşturur.
     *
     * @param measurement Yeni kan şekeri ölçümü
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean processBloodSugarMeasurement(BloodSugarMeasurement measurement) {
        try {
            BigDecimal value = measurement.getMeasurementValue();

            // Değere göre uyarı oluşturma
            if (value.compareTo(HYPOGLYCEMIA_THRESHOLD) < 0) {
                // Hipoglisemi uyarısı (< 70 mg/dL)
                Alert alert = new Alert();
                alert.setPatientId(measurement.getPatientId());
                alert.setAlertType("HYPOGLYCEMIA");
                alert.setAlertMessage("Hastanın kan şekeri seviyesi 70 mg/dL'nin altına düştü (" + value +
                        " mg/dL). Hipoglisemi riski! Hızlı müdahale gerekebilir.");
                alert.setRead(false);
                alert.setUrgent(true);
                alertDao.save(alert);
            } else if (value.compareTo(VERY_HIGH_THRESHOLD) > 0) {
                // Hiperglisemi uyarısı (> 200 mg/dL)
                Alert alert = new Alert();
                alert.setPatientId(measurement.getPatientId());
                alert.setAlertType("HYPERGLYCEMIA");
                alert.setAlertMessage("Hastanın kan şekeri 200 mg/dL'nin üzerinde (" + value +
                        " mg/dL). Hiperglisemi durumu. Acil müdahale gerekebilir.");
                alert.setRead(false);
                alert.setUrgent(true);
                alertDao.save(alert);
            } else if (value.compareTo(HIGH_THRESHOLD) > 0) {
                // Yüksek kan şekeri uyarısı (150-200 mg/dL)
                Alert alert = new Alert();
                alert.setPatientId(measurement.getPatientId());
                alert.setAlertType("HIGH_GLUCOSE");
                alert.setAlertMessage("Hastanın kan şekeri 180-200 mg/dL arasında (" + value +
                        " mg/dL). Kan şekeri yüksek. Kontrol gereklidir.");
                alert.setRead(false);
                alert.setUrgent(false);
                alertDao.save(alert);
            } else if (value.compareTo(MILD_HIGH_THRESHOLD) > 0) {
                // Hafif yüksek kan şekeri uyarısı (110-150 mg/dL)
                Alert alert = new Alert();
                alert.setPatientId(measurement.getPatientId());
                alert.setAlertType("MILD_HIGH_GLUCOSE");
                alert.setAlertMessage("Hastanın kan şekeri 150-180 mg/dL arasında (" + value +
                        " mg/dL). Kan şekeri hafif yüksek. Takip edilmelidir.");
                alert.setRead(false);
                alert.setUrgent(false);
                alertDao.save(alert);
            }

            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Kan şekeri ölçümü işlenirken hata oluştu", e);
            return false;
        }
    }

    /**
     * Tüm hastaların günlük ölçümlerini kontrol eder ve
     * eksik veya yetersiz ölçüm durumlarında uyarı oluşturur.
     * Bu metod genellikle günlük bir zamanlanmış görev olarak çalıştırılır.
     */
    public void checkDailyMeasurements() {
        try {
            LocalDate today = DateTime.getCurrentDate();
            List<Patient> allPatients = patientDao.findAll();

            for (Patient patient : allPatients) {
                int measurementCount = measurementDao.countMeasurementsByPatientAndDate(patient.getPatientId(), today);

                if (measurementCount == 0) {
                    // Hiç ölçüm yoksa
                    alertDao.createMissingMeasurementAlert(patient.getPatientId());
                } else if (measurementCount < 3) {
                    // 3'ten az ölçüm varsa
                    alertDao.createInsufficientMeasurementAlert(patient.getPatientId(), measurementCount);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Günlük ölçüm kontrolü sırasında hata oluştu", e);
        }
    }

    /**
     * Doktorlara gönderilecek, okunmamış acil uyarıların sayısını döndürür.
     * Bu metod genellikle doktor ekranında gösterilecek bildirimleri belirlemek için kullanılır.
     *
     * @param doctorId Doktor ID'si
     * @return Acil uyarı sayısı
     */
    public int getUnreadUrgentAlertCountForDoctor(Integer doctorId) {
        try {
            int count = 0;
            // Doktora ait tüm hastaları bul
            List<Patient> patients = patientDao.findByDoctorId(doctorId);

            // Her hasta için okunmamış acil uyarıları say
            for (Patient patient : patients) {
                List<Alert> urgentAlerts = alertDao.findUrgentByPatientId(patient.getPatientId());
                for (Alert alert : urgentAlerts) {
                    if (!alert.isRead()) {
                        count++;
                    }
                }
            }

            return count;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Doktor için acil uyarı sayısı hesaplanırken hata oluştu: " + doctorId, e);
            return 0;
        }
    }

    /**
     * Özel bir uyarı oluşturur.
     *
     * @param patientId Hasta ID'si
     * @param alertType Uyarı tipi
     * @param message Uyarı mesajı
     * @param isUrgent Acil mi?
     * @return Oluşturulan uyarının ID'si, başarısızsa null
     */
    public Integer createCustomAlert(Integer patientId, String alertType, String message, boolean isUrgent) {
        try {
            Alert alert = new Alert();
            alert.setPatientId(patientId);
            alert.setAlertType(alertType);
            alert.setAlertMessage(message);
            alert.setRead(false);
            alert.setUrgent(isUrgent);

            return alertDao.save(alert);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Özel uyarı oluşturma hatası", e);
            return null;
        }
    }
}