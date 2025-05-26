package org.service;

import org.model.Alert;
import org.model.BloodSugarMeasurement;
import org.model.User;
import org.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * E-posta bildirimleri için servis sınıfı.
 * Gmail uygulama şifresi kullanarak kimlik doğrulama yapar.
 */
public class NotificationService {

    // E-posta gönderen hesap bilgileri
    private static final String SENDER_EMAIL = "diyabettakip20@gmail.com";
    private static final String APP_PASSWORD = "gdqi lgjo souh gyir";

    // SMTP sunucu ayarları
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    /**
     * E-posta gönderir.
     *
     * @param to Alıcı e-posta adresi
     * @param subject Konu
     * @param body İçerik
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean sendEmail(String to, String subject, String body) {
        try {
            // E-posta formatını doğrula
            if (!ValidationUtil.validateEmail(to)) {
                System.err.println("Geçersiz alıcı e-posta formatı.");
                return false;
            }

            if (subject == null || subject.trim().isEmpty()) {
                System.err.println("E-posta konusu boş olamaz.");
                return false;
            }

            if (body == null || body.trim().isEmpty()) {
                System.err.println("E-posta içeriği boş olamaz.");
                return false;
            }

            // E-posta sunucusu ayarları
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", SMTP_HOST);
            properties.put("mail.smtp.port", SMTP_PORT);

            // Oturum oluştur - Uygulama şifresi ile kimlik doğrulama yap
            Session session = Session.getInstance(properties, new jakarta.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
                }
            });

            // Mesaj oluştur
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            // Mesajı gönder
            Transport.send(message);

            System.out.println("E-posta gönderildi: " + to + ", Konu: " + subject);
            return true;
        } catch (MessagingException e) {
            System.err.println("E-posta gönderilirken bir hata oluştu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Uyarı bildirimi gönderir.
     *
     * @param alert Gönderilecek uyarı
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean sendAlertNotification(Alert alert) {
        // Doktora e-posta gönder
        String subject = "Diyabet Takip Sistemi - " + alert.getAlertType().getTip_adi();

        String body = "Sayın Dr. " + alert.getDoctor().getAd() + " " + alert.getDoctor().getSoyad() + ",\n\n" +
                "Hastanız " + alert.getPatient().getAd() + " " + alert.getPatient().getSoyad() +
                " için bir uyarı oluşturuldu:\n\n" +
                alert.getMesaj() + "\n\n" +
                "Lütfen sisteme giriş yaparak hastanızın durumunu kontrol edin.";

        return sendEmail(alert.getDoctor().getEmail(), subject, body);
    }
}