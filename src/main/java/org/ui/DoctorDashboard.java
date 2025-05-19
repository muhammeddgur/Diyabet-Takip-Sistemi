package org.ui;

import org.model.Patient;
import org.model.Doctor;
import org.model.User;
import org.service.AuthenticationService;
import org.service.PatientService;
import org.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Doktor kontrol paneli
 */
public class DoctorDashboard extends JPanel {
    private User currentUser;
    private AuthenticationService authService;
    private PatientService patientService;
    private MainFrame parent;

    private JLabel welcomeLabel;
    private JTabbedPane tabbedPane;
    private JPanel patientListPanel;
    private JPanel addPatientPanel;
    private JButton logoutButton;

    // Hasta ekleme form alanları
    private JTextField tcKimlikField;
    private JTextField adField;
    private JTextField soyadField;
    private JTextField emailField;
    private JTextField yasField;
    private JComboBox<String> cinsiyetCombo;
    private JPasswordField passwordField;

    public DoctorDashboard(User user, AuthenticationService authService, PatientService patientService, MainFrame parent) {
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
        welcomeLabel = new JLabel("Hoş Geldiniz, Dr. " + currentUser.getAd() + " " + currentUser.getSoyad());
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

        // Sekme paneli
        tabbedPane = new JTabbedPane();

        // Hasta Listesi Sekmesi
        patientListPanel = createPatientListPanel();
        tabbedPane.addTab("Hastalarım", patientListPanel);

        // Hasta Ekleme Sekmesi
        addPatientPanel = createAddPatientPanel();
        tabbedPane.addTab("Hasta Ekle", addPatientPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPatientListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Hasta listesini bir tablo olarak göster
        String[] columnNames = {"TC Kimlik", "Ad", "Soyad", "E-posta", "Cinsiyet"};

        List<Patient> patients = patientService.getDoctorPatients(currentUser.getUser_id());
        Object[][] data = new Object[patients.size()][6];

        for (int i = 0; i < patients.size(); i++) {
            Patient patient = patients.get(i);
            data[i][0] = patient.getTc_kimlik();
            data[i][1] = patient.getAd();
            data[i][2] = patient.getSoyad();
            data[i][3] = patient.getEmail();
            data[i][4] = patient.getCinsiyet();
        }

        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Yenile");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshPatientList();
            }
        });

        panel.add(refreshButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAddPatientPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // TC Kimlik
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("TC Kimlik No:"), gbc);

        gbc.gridx = 1;
        tcKimlikField = new JTextField(15);
        panel.add(tcKimlikField, gbc);

        // Ad
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Ad:"), gbc);

        gbc.gridx = 1;
        adField = new JTextField(15);
        panel.add(adField, gbc);

        // Soyad
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Soyad:"), gbc);

        gbc.gridx = 1;
        soyadField = new JTextField(15);
        panel.add(soyadField, gbc);

        // E-posta
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("E-posta:"), gbc);

        gbc.gridx = 1;
        emailField = new JTextField(15);
        panel.add(emailField, gbc);

        // Şifre
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Şifre:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        // Cinsiyet
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(new JLabel("Cinsiyet:"), gbc);

        gbc.gridx = 1;
        cinsiyetCombo = new JComboBox<>(new String[] {"Erkek", "Kadın"});
        panel.add(cinsiyetCombo, gbc);

        // Ekle butonu
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton addButton = new JButton("Hastayı Ekle");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPatient();
            }
        });
        panel.add(addButton, gbc);

        return panel;
    }

    private void addPatient() {
        // Form alanlarını al
        String tcKimlik = tcKimlikField.getText();
        String ad = adField.getText();
        String soyad = soyadField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String yasStr = yasField.getText();
        String cinsiyet = cinsiyetCombo.getSelectedItem().toString().substring(0, 1); // E veya K

        // Validasyon kontrolleri
        if (tcKimlik.isEmpty() || ad.isEmpty() || soyad.isEmpty() ||
                email.isEmpty() || password.isEmpty() || yasStr.isEmpty()) {
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

        // Yaş doğrulama
        int yas;
        try {
            yas = Integer.parseInt(yasStr);
            if (yas <= 0 || yas > 120) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Yaş değeri geçerli bir sayı olmalıdır (1-120 arası)!",
                    "Doğrulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Patient nesnesi oluştur
        Patient patient = new Patient();
        patient.setTc_kimlik(tcKimlik);
        patient.setAd(ad);
        patient.setSoyad(soyad);
        patient.setEmail(email);
        patient.setPassword(password);
        patient.setCinsiyet(cinsiyet.charAt(0));
        patient.setKullanici_tipi("hasta"); // Hasta kaydı

        // Doktoru ayarla
        Doctor doctor = new Doctor();
        doctor.setDoctor_id(currentUser.getUser_id());
        patient.setDoctor(doctor);

        // Kayıt işlemini gerçekleştir
        Patient addedPatient = patientService.addPatient(patient);

        if (addedPatient != null) {
            JOptionPane.showMessageDialog(this,
                    "Hasta başarıyla eklendi!",
                    "İşlem Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);
            clearAddPatientFields();
            refreshPatientList();
            tabbedPane.setSelectedIndex(0); // Hasta listesi sekmesine geç
        } else {
            JOptionPane.showMessageDialog(this,
                    "Hasta eklenemedi. Bu TC Kimlik No veya e-posta zaten kullanılıyor olabilir.",
                    "İşlem Hatası",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearAddPatientFields() {
        tcKimlikField.setText("");
        adField.setText("");
        soyadField.setText("");
        emailField.setText("");
        passwordField.setText("");
        yasField.setText("");
        cinsiyetCombo.setSelectedIndex(0);
    }

    private void refreshPatientList() {
        // Hasta listesi panelini yeniden oluştur
        tabbedPane.remove(patientListPanel);
        patientListPanel = createPatientListPanel();
        tabbedPane.insertTab("Hastalarım", null, patientListPanel, null, 0);
        tabbedPane.setSelectedIndex(0);
    }

    public void refreshData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Hoş Geldiniz, Dr. " + currentUser.getAd() + " " + currentUser.getSoyad());
        refreshPatientList();
    }
}