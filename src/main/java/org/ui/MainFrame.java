package org.ui;

import org.model.User;
import org.service.AuthenticationService;
import org.service.PatientService;

import javax.swing.*;
import java.awt.*;

/**
 * Uygulamanın ana penceresi
 */
public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private DoctorDashboard doctorDashboard;
    private PatientDashboard patientDashboard;

    private AuthenticationService authService;
    private PatientService patientService;

    public MainFrame(AuthenticationService authService, PatientService patientService) {
        this.authService = authService;
        this.patientService = patientService;

        initComponents();
        setupFrame();
    }

    private void initComponents() {
        // CardLayout ile farklı paneller arasında geçiş yapabilmek için
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Panelleri oluştur
        loginPanel = new LoginPanel(authService, this);
        registerPanel = new RegisterPanel(authService, this);

        // Panel ekle
        contentPanel.add(loginPanel, "login");
        contentPanel.add(registerPanel, "register");

        // Pencereye ekle
        add(contentPanel);

        // Başlangıçta login panelini göster
        cardLayout.show(contentPanel, "login");
    }

    private void setupFrame() {
        setTitle("Diyabet Takip Sistemi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 900);
        setMinimumSize(new Dimension(1000, 900)); // Minimum pencere boyutu
        setLocationRelativeTo(null); // Ekranın ortasında
        setResizable(true);
    }

    /**
     * Giriş panelini gösterir
     */
    public void showLoginPanel() {
        loginPanel.clearFields();
        cardLayout.show(contentPanel, "login");
    }

    /**
     * Kayıt panelini gösterir
     */
    public void showRegisterPanel() {
        registerPanel.clearFields();
        cardLayout.show(contentPanel, "register");
    }

    /**
     * Kullanıcı tipine göre ilgili paneli gösterir
     *
     * @param user Giriş yapan kullanıcı
     */
    public void showUserDashboard(User user) {
        if ("doktor".equalsIgnoreCase(user.getKullanici_tipi())) {
            if (doctorDashboard == null) {
                doctorDashboard = new DoctorDashboard(user, authService, patientService, this);
                contentPanel.add(doctorDashboard, "doctorDashboard");
            } else {
                doctorDashboard.refreshData(user);
            }
            cardLayout.show(contentPanel, "doctorDashboard");
        } else if ("hasta".equalsIgnoreCase(user.getKullanici_tipi())) {
            if (patientDashboard == null) {
                patientDashboard = new PatientDashboard(user, authService, patientService, this);
                contentPanel.add(patientDashboard, "patientDashboard");
            } else {
                patientDashboard.refreshData(user);
            }
            cardLayout.show(contentPanel, "patientDashboard");
        }
    }
}