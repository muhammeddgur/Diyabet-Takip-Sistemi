package org.util;

import org.mindrot.jbcrypt.BCrypt;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Şifre işlemlerini yönetmek için yardımcı sınıf.
 */
public class PasswordUtil {
    private static final Logger LOGGER = Logger.getLogger(PasswordUtil.class.getName());
    private static final int WORK_FACTOR = 12; // BCrypt için iş faktörü (zorluk seviyesi)

    private PasswordUtil() {
        // Örnekleme önlemek için private constructor
    }

    /**
     * Şifreyi hashler.
     *
     * @param password Hashlenecek şifre
     * @return Hashlenmiş şifre
     * @throws NoSuchAlgorithmException Algoritma bulunamazsa
     */
    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        try {
            return BCrypt.hashpw(password, BCrypt.gensalt(WORK_FACTOR));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Şifre hashleme hatası", e);
            throw new NoSuchAlgorithmException("Şifreleme algoritması bulunamadı veya hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Şifreyi doğrular.
     *
     * @param password Kontrol edilecek şifre
     * @param hashedPassword Hashlenmiş şifre
     * @return Eşleşme durumu
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Şifre doğrulama hatası: Geçersiz hash formatı", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Şifre doğrulama hatası", e);
            return false;
        }
    }

    /**
     * Rastgele şifre oluşturur.
     *
     * @param length Şifre uzunluğu
     * @return Oluşturulan şifre
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            length = 8;
        }

        String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerChars = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()_-+=<>?";
        String allChars = upperChars + lowerChars + numbers + specialChars;

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        // En az bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter ekleyelim
        sb.append(upperChars.charAt(random.nextInt(upperChars.length())));
        sb.append(lowerChars.charAt(random.nextInt(lowerChars.length())));
        sb.append(numbers.charAt(random.nextInt(numbers.length())));
        sb.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Geri kalan karakterleri rastgele ekleyelim
        for (int i = 4; i < length; i++) {
            int randomIndex = random.nextInt(allChars.length());
            sb.append(allChars.charAt(randomIndex));
        }

        // Karakterleri karıştıralım
        char[] password = sb.toString().toCharArray();
        for (int i = password.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = password[index];
            password[index] = password[i];
            password[i] = temp;
        }

        return new String(password);
    }

    /**
     * Şifrenin güçlülüğünü kontrol eder.
     *
     * @param password Kontrol edilecek şifre
     * @return Güçlülük puanı (0-4)
     */
    public static int checkPasswordStrength(String password) {
        int score = 0;

        if (password.length() >= 8) {
            score++;
        }

        if (password.matches(".*[A-Z].*")) {
            score++;
        }

        if (password.matches(".*[a-z].*") && password.matches(".*[0-9].*")) {
            score++;
        }

        if (password.matches(".*[!@#$%^&*()_\\-+=<>?].*")) {
            score++;
        }

        return score;
    }
}