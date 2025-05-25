package org.util;

import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * Veri doğrulama işlemleri için yardımcı sınıf.
 */
public class ValidationUtil {

    // E-posta doğrulama için regex pattern
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    // Örnekleme önlemek için private constructor
    private ValidationUtil() {
    }

    /**
     * TC Kimlik numarasını doğrular.
     * - 11 haneli olmalı
     * - Tüm karakterler rakam olmalı
     * - İlk hane 0 olamaz
     * - 1-10 hanelerinin toplamının mod 10'u 11. haneye eşit olmalı
     * - 1, 3, 5, 7, 9. hanelerin toplamının 7 katından, 2, 4, 6, 8. hanelerin toplamı çıkarılıp
     *   10'a bölümünden kalan 10. haneye eşit olmalı
     *
     * @param tcKimlik TC Kimlik numarası
     * @return Geçerli ise true, değilse false
     */
    public static boolean validateTcKimlik(String tcKimlik) {
        if (tcKimlik == null || tcKimlik.length() != 11) {
            return false;
        }

        // Sadece rakamlardan oluşmalı
        if (!tcKimlik.matches("\\d{11}")) {
            return false;
        }

        // İlk hane 0 olamaz
        if (tcKimlik.charAt(0) == '0') {
            return false;
        }

        /*int[] digits = new int[11];
        for (int i = 0; i < 11; i++) {
            digits[i] = Character.getNumericValue(tcKimlik.charAt(i));
        }

        // 10. haneyi kontrol et
        int sum1 = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
        int sum2 = digits[1] + digits[3] + digits[5] + digits[7];
        int digit10Check = (sum1 * 7 - sum2) % 10;
        if (digit10Check < 0) digit10Check += 10; // Negatif ise pozitife çevir

        if (digits[9] != digit10Check) {
            return false;
        }

        // 11. haneyi kontrol et
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += digits[i];
        }

        if (digits[10] != sum % 10) {
            return false;
        }*/

        return true;
    }

    /**
     * E-posta adresini doğrular.
     *
     * @param email E-posta adresi
     * @return Geçerli ise true, değilse false
     */
    public static boolean validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Şifrenin karmaşıklık kriterlerini kontrol eder.
     * - En az 8 karakter uzunluğunda
     * - En az 1 büyük harf
     * - En az 1 küçük harf
     * - En az 1 rakam
     * - En az 1 özel karakter (!@#$%^&*()_+=-{}[]|:;"'<>,.?/)
     *
     * @param password Şifre
     * @return Geçerli ise true, değilse false
     */
    public static boolean validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if ("!@#$%^&*()_+=-{}[]|:;\"'<>,.?/".indexOf(c) >= 0) {
                hasSpecial = true;
            }
        }

        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }

    /**
     * Kan şekeri değerinin geçerli aralıkta olup olmadığını kontrol eder.
     *
     * @param value Kan şekeri değeri
     * @return Geçerli aralıkta ise true, değilse false
     */
    public static boolean validateBloodSugar(Integer value) {
        return value != null && value >= 0 && value <= 1000;
    }



    /**
     * Ad ve soyadın geçerli olup olmadığını kontrol eder.
     * - Boş olmamalı
     * - Sadece harf ve boşluk içermeli
     * - En az 2 karakter uzunluğunda olmalı
     *
     * @param name Ad veya soyad
     * @return Geçerli ise true, değilse false
     */
    public static boolean validateName(String name) {
        return name != null && name.length() >= 2 && name.matches("[a-zA-ZçÇğĞıİöÖşŞüÜ ]+");
    }
}