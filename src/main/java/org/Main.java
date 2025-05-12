package org;

import org.service.AuthenticationService;
import org.ui.LoginFrame;
import org.util.DatabaseConnection;

import javax.swing.*;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {

        // MANUEL 1 DOKTOR EKLEDİM TC: 12345678901 ŞİFRE: 123456 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        SwingUtilities.invokeLater(() -> {
            try {
                Connection conn = DatabaseConnection.getConnection();
                AuthenticationService authService = new AuthenticationService(conn);
                LoginFrame loginFrame = new LoginFrame(authService);
                System.out.println();
                loginFrame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Veritabanı bağlantısı kurulamadı!\n" + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}