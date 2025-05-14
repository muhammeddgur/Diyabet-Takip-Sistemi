package org.service;

import org.dao.DoctorDao;
import org.dao.PatientDao;
import org.dao.UserDao;
import org.model.Doctor;
import org.model.Patient;
import org.model.User;
import org.util.DateTime;
import org.util.Password;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): 2025-05-14 16:07:56
 * Current User's Login: Emirhan-Karabulut
 *
 * Kullanıcı kimlik doğrulama ve oturum yönetimi için servis sınıfı.
 */
public class AuthenticationService {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());

    private final UserDao userDao;
    private final DoctorDao doctorDao;
    private final PatientDao patientDao;

    // Aktif oturum bilgilerini tutma
    private User currentUser;
    private Doctor currentDoctor;
    private Patient currentPatient;
    private LocalDateTime loginTime;

    public AuthenticationService() {
        this.userDao = new UserDao();
        this.doctorDao = new DoctorDao();
        this.patientDao = new PatientDao();
    }

    /**
     * TC kimlik numarası ve şifre ile kullanıcı girişi yapar.
     *
     * @param tcIdentity TC kimlik numarası
     * @param password Şifre
     * @return Giriş başarılıysa true, değilse false
     */
    public boolean login(String tcIdentity, String password) {
        try {
            // Kullanıcıyı TC kimlik numarasına göre bul
            User user = userDao.findByTcIdentity(tcIdentity);

            if (user == null) {
                LOGGER.info("Kullanıcı bulunamadı: " + tcIdentity);
                return false;
            }

            // Şifreyi doğrula
            if (!Password.verifyPassword(password, user.getPassword())) {
                LOGGER.info("Şifre doğrulama başarısız: " + tcIdentity);
                return false;
            }

            // Giriş başarılı, oturum bilgilerini sakla
            this.currentUser = user;
            this.loginTime = DateTime.getCurrentDateTime();

            // Kullanıcının doktor veya hasta bilgilerini de yükle
            if ("DOCTOR".equals(user.getUserType())) {
                this.currentDoctor = doctorDao.findByUserId(user.getUserId());
            } else if ("PATIENT".equals(user.getUserType())) {
                this.currentPatient = patientDao.findByUserId(user.getUserId());
            }

            LOGGER.info("Kullanıcı girişi başarılı: " + tcIdentity);
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Giriş işlemi sırasında veritabanı hatası", e);
            return false;
        }
    }

    /**
     * Kullanıcı çıkışı yapar ve oturum bilgilerini temizler.
     */
    public void logout() {
        this.currentUser = null;
        this.currentDoctor = null;
        this.currentPatient = null;
        this.loginTime = null;
        LOGGER.info("Kullanıcı çıkışı yapıldı");
    }

    /**
     * Kullanıcının şifresini değiştirir.
     *
     * @param userId Kullanıcı ID
     * @param oldPassword Eski şifre
     * @param newPassword Yeni şifre
     * @return İşlem başarılıysa true, değilse false
     */
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        try {
            User user = userDao.findById(userId);

            if (user == null) {
                LOGGER.info("Şifre değiştirme işlemi için kullanıcı bulunamadı: " + userId);
                return false;
            }

            // Eski şifreyi doğrula
            if (!Password.verifyPassword(oldPassword, user.getPassword())) {
                LOGGER.info("Şifre değiştirme işlemi için eski şifre doğrulanamadı: " + userId);
                return false;
            }

            // Yeni şifreyi güvenlik kontrolü
            int passwordStrength = Password.checkPasswordStrength(newPassword);
            if (passwordStrength < 3) {
                LOGGER.info("Şifre değiştirme işlemi için yeni şifre yeterince güçlü değil: " + userId);
                return false;
            }

            // Şifreyi değiştir
            userDao.changePassword(userId, newPassword);
            LOGGER.info("Şifre değiştirme işlemi başarılı: " + userId);
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Şifre değiştirme sırasında veritabanı hatası", e);
            return false;
        }
    }

    /**
     * Şifremi unuttum işlemi için yeni bir rastgele şifre oluşturur.
     * Gerçek bir uygulamada e-posta gönderme işlemi de yapılmalıdır.
     *
     * @param tcIdentity TC kimlik numarası
     * @param email E-posta adresi (doğrulama için)
     * @return Başarılıysa yeni şifre, başarısızsa null
     */
    public String resetPassword(String tcIdentity, String email) {
        try {
            User user = userDao.findByTcIdentity(tcIdentity);

            if (user == null || !user.getEmail().equals(email)) {
                LOGGER.info("Şifre sıfırlama için kullanıcı bulunamadı veya e-posta eşleşmedi: " + tcIdentity);
                return null;
            }

            // Yeni bir rastgele şifre oluştur
            String newPassword = Password.generateRandomPassword(10);

            // Şifreyi güncelle
            userDao.changePassword(user.getUserId(), newPassword);

            // Gerçek bir uygulamada burada e-posta gönderme işlemi yapılmalıdır
            LOGGER.info("Şifre sıfırlama başarılı, e-posta gönderildi: " + tcIdentity);

            return newPassword;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Şifre sıfırlama sırasında veritabanı hatası", e);
            return null;
        }
    }

    /**
     * Mevcut oturum açmış kullanıcıyı döndürür.
     *
     * @return Oturum açmış kullanıcı, yoksa null
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Mevcut oturum açmış doktoru döndürür.
     *
     * @return Oturum açmış doktor, yoksa null
     */
    public Doctor getCurrentDoctor() {
        return currentDoctor;
    }

    /**
     * Mevcut oturum açmış hastayı döndürür.
     *
     * @return Oturum açmış hasta, yoksa null
     */
    public Patient getCurrentPatient() {
        return currentPatient;
    }

    /**
     * Giriş yapma zamanını döndürür.
     *
     * @return Giriş yapma zamanı, yoksa null
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * TC kimlik numarasına göre kullanıcıyı bulur.
     *
     * @param tcIdentity TC kimlik numarası
     * @return Bulunan kullanıcı, yoksa null
     */
    public User getUserByTcIdentity(String tcIdentity) {
        try {
            return userDao.findByTcIdentity(tcIdentity);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Kullanıcı arama sırasında veritabanı hatası", e);
            return null;
        }
    }

    /**
     * Kullanıcının bir oturumu olup olmadığını kontrol eder.
     *
     * @return Oturum varsa true, yoksa false
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Kullanıcının doktor olup olmadığını kontrol eder.
     *
     * @return Doktor ise true, değilse false
     */
    public boolean isDoctor() {
        return isLoggedIn() && "DOCTOR".equals(currentUser.getUserType());
    }

    /**
     * Kullanıcının hasta olup olmadığını kontrol eder.
     *
     * @return Hasta ise true, değilse false
     */
    public boolean isPatient() {
        return isLoggedIn() && "PATIENT".equals(currentUser.getUserType());
    }

    /**
     * Yeni doktor kullanıcısı oluşturur (yönetici yetkisi gerekir).
     * Doktor nesnesinin specialization, hospital ve licenseNumber alanları kaldırıldı.
     *
     * @param user Kullanıcı temel bilgileri
     * @param doctor Doktor bilgileri (sadece user_id içeriyor)
     * @return Oluşturulan doktorun ID'si, başarısızsa null
     */
    public Integer createDoctor(User user, Doctor doctor) {
        try {
            // Kullanıcı tipini DOCTOR olarak ayarla
            user.setUserType("DOCTOR");

            // Kullanıcıyı kaydet
            Integer userId = userDao.save(user);
            if (userId == null) {
                LOGGER.log(Level.SEVERE, "Doktor oluşturma: Kullanıcı kaydedilemedi");
                return null;
            }

            // Doktor nesnesine kullanıcı ID'sini ayarla
            doctor.setUserId(userId);

            // Doktoru kaydet
            Integer doctorId = doctorDao.save(doctor);

            if (doctorId == null) {
                LOGGER.log(Level.SEVERE, "Doktor oluşturma: Doktor kaydedilemedi");
                // Temizlik: Oluşturulan kullanıcıyı sil
                try {
                    userDao.delete(userId);
                } catch (SQLException cleanupEx) {
                    LOGGER.log(Level.SEVERE, "Temizlik işlemi başarısız: Kullanıcı silinemedi", cleanupEx);
                }
                return null;
            }

            LOGGER.info("Yeni doktor başarıyla oluşturuldu. UserID: " + userId + ", DoctorID: " + doctorId);
            return doctorId;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Doktor oluşturma sırasında veritabanı hatası", e);
            return null;
        }
    }

    /**
     * Yeni hasta kullanıcısı oluşturur (doktor yetkisi gerekir).
     *
     * @param user Kullanıcı temel bilgileri
     * @param patient Hasta bilgileri
     * @return Oluşturulan hastanın ID'si, başarısızsa null
     */
    public Integer createPatient(User user, Patient patient) {
        try {
            // Kullanıcı tipini PATIENT olarak ayarla
            user.setUserType("PATIENT");

            // Rastgele bir şifre oluştur
            String password = Password.generateRandomPassword(10);
            user.setPassword(password);

            // Kullanıcıyı kaydet
            Integer userId = userDao.save(user);
            if (userId == null) {
                return null;
            }

            // Hasta nesnesine kullanıcı ID'sini ayarla
            patient.setUserId(userId);

            // Hastayı kaydet
            Integer patientId = patientDao.save(patient);

            // Gerçek bir uygulamada burada hastanın e-posta adresine kullanıcı adı ve şifre gönderilir
            LOGGER.info("Yeni hasta oluşturuldu, giriş bilgileri e-posta ile gönderildi: " + user.getEmail());

            return patientId;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Hasta oluşturma sırasında veritabanı hatası", e);
            return null;
        }
    }
}