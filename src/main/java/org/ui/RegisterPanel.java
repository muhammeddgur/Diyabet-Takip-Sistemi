package org.ui;

import org.model.User;
import org.service.AuthenticationService;
import org.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Doktor kayıt paneli
 */
public class RegisterPanel extends JPanel {
    private JTextField tcKimlikField;
    private JTextField adField;
    private JTextField soyadField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> cinsiyetCombo;
    private JSpinner dogumTarihiSpinner;
    private JButton registerButton;
    private JButton backButton;
    private AuthenticationService authService;
    private MainFrame parent;

    public RegisterPanel(AuthenticationService authService, MainFrame parent) {
        this.authService = authService;
        this.parent = parent;

        setLayout(new GridBagLayout());
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Başlık
        JLabel titleLabel = new JLabel("Doktor Kaydı", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 20, 5);
        add(titleLabel, gbc);

        // TC Kimlik
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(new JLabel("TC Kimlik No:"), gbc);

        gbc.gridx = 1;
        tcKimlikField = new JTextField(15);
        add(tcKimlikField, gbc);

        // Ad
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Ad:"), gbc);

        gbc.gridx = 1;
        adField = new JTextField(15);
        add(adField, gbc);

        // Soyad
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Soyad:"), gbc);

        gbc.gridx = 1;
        soyadField = new JTextField(15);
        add(soyadField, gbc);

        // E-posta
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("E-posta:"), gbc);

        gbc.gridx = 1;
        emailField = new JTextField(15);
        add(emailField, gbc);

        // Doğum Tarihi
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(new JLabel("Doğum Tarihi:"), gbc);

        gbc.gridx = 1;
        // Bugünün tarihinden 30 yıl öncesi varsayılan değer olarak
        Date defaultDate = new Date();
        defaultDate.setYear(defaultDate.getYear() - 30);

        SpinnerDateModel dateModel = new SpinnerDateModel(defaultDate, null, new Date(), java.util.Calendar.DAY_OF_MONTH);
        dogumTarihiSpinner = new JSpinner(dateModel);
        dogumTarihiSpinner.setEditor(new JSpinner.DateEditor(dogumTarihiSpinner, "dd.MM.yyyy"));
        add(dogumTarihiSpinner, gbc);

        // Cinsiyet
        gbc.gridx = 0;
        gbc.gridy = 6;
        add(new JLabel("Cinsiyet:"), gbc);

        gbc.gridx = 1;
        cinsiyetCombo = new JComboBox<>(new String[]{"Erkek", "Kadın"});
        add(cinsiyetCombo, gbc);

        // Şifre
        gbc.gridx = 0;
        gbc.gridy = 7;
        add(new JLabel("Şifre:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        add(passwordField, gbc);

        // Şifre Onay
        gbc.gridx = 0;
        gbc.gridy = 8;
        add(new JLabel("Şifre (Tekrar):"), gbc);

        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(15);
        add(confirmPasswordField, gbc);

        // Kayıt ol butonu
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        registerButton = new JButton("Kaydı Tamamla");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });
        add(registerButton, gbc);

        // Geri dön butonu
        gbc.gridy = 10;
        backButton = new JButton("Giriş Ekranına Dön");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.showLoginPanel();
            }
        });
        add(backButton, gbc);
    }

    private void register() {
        // Form alanlarını al
        String tcKimlik = tcKimlikField.getText();
        String ad = adField.getText();
        String soyad = soyadField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Doğum tarihini al (JSpinner'dan Date olarak alınır, LocalDate'e çevrilir)
        Date spinnerDate = (Date) dogumTarihiSpinner.getValue();
        LocalDate dogumTarihi = spinnerDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Cinsiyeti al (E veya K formatında)
        String cinsiyet = "E"; // Varsayılan değer
        if (cinsiyetCombo.getSelectedItem().toString().equals("Kadın")) {
            cinsiyet = "K";
        }

        // Validasyon kontrolleri
        if (tcKimlik.isEmpty() || ad.isEmpty() || soyad.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Tüm alanlar doldurulmalıdır!",
                    "Form Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // TC Kimlik No doğrulama
        if (!ValidationUtil.validateTcKimlik(tcKimlik)) {
            JOptionPane.showMessageDialog(this,
                    "Geçersiz TC Kimlik No formatı!",
                    "Doğrulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ad ve soyad doğrulama
        if (!ValidationUtil.validateName(ad) || !ValidationUtil.validateName(soyad)) {
            JOptionPane.showMessageDialog(this,
                    "Ad ve soyad sadece harflerden oluşmalıdır!",
                    "Doğrulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // E-posta doğrulama
        if (!ValidationUtil.validateEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Geçersiz e-posta formatı!",
                    "Doğrulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Şifre doğrulama
        if (!ValidationUtil.validatePassword(password)) {
            JOptionPane.showMessageDialog(this,
                    "Şifre en az 8 karakter, büyük/küçük harf, rakam ve özel karakter içermelidir!",
                    "Doğrulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Şifre eşleşme kontrolü
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Şifreler eşleşmiyor!",
                    "Doğrulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // User nesnesi oluştur
        User user = new User();
        user.setTc_kimlik(tcKimlik);
        user.setAd(ad);
        user.setSoyad(soyad);
        user.setEmail(email);
        user.setPassword(password);
        user.setDogum_tarihi(dogumTarihi);  // Doğum tarihini ekledik
        user.setCinsiyet(cinsiyet.charAt(0));        // Cinsiyeti ekledik
        user.setKullanici_tipi("doktor");  // Doktor kaydı

        // Kayıt işlemini gerçekleştir
        User registeredUser = authService.register(user);

        if (registeredUser != null) {
            JOptionPane.showMessageDialog(this,
                    "Kayıt başarıyla tamamlandı! Giriş yapabilirsiniz.",
                    "Kayıt Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            parent.showLoginPanel();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Kayıt işlemi başarısız. Bu TC Kimlik No veya e-posta zaten kullanılıyor olabilir.",
                    "Kayıt Hatası",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearFields() {
        tcKimlikField.setText("");
        adField.setText("");
        soyadField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");

        // Varsayılan değerlere sıfırla
        Date defaultDate = new Date();
        defaultDate.setYear(defaultDate.getYear() - 30);
        dogumTarihiSpinner.setValue(defaultDate);
        cinsiyetCombo.setSelectedIndex(0);
    }
}