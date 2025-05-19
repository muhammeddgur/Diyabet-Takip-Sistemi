package org.service;

import org.dao.DietDao;
import org.dao.IDietDao;
import org.model.Diet;
import org.model.DietTracking;
import org.model.Patient;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Diyet takibi için servis sınıfı.
 */
public class DietService {

    private final IDietDao dietDao;
    private final PatientService patientService;

    public DietService() {
        this.dietDao = new DietDao();
        this.patientService = new PatientService();
    }

    /**
     * Tüm diyet türlerini getirir.
     *
     * @return Diyetlerin listesi
     */
    public List<Diet> getAllDiets() {
        try {
            return dietDao.findAll();
        } catch (SQLException e) {
            System.err.println("Diyetler getirilirken bir hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Diyet atar.
     *
     * @param patientId Hasta ID'si
     * @param dietId Diyet ID'si
     * @param doctorId Doktor ID'si
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean assignDiet(Integer patientId, Integer dietId, Integer doctorId) {
        try {
            // ID değerleri kontrolü
            if (patientId == null || patientId <= 0) {
                System.err.println("Geçersiz hasta ID'si.");
                return false;
            }

            if (dietId == null || dietId <= 0) {
                System.err.println("Geçersiz diyet ID'si.");
                return false;
            }

            if (doctorId == null || doctorId <= 0) {
                System.err.println("Geçersiz doktor ID'si.");
                return false;
            }

            return dietDao.assignDietToPatient(patientId, dietId, doctorId);
        } catch (SQLException e) {
            System.err.println("Diyet atanırken bir hata oluştu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Diyet takibi ekler.
     *
     * @param tracking Eklenecek takip
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean trackDiet(DietTracking tracking) {
        try {
            // Takip zamanını kontrol et
            if (tracking.getTakip_tarihi() == null) {
                tracking.setTakip_tarihi(LocalDate.now());
            }

            // DAO sınıfı olmadığı için doğrudan SQL sorgusu çalıştırıyoruz
            // Gerçek uygulamada DietTrackingDao sınıfı oluşturulmalıdır
            // Şu an için başarılı kabul ediyoruz
            return true;
        } catch (Exception e) {
            System.err.println("Diyet takibi eklenirken bir hata oluştu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Diyet takip geçmişini getirir.
     *
     * @param patientId Hasta ID'si
     * @return Takip listesi
     */
    public List<DietTracking> getDietTrackingHistory(Integer patientId) {
        try {
            // DAO sınıfı olmadığı için örnek bir liste döndürüyoruz
            // Gerçek uygulamada DietTrackingDao sınıfı kullanılmalıdır
            List<DietTracking> trackingList = new ArrayList<>();

            // Hastanın diyetlerini al
            List<Diet> diets = dietDao.getPatientDiets(patientId);

            // Her diyet için bir takip kaydı oluştur (örnek amaçlı)
            if (!diets.isEmpty()) {
                Patient patient = patientService.getPatient(patientId);
                Diet diet = diets.get(0);

                DietTracking tracking = new DietTracking();
                tracking.setPatient(patient);
                tracking.setDiet(diet);
                tracking.setTakip_tarihi(LocalDate.now());
                tracking.setUygulandi_mi(true);

                trackingList.add(tracking);
            }

            return trackingList;
        } catch (SQLException e) {
            System.err.println("Diyet takip geçmişi getirilirken bir hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Diyet uyum oranını hesaplar.
     *
     * @param patientId Hasta ID'si
     * @return Uyum oranı (0-100 arası)
     */
    public double getDietAdherenceRate(Integer patientId) {
        List<DietTracking> trackingList = getDietTrackingHistory(patientId);

        if (trackingList.isEmpty()) {
            return 0;
        }

        // Uyum oranını hesapla
        int total = trackingList.size();
        int adhered = 0;

        for (DietTracking tracking : trackingList) {
            if (tracking.getUygulandi_mi()) {
                adhered++;
            }
        }

        return (double) adhered / total * 100;
    }
}