package org.ui;

import org.model.User;
import org.service.AuthenticationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Kullanıcı giriş paneli
 */
public class LoginPanel extends JPanel {
    private JTextField tcKimlikField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private AuthenticationService authService;
    private MainFrame parent;

    public LoginPanel(AuthenticationService authService, MainFrame parent) {
        this.authService = authService;
        this.parent = parent;

        setLayout(new GridBagLayout());
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Logo veya başlık
        JLabel titleLabel = new JLabel("Diyabet Takip Sistemi", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 20, 5);
        add(titleLabel, gbc);

        // TC Kimlik alanı
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(new JLabel("TC Kimlik No:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        tcKimlikField = new JTextField(15);
        add(tcKimlikField, gbc);

        // Şifre alanı
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Şifre:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        passwordField = new JPasswordField(15);
        add(passwordField, gbc);

        // Giriş butonu
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        loginButton = new JButton("Giriş Yap");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        add(loginButton, gbc);

        // Kayıt ol butonu
        gbc.gridx = 0;
        gbc.gridy = 4;
        registerButton = new JButton("Doktor Kaydı Oluştur");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.showRegisterPanel();
            }
        });
        add(registerButton, gbc);
    }

    private void login() {
        String tcKimlik = tcKimlikField.getText();
        String password = new String(passwordField.getPassword());

        // Giriş alanlarını kontrol et
        if (tcKimlik.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "TC Kimlik No ve Şifre alanları boş olamaz!",
                    "Giriş Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // AuthService ile giriş yap
        User user = authService.login(tcKimlik, password);

        if (user != null) {
            JOptionPane.showMessageDialog(this,
                    "Hoş Geldiniz, " + user.getAd() + " " + user.getSoyad(),
                    "Giriş Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);

            // Kullanıcı tipine göre ilgili ekrana yönlendir
            parent.showUserDashboard(user);
        } else {
            JOptionPane.showMessageDialog(this,
                    "TC Kimlik No veya şifre hatalı!",
                    "Giriş Hatası",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearFields() {
        tcKimlikField.setText("");
        passwordField.setText("");
    }
}