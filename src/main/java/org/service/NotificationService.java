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
            Transport.send(message); // Gerçek gönderim için bu satırı açın

            // Şu an için simülasyon yapıyoruz
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

    /**
     * Ölçüm hatırlatması gönderir.
     *
     * @param patient Hasta
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean sendMeasurementReminder(User patient) {
        String subject = "Diyabet Takip Sistemi - Ölçüm Hatırlatması";

        String body = "Sayın " + patient.getAd() + " " + patient.getSoyad() + ",\n\n" +
                "Bugün kan şekeri ölçümlerinizi yapmayı unutmayın.\n" +
                "Düzenli ölçümler, diyabet yönetiminiz için çok önemlidir.\n\n" +
                "Sağlıklı günler dileriz.";

        return sendEmail(patient.getEmail(), subject, body);
    }

    /**
     * Haftalık rapor gönderir.
     *
     * @param patientId Hasta ID'si
     * @return İşlem başarılı ise true, değilse false
     */
    public boolean sendWeeklyReport(Integer patientId) {
        try {
            // Hastanın bilgilerini ve ölçümlerini al
            PatientService patientService = new PatientService();
            MeasurementService measurementService = new MeasurementService();

            // Hastayı al
            User patient = patientService.getPatient(patientId);
            if (patient == null) {
                System.err.println("Hasta bulunamadı.");
                return false;
            }

            // Haftalık ölçümleri al
            List<BloodSugarMeasurement> measurements = measurementService.getWeeklyMeasurements(patientId);

            // Ortalama değeri hesapla
            double totalValue = 0;
            for (BloodSugarMeasurement measurement : measurements) {
                totalValue += measurement.getOlcum_degeri();
            }

            double averageValue = measurements.isEmpty() ? 0 : totalValue / measurements.size();

            // Rapor oluştur
            String subject = "Diyabet Takip Sistemi - Haftalık Rapor";

            String body = "Sayın " + patient.getAd() + " " + patient.getSoyad() + ",\n\n" +
                    "Geçtiğimiz hafta için kan şekeri ölçüm raporunuz:\n\n" +
                    "Toplam ölçüm sayısı: " + measurements.size() + "\n" +
                    "Ortalama kan şekeri değeriniz: " + String.format("%.2f", averageValue) + " mg/dL\n\n";

            // Değerlendirme ekle
            if (averageValue < 70) {
                body += "Kan şekeri ortalamanız düşük görünüyor. Lütfen doktorunuzla iletişime geçin.\n";
            } else if (averageValue <= 140) {
                body += "Kan şekeri ortalamanız ideal aralıkta. Tebrikler!\n";
            } else if (averageValue <= 200) {
                body += "Kan şekeri ortalamanız hedef değerin üzerinde. Diyet ve egzersiz programınızı gözden geçirin.\n";
            } else {
                body += "Kan şekeri ortalamanız çok yüksek. Lütfen acilen doktorunuzla iletişime geçin.\n";
            }

            body += "\nSağlıklı günler dileriz.";

            // E-posta gönder
            return sendEmail(patient.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Haftalık rapor gönderilirken bir hata oluştu: " + e.getMessage());
            return false;
        }
    }
}