package org.ui;

import org.dao.InsulinReferenceDao;
import org.dao.MeasurementDao;
import org.model.*;
import org.service.*;
import org.util.DateTimeUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Hasta kontrol paneli
 */
public class PatientDashboard extends JPanel {
    private User currentUser;
    private Patient patient;
    private AuthenticationService authService;
    private PatientService patientService;
    private MeasurementService measurementService;
    private DietService dietService;
    private ExerciseService exerciseService;
    private MainFrame parent;

    JTextField measurementField;
    JComboBox<String> periodCombo;

    private JLabel welcomeLabel;
    private JButton logoutButton;
    private JTabbedPane tabbedPane;

    public PatientDashboard(User user, AuthenticationService authService, PatientService patientService, MainFrame parent) {
        this.currentUser = user;
        this.authService = authService;
        this.patientService = patientService;
        this.patient = patientService.getPatientByUserId(this.currentUser.getUser_id());
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

        // Sol taraf - Profil resmi ve hoş geldiniz mesajı
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Profil resmi etiketi
        JLabel profileImageLabel = createProfileImageLabel();
        profilePanel.add(profileImageLabel);

        // Hoş geldiniz metni
        welcomeLabel = new JLabel("Hoş Geldiniz, " + currentUser.getAd() + " " + currentUser.getSoyad());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        profilePanel.add(welcomeLabel);

        topPanel.add(profilePanel, BorderLayout.WEST);

        // Sağ taraf - Çıkış butonu
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

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Profil resmi etiketini oluşturur ve içeriğini yükler
     */
    private JLabel createProfileImageLabel() {
        JLabel profileImageLabel = new JLabel();
        profileImageLabel.setPreferredSize(new Dimension(80, 80));
        profileImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        profileImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Profil resminin kontrolü
        if (patient != null && patient.getProfil_resmi() != null && patient.getProfil_resmi().length > 0) {
            try {
                // Profil resmini byte dizisinden yükle
                ByteArrayInputStream bis = new ByteArrayInputStream(patient.getProfil_resmi());
                BufferedImage originalImage = ImageIO.read(bis);

                if (originalImage != null) {
                    // Resmi boyutlandır - daha büyük boyut
                    Image scaledImage = originalImage.getScaledInstance(75, 75, Image.SCALE_SMOOTH);
                    profileImageLabel.setIcon(new ImageIcon(scaledImage));
                } else {
                    // Yüklenemezse default görsel oluştur
                    createDefaultProfileImage(profileImageLabel);
                }
            } catch (IOException e) {
                System.err.println("Profil resmi yüklenemedi: " + e.getMessage());
                // Hata durumunda default görsel oluştur
                createDefaultProfileImage(profileImageLabel);
            }
        } else {
            // Profil resmi yoksa default görsel oluştur
            createDefaultProfileImage(profileImageLabel);
        }

        return profileImageLabel;
    }

    /**
     * Varsayılan profil resmi oluşturur (kullanıcının baş harflerinden)
     */
    private void createDefaultProfileImage(JLabel imageLabel) {
        // Varsayılan profil resmi oluştur (baş harflerden) - daha büyük boyut
        BufferedImage img = new BufferedImage(75, 75, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        // Arkaplanı mavi yap
        g2.setColor(new Color(70, 130, 180)); // Steel blue
        g2.fillRect(0, 0, 75, 75); // Boyut güncellendi

        // Baş harfleri beyaz renkte yaz - daha büyük font
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 32)); // Font boyutu 20'den 32'ye büyütüldü

        // Kullanıcı adının baş harflerini al
        String initials = "";
        if (currentUser.getAd() != null && !currentUser.getAd().isEmpty()) {
            initials += currentUser.getAd().charAt(0);
        }
        if (currentUser.getSoyad() != null && !currentUser.getSoyad().isEmpty()) {
            initials += currentUser.getSoyad().charAt(0);
        }

        // Metni ortala
        FontMetrics metrics = g2.getFontMetrics();
        int textWidth = metrics.stringWidth(initials);
        int textHeight = metrics.getHeight();
        int x = (75 - textWidth) / 2;
        int y = ((75 - textHeight) / 2) + metrics.getAscent();

        g2.drawString(initials, x, y);
        g2.dispose();

        // Label'a ekle
        imageLabel.setIcon(new ImageIcon(img));
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

        // Günlük kan şekeri değerleri
        JPanel dailyMeasurementsPanel = new JPanel(new BorderLayout());
        dailyMeasurementsPanel.add(new JLabel("Günlük Kan Şekeri Değerleri:"), BorderLayout.NORTH);

        // Günlük değerleri gösteren mini tablo
        DefaultTableModel miniTableModel = new DefaultTableModel(
                new Object[][]{{"Sabah", "Öğle", "İkindi", "Akşam", "Gece"},
                        {"Veri yok", "Veri yok", "Veri yok", "Veri yok", "Veri yok"}},
                new String[]{"", "", "", "", ""}
        );
        JTable miniTable = new JTable(miniTableModel);
        miniTable.setEnabled(false);
        miniTable.setRowHeight(25);
        JScrollPane miniScrollPane = new JScrollPane(miniTable);
        miniScrollPane.setPreferredSize(new Dimension(200, 60));

        dailyMeasurementsPanel.add(miniScrollPane, BorderLayout.CENTER);
        infoPanel.add(dailyMeasurementsPanel);

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

        // TODO: Grafiği gerçek verilerle güncelleyen kod burada olacak

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
        measurementField = new JTextField(10);
        formPanel.add(measurementField, gbc);

        // Periyot
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Ölçüm Zamanı:"), gbc);

        gbc.gridx = 1;
        String[] periods = {
                "Sabah (07:00-08:00)",
                "Öğle (12:00-13:00)",
                "İkindi (15:00-16:00)",
                "Akşam (18:00-19:00)",
                "Gece (22:00-23:00)"
        };
        periodCombo = new JComboBox<>(periods);
        formPanel.add(periodCombo, gbc);

        // Ekle butonu
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton addButton = new JButton("Ölçümü Kaydet");
        addButton.addActionListener(e -> addMeasurement());
        formPanel.add(addButton, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Filtreleme ve tablo paneli
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        // Filtreleme paneli
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("İnsülin Değerlerini Filtrele"));

        filterPanel.add(new JLabel("Başlangıç Tarihi:"));
        JTextField startDateField = new JTextField(10);
        startDateField.setText(LocalDate.now().minusWeeks(1).toString()); // Son 1 hafta
        filterPanel.add(startDateField);

        filterPanel.add(new JLabel("Bitiş Tarihi:"));
        JTextField endDateField = new JTextField(10);
        endDateField.setText(LocalDate.now().toString()); // Bugün
        filterPanel.add(endDateField);

        JButton filterButton = new JButton("Filtrele");
        filterButton.addActionListener(e -> {
            // TODO: Burada tarih aralığına göre filtreleme işlemi yapılacak
            JOptionPane.showMessageDialog(panel, "Seçilen tarih aralığındaki ölçümler filtrelenecek.",
                    "Bilgi", JOptionPane.INFORMATION_MESSAGE);
        });
        filterPanel.add(filterButton);

        centerPanel.add(filterPanel, BorderLayout.NORTH);

        // Ölçüm listesi tablosu
        String[] columnNames = {"Tarih", "Saat", "Değer (mg/dL)", "Periyot", "İnsülin Dozu", "Durum"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        JTable measurementsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(measurementsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Kan Şekeri Ölçümlerim"));

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Ölçüm ekleme işlemini gerçekleştirir
     */
    private void addMeasurement() {
        // Form kontrolü
        String valueStr = measurementField.getText().trim();
        if (valueStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen ölçüm değerini girin!", "Form Hatası", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int value = Integer.parseInt(valueStr);
            if (value <= 0 || value >= 1000) {
                throw new NumberFormatException();
            }

            String periodText = (String) periodCombo.getSelectedItem();

            // Ölçüm nesnesi oluştur
            BloodSugarMeasurement measurement = new BloodSugarMeasurement();
            measurement.setPatient(patient);
            measurement.setPatient_id(patient.getPatient_id());
            measurement.setOlcum_degeri(value);
            measurement.setOlcum_zamani(periodText);
            measurement.setOlcum_tarihi(DateTimeUtil.getCurrentDateTime()); // Şu anki tarih ve saat
            measurement.setInsulin_miktari(0.0);

            // Service aracılığıyla Dao kullanarak tabloya ekler
            boolean success = measurementService.addMeasurement(measurement);

            MeasurementDao measurementDao = new MeasurementDao();
            InsulinReferenceDao insulinReferenceDao = new InsulinReferenceDao();
            // SQLException'i ele alıyoruz
            try {
                measurementDao.updateFlag(measurement.getMeasurement_id());
                int averageValue =(int) measurementDao.getDailyAverage(measurement.getPatient_id(), measurement.getOlcum_tarihi().toLocalDate());
                InsulinReference insulinReference = insulinReferenceDao.findByBloodSugarValue(averageValue);
                measurement.setInsulin_miktari(insulinReference.getInsulin_dose());
                measurementDao.updateInsulinAmount(measurement.getMeasurement_id(),insulinReference.getInsulin_dose());

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage(), "Veritabanı Hatası", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace(); // Loglama için
                return; // Hata durumunda işlemi sonlandır
            }

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Kan şekeri ölçümü başarıyla kaydedildi.",
                        "İşlem Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);

                // Form alanlarını temizle
                measurementField.setText("");
                periodCombo.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ölçüm kaydedilirken bir hata oluştu.",
                        "Kayıt Hatası",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Ölçüm değeri 1-999 arasında bir sayı olmalıdır!",
                    "Doğrulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
        }
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
        dietDetailsArea.setText("Veri yok"); // TODO: Veritabanından yüklenecek
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
        String[] dietStatus = {"Evet, uyguladım", "Hayır, uygulamadım"};
        JComboBox<String> dietStatusCombo = new JComboBox<>(dietStatus);
        reportPanel.add(dietStatusCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton reportButton = new JButton("Bildir");
        reportButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(panel,
                    "Diyet durumu başarıyla bildirildi:\n" + dietStatusCombo.getSelectedItem(),
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);
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
        exerciseDetailsArea.setText("Veri yok"); // TODO: Veritabanından yüklenecek
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
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton reportButton = new JButton("Bildir");
        reportButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(panel,
                    "Egzersiz durumu başarıyla bildirildi.",
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        reportPanel.add(reportButton, gbc);

        panel.add(reportPanel, BorderLayout.CENTER);

        return panel;
    }

    public void refreshData(User user) {
        this.currentUser = user;
        this.patient = patientService.getPatientByUserId(this.currentUser.getUser_id());

        // Hoşgeldiniz mesajını güncelle
        welcomeLabel.setText("Hoş Geldiniz, " + currentUser.getAd() + " " + currentUser.getSoyad());

        // Üst panelde profil resmini güncelle (eğer değiştiyse)
        Component[] components = ((JPanel)((JPanel)getComponent(0)).getComponent(0)).getComponents();
        for (Component c : components) {
            if (c instanceof JLabel && c != welcomeLabel) {
                JLabel imageLabel = (JLabel)c;
                // Mevcut profil resmini kaldır
                imageLabel.setIcon(null);
                // Yeni profil resmini yükle
                if (patient != null && patient.getProfil_resmi() != null && patient.getProfil_resmi().length > 0) {
                    try {
                        ByteArrayInputStream bis = new ByteArrayInputStream(patient.getProfil_resmi());
                        BufferedImage originalImage = ImageIO.read(bis);
                        if (originalImage != null) {
                            Image scaledImage = originalImage.getScaledInstance(75, 75, Image.SCALE_SMOOTH);
                            imageLabel.setIcon(new ImageIcon(scaledImage));
                        } else {
                            createDefaultProfileImage(imageLabel);
                        }
                    } catch (IOException e) {
                        createDefaultProfileImage(imageLabel);
                    }
                } else {
                    createDefaultProfileImage(imageLabel);
                }
                break;
            }
        }

        // TODO: Verileri yükle ve panelleri güncelle - veritabanından güncel bilgileri çek
    }
}