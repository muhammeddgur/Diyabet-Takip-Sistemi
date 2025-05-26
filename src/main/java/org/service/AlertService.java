package org.service;

import org.dao.AlertDao;
import org.dao.AlertTypeDao;
import org.dao.IAlertDao;
import org.dao.IAlertTypeDao;
import org.model.Alert;
import org.model.AlertType;
import org.model.BloodSugarMeasurement;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Uyarı ve bildirimler için servis sınıfı.
 */
public class AlertService {

    private final IAlertDao alertDao;
    private final IAlertTypeDao alertTypeDao;
    private final NotificationService notificationService;

    public AlertService() {
        this.alertDao = new AlertDao();
        this.alertTypeDao = new AlertTypeDao();
        this.notificationService = new NotificationService();
    }

    /**
     * Yeni uyarı oluşturur.
     *
     * @param alert Oluşturulacak uyarı
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean createAlert(Alert alert) {
        try {
            // Oluşturulma zamanını ayarla
            if (alert.getOlusturma_zamani() == null) {
                alert.setOlusturma_zamani(LocalDateTime.now());
            }

            // Uyarıyı kaydet
            boolean saved = alertDao.save(alert);

            if (saved) {
                // Uyarı bildirimini gönder
                notificationService.sendAlertNotification(alert);
                return true;
            }

            return false;
        } catch (SQLException e) {
            System.err.println("Uyarı oluşturulurken bir hata oluştu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hasta uyarılarını getirir.
     *
     * @param patientId Hasta ID'si
     * @return Uyarıların listesi
     */
    public List<Alert> getPatientAlerts(Integer patientId) {
        try {
            return alertDao.findByPatientId(patientId);
        } catch (SQLException e) {
            System.err.println("Hasta uyarıları getirilirken bir hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Doktor uyarılarını getirir.
     *
     * @param doctorId Doktor ID'si
     * @return Uyarıların listesi
     */
    public List<Alert> getDoctorAlerts(Integer doctorId) {
        try {
            return alertDao.findByDoctorId(doctorId);
        } catch (SQLException e) {
            System.err.println("Doktor uyarıları getirilirken bir hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Kan şekeri kontrolü yapar.
     *
     * @param measurement Kontrol edilecek ölçüm
     */
    public void checkBloodSugar(BloodSugarMeasurement measurement) {
        try {
            // Yüksek kan şekeri için uyarı tipi bul
            AlertType alertType = null;

            if (measurement.getOlcum_degeri() > 200) {
                alertType = alertTypeDao.findByName("Acil Müdahale Uyarısı");
            } else if (measurement.getOlcum_degeri() > 150) {
                alertType = alertTypeDao.findByName("İzleme Uyarısı");
            } else if (measurement.getOlcum_degeri() > 110) {
                alertType = alertTypeDao.findByName("Takip Uyarısı");
            } else if (measurement.getOlcum_degeri() < 70) {
                alertType = alertTypeDao.findByName("Acil Uyarı");
            }

            if (alertType == null) {
                return;
            }

            // Uyarı oluştur
            Alert alert = new Alert();
            alert.setPatient(measurement.getPatient());
            alert.setDoctor(measurement.getPatient().getDoctor());
            alert.setAlertType(alertType);
            alert.setMesaj(alertType.getAciklama() +
                    "Değer: " + measurement.getOlcum_degeri() + " mg/dL. " +
                    "Zaman: " + measurement.getOlcum_tarihi());

            // Uyarıyı kaydet
            createAlert(alert);
        } catch (SQLException e) {
            System.err.println("Kan şekeri kontrolü sırasında bir hata oluştu: " + e.getMessage());
        }
    }
}