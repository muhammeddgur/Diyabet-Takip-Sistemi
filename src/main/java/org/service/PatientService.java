package org.service;

import org.dao.DoctorDao;
import org.dao.PatientDao;
import org.dao.UserDao;
import org.dao.IDoctorDao;
import org.dao.IPatientDao;
import org.dao.IUserDao;
import org.model.Doctor;
import org.model.Patient;
import org.model.User;
import org.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Hasta işlemleri için servis sınıfı.
 */
public class PatientService {

    private final IPatientDao patientDao;
    private final IDoctorDao doctorDao;
    private final IUserDao userDao;
    private final NotificationService notificationService;

    public PatientService() {
        this.patientDao = new PatientDao();
        this.doctorDao = new DoctorDao();
        this.userDao = new UserDao();
        this.notificationService = new NotificationService();
    }

    /**
     * Hasta bilgisini getirir.
     *
     * @param patientId Hasta ID'si
     * @return Hasta bilgisi veya null
     */
    public Patient getPatient(Integer patientId) {
        try {
            return patientDao.findById(patientId);
        } catch (SQLException e) {
            System.err.println("Hasta bilgisi getirme sırasında bir hata oluştu: " + e.getMessage());
            return null;
        }
    }

    /**
     * Yeni hasta ekler.
     *
     * @param patient Eklenecek hasta bilgileri
     * @return İşlem başarılı ise eklenen hasta, değilse null
     */
    public Patient addPatient(Patient patient) {
        try {
            // TC kimlik formatını doğrula
            if (!ValidationUtil.validateTcKimlik(patient.getTc_kimlik())) {
                System.err.println("Geçersiz TC kimlik numarası formatı.");
                return null;
            }

            // Ad ve soyad doğrulaması
            if (!ValidationUtil.validateName(patient.getAd()) || !ValidationUtil.validateName(patient.getSoyad())) {
                System.err.println("Ad veya soyad geçersiz format içeriyor.");
                return null;
            }

            if(!ValidationUtil.validatePassword(patient.getPassword())) {
                System.err.println("Şifre minimum 8 karakter, Büyük-küçük harf, rakam ve özel karakter içermelidir.");
                return null;
            }

            // E-posta formatını doğrula
            if (!ValidationUtil.validateEmail(patient.getEmail())) {
                System.err.println("Geçersiz e-posta formatı.");
                return null;
            }

            // Kullanıcı daha önce kaydedilmiş mi kontrol et
            User existingUser = userDao.findByTcKimlik(patient.getTc_kimlik());
            if (existingUser != null) {
                System.err.println("Bu TC kimlik numarası ile kayıtlı bir kullanıcı zaten var.");
                return null;
            }

            // Hastanın doktoru atanmış mı kontrol et
            if (patient.getDoctor() == null || patient.getDoctor().getDoctor_id() == null) {
                System.err.println("Hastanın bir doktoru olmalıdır.");
                return null;
            }

            // Oluşturulma zamanını ayarla
            patient.setCreated_at(LocalDateTime.now());
            patient.setKullanici_tipi("hasta");

            // Hastayı kaydet
            boolean saved = patientDao.save(patient);

            if (saved) {
                // Hoş geldin e-postası gönder
                notificationService.sendEmail(
                        patient.getEmail(),
                        "Diyabet Takip Sistemine Hoş Geldiniz",
                        "Sayın " + patient.getAd() + " " + patient.getSoyad() + ",\n\n" +
                                "Diyabet Takip Sistemine kaydınız başarıyla gerçekleştirilmiştir. " +
                                "Sisteme TC kimlik numaranız ve şifreniz ile giriş yapabilirsiniz."
                );

                return patient;
            }
        } catch (SQLException e) {
            System.err.println("Hasta ekleme sırasında bir hata oluştu: " + e.getMessage());
        }

        return null;
    }

    /**
     * Hasta bilgilerini günceller.
     *
     * @param patient Güncellenecek hasta bilgileri
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean updatePatient(Patient patient) {
        try {
            // Hasta var mı kontrol et
            Patient existingPatient = patientDao.findById(patient.getPatient_id());
            if (existingPatient == null) {
                System.err.println("Güncellenecek hasta bulunamadı.");
                return false;
            }

            // TC kimlik değişmişse yeni değeri doğrula
            if (!existingPatient.getTc_kimlik().equals(patient.getTc_kimlik()) &&
                    !ValidationUtil.validateTcKimlik(patient.getTc_kimlik())) {
                System.err.println("Geçersiz TC kimlik numarası formatı.");
                return false;
            }

            // Ad ve soyad doğrulaması
            if (!ValidationUtil.validateName(patient.getAd()) || !ValidationUtil.validateName(patient.getSoyad())) {
                System.err.println("Ad veya soyad geçersiz format içeriyor.");
                return false;
            }

            if(!ValidationUtil.validatePassword(patient.getPassword())) {
                System.err.println("Şifre minimum 8 karakter, Büyük-küçük harf, rakam ve özel karakter içermelidir.");
                return false;
            }

            // E-posta formatını doğrula
            if (!ValidationUtil.validateEmail(patient.getEmail())) {
                System.err.println("Geçersiz e-posta formatı.");
                return false;
            }

            // Hastayı güncelle
            return patientDao.save(patient);
        } catch (SQLException e) {
            System.err.println("Hasta güncelleme sırasında bir hata oluştu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hastaya doktor atar.
     *
     * @param patientId Hasta ID'si
     * @param doctorId Doktor ID'si
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean assignDoctor(Integer patientId, Integer doctorId) {
        try {
            // Hasta ve doktorun var olduğunu kontrol et
            Patient patient = patientDao.findById(patientId);
            Doctor doctor = doctorDao.findById(doctorId);

            if (patient == null) {
                System.err.println("Hasta bulunamadı.");
                return false;
            }

            if (doctor == null) {
                System.err.println("Doktor bulunamadı.");
                return false;
            }

            // Doktoru değiştir
            return patientDao.changeDoctor(patientId, doctorId);
        } catch (SQLException e) {
            System.err.println("Doktor atama sırasında bir hata oluştu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Doktorun hastalarını getirir.
     *
     * @param doctorId Doktor ID'si
     * @return Hastaların listesi
     */
    public List<Patient> getDoctorPatients(Integer doctorId) {
        try {
            return patientDao.findByDoctorId(doctorId);
        } catch (SQLException e) {
            System.err.println("Doktorun hastaları getirilirken bir hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}