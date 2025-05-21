package org;

import org.dao.DatabaseConnectionManager;
import org.service.AuthenticationService;
import org.service.PatientService;
import org.ui.MainFrame;

import javax.swing.*;

//  AYNI BELİRTİ AYNI GÜN SEÇİLİP EKLENEBİLİYOR


/**
 * Diyabet Takip Sistemi'nin başlatıldığı ana sınıf
 */
public class Main {

    public static void main(String[] args) {
        // Swing UI thread'i için güvenli başlatma
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initializeApp();
            }
        });
    }

    private static void initializeApp() {
        try {
            // Look and Feel ayarla
            setLookAndFeel();

            // Veritabanı bağlantısını başlat
            DatabaseConnectionManager dbManager = DatabaseConnectionManager.getInstance();
            System.out.println("Veritabanı bağlantısı kuruldu.");

            // Servisleri başlat
            AuthenticationService authService = new AuthenticationService();
            PatientService patientService = new PatientService();

            // UI'ı başlat
            MainFrame mainFrame = new MainFrame(authService, patientService);
            mainFrame.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Uygulama başlatılırken hata oluştu: " + e.getMessage(),
                    "Başlatma Hatası",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void setLookAndFeel() {
        try {
            // Sistem görünümünü kullan
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Look and feel ayarlanamadı: " + e.getMessage());
            // Hata olursa varsayılan görünümü kullan
        }
    }
}