package org.ui;

import org.model.User;
import org.service.AuthenticationService;
import org.service.PatientService;

import javax.naming.AuthenticationException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Hasta kontrol paneli
 */
public class PatientDashboard extends JPanel {
    private User currentUser;
    private AuthenticationService authService;
    private PatientService patientService;
    private MainFrame parent;

    private JLabel welcomeLabel;
    private JButton logoutButton;

    public PatientDashboard(User user, AuthenticationService authService, PatientService patientService, MainFrame parent) {
        this.currentUser = user;
        this.authService = authService;
        this.patientService = patientService;
        this.parent = parent;

        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Üst panel
        JPanel topPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel("Hoş Geldiniz, " + currentUser.getAd() + " " + currentUser.getSoyad());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        logoutButton = new JButton("Çıkış Yap");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.showLoginPanel();
            }
        });
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Merkez panel - Hasta için özelleştirilmiş içerik
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JLabel infoLabel = new JLabel("Hasta paneline hoş geldiniz. Şu an için sınırlı işlevler mevcuttur.");
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(Box.createVerticalStrut(50));
        centerPanel.add(infoLabel);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Hoş Geldiniz, " + currentUser.getAd() + " " + currentUser.getSoyad());
    }
}