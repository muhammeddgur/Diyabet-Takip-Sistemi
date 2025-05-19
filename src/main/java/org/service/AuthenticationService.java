package org.service;

import org.dao.IUserDao;
import org.dao.UserDao;
import org.model.User;
import org.util.PasswordUtil;
import org.util.ValidationUtil;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kimlik doğrulama ve kullanıcı yönetimi işlemlerini yürüten servis.
 */
public class AuthenticationService {

    private final IUserDao userDao;
    private final NotificationService notificationService;

    public AuthenticationService() {
        this.userDao = new UserDao();
        this.notificationService = new NotificationService();
    }

    /**
     * Kullanıcı girişi yapar.
     *
     * @param tcKimlik TC kimlik numarası
     * @param password Şifre
     * @return Giriş başarılı ise kullanıcı nesnesi, değilse null
     */
    public User login(String tcKimlik, String password) {
        try {
            // Önce kullanıcıyı TC kimlik numarasına göre bulalım
            User user = userDao.findByTcKimlik(tcKimlik);

            if (user == null) {
                System.out.println("TC Kimlik numarası ile kullanıcı bulunamadı: " + tcKimlik);
                return null;
            }

            // ✅ DOĞRU YAKLAŞIM: Girilen şifreyi ve veritabanındaki hash'i karşılaştırma
            boolean passwordMatches = PasswordUtil.verifyPassword(password, user.getPassword());

            if (!passwordMatches) {
                System.out.println("Şifre eşleşmiyor: " + tcKimlik);
                return null;
            }

            // Son giriş zamanını güncelle
            user.setLast_login(LocalDateTime.now());
            userDao.updateLastLogin(user.getUser_id());

            return user;
        } catch (SQLException e) {
            System.err.println("Giriş işlemi sırasında bir hata oluştu: " + e.getMessage());
            return null;
        }
    }

    /**
     * Kullanıcı çıkışı yapar.
     *
     * @param userId Kullanıcı ID'si
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean logout(Integer userId) {
        // Bu metod şu an için bir şey yapmıyor
        // Gerçek uygulamada oturum yönetimi burada yapılabilir
        return true;
    }

    /**
     * Yeni kullanıcı kaydı yapar.
     *
     * @param user Kaydedilecek kullanıcı bilgileri
     * @return Kayıt başarılı ise kullanıcı nesnesi, değilse null
     */
    public User register(User user) {
        try {
            // TC kimlik formatını doğrula
            if (!ValidationUtil.validateTcKimlik(user.getTc_kimlik())) {
                System.err.println("Geçersiz TC kimlik numarası formatı.");
                return null;
            }

            // Ad ve soyad doğrulaması
            if (!ValidationUtil.validateName(user.getAd()) || !ValidationUtil.validateName(user.getSoyad())) {
                System.err.println("Ad veya soyad geçersiz format içeriyor.");
                return null;
            }

            // E-posta formatını doğrula
            if (!ValidationUtil.validateEmail(user.getEmail())) {
                System.err.println("Geçersiz e-posta formatı.");
                return null;
            }

            // Şifre karmaşıklığını doğrula
            if (!ValidationUtil.validatePassword(user.getPassword())) {
                System.err.println("Şifre gereksinimleri karşılanmıyor (en az 8 karakter, büyük/küçük harf, rakam ve özel karakter).");
                return null;
            }

            // Kullanıcının daha önce kayıtlı olup olmadığını kontrol et
            User existingUser = userDao.findByTcKimlik(user.getTc_kimlik());
            if (existingUser != null) {
                System.err.println("Bu TC kimlik numarası ile kayıtlı bir kullanıcı zaten var.");
                return null;
            }

            existingUser = userDao.findByEmail(user.getEmail());
            if (existingUser != null) {
                System.err.println("Bu e-posta adresi ile kayıtlı bir kullanıcı zaten var.");
                return null;
            }

            // Şifreyi hashle
            String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);

            // Oluşturulma zamanını ayarla
            user.setCreated_at(LocalDateTime.now());

            // Kullanıcıyı kaydet
            boolean saved = userDao.save(user);

            if (saved) {
                // Hoş geldin e-postası gönder
                notificationService.sendEmail(
                        user.getEmail(),
                        "Diyabet Takip Sistemine Hoş Geldiniz",
                        "Sayın " + user.getAd() + " " + user.getSoyad() + ",\n\n" +
                                "Diyabet Takip Sistemine kaydınız başarıyla gerçekleştirilmiştir. " +
                                "Sisteme TC kimlik numaranız ve şifreniz ile giriş yapabilirsiniz."
                );

                return user;
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            System.err.println("Kayıt işlemi sırasında bir hata oluştu: " + e.getMessage());
        }

        return null;
    }

    /**
     * Şifre sıfırlama işlemi başlatır.
     *
     * @param email E-posta adresi
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean resetPassword(String email) {
        try {
            // E-posta formatını doğrula
            if (!ValidationUtil.validateEmail(email)) {
                System.err.println("Geçersiz e-posta formatı.");
                return false;
            }

            // E-posta adresine sahip kullanıcıyı bul
            User user = userDao.findByEmail(email);

            if (user == null) {
                System.err.println("Bu e-posta adresiyle kayıtlı kullanıcı bulunamadı.");
                return false;
            }

            // Geçici bir şifre oluştur
            String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);

            // Şifreyi hashle
            String hashedPassword = PasswordUtil.hashPassword(temporaryPassword);
            user.setPassword(hashedPassword);

            // Kullanıcıyı güncelle
            boolean updated = userDao.save(user);

            if (updated) {
                // Geçici şifre bilgisini e-posta ile gönder
                notificationService.sendEmail(
                        user.getEmail(),
                        "Diyabet Takip Sistemi - Şifre Sıfırlama",
                        "Sayın " + user.getAd() + " " + user.getSoyad() + ",\n\n" +
                                "Şifre sıfırlama talebiniz alınmıştır. Geçici şifreniz: " + temporaryPassword + "\n" +
                                "Bu şifre ile giriş yaptıktan sonra lütfen şifrenizi değiştirin."
                );

                return true;
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            System.err.println("Şifre sıfırlama işlemi sırasında bir hata oluştu: " + e.getMessage());
        }

        return false;
    }

    /**
     * Kullanıcı şifresini değiştirir.
     *
     * @param userId Kullanıcı ID'si
     * @param oldPassword Eski şifre
     * @param newPassword Yeni şifre
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        try {
            // Kullanıcıyı bul
            User user = userDao.findById(userId);

            if (user == null) {
                System.err.println("Kullanıcı bulunamadı.");
                return false;
            }

            // Eski şifreyi doğrula
            String hashedOldPassword = PasswordUtil.hashPassword(oldPassword);
            if (!user.getPassword().equals(hashedOldPassword)) {
                System.err.println("Eski şifre yanlış.");
                return false;
            }

            // Yeni şifre formatını doğrula
            if (!ValidationUtil.validatePassword(newPassword)) {
                System.err.println("Yeni şifre gereksinimleri karşılamıyor (en az 8 karakter, büyük/küçük harf, rakam ve özel karakter).");
                return false;
            }

            // Yeni şifreyi hashle
            String hashedNewPassword = PasswordUtil.hashPassword(newPassword);
            user.setPassword(hashedNewPassword);

            // Kullanıcıyı güncelle
            boolean updated = userDao.save(user);

            if (updated) {
                // Şifre değişikliği bilgisini e-posta ile gönder
                notificationService.sendEmail(
                        user.getEmail(),
                        "Diyabet Takip Sistemi - Şifre Değişikliği",
                        "Sayın " + user.getAd() + " " + user.getSoyad() + ",\n\n" +
                                "Şifreniz başarıyla değiştirilmiştir."
                );

                return true;
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            System.err.println("Şifre değiştirme işlemi sırasında bir hata oluştu: " + e.getMessage());
        }

        return false;
    }
}