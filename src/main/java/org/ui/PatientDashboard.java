package org.ui;

import org.model.*;
import org.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Hasta kontrol paneli
 */
public class PatientDashboard extends JPanel {
    private User currentUser;
    private AuthenticationService authService;
    private PatientService patientService;
    private MeasurementService measurementService;
    private DietService dietService;
    private ExerciseService exerciseService;
    private MainFrame parent;

    private JLabel welcomeLabel;
    private JButton logoutButton;
    private JTabbedPane tabbedPane;

    public PatientDashboard(User user, AuthenticationService authService, PatientService patientService, MainFrame parent) {
        this.currentUser = user;
        this.authService = authService;
        this.patientService = patientService;
        this.measurementService = new MeasurementService();
        this.dietService = new DietService();
        this.exerciseService = new ExerciseService();
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
        logoutButton.addActionListener(e -> parent.showLoginPanel());
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Ana Sekme Paneli
        tabbedPane = new JTabbedPane();

        // Ana Sayfa Sekmesi
        JPanel homePanel = createHomePanel();
        tabbedPane.addTab("Ana Sayfa", homePanel);

        // Kan Şekeri Ölçüm Sekmesi
        JPanel bloodSugarPanel = createBloodSugarPanel();
        tabbedPane.addTab("Kan Şekeri Ölçümleri", bloodSugarPanel);

        // Diyet Takip Sekmesi
        JPanel dietPanel = createDietPanel();
        tabbedPane.addTab("Diyet Takibi", dietPanel);

        // Egzersiz Takip Sekmesi
        JPanel exercisePanel = createExercisePanel();
        tabbedPane.addTab("Egzersiz Takibi", exercisePanel);

        // Belirti Sekmesi
        JPanel symptomsPanel = createSymptomsPanel();
        tabbedPane.addTab("Belirtiler", symptomsPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Ana Sayfa Paneli - Özet bilgiler
     */
    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Üst bilgi paneli
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 20, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Özet Bilgiler"));

        // Günlük kan şekeri ortalaması
        JPanel avgPanel = new JPanel(new BorderLayout());
        avgPanel.add(new JLabel("Günlük Kan Şekeri Ortalaması:"), BorderLayout.NORTH);
        JLabel avgValueLabel = new JLabel("Veri yok");
        avgValueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        avgPanel.add(avgValueLabel, BorderLayout.CENTER);
        infoPanel.add(avgPanel);

        // Diyet uyum oranı
        JPanel dietPanel = new JPanel(new BorderLayout());
        dietPanel.add(new JLabel("Diyet Uyum Oranı:"), BorderLayout.NORTH);
        JProgressBar dietProgress = new JProgressBar(0, 100);
        dietProgress.setValue(0);
        dietProgress.setStringPainted(true);
        dietProgress.setString("Veri yok");
        dietPanel.add(dietProgress, BorderLayout.CENTER);
        infoPanel.add(dietPanel);

        // Son ölçüm
        JPanel lastMeasurementPanel = new JPanel(new BorderLayout());
        lastMeasurementPanel.add(new JLabel("Son Ölçüm:"), BorderLayout.NORTH);
        JLabel lastMeasurementLabel = new JLabel("Veri yok");
        lastMeasurementLabel.setFont(new Font("Arial", Font.BOLD, 16));
        lastMeasurementPanel.add(lastMeasurementLabel, BorderLayout.CENTER);
        infoPanel.add(lastMeasurementPanel);

        // Egzersiz uyum oranı
        JPanel exercisePanel = new JPanel(new BorderLayout());
        exercisePanel.add(new JLabel("Egzersiz Uyum Oranı:"), BorderLayout.NORTH);
        JProgressBar exerciseProgress = new JProgressBar(0, 100);
        exerciseProgress.setValue(0);
        exerciseProgress.setStringPainted(true);
        exerciseProgress.setString("Veri yok");
        exercisePanel.add(exerciseProgress, BorderLayout.CENTER);
        infoPanel.add(exercisePanel);

        panel.add(infoPanel, BorderLayout.NORTH);

        // Orta kısım - Grafik alanı
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("Kan Şekeri Değerleri (Son 7 Gün)"));
        chartPanel.setPreferredSize(new Dimension(600, 300));

        // Grafik simülasyonu
        JLabel chartPlaceholder = new JLabel("<html><center>Bu alanda kan şekeri değerlerinin grafiği gösterilecektir.<br>" +
                "Veri girişi yapıldıkça grafik otomatik güncellenecektir.</center></html>", JLabel.CENTER);
        chartPlaceholder.setForeground(Color.GRAY);
        chartPanel.add(chartPlaceholder, BorderLayout.CENTER);

        panel.add(chartPanel, BorderLayout.CENTER);

        // Alt kısım - İşlem butonları
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton addMeasurementButton = new JButton("Yeni Ölçüm Ekle");
        addMeasurementButton.addActionListener(e -> {
            tabbedPane.setSelectedIndex(1); // Kan Şekeri Ölçümleri sekmesine git
        });
        buttonPanel.add(addMeasurementButton);

        JButton updateDietButton = new JButton("Diyet Durumu Güncelle");
        updateDietButton.addActionListener(e -> {
            tabbedPane.setSelectedIndex(2); // Diyet Takibi sekmesine git
        });
        buttonPanel.add(updateDietButton);

        JButton updateExerciseButton = new JButton("Egzersiz Durumu Güncelle");
        updateExerciseButton.addActionListener(e -> {
            tabbedPane.setSelectedIndex(3); // Egzersiz Takibi sekmesine git
        });
        buttonPanel.add(updateExerciseButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Kan Şekeri Ölçüm Paneli
     */
    private JPanel createBloodSugarPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Yeni ölçüm ekleme formu
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Yeni Kan Şekeri Ölçümü Ekle"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Ölçüm değeri
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Ölçüm Değeri (mg/dL):"), gbc);

        gbc.gridx = 1;
        JTextField measurementField = new JTextField(10);
        formPanel.add(measurementField, gbc);

        // Periyot
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Ölçüm Zamanı:"), gbc);

        gbc.gridx = 1;
        String[] periods = {
                "Sabah (07:00-09:00)",
                "Öğle (12:00-14:00)",
                "İkindi (15:00-17:00)",
                "Akşam (18:00-20:00)",
                "Gece (22:00-24:00)"
        };
        JComboBox<String> periodCombo = new JComboBox<>(periods);
        formPanel.add(periodCombo, gbc);

        // Ekle butonu
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton addButton = new JButton("Ölçümü Kaydet");
        addButton.addActionListener(e -> {
            try {
                double value = Double.parseDouble(measurementField.getText().trim());
                if (value <= 0 || value > 600) {
                    throw new NumberFormatException();
                }

                // Ölçüm kaydetme simülasyonu
                JOptionPane.showMessageDialog(panel,
                        "Ölçüm başarıyla kaydedildi.\nDeğer: " + value + " mg/dL\nZaman: " + periodCombo.getSelectedItem(),
                        "Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);

                measurementField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Lütfen geçerli bir ölçüm değeri girin (1-600 arası).",
                        "Hatalı Değer",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        formPanel.add(addButton, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Ölçüm listesi tablosu
        String[] columnNames = {"Tarih", "Saat", "Değer (mg/dL)", "Periyot", "Durum"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        // Boş tablo
        JTable measurementsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(measurementsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Kan Şekeri Ölçümlerim"));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Diyet Takip Paneli
     */
    private JPanel createDietPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Diyet Bilgileri
        JPanel dietInfoPanel = new JPanel(new BorderLayout());
        dietInfoPanel.setBorder(BorderFactory.createTitledBorder("Diyet Planım"));

        JTextArea dietDetailsArea = new JTextArea(5, 30);
        dietDetailsArea.setText("Veri yok");
        dietDetailsArea.setEditable(false);
        dietDetailsArea.setBackground(null);
        JScrollPane dietScrollPane = new JScrollPane(dietDetailsArea);
        dietInfoPanel.add(dietScrollPane, BorderLayout.CENTER);

        panel.add(dietInfoPanel, BorderLayout.NORTH);

        // Diyet Durum Bildirimi Paneli
        JPanel reportPanel = new JPanel(new GridBagLayout());
        reportPanel.setBorder(BorderFactory.createTitledBorder("Bugün Diyet Durumumu Bildir"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        reportPanel.add(new JLabel("Bugün diyet planını uyguladın mı?"), gbc);

        gbc.gridx = 1;
        String[] dietStatus = {"Evet, tamamen uyguladım", "Kısmen uyguladım", "Hayır, uygulayamadım"};
        JComboBox<String> dietStatusCombo = new JComboBox<>(dietStatus);
        reportPanel.add(dietStatusCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        reportPanel.add(new JLabel("Notlar:"), gbc);

        gbc.gridx = 1;
        JTextField notesField = new JTextField(20);
        reportPanel.add(notesField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton reportButton = new JButton("Bildir");
        reportButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(panel,
                    "Diyet durumu başarıyla bildirildi:\n" + dietStatusCombo.getSelectedItem(),
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);

            notesField.setText("");
        });
        reportPanel.add(reportButton, gbc);

        panel.add(reportPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Egzersiz Takip Paneli
     */
    private JPanel createExercisePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Egzersiz Bilgileri
        JPanel exerciseInfoPanel = new JPanel(new BorderLayout());
        exerciseInfoPanel.setBorder(BorderFactory.createTitledBorder("Egzersiz Planım"));

        JTextArea exerciseDetailsArea = new JTextArea(5, 30);
        exerciseDetailsArea.setText("Veri yok");
        exerciseDetailsArea.setEditable(false);
        exerciseDetailsArea.setBackground(null);
        JScrollPane exerciseScrollPane = new JScrollPane(exerciseDetailsArea);
        exerciseInfoPanel.add(exerciseScrollPane, BorderLayout.CENTER);

        panel.add(exerciseInfoPanel, BorderLayout.NORTH);

        // Egzersiz Durum Bildirimi Paneli
        JPanel reportPanel = new JPanel(new GridBagLayout());
        reportPanel.setBorder(BorderFactory.createTitledBorder("Bugün Egzersiz Durumumu Bildir"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        reportPanel.add(new JLabel("Bugün egzersiz yaptın mı?"), gbc);

        gbc.gridx = 1;
        String[] exerciseStatus = {"Evet, yaptım", "Hayır, yapamadım"};
        JComboBox<String> exerciseStatusCombo = new JComboBox<>(exerciseStatus);
        reportPanel.add(exerciseStatusCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        reportPanel.add(new JLabel("Süre (dakika):"), gbc);

        gbc.gridx = 1;
        JTextField durationField = new JTextField();
        reportPanel.add(durationField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        reportPanel.add(new JLabel("Notlar:"), gbc);

        gbc.gridx = 1;
        JTextField notesField = new JTextField(20);
        reportPanel.add(notesField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton reportButton = new JButton("Bildir");
        reportButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(panel,
                    "Egzersiz durumu başarıyla bildirildi.",
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);

            notesField.setText("");
        });
        reportPanel.add(reportButton, gbc);

        panel.add(reportPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Belirti Takip Paneli
     */
    private JPanel createSymptomsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Belirti Ekleme Formu
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Yeni Belirti Ekle"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Belirti:"), gbc);

        gbc.gridx = 1;
        String[] symptoms = {
                "Susuzluk/Ağız Kuruluğu",
                "Sık İdrara Çıkma",
                "Yorgunluk",
                "Bulanık Görme",
                "Baş Dönmesi",
                "Uyuşma/Karıncalanma"
        };
        JComboBox<String> symptomCombo = new JComboBox<>(symptoms);
        formPanel.add(symptomCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Şiddeti:"), gbc);

        gbc.gridx = 1;
        String[] severities = {"Hafif", "Orta", "Şiddetli"};
        JComboBox<String> severityCombo = new JComboBox<>(severities);
        formPanel.add(severityCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Açıklama:"), gbc);

        gbc.gridx = 1;
        JTextField descriptionField = new JTextField(20);
        formPanel.add(descriptionField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton addButton = new JButton("Ekle");
        addButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(panel,
                    "Belirti başarıyla eklendi.",
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);

            descriptionField.setText("");
        });
        formPanel.add(addButton, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Belirti Geçmişi Tablosu
        String[] columnNames = {"Tarih", "Belirti", "Şiddeti", "Açıklama"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        // Boş tablo
        JTable symptomsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(symptomsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Belirttiğim Semptomlar"));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void refreshData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Hoş Geldiniz, " + currentUser.getAd() + " " + currentUser.getSoyad());
    }
}