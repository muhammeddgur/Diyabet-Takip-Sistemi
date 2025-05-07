import util.DatabaseConnection;
import service.AuthenticationService;
import ui.LoginFrame;

import javax.swing.*;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Connection conn = DatabaseConnection.getConnection();
                AuthenticationService authService = new AuthenticationService(conn);
                LoginFrame loginFrame = new LoginFrame(authService);
                loginFrame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Veritabanı bağlantısı kurulamadı!\n" + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}