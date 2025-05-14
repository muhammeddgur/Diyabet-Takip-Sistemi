package org;

import org.service.AuthenticationService;
import org.service.AlertService;
import org.util.DatabaseConnection;
import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Diyabet Takip Sistemi - Ana Sınıf
 * Bu sınıf, uygulamanın başlangıç noktasıdır ve gerekli servisleri başlatır.
 *
 * @author Emirhan Karabulut
 */
public class Main {

    public static void main(String[] args) {
        printApplicationHeader();

        // GUI'yi EDT (Event Dispatch Thread) üzerinde başlat
        SwingUtilities.invokeLater(() -> {
            try {
                // Veritabanı bağlantısını oluştur
                Connection conn = DatabaseConnection.getConnection();
                System.out.println("Veritabanı bağlantısı başarıyla kuruldu.");

                // Servisleri başlat
                AuthenticationService authService = new AuthenticationService();
                AlertService alertService = new AlertService();

                // Login ekranını göster
                createAndShowGUI(authService, alertService);

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Veritabanı bağlantısı kurulamadı!\n" + e.getMessage(),
                        "Bağlantı Hatası",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    /**
     * GUI bileşenlerini oluşturur ve gösterir
     */
    private static void createAndShowGUI(AuthenticationService authService, AlertService alertService) {
        try {
            // Look and Feel'i sistem görünümüne ayarla
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Ana ekranlar için LoginFrame sınıfı örneği oluştur ve göster
            JFrame loginFrame = new org.ui.LoginFrame(authService);
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginFrame.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Uygulama başlatılırken hata oluştu!\n" + e.getMessage(),
                    "Uygulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Uygulama başlık bilgilerini konsola yazdırır
     */
    private static void printApplicationHeader() {
        System.out.println("===================================================");
        System.out.println("      DİYABET TAKİP VE YÖNETİM SİSTEMİ");
        System.out.println("===================================================");

        // Tarih ve saat bilgisini formatla ve yazdır
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(utcFormatter);

        System.out.println("Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): " + formattedDateTime);
        System.out.println("===================================================");
        System.out.println("Uygulama başlatılıyor...");
    }
}