package org.util;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Password {
    private static final Logger LOGGER = Logger.getLogger(Password.class.getName());
    private static final int WORK_FACTOR = 12; // BCrypt için iş faktörü (zorluk seviyesi)

    private Password() {
        // Private constructor to prevent instantiation
    }


    public static String hashPassword(String password) {
        try {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(WORK_FACTOR));
            return hashedPassword;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Şifre hashleme hatası", e);
            throw new RuntimeException("Şifreleme hatası", e);
        }
    }


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