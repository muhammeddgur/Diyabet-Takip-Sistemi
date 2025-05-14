package org.ui;

import org.service.AuthenticationService;
import org.model.User;
import org.model.Doctor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LoginFrame extends JFrame {
    private JTextField tcField;
    private JPasswordField passField;
    private JButton loginBtn;
    private JButton forgotPasswordBtn;
    private JButton registerDoctorBtn;
    private AuthenticationService authService;


    public LoginFrame(AuthenticationService authService) {
        this.authService = authService;

        // Frame ayarları
        setTitle("Diyabet Takip ve Yönetim Sistemi - Giriş");
        setSize(400, 350);
        setLocationRelativeTo(null); // Ekranın ortasında göster
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Ana panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Üst panel - Logo ve başlık
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 152, 219)); // Mavi ton
        headerPanel.setPreferredSize(new Dimension(400, 70));
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("DİYABET TAKİP SİSTEMİ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel subtitleLabel = new JLabel("KOCAELİ ÜNİVERSİTESİ", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.WHITE);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Orta panel - Giriş formu
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel tcLabel = new JLabel("T.C. Kimlik No:");
        tcLabel.setBounds(50, 20, 100, 25);
        formPanel.add(tcLabel);

        tcField = new JTextField();
        tcField.setBounds(150, 20, 180, 25);
        formPanel.add(tcField);

        JLabel passLabel = new JLabel("Şifre:");
        passLabel.setBounds(50, 60, 100, 25);
        formPanel.add(passLabel);

        passField = new JPasswordField();
        passField.setBounds(150, 60, 180, 25);
        formPanel.add(passField);

        loginBtn = new JButton("Giriş Yap");
        loginBtn.setBounds(150, 100, 180, 30);
        loginBtn.setBackground(new Color(52, 152, 219)); // Mavi ton
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setFocusPainted(false);
        formPanel.add(loginBtn);

        forgotPasswordBtn = new JButton("Şifremi Unuttum");
        forgotPasswordBtn.setBounds(150, 140, 180, 30);
        forgotPasswordBtn.setBackground(new Color(236, 240, 241)); // Açık gri
        forgotPasswordBtn.setFocusPainted(false);
        formPanel.add(forgotPasswordBtn);

        registerDoctorBtn = new JButton("Yeni Doktor Kaydı");
        registerDoctorBtn.setBounds(150, 180, 180, 30);
        registerDoctorBtn.setBackground(new Color(46, 204, 113)); // Yeşil ton
        registerDoctorBtn.setForeground(Color.BLACK); // Siyah renk
        registerDoctorBtn.setFocusPainted(false);
        formPanel.add(registerDoctorBtn);

        // Alt panel - Bilgi
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(236, 240, 241)); // Açık gri
        footerPanel.setPreferredSize(new Dimension(400, 30));

        JLabel infoLabel = new JLabel("Programlama Lab II - Diyabet Takip Sistemi Projesi", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        footerPanel.add(infoLabel);

        // Panelleri ana panele ekle
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // Frame'e paneli ekle
        setContentPane(mainPanel);

        // Event listeners
        loginBtn.addActionListener(e -> onLogin());
        forgotPasswordBtn.addActionListener(e -> onForgotPassword());
        registerDoctorBtn.addActionListener(e -> onDoctorRegister());

        // Enter tuşu ile giriş yapma
        passField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onLogin();
                }
            }
        });
    }

    private void onLogin() {
        String tc = tcField.getText().trim();
        String password = new String(passField.getPassword());

        // Validasyon
        if (tc.isEmpty()) {
            showError("T.C. Kimlik No alanı boş bırakılamaz.");
            tcField.requestFocus();
            return;
        }

        if (!validateTcKimlik(tc)) {
            showError("Geçersiz T.C. Kimlik No. 11 haneli olmalıdır.");
            tcField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Şifre alanı boş bırakılamaz.");
            passField.requestFocus();
            return;
        }

        // Giriş işlemi
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            if (authService.login(tc, password)) {
                User user = authService.getCurrentUser();

                JOptionPane.showMessageDialog(
                        this,
                        "Hoş geldiniz, " + user.getFirstName() + " " + user.getLastName(),
                        "Giriş Başarılı",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // Ana ekranı aç ve bu ekranı kapat
                dispose();

                if (authService.isDoctor()) {
                    new DoctorDashboard(user).setVisible(true);
                } else if (authService.isPatient()) {
                    new PatientDashboard(user).setVisible(true);
                }
            } else {
                showError("T.C. Kimlik No veya şifre hatalı.");
                passField.setText("");
                passField.requestFocus();
            }
        } catch (Exception ex) {
            showError("Giriş sırasında bir hata oluştu: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void onForgotPassword() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel tcLabel = new JLabel("T.C. Kimlik No:");
        JTextField tcField = new JTextField();

        JLabel emailLabel = new JLabel("E-posta Adresi:");
        JTextField emailField = new JTextField();

        panel.add(tcLabel);
        panel.add(tcField);
        panel.add(emailLabel);
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Şifre Sıfırlama",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String tc = tcField.getText().trim();
            String email = emailField.getText().trim();

            if (tc.isEmpty() || email.isEmpty()) {
                showError("Tüm alanları doldurunuz.");
                return;
            }

            if (!validateTcKimlik(tc)) {
                showError("Geçersiz T.C. Kimlik No. 11 haneli olmalıdır.");
                return;
            }

            if (!validateEmail(email)) {
                showError("Geçersiz e-posta adresi.");
                return;
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            try {
                String newPassword = authService.resetPassword(tc, email);

                if (newPassword != null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Yeni şifreniz: " + newPassword + "\n\n" +
                                    "Not: Gerçek bir uygulamada bu şifre e-posta adresinize gönderilir,\n" +
                                    "ekranda gösterilmez. Güvenlik için lütfen şifrenizi giriş yaptıktan\n" +
                                    "sonra değiştiriniz.",
                            "Şifre Sıfırlandı",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    showError("T.C. Kimlik No veya e-posta adresi hatalı.");
                }
            } catch (Exception ex) {
                showError("Şifre sıfırlama sırasında bir hata oluştu: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private void onDoctorRegister() {
        JPanel personalPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // TC Kimlik
        gbc.gridx = 0; gbc.gridy = 0;
        personalPanel.add(new JLabel("T.C. Kimlik No:*"), gbc);

        gbc.gridx = 1;
        JTextField tcField = new JTextField(15);
        personalPanel.add(tcField, gbc);

        // Ad
        gbc.gridx = 0; gbc.gridy = 1;
        personalPanel.add(new JLabel("Ad:*"), gbc);

        gbc.gridx = 1;
        JTextField firstNameField = new JTextField(15);
        personalPanel.add(firstNameField, gbc);

        // Soyad
        gbc.gridx = 0; gbc.gridy = 2;
        personalPanel.add(new JLabel("Soyad:*"), gbc);

        gbc.gridx = 1;
        JTextField lastNameField = new JTextField(15);
        personalPanel.add(lastNameField, gbc);

        // Doğum Tarihi
        gbc.gridx = 0; gbc.gridy = 3;
        personalPanel.add(new JLabel("Doğum Tarihi:*"), gbc);

        gbc.gridx = 1;
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        String[] days = new String[32];
        days[0] = "Gün";
        for (int i = 1; i <= 31; i++) days[i] = String.valueOf(i);

        String[] months = {"Ay", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

        String[] years = new String[101];
        years[0] = "Yıl";
        for (int i = 1; i <= 100; i++) years[i] = String.valueOf(2025 - i);

        JComboBox<String> dayCombo = new JComboBox<>(days);
        JComboBox<String> monthCombo = new JComboBox<>(months);
        JComboBox<String> yearCombo = new JComboBox<>(years);

        datePanel.add(dayCombo);
        datePanel.add(monthCombo);
        datePanel.add(yearCombo);

        personalPanel.add(datePanel, gbc);

        // Cinsiyet
        gbc.gridx = 0; gbc.gridy = 4;
        personalPanel.add(new JLabel("Cinsiyet:*"), gbc);

        gbc.gridx = 1;
        String[] genders = {"Seçiniz", "Erkek", "Kadın"};
        JComboBox<String> genderCombo = new JComboBox<>(genders);
        personalPanel.add(genderCombo, gbc);

        // E-posta
        gbc.gridx = 0; gbc.gridy = 5;
        personalPanel.add(new JLabel("E-posta:*"), gbc);

        gbc.gridx = 1;
        JTextField emailField = new JTextField(15);
        personalPanel.add(emailField, gbc);

        // Şifre
        gbc.gridx = 0; gbc.gridy = 6;
        personalPanel.add(new JLabel("Şifre:*"), gbc);

        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(15);
        personalPanel.add(passwordField, gbc);

        // Şifre Tekrar
        gbc.gridx = 0; gbc.gridy = 7;
        personalPanel.add(new JLabel("Şifre (Tekrar):*"), gbc);

        gbc.gridx = 1;
        JPasswordField passwordConfirmField = new JPasswordField(15);
        personalPanel.add(passwordConfirmField, gbc);

        // Profil Resmi
        gbc.gridx = 0; gbc.gridy = 8;
        personalPanel.add(new JLabel("Profil Resmi:"), gbc);

        gbc.gridx = 1;
        JButton chooseImageBtn = new JButton("Resim Seç");
        personalPanel.add(chooseImageBtn, gbc);

        // Not açıklaması
        JLabel noteLabel = new JLabel("* ile işaretli alanlar zorunludur.");
        noteLabel.setForeground(Color.RED);

        // Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(personalPanel, BorderLayout.CENTER);
        mainPanel.add(noteLabel, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // JOptionPane göster
        int result = JOptionPane.showConfirmDialog(
                this,
                mainPanel,
                "Yeni Doktor Kaydı",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            // Form validasyonu
            String tc = tcField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();

            // Cinsiyet değerini 'E' veya 'K' formatına dönüştür
            String gender = "";
            if (genderCombo.getSelectedIndex() > 0) {
                String selectedGender = genderCombo.getSelectedItem().toString();
                if ("Erkek".equals(selectedGender)) {
                    gender = "E";
                } else if ("Kadın".equals(selectedGender)) {
                    gender = "K";
                }
            }

            String password = new String(passwordField.getPassword());
            String passwordConfirm = new String(passwordConfirmField.getPassword());

            // Zorunlu alan kontrolü
            if (tc.isEmpty() || firstName.isEmpty() || lastName.isEmpty() ||
                    email.isEmpty() || password.isEmpty() ||
                    dayCombo.getSelectedIndex() == 0 || monthCombo.getSelectedIndex() == 0 || yearCombo.getSelectedIndex() == 0 ||
                    genderCombo.getSelectedIndex() == 0) {

                showError("Lütfen * ile işaretli tüm alanları doldurunuz.");
                return;
            }

            // TC Kimlik No kontrolü
            if (!validateTcKimlik(tc)) {
                showError("Geçersiz T.C. Kimlik No. 11 haneli olmalıdır.");
                return;
            }

            // E-posta kontrolü
            if (!validateEmail(email)) {
                showError("Geçersiz e-posta adresi.");
                return;
            }

            // Şifre eşleşmesi kontrolü
            if (!password.equals(passwordConfirm)) {
                showError("Şifreler eşleşmiyor.");
                return;
            }

            // Şifre uzunluğu kontrolü
            if (password.length() < 6) {
                showError("Şifre en az 6 karakter olmalıdır.");
                return;
            }

            // Doğum tarihi oluştur
            String day = dayCombo.getSelectedItem().toString();
            String month = monthCombo.getSelectedItem().toString();
            String year = yearCombo.getSelectedItem().toString();

            LocalDate birthDate = LocalDate.of(
                    Integer.parseInt(year),
                    Integer.parseInt(month),
                    Integer.parseInt(day)
            );

            // Kayıt işlemi
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            try {
                // User nesnesi oluştur
                User user = new User();
                user.setTcIdentity(tc);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setPassword(password);
                user.setEmail(email);
                user.setGender(gender); // 'E' veya 'K' olarak dönüştürülmüş değer
                user.setBirthDate(birthDate);
                user.setUserType("DOCTOR");

                // Doctor nesnesi oluştur - artık ek alanlar yok
                Doctor doctor = new Doctor();

                // Doktoru kaydet
                Integer doctorId = authService.createDoctor(user, doctor);

                if (doctorId != null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Doktor kaydı başarıyla oluşturuldu!\n\n" +
                                    "T.C. Kimlik No: " + tc + "\n" +
                                    "Ad Soyad: " + firstName + " " + lastName + "\n\n" +
                                    "Bu bilgilerle giriş yapabilirsiniz.",
                            "Kayıt Başarılı",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    // Giriş alanlarını otomatik doldur
                    this.tcField.setText(tc);
                    this.passField.setText(password);
                } else {
                    showError("Doktor kaydı oluşturulamadı. Lütfen daha sonra tekrar deneyiniz.");
                }
            } catch (Exception ex) {
                showError("Kayıt sırasında bir hata oluştu: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private boolean validateTcKimlik(String tc) {
        return tc.length() == 11 && tc.matches("\\d+");
    }

    private boolean validateEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Hata",
                JOptionPane.ERROR_MESSAGE
        );
    }
}