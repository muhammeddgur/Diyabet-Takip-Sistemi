package org.ui;

import org.dao.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.model.*;
import org.service.*;
import org.util.DateTimeUtil;
import org.util.ValidationUtil;
import org.util.PasswordUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Doktor kontrol paneli
 */
public class DoctorDashboard extends JPanel {
    private User currentUser;
    private Integer doctorId;
    private AuthenticationService authService;
    private PatientService patientService;
    private MeasurementService measurementService;
    private DietService dietService;
    private ExerciseService exerciseService;
    private InsulinRecommendationService insulinRecommendationService;
    private RecommendationService recommendationService;
    private MainFrame parent;
    private SymptomService symptomService;
    private DietDao dietDao;
    private ExerciseDao exerciseDao;
    private int bloodSugarValue = 0; // Ölçüm değeri
    private String bloodSugarPeriod = ""; // Zaman dilimi (sabah, öğle, vb.)
    private boolean hasBloodSugarMeasurement = false; // Ölçüm girildi mi?
    private JLabel currentMeasurementLabel; // Mevcut kan şekeri ölçüm bilgisi
    // Sınıf seviyesinde yeni değişkenler ekleyin
    private JLabel patientProfileImageLabel;
    private byte[] patientProfileImageBytes; // Seçilen hasta profil resmi
    // Analiz paneli için gereken değişkenler
    private JPanel mainPanel; // Ana panel referansı
    private JPanel dateRangePanel; // Tarih aralığı seçim paneli
    private JTextField startDateField; // Başlangıç tarihi
    private JTextField endDateField; // Bitiş tarihi


    // Kart layout ve panel referansları
    private CardLayout cardLayout;
    private JPanel centerPanel;
    private static final String TABS_PANEL = "TABS_PANEL";
    private static final String DETAIL_PANEL = "DETAIL_PANEL";

    // Hasta Detay Panel ve Bileşenleri
    private JPanel patientDetailPanel;
    private Patient selectedPatient;
    private JTabbedPane patientDetailTabs;

    private JLabel welcomeLabel;
    private JTabbedPane tabbedPane;
    private JPanel patientListPanel;
    private JPanel patientManagementPanel;
    private JPanel addPatientPanel;
    private JPanel measurementPanel;
    private JPanel dietPlanPanel;
    private JPanel exercisePlanPanel;
    private JButton logoutButton;

    // Hasta yönetimi paneli bileşenleri
    private JTable patientTable;
    private DefaultTableModel patientTableModel;
    private JComboBox<String> bloodLevelFilterCombo;
    private JComboBox<String> symptomFilterCombo;
    private JButton viewPatientButton;

    // Hasta ekleme form alanları
    private JTextField tcKimlikField;
    private JTextField adField;
    private JTextField soyadField;
    private JTextField emailField;
    private JSpinner dogumTarihiSpinner;
    private JComboBox<String> cinsiyetCombo;
    private JPasswordField passwordField;
    // Hasta ekleme form alanları - yeni eklenenler
    private JComboBox<Symptom> addPatientSymptomCombo; // Belirti seçimi için ComboBox
    private JTable addPatientSymptomsTable; // Belirtiler tablosu
    private DefaultTableModel addPatientSymptomsModel; // Belirtiler tablo modeli
    private JComboBox<Diet> addPatientDietCombo; // Diyet seçimi için ComboBox
    private JTextArea addPatientDietDesc; // Diyet açıklaması
    private JComboBox<Exercise> addPatientExerciseCombo; // Egzersiz seçimi için ComboBox
    private JTextArea addPatientExerciseDesc; // Egzersiz açıklaması
    private JLabel dietRecommendationLabel; // Diyet önerisi etiketi
    private JLabel exerciseRecommendationLabel; // Egzersiz önerisi etiketi
    private JLabel customSymptomLabel; // Özel belirti etiketi
    private JTextField customSymptomField; // Özel belirti metin alanı

    // Ölçüm ekleme form alanları
    private JComboBox<Patient> bloodMeasurePatientCombo;
    private JComboBox<Patient> dietPatientCombo;
    private JComboBox<Patient> exercisePatientCombo;
    private JTextField measurementValueField;
    private JComboBox<String> periodCombo;




    // Hasta verileri
    private List<Patient> allPatients;
    private List<Patient> filteredPatients;

    public DoctorDashboard(User user, AuthenticationService authService, PatientService patientService, MainFrame parent) {
        this.currentUser = user;
        this.authService = authService;
        this.patientService = patientService;
        this.measurementService = new org.service.MeasurementService();
        this.dietService = new org.service.DietService();
        this.exerciseService = new org.service.ExerciseService();
        this.insulinRecommendationService = new org.service.InsulinRecommendationService();
        this.recommendationService = new org.service.RecommendationService();
        this.symptomService = new SymptomService();
        this.parent = parent;
        this.allPatients = new ArrayList<>();
        this.filteredPatients = new ArrayList<>();
        // DAO nesnelerini başlat
        this.dietDao = new DietDao();
        this.exerciseDao = new ExerciseDao();

        // Doktor ID'sini bul ve sakla
        try {
            DoctorDao doctorDao = new DoctorDao();
            Doctor doctor = doctorDao.findByUserId(user.getUser_id());
            if (doctor != null) {
                this.doctorId = doctor.getDoctor_id();
                System.out.println("Doktor ID bulundu: " + this.doctorId);
            } else {
                System.err.println("Doktor bilgisi bulunamadı!");
                JOptionPane.showMessageDialog(null, "Doktor bilgisi bulunamadı!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            System.err.println("Doktor bilgisi alınırken hata: " + e.getMessage());
        }

        setLayout(new BorderLayout());
        initComponents();
        loadPatientData();
    }

    private void initComponents() {
        // Üst panel - her zaman sabit kalacak
        JPanel topPanel = new JPanel(new BorderLayout());

        // Sol taraf - Profil resmi ve hoşgeldin mesajı
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // updateProfileImage metodunu kullanarak profil resmi oluştur
        JLabel profileImageLabel = updateProfileImage();
        // Resmin çevresine biraz boşluk ekleyerek daha iyi hizalama
        profileImageLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        profilePanel.add(profileImageLabel);

        // Hoş geldiniz metni
        welcomeLabel = new JLabel("Hoş Geldiniz, Dr. " + currentUser.getAd() + " " + currentUser.getSoyad());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        profilePanel.add(welcomeLabel);

        topPanel.add(profilePanel, BorderLayout.WEST);

        // Sağ taraf - Çıkış butonu
        logoutButton = new JButton("Çıkış Yap");
        logoutButton.addActionListener(e -> parent.showLoginPanel());
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Diğer kodlar (değişmeden kalır)...
        // Merkez panel - CardLayout ile panel geçişlerini yönet
        centerPanel = new JPanel();
        cardLayout = new CardLayout();
        centerPanel.setLayout(cardLayout);

        // Ana sekme paneli
        tabbedPane = new JTabbedPane();

        // Basit Hasta Listesi Sekmesi
        patientListPanel = createPatientListPanel();
        tabbedPane.addTab("Hasta Listesi", patientListPanel);

        // Hasta Yönetimi Sekmesi
        patientManagementPanel = createPatientManagementPanel();
        tabbedPane.addTab("Hasta Yönetimi", patientManagementPanel);

        // Hasta Ekleme Sekmesi - yeni format
        addPatientPanel = createAddPatientPanel();
        tabbedPane.addTab("Hasta Ekle", addPatientPanel);

        // Hasta Detay Paneli
        patientDetailPanel = createPatientDetailPanel();

        // Panelleri CardLayout'a ekle
        centerPanel.add(tabbedPane, TABS_PANEL);
        centerPanel.add(patientDetailPanel, DETAIL_PANEL);

        // Ana panele ekle
        add(centerPanel, BorderLayout.CENTER);

        // Başlangıçta sekme panelini göster
        cardLayout.show(centerPanel, TABS_PANEL);
    }

    private JLabel updateProfileImage() {
        JLabel profileImageLabel = new JLabel();
        profileImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        profileImageLabel.setPreferredSize(new Dimension(80, 80)); // 70x70'ten 80x80'e değiştirildi

        if (currentUser != null && currentUser.getProfil_resmi() != null && currentUser.getProfil_resmi().length > 0) {
            try {
                // Byte dizisinden resim oluştur
                ByteArrayInputStream bais = new ByteArrayInputStream(currentUser.getProfil_resmi());
                BufferedImage img = ImageIO.read(bais);

                // Resim varsa, boyutlandır ve göster
                if (img != null) {
                    Image scaledImg = img.getScaledInstance(75, 75, Image.SCALE_SMOOTH); // 65x65'ten 75x75'e değiştirildi
                    profileImageLabel.setIcon(new ImageIcon(scaledImg));

                    // Resmi ortala
                    profileImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    profileImageLabel.setVerticalAlignment(SwingConstants.CENTER);
                } else {
                    setDefaultProfileImage(profileImageLabel);
                }
            } catch (Exception e) {
                System.err.println("Profil resmi yüklenirken bir hata oluştu: " + e.getMessage());
                setDefaultProfileImage(profileImageLabel);
            }
        } else {
            setDefaultProfileImage(profileImageLabel);
        }

        return profileImageLabel;
    }

    // Varsayılan profil resmi gösterimi için yardımcı metot
    private void setDefaultProfileImage(JLabel imageLabel) {
        // Baş harfler için daha büyük avatar oluşturma (hasta paneliyle eşitlemek için)
        BufferedImage img = new BufferedImage(75, 75, BufferedImage.TYPE_INT_RGB); // 75x75 boyutuna güncellendi
        Graphics2D g2 = img.createGraphics();

        // Arkaplanı mavi yap
        g2.setColor(new Color(70, 130, 180)); // Steel blue
        g2.fillRect(0, 0, 75, 75);

        // Baş harfleri beyaz renkte yaz
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 32)); // Font boyutu artırıldı

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
        imageLabel.setText("");
    }



    private JPanel createAddPatientPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. BÖLÜM: KİŞİSEL BİLGİLER
        JPanel basicInfoPanel = new JPanel(new GridBagLayout());
        basicInfoPanel.setBorder(BorderFactory.createTitledBorder("Kişisel Bilgiler"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // TC Kimlik
        gbc.gridx = 0;
        gbc.gridy = 0;
        basicInfoPanel.add(new JLabel("TC Kimlik No:"), gbc);

        gbc.gridx = 1;
        tcKimlikField = new JTextField(15);
        basicInfoPanel.add(tcKimlikField, gbc);

        // Ad
        gbc.gridx = 0;
        gbc.gridy = 1;
        basicInfoPanel.add(new JLabel("Ad:"), gbc);

        gbc.gridx = 1;
        adField = new JTextField(15);
        basicInfoPanel.add(adField, gbc);

        // Soyad
        gbc.gridx = 0;
        gbc.gridy = 2;
        basicInfoPanel.add(new JLabel("Soyad:"), gbc);

        gbc.gridx = 1;
        soyadField = new JTextField(15);
        basicInfoPanel.add(soyadField, gbc);

        // E-posta
        gbc.gridx = 0;
        gbc.gridy = 3;
        basicInfoPanel.add(new JLabel("E-posta:"), gbc);

        gbc.gridx = 1;
        emailField = new JTextField(15);
        basicInfoPanel.add(emailField, gbc);

        // Şifre
        gbc.gridx = 0;
        gbc.gridy = 4;
        basicInfoPanel.add(new JLabel("Şifre:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        basicInfoPanel.add(passwordField, gbc);

        // Doğum Tarihi
        gbc.gridx = 0;
        gbc.gridy = 5;
        basicInfoPanel.add(new JLabel("Doğum Tarihi:"), gbc);

        gbc.gridx = 1;
        Date defaultDate = new Date();
        defaultDate.setYear(defaultDate.getYear() - 30);
        SpinnerDateModel dateModel = new SpinnerDateModel(defaultDate, null, new Date(), java.util.Calendar.DAY_OF_MONTH);
        dogumTarihiSpinner = new JSpinner(dateModel);
        dogumTarihiSpinner.setEditor(new JSpinner.DateEditor(dogumTarihiSpinner, "dd.MM.yyyy"));
        basicInfoPanel.add(dogumTarihiSpinner, gbc);

        // Cinsiyet
        gbc.gridx = 0;
        gbc.gridy = 6;
        basicInfoPanel.add(new JLabel("Cinsiyet:"), gbc);

        gbc.gridx = 1;
        cinsiyetCombo = new JComboBox<>(new String[] {"Erkek", "Kadın"});
        basicInfoPanel.add(cinsiyetCombo, gbc);

        // Profil Resmi - YENİ EKLENEN KISIM
        gbc.gridx = 0;
        gbc.gridy = 7;
        basicInfoPanel.add(new JLabel("Profil Resmi:"), gbc);

        gbc.gridx = 1;
        JPanel imagePanel = new JPanel(new BorderLayout(5, 5));

        // Profil resmi önizleme etiketi
        patientProfileImageLabel = new JLabel();
        patientProfileImageLabel.setPreferredSize(new Dimension(100, 120));
        patientProfileImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        patientProfileImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        patientProfileImageLabel.setText("Resim yok");

        // Resim seçme butonu
        JButton selectImageButton = new JButton("Resim Seç");
        selectImageButton.addActionListener(e -> selectPatientImage());

        // Panelleri düzenle
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(selectImageButton);

        imagePanel.add(buttonPanel, BorderLayout.NORTH);
        imagePanel.add(patientProfileImageLabel, BorderLayout.CENTER);

        basicInfoPanel.add(imagePanel, gbc);

        mainPanel.add(basicInfoPanel);

        // 2. BÖLÜM: BELİRTİLER
        JPanel symptomsPanel = new JPanel(new BorderLayout(5, 5));
        symptomsPanel.setBorder(BorderFactory.createTitledBorder("Belirtiler"));

        // Belirti seçim alanı
        JPanel symptomInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        symptomInputPanel.add(new JLabel("Belirti:"));

        addPatientSymptomCombo = new JComboBox<>();
        try {
            List<Symptom> allSymptoms = symptomService.getAllSymptoms();
            for (Symptom symptom : allSymptoms) {
                addPatientSymptomCombo.addItem(symptom);
            }

            // "Diğer" seçeneğini ekle
            Symptom otherSymptom = new Symptom();
            otherSymptom.setSymptom_id(-1);
            otherSymptom.setSymptom_adi("Diğer");
            addPatientSymptomCombo.addItem(otherSymptom);
        } catch (Exception e) {
            System.err.println("Belirtiler yüklenirken hata: " + e.getMessage());
        }
        symptomInputPanel.add(addPatientSymptomCombo);

        // Özel belirti alanı
        customSymptomField = new JTextField(15);
        customSymptomLabel = new JLabel("Özel Belirti:");
        customSymptomLabel.setVisible(false);
        customSymptomField.setVisible(false);

        addPatientSymptomCombo.addActionListener(e -> {
            Symptom selected = (Symptom) addPatientSymptomCombo.getSelectedItem();
            boolean isOther = selected != null && selected.getSymptom_id() == -1;
            customSymptomLabel.setVisible(isOther);
            customSymptomField.setVisible(isOther);
        });

        symptomInputPanel.add(customSymptomLabel);
        symptomInputPanel.add(customSymptomField);

        JButton addSymptomButton = new JButton("Ekle");
        addSymptomButton.addActionListener(e -> {
            Symptom selected = (Symptom) addPatientSymptomCombo.getSelectedItem();
            if (selected != null) {
                String symptomName = selected.getSymptom_adi();

                // "Diğer" seçilmişse özel belirti adını kullan
                if (selected.getSymptom_id() == -1) {
                    if (customSymptomField.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(symptomsPanel,
                                "Lütfen özel belirti adı girin",
                                "Uyarı",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    symptomName = customSymptomField.getText().trim();
                }

                // Belirti daha önce eklendi mi kontrol et
                for (int i = 0; i < addPatientSymptomsModel.getRowCount(); i++) {
                    if (addPatientSymptomsModel.getValueAt(i, 0).equals(symptomName)) {
                        JOptionPane.showMessageDialog(symptomsPanel,
                                "Bu belirti zaten eklenmiş",
                                "Uyarı",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                // Belirtiyi tabloya ekle
                addPatientSymptomsModel.addRow(new Object[] {symptomName, false});

                // Alanları temizle
                addPatientSymptomCombo.setSelectedIndex(0);
                customSymptomField.setText("");
                customSymptomField.setVisible(false);
                customSymptomLabel.setVisible(false);
            }
        });
        symptomInputPanel.add(addSymptomButton);

        symptomsPanel.add(symptomInputPanel, BorderLayout.NORTH);

        // Belirti listesi
        String[] columns = {"Belirti", "Kaldır"};
        addPatientSymptomsModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        addPatientSymptomsTable = new JTable(addPatientSymptomsModel);
        addPatientSymptomsTable.getColumnModel().getColumn(1).setMaxWidth(50);

        // Kaldır sütunu için tıklama dinleyicisi
        addPatientSymptomsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = addPatientSymptomsTable.rowAtPoint(evt.getPoint());
                int col = addPatientSymptomsTable.columnAtPoint(evt.getPoint());
                if (col == 1 && row >= 0) {
                    addPatientSymptomsModel.removeRow(row);
                }
            }
        });

        JScrollPane symptomsScrollPane = new JScrollPane(addPatientSymptomsTable);
        symptomsScrollPane.setPreferredSize(new Dimension(400, 100));
        symptomsPanel.add(symptomsScrollPane, BorderLayout.CENTER);

        mainPanel.add(symptomsPanel);

// 3. BÖLÜM: KAN ŞEKERİ ÖLÇÜM DEĞERLERİ
        JPanel bloodSugarPanel = new JPanel(new BorderLayout(5, 5));
        bloodSugarPanel.setBorder(BorderFactory.createTitledBorder("Kan Şekeri Ölçümü"));

// Üst kısmı düzenle - Grid yerine daha esnek bir düzen
        JPanel bloodSugarInputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints bloodSugarGbc = new GridBagConstraints();
        bloodSugarGbc.fill = GridBagConstraints.HORIZONTAL;
        bloodSugarGbc.insets = new Insets(5, 5, 5, 5);

// Zaman dilimi seçimi
        bloodSugarGbc.gridx = 0;
        bloodSugarGbc.gridy = 0;
        bloodSugarInputPanel.add(new JLabel("Zaman Dilimi:"), bloodSugarGbc);

        bloodSugarGbc.gridx = 1;
        periodCombo = new JComboBox<>(new String[] {
                "Sabah (07:00-08:00)",
                "Öğle (12:00-13:00)",
                "İkindi (15:00-16:00)",
                "Akşam (18:00-19:00)",
                "Gece (22:00-23:00)"
        });
        bloodSugarInputPanel.add(periodCombo, bloodSugarGbc);

// Ölçüm değeri girişi
        bloodSugarGbc.gridx = 0;
        bloodSugarGbc.gridy = 1;
        bloodSugarInputPanel.add(new JLabel("Ölçüm Değeri (mg/dL):"), bloodSugarGbc);

        bloodSugarGbc.gridx = 1;
        measurementValueField = new JTextField(10);
        bloodSugarInputPanel.add(measurementValueField, bloodSugarGbc);

        // Mevcut ölçüm bilgisi (varsa gösterilecek)
        bloodSugarGbc.gridx = 0;
        bloodSugarGbc.gridy = 2;
        bloodSugarGbc.gridwidth = 2;
        currentMeasurementLabel = new JLabel("Henüz ölçüm girilmedi");  // DÜZELTME
        bloodSugarInputPanel.add(currentMeasurementLabel, bloodSugarGbc);

        bloodSugarPanel.add(bloodSugarInputPanel, BorderLayout.CENTER);

// Buton Paneli
        JPanel bloodSugarButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

// Ölçümü Kaydet butonu
        JButton addMeasurementButton = new JButton("Ölçümü Kaydet");
        addMeasurementButton.addActionListener(e -> {
            String valueStr = measurementValueField.getText().trim();
            if (valueStr.isEmpty()) {
                JOptionPane.showMessageDialog(bloodSugarPanel,
                        "Lütfen ölçüm değerini girin!",
                        "Form Hatası",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int value = Integer.parseInt(valueStr);
                if (value <= 0 || value >= 1000) {
                    throw new NumberFormatException();
                }

                String periodText = (String) periodCombo.getSelectedItem();

                // Değerleri doğrudan sakla
                bloodSugarValue = value;
                bloodSugarPeriod = periodText;
                hasBloodSugarMeasurement = true;

                // Kullanıcıya bilgi ver
                currentMeasurementLabel.setText("Mevcut ölçüm: " + value + " mg/dL (" + periodText + ")");

                JOptionPane.showMessageDialog(bloodSugarPanel,
                        "Ölçüm değeri kaydedildi: " + value + " mg/dL (" + periodText + ")",
                        "İşlem Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);

                // Form alanını temizle
                measurementValueField.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(bloodSugarPanel,
                        "Ölçüm değeri 1-999 arasında bir sayı olmalıdır!",
                        "Doğrulama Hatası",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        bloodSugarButtonPanel.add(addMeasurementButton);
        bloodSugarPanel.add(bloodSugarButtonPanel, BorderLayout.SOUTH);

        mainPanel.add(bloodSugarPanel);

        // 4. BÖLÜM: ÖNERİLER
        JPanel recommendationsPanel = new JPanel(new BorderLayout(5, 5));
        recommendationsPanel.setBorder(BorderFactory.createTitledBorder("Diyet ve Egzersiz Önerileri"));

        JButton generateButton = new JButton("Belirtilere ve Kan Şekerine Göre Öneriler Oluştur");
        generateButton.addActionListener(e -> generateRecommendations());

        recommendationsPanel.add(generateButton, BorderLayout.NORTH);

        JPanel recommendationsLabelsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        dietRecommendationLabel = new JLabel("<html><b>Diyet Önerisi:</b> Henüz oluşturulmadı</html>");
        exerciseRecommendationLabel = new JLabel("<html><b>Egzersiz Önerisi:</b> Henüz oluşturulmadı</html>");
        recommendationsLabelsPanel.add(dietRecommendationLabel);
        recommendationsLabelsPanel.add(exerciseRecommendationLabel);

        recommendationsPanel.add(recommendationsLabelsPanel, BorderLayout.CENTER);

        mainPanel.add(recommendationsPanel);

        // 5. BÖLÜM: DİYET VE EGZERSİZ PLANLARI
        JPanel plansPanel = new JPanel(new GridLayout(2, 1, 5, 10));

        // Diyet planı
        JPanel dietPanel = new JPanel(new BorderLayout(5, 5));
        dietPanel.setBorder(BorderFactory.createTitledBorder("Diyet Planı"));

        JPanel dietSelectionPanel = new JPanel(new BorderLayout());
        dietSelectionPanel.add(new JLabel("Diyet Planı Seçin:"), BorderLayout.WEST);

        addPatientDietCombo = new JComboBox<>();
        try {
            List<Diet> allDiets = dietDao.findAll();
            for (Diet diet : allDiets) {
                addPatientDietCombo.addItem(diet);
            }
        } catch (Exception e) {
            System.err.println("Diyet planları yüklenirken hata: " + e.getMessage());
        }

        dietSelectionPanel.add(addPatientDietCombo, BorderLayout.CENTER);
        dietPanel.add(dietSelectionPanel, BorderLayout.NORTH);

        addPatientDietDesc = new JTextArea(3, 30);
        addPatientDietDesc.setEditable(false);
        addPatientDietDesc.setLineWrap(true);
        addPatientDietDesc.setWrapStyleWord(true);
        JScrollPane dietDescScroll = new JScrollPane(addPatientDietDesc);
        dietPanel.add(dietDescScroll, BorderLayout.CENTER);

        // Diyet seçimi değiştiğinde açıklamayı güncelle
        addPatientDietCombo.addActionListener(e -> {
            Diet selectedDiet = (Diet) addPatientDietCombo.getSelectedItem();
            if (selectedDiet != null) {
                addPatientDietDesc.setText(selectedDiet.getAciklama());
            }

        });

        // Egzersiz planı
        JPanel exercisePanel = new JPanel(new BorderLayout(5, 5));
        exercisePanel.setBorder(BorderFactory.createTitledBorder("Egzersiz Planı"));

        JPanel exerciseSelectionPanel = new JPanel(new BorderLayout());
        exerciseSelectionPanel.add(new JLabel("Egzersiz Planı Seçin:"), BorderLayout.WEST);

        addPatientExerciseCombo = new JComboBox<>();
        try {
            List<Exercise> allExercises = exerciseDao.findAll();
            for (Exercise exercise : allExercises) {
                addPatientExerciseCombo.addItem(exercise);
            }
        } catch (Exception e) {
            System.err.println("Egzersiz planları yüklenirken hata: " + e.getMessage());
        }

        exerciseSelectionPanel.add(addPatientExerciseCombo, BorderLayout.CENTER);
        exercisePanel.add(exerciseSelectionPanel, BorderLayout.NORTH);

        addPatientExerciseDesc = new JTextArea(3, 30);
        addPatientExerciseDesc.setEditable(false);
        addPatientExerciseDesc.setLineWrap(true);
        addPatientExerciseDesc.setWrapStyleWord(true);
        JScrollPane exerciseDescScroll = new JScrollPane(addPatientExerciseDesc);
        exercisePanel.add(exerciseDescScroll, BorderLayout.CENTER);

        // Egzersiz seçimi değiştiğinde açıklamayı güncelle
        addPatientExerciseCombo.addActionListener(e -> {
            Exercise selectedExercise = (Exercise) addPatientExerciseCombo.getSelectedItem();
            if (selectedExercise != null) {
                addPatientExerciseDesc.setText(selectedExercise.getAciklama());
            }
        });

        plansPanel.add(dietPanel);
        plansPanel.add(exercisePanel);

        mainPanel.add(plansPanel);

        // 6. BÖLÜM: KAYDET BUTONU
        JPanel saveButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Hastayı Tüm Bilgilerle Kaydet");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.addActionListener(e -> savePatientWithAllInfo());
        saveButtonPanel.add(saveButton);

        mainPanel.add(saveButtonPanel);

        // Scrollable Panel
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(scrollPane, BorderLayout.CENTER);

        return containerPanel;
    }

    // Hasta için resim seçme metodu
    private void selectPatientImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Hasta Profil Resmi Seç");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Resim Dosyaları", "jpg", "jpeg", "png"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                System.out.println("Seçilen hasta profil resmi: " + selectedFile.getAbsolutePath());

                // Resmin boyutunu kontrol et (maksimum 2MB)
                if (selectedFile.length() > 2 * 1024 * 1024) {
                    JOptionPane.showMessageDialog(this,
                            "Resim dosyası çok büyük (maksimum 2MB).",
                            "Dosya Boyutu Hatası",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Resmi oku
                BufferedImage originalImage = ImageIO.read(selectedFile);
                if (originalImage == null) {
                    JOptionPane.showMessageDialog(this,
                            "Seçilen dosya geçerli bir resim değil.",
                            "Resim Okuma Hatası",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Resmi yeniden boyutlandırma (en boy oranını koru)
                int targetWidth = 200;
                int targetHeight = 200;

                // Resmi daha küçük boyuta getir
                BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = resizedImage.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
                g.dispose();

                // Önizleme için resmi göster (daha küçük boyutta)
                int previewWidth = 90;
                int previewHeight = 90;
                Image scaledImg = resizedImage.getScaledInstance(previewWidth, previewHeight, Image.SCALE_SMOOTH);
                patientProfileImageLabel.setIcon(new ImageIcon(scaledImg));
                patientProfileImageLabel.setText(null);

                // Resmi byte dizisine dönüştür
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "png", baos);
                patientProfileImageBytes = baos.toByteArray();
                System.out.println("Hasta profil resmi byte dizisine dönüştürüldü: " + patientProfileImageBytes.length + " bytes");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Resim yüklenirken bir hata oluştu: " + e.getMessage(),
                        "Resim Yükleme Hatası",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();

                // Hata durumunda resmi temizle
                patientProfileImageBytes = null;
                patientProfileImageLabel.setIcon(null);
                patientProfileImageLabel.setText("Resim yok");
            }
        }
    }

    private void generateRecommendations() {
        // Kan şekeri kontrolü
        if (!hasBloodSugarMeasurement) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen önce kan şekeri ölçümü yapın.",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Belirtiler kontrolü
        if (addPatientSymptomsModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "En az bir belirti eklemelisiniz.",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Belirtileri topla
            List<String> symptomNames = new ArrayList<>();
            for (int i = 0; i < addPatientSymptomsModel.getRowCount(); i++) {
                symptomNames.add((String) addPatientSymptomsModel.getValueAt(i, 0));
            }

            // Kan şekeri değerini doğrudan kullan
            int bloodSugar = bloodSugarValue;

            // Önerileri al
            Diet recommendedDiet = recommendationService.recommendDietBySymptoms(symptomNames, bloodSugar);
            Exercise recommendedExercise = recommendationService.recommendExerciseBySymptoms(symptomNames, bloodSugar);

            // Diyet önerisi
            if (recommendedDiet != null) {
                dietRecommendationLabel.setText("<html><b>Diyet Önerisi:</b> " + recommendedDiet.getDiet_adi() + "</html>");

                // ComboBox'ta seç
                for (int i = 0; i < addPatientDietCombo.getItemCount(); i++) {
                    Diet diet = (Diet) addPatientDietCombo.getItemAt(i);
                    if (diet.getDiet_id() == recommendedDiet.getDiet_id()) {
                        addPatientDietCombo.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                dietRecommendationLabel.setText("<html><b>Diyet Önerisi:</b> Öneri oluşturulamadı. Lütfen uygun bir diyet seçin.</html>");
            }

            // Egzersiz önerisi
            if (recommendedExercise != null) {
                exerciseRecommendationLabel.setText("<html><b>Egzersiz Önerisi:</b> " + recommendedExercise.getExercise_adi() + "</html>");

                // ComboBox'ta seç
                for (int i = 0; i < addPatientExerciseCombo.getItemCount(); i++) {
                    Exercise exercise = (Exercise) addPatientExerciseCombo.getItemAt(i);
                    if (exercise.getExercise_id() == recommendedExercise.getExercise_id()) {
                        addPatientExerciseCombo.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                exerciseRecommendationLabel.setText("<html><b>Egzersiz Önerisi:</b> Öneri oluşturulamadı. Lütfen uygun bir egzersiz seçin.</html>");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Öneriler oluşturulurken bir hata oluştu: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Hastayı tüm bilgilerle kaydeder
     */
    private void savePatientWithAllInfo() {
        // 1. Temel bilgileri doğrula
        if (!validateBasicInfo()) {
            return;
        }

        // 2. Belirtileri kontrol et
        if (addPatientSymptomsModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "En az bir belirti eklemelisiniz.",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Kan şekeri değerini kontrol et
        if (!hasBloodSugarMeasurement) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen kan şekeri ölçümü yapın.",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 4. Diyet ve egzersiz seçimlerini kontrol et
        if (addPatientDietCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen bir diyet planı seçin.",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (addPatientExerciseCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen bir egzersiz planı seçin.",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 5. Hasta nesnesini oluştur
        Patient patient = new Patient();
        patient.setTc_kimlik(tcKimlikField.getText().trim());
        patient.setAd(adField.getText().trim());
        patient.setSoyad(soyadField.getText().trim());
        patient.setEmail(emailField.getText().trim());

        // Profil resmi ekle - YENİ EKLENEN KISIM
        patient.setProfil_resmi(patientProfileImageBytes);

        // Doğum tarihi
        Date spinnerDate = (Date) dogumTarihiSpinner.getValue();
        LocalDate dogumTarihi = spinnerDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        patient.setDogum_tarihi(dogumTarihi);

        // Cinsiyet
        String cinsiyet = cinsiyetCombo.getSelectedItem().toString().substring(0, 1); // E veya K
        patient.setCinsiyet(cinsiyet.charAt(0));
        patient.setKullanici_tipi("hasta");

        // Şifre
        String password = new String(passwordField.getPassword());
        try {
            String hashedPassword = PasswordUtil.hashPassword(password);
            patient.setPassword(hashedPassword);
        } catch (NoSuchAlgorithmException ex) {
            JOptionPane.showMessageDialog(this,
                    "Şifre hashleme sırasında bir hata oluştu: " + ex.getMessage(),
                    "Sistem Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Doktor bilgisi
        try {
            DoctorDao doctorDao = new DoctorDao();
            Doctor currentDoctor = doctorDao.findByUserId(currentUser.getUser_id());
            if (currentDoctor != null) {
                patient.setDoctor(currentDoctor);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Doktor bilgisi bulunamadı!",
                        "Sistem Hatası",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Doktor bilgisi alınırken bir hata oluştu: " + e.getMessage(),
                    "Veritabanı Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 6. Hastayı kaydet
        Patient addedPatient = patientService.addPatient(patient, password);

        if (addedPatient != null) {
            try {
                // 7. Belirtileri ekle
                for (int i = 0; i < addPatientSymptomsModel.getRowCount(); i++) {
                    String symptomName = (String) addPatientSymptomsModel.getValueAt(i, 0);
                    symptomService.addSymptomToPatientByName(
                            addedPatient.getPatient_id(),
                            symptomName,
                            "",
                            LocalDate.now()
                    );
                }

                // 8. Kan şekeri ölçümünü ekle
                if (hasBloodSugarMeasurement) { // Ölçüm yapıldıysa
                    try {
                        BloodSugarMeasurement measurement = new BloodSugarMeasurement();
                        measurement.setPatient(addedPatient);
                        measurement.setPatient_id(addedPatient.getPatient_id());
                        measurement.setOlcum_degeri(bloodSugarValue);
                        measurement.setOlcum_zamani(bloodSugarPeriod);
                        measurement.setOlcum_tarihi(DateTimeUtil.getCurrentDateTime());
                        measurement.setIs_valid_time(false); // Geçerli ölçüm zamanı
                        measurement.setInsulin_miktari(0.0);

                        // Ölçümü veritabanına kaydet
                        boolean success = measurementService.addMeasurement(measurement,false);

                        if (success) {
                            JOptionPane.showMessageDialog(this,
                                    "Kan şekeri ölçümü başarıyla kaydedildi.",
                                    "İşlem Başarılı",
                                    JOptionPane.INFORMATION_MESSAGE);
                            System.out.println("Kan şekeri ölçümü kaydedildi: " + bloodSugarValue + " mg/dL");
                        }
                    } catch (Exception ex) {
                        System.err.println("Ölçüm kaydedilirken hata: " + ex.getMessage());
                    }
                }

                // 9. Diyet planını ekle
                Diet selectedDiet = (Diet) addPatientDietCombo.getSelectedItem();
                boolean dietSuccess = dietDao.assignDietToPatient(
                        addedPatient.getPatient_id(),
                        selectedDiet.getDiet_id(),
                        doctorId
                );

                // 10. Egzersiz planını ekle
                Exercise selectedExercise = (Exercise) addPatientExerciseCombo.getSelectedItem();
                boolean exerciseSuccess = exerciseDao.assignExerciseToPatient(
                        addedPatient.getPatient_id(),
                        selectedExercise.getExercise_id(),
                        doctorId
                );

                JOptionPane.showMessageDialog(this,
                        "Hasta ve tüm ilişkili bilgileri başarıyla kaydedildi!",
                        "İşlem Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);

                // Formu temizle
                clearAllFields();

                // Hasta listesini yenile
                refreshPatientList();
                loadPatientData();
                tabbedPane.setSelectedIndex(0); // Hasta listesi sekmesine geç

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Hasta kaydedildi ancak ek bilgiler eklenirken bir hata oluştu: " + e.getMessage(),
                        "Kısmi Başarı",
                        JOptionPane.WARNING_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Hasta eklenemedi. Bu TC Kimlik No veya e-posta zaten kullanılıyor olabilir.",
                    "İşlem Hatası",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Temel hasta bilgilerinin doğruluğunu kontrol eder
     */
    private boolean validateBasicInfo() {
        // TC Kimlik kontrolü
        String tcKimlik = tcKimlikField.getText().trim();
        if (tcKimlik.isEmpty() || !ValidationUtil.validateTcKimlik(tcKimlik)) {
            JOptionPane.showMessageDialog(this,
                    "Geçerli bir TC Kimlik No girmelisiniz.",
                    "Doğrulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Ad ve soyad kontrolü
        String ad = adField.getText().trim();
        String soyad = soyadField.getText().trim();
        if (ad.isEmpty() || soyad.isEmpty() || !ValidationUtil.validateName(ad) || !ValidationUtil.validateName(soyad)) {
            JOptionPane.showMessageDialog(this,
                    "Geçerli bir ad ve soyad girmelisiniz.",
                    "Doğrulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // E-posta kontrolü
        String email = emailField.getText().trim();
        if (email.isEmpty() || !ValidationUtil.validateEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Geçerli bir e-posta adresi girmelisiniz.",
                    "Doğrulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Şifre kontrolü
        String password = new String(passwordField.getPassword());
        if (password.isEmpty() || !ValidationUtil.validatePassword(password)) {
            JOptionPane.showMessageDialog(this,
                    "Şifre en az 8 karakter uzunluğunda olmalı ve büyük harf, küçük harf, rakam ve özel karakter içermelidir.",
                    "Doğrulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void clearAllFields() {
        // Temel alanlar
        tcKimlikField.setText("");
        adField.setText("");
        soyadField.setText("");
        emailField.setText("");
        passwordField.setText("");

        // Doğum tarihi
        Date defaultDate = new Date();
        defaultDate.setYear(defaultDate.getYear() - 30);
        dogumTarihiSpinner.setValue(defaultDate);

        // Cinsiyet
        cinsiyetCombo.setSelectedIndex(0);

        // Belirtiler
        addPatientSymptomsModel.setRowCount(0);

        // Profil resmi temizleme - YENİ EKLENEN KISIM
        patientProfileImageBytes = null;
        if (patientProfileImageLabel != null) {
            patientProfileImageLabel.setIcon(null);
            patientProfileImageLabel.setText("Resim yok");
        }

        // Kan şekeri ölçümünü sıfırla
        bloodSugarValue = 0;
        bloodSugarPeriod = "";
        hasBloodSugarMeasurement = false;
        measurementValueField.setText("");
        periodCombo.setSelectedIndex(0);

        // Mevcut ölçüm bilgisini sıfırla - NULL KONTROLÜ EKLEYİN
        if (currentMeasurementLabel != null) {  // Bu kontrolü ekleyin
            currentMeasurementLabel.setText("Henüz ölçüm girilmedi");
        }

        // Mevcut ölçüm bilgisini sıfırla
        currentMeasurementLabel.setText("Henüz ölçüm girilmedi");

        // Öneriler
        dietRecommendationLabel.setText("<html><b>Diyet Önerisi:</b> Henüz oluşturulmadı</html>");
        exerciseRecommendationLabel.setText("<html><b>Egzersiz Önerisi:</b> Henüz oluşturulmadı</html>");

        // Diyet ve egzersiz
        if (addPatientDietCombo.getItemCount() > 0) {
            addPatientDietCombo.setSelectedIndex(0);
        }
        if (addPatientExerciseCombo.getItemCount() > 0) {
            addPatientExerciseCombo.setSelectedIndex(0);
        }
    }

    /**
     * Mevcut clearAddPatientFields metodunu bu metoda yönlendir
     */
    private void clearAddPatientFields() {
        clearAllFields();
    }

    /**
     * Hasta combo box'ını günceller
     */
    private JComboBox<Patient> updatePatientCombo(JComboBox<Patient> combo) {
        combo.removeAllItems();
        for (Patient patient : allPatients) {
            combo.addItem(patient);
        }
        return combo;
    }

    private JPanel createPatientListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Hasta listesini bir tablo olarak göster
        String[] columnNames = {"TC Kimlik", "Ad", "Soyad", "E-posta", "Cinsiyet"};

        // doctorId kullanarak hastaları al
        List<Patient> patients = patientService.getDoctorPatients(doctorId);
        System.out.println("Bulunan hasta sayısı: " + patients.size());

        Object[][] data = new Object[patients.size()][5];

        for (int i = 0; i < patients.size(); i++) {
            Patient patient = patients.get(i);
            data[i][0] = patient.getTc_kimlik();
            data[i][1] = patient.getAd();
            data[i][2] = patient.getSoyad();
            data[i][3] = patient.getEmail();
            data[i][4] = patient.getCinsiyet() == 'E' ? "Erkek" : "Kadın";
        }

        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton viewButton = new JButton("Hasta Detayını Görüntüle");
        viewButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String tcKimlik = (String) table.getValueAt(selectedRow, 0);
                showPatientDetail(tcKimlik);
            } else {
                JOptionPane.showMessageDialog(panel,
                        "Lütfen bir hasta seçin.",
                        "Hasta Seçilmedi",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(viewButton);

        JButton refreshButton = new JButton("Yenile");
        refreshButton.addActionListener(e -> refreshPatientList());
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPatientManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Filtreleme kontrolleri
        JPanel filterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Kan seviyesi filtresi - Bu kısım sabit kalabilir
        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("Kan Şekeri Seviyesi:"), gbc);

        gbc.gridx = 1;
        bloodLevelFilterCombo = new JComboBox<>(new String[] {
                "Tümü",
                "Düşük-Hipoglisemi (70 mg/dL altı)",
                "Normal (70-99 mg/dL)",
                "Orta-Prediyabet (100-125 mg/dL)",
                "Yüksek-Diyabet (126 mg/dL üstü)"
        });
        filterPanel.add(bloodLevelFilterCombo, gbc);

        // Belirti filtresi - Veritabanından gerçek belirtileri al
        gbc.gridx = 0;
        gbc.gridy = 1;
        filterPanel.add(new JLabel("Hastalık Belirtisi:"), gbc);

        gbc.gridx = 1;

        // Veritabanından tüm semptomları al
        List<Symptom> allSymptoms = symptomService.getAllSymptoms();
        List<String> symptomOptions = new ArrayList<>();
        symptomOptions.add("Tümü");  // İlk seçenek her zaman "Tümü" olsun

        // Tüm semptom adlarını listeye ekle
        for (Symptom symptom : allSymptoms) {
            symptomOptions.add(symptom.getSymptom_adi());
        }

        // Combobox'ı oluştur
        symptomFilterCombo = new JComboBox<>(symptomOptions.toArray(new String[0]));
        filterPanel.add(symptomFilterCombo, gbc);

        // Filtreleri uygula/temizle butonları
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton applyFilterButton = new JButton("Filtreleri Uygula");
        applyFilterButton.addActionListener(e -> applyFilters());
        buttonPanel.add(applyFilterButton);

        JButton clearFilterButton = new JButton("Filtreleri Temizle");
        clearFilterButton.addActionListener(e -> clearFilters());
        buttonPanel.add(clearFilterButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        filterPanel.add(buttonPanel, gbc);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Hasta tablosu - Bu kısım değişmiyor
        String[] columnNames = {
                "TC Kimlik",
                "Ad",
                "Soyad",
                "E-posta",
                "Cinsiyet",
                "Son Ölçüm",
                "Belirti Sayısı"
        };

        patientTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabloyu salt okunur yap
            }
        };

        patientTable = new JTable(patientTableModel);
        patientTable.getSelectionModel().addListSelectionListener(e -> {
            // Seçim değiştiğinde detay panelini güncelle
            if (!e.getValueIsAdjusting() && patientTable.getSelectedRow() != -1) {
                String tcKimlik = (String) patientTable.getValueAt(patientTable.getSelectedRow(), 0);
                if (tcKimlik != null && !tcKimlik.isEmpty()) {
                    viewPatientButton.setEnabled(true);
                }
            }
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(patientTableModel);
        patientTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(patientTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Alt kısım - hasta detay bilgisi & görüntüleme butonu
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Görüntüleme butonu
        viewPatientButton = new JButton("Hasta Detayını Görüntüle");
        viewPatientButton.setEnabled(false);
        viewPatientButton.addActionListener(e -> {
            int selectedRow = patientTable.getSelectedRow();
            if (selectedRow >= 0) {
                String tcKimlik = (String) patientTable.getValueAt(selectedRow, 0);
                showPatientDetail(tcKimlik);
            }
        });

        JPanel buttonContainerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonContainerPanel.add(viewPatientButton);
        bottomPanel.add(buttonContainerPanel, BorderLayout.SOUTH);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPatientDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Hasta bilgileri paneli
        JPanel infoPanel = createPatientInfoPanel();
        panel.add(infoPanel, BorderLayout.NORTH);

        // Sekme paneli
        patientDetailTabs = new JTabbedPane();

        // Kan şekeri takip sekmesi - Gün ve saat bazlı takip
        JPanel bloodSugarPanel = createBloodSugarPanel();
        patientDetailTabs.addTab("Kan Şekeri Takibi", bloodSugarPanel);

        // İlişki analizi sekmesi - Kan şekeri, diyet ve egzersiz ilişkisi grafiği
        JPanel analysisPanel = createAnalysisPanel();
        patientDetailTabs.addTab("İlişki Analizi", analysisPanel);

        // YENİ: Diyet ve egzersiz takip sekmesi
        JPanel dietExercisePanel = createDietExerciseTrackingPanel();
        patientDetailTabs.addTab("Diyet ve Egzersiz Takibi", dietExercisePanel);

        // Uyarılar sekmesi - Hastaların güne göre uyarıları
        JPanel alertsPanel = createAlertsPanel();
        patientDetailTabs.addTab("Uyarılar", alertsPanel);

        panel.add(patientDetailTabs, BorderLayout.CENTER);

        return panel;
    }

    // Diyet ve egzersiz takip paneli - Hastaların diyet ve egzersiz geçmişi
    private JPanel createDietExerciseTrackingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Üst kısım - Genel uyum oranları
        JPanel summaryPanel = new JPanel(new GridLayout(3, 1, 5, 15));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Uyum Oranları"));

        // Tarih aralığı seçimi
        JPanel dateRangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateRangePanel.add(new JLabel("Tarih Aralığı: "));
        JComboBox<String> dateRangeCombo = new JComboBox<>(new String[] {
                "Son 7 Gün", "Son 30 Gün", "Son 3 Ay", "Tüm Zamanlar"
        });
        dateRangeCombo.setSelectedItem("Tüm Zamanlar"); // Default olarak son 7 gün seçili olsun
        dateRangePanel.add(dateRangeCombo);

        // Progress bar'lar ve tablolar için referans değişkenleri
        final JProgressBar dietProgressBar = new JProgressBar(0, 100);
        dietProgressBar.setValue(0);
        dietProgressBar.setStringPainted(true);
        dietProgressBar.setString("Veri yok");

        final JProgressBar exerciseProgressBar = new JProgressBar(0, 100);
        exerciseProgressBar.setValue(0);
        exerciseProgressBar.setStringPainted(true);
        exerciseProgressBar.setString("Veri yok");

        // Tablolar için model değişkenleri
        final DefaultTableModel dietModel = new DefaultTableModel(
                new String[]{"Tarih", "Uygulandı"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        final DefaultTableModel exerciseModel = new DefaultTableModel(
                new String[]{"Tarih", "Uygulandı"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Güncelleme butonu ve işleyicisi
        JButton updateButton = new JButton("Güncelle");
        updateButton.addActionListener(e -> {
            if (selectedPatient == null || selectedPatient.getPatient_id() == null) {
                JOptionPane.showMessageDialog(panel,
                        "Lütfen önce bir hasta seçin.",
                        "Hasta Seçilmedi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Seçilen tarih aralığına göre verileri yükle
            LocalDate endDate = LocalDate.now();
            LocalDate startDate;

            String selectedRange = (String) dateRangeCombo.getSelectedItem();
            if ("Son 7 Gün".equals(selectedRange)) {
                startDate = endDate.minusDays(7);
            } else if ("Son 30 Gün".equals(selectedRange)) {
                startDate = endDate.minusDays(30);
            } else if ("Son 3 Ay".equals(selectedRange)) {
                startDate = endDate.minusMonths(3);
            } else { // "Tüm Zamanlar"
                startDate = endDate.minusYears(5);
            }

            try {
                // ---------- 1. DİYET VERİLERİNİ YÜKLE ----------
                dietModel.setRowCount(0); // Tabloyu temizle

                DietTrackingDao dietTrackingDao = new DietTrackingDao();
                PatientDietsDao patientDietsDao = new PatientDietsDao();

                try {
                    // Diyet uyum oranını hesapla
                    double dietCompliance = dietTrackingDao.getComplianceRatio(
                            selectedPatient.getPatient_id(), startDate, endDate);

                    // Progress bar'ı güncelle
                    dietProgressBar.setValue((int) dietCompliance);
                    dietProgressBar.setString("%" + String.format("%.1f", dietCompliance));

                    // Tüm takip verilerini getir
                    List<DietTracking> allTrackings = dietTrackingDao.findAllByPatientId(selectedPatient.getPatient_id());
                    List<DietTracking> filteredTrackings = new ArrayList<>();

                    // Tarih aralığına göre filtrele
                    for (DietTracking tracking : allTrackings) {
                        LocalDate trackingDate = tracking.getTakip_tarihi();
                        if ((trackingDate.isEqual(startDate) || trackingDate.isAfter(startDate)) &&
                                (trackingDate.isEqual(endDate) || trackingDate.isBefore(endDate))) {
                            filteredTrackings.add(tracking);
                        }
                    }

                    // Verileri tabloya ekle
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

                    for (DietTracking tracking : filteredTrackings) {
                        String dateStr = tracking.getTakip_tarihi().format(dateFormatter);
                        String appliedStr = tracking.getUygulandi_mi() ? "Evet" : "Hayır";

                        dietModel.addRow(new Object[]{dateStr, appliedStr});
                    }

                    // Eğer veri yoksa bilgi mesajı ekle
                    if (filteredTrackings.isEmpty()) {
                        dietModel.addRow(new Object[]{"Veri yok", "Veri yok"});
                    }
                } catch (SQLException ex) {
                    dietProgressBar.setValue(0);
                    dietProgressBar.setString("Hata");
                    dietModel.addRow(new Object[]{"Hata: " + ex.getMessage(), "-"});
                    System.err.println("Diyet verileri yüklenirken hata: " + ex.getMessage());
                    ex.printStackTrace();
                }

                // ---------- 2. EGZERSİZ VERİLERİNİ YÜKLE ----------
                exerciseModel.setRowCount(0); // Tabloyu temizle

                ExerciseTrackingDao exerciseTrackingDao = new ExerciseTrackingDao();

                try {
                    // Egzersiz uyum oranını hesapla
                    double exerciseCompliance = exerciseTrackingDao.getComplianceRatio(
                            selectedPatient.getPatient_id(), startDate, endDate);

                    // Progress bar'ı güncelle
                    exerciseProgressBar.setValue((int) exerciseCompliance);
                    exerciseProgressBar.setString("%" + String.format("%.1f", exerciseCompliance));

                    // Hastanın egzersiz verilerini getir - SQL sorgusu yerine DAO metodunu kullan
                    List<ExerciseTracking> exerciseTrackings = exerciseTrackingDao.findByPatientIdAndDateRange(
                            selectedPatient.getPatient_id(), startDate, endDate);

                    // Verileri tabloya ekle
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

                    for (ExerciseTracking tracking : exerciseTrackings) {
                        String dateStr = tracking.getTakip_tarihi().format(dateFormatter);
                        String appliedStr = tracking.getUygulandi_mi() ? "Evet" : "Hayır";

                        exerciseModel.addRow(new Object[]{dateStr, appliedStr});
                    }

                    // Eğer veri yoksa bilgi mesajı ekle
                    if (exerciseTrackings.isEmpty()) {
                        exerciseModel.addRow(new Object[]{"Veri yok", "Veri yok"});
                    }
                } catch (SQLException ex) {
                    exerciseProgressBar.setValue(0);
                    exerciseProgressBar.setString("Hata");
                    exerciseModel.addRow(new Object[]{"Hata: " + ex.getMessage(), "-"});
                    System.err.println("Egzersiz verileri yüklenirken hata: " + ex.getMessage());
                    ex.printStackTrace();
                }

                // Bilgilendirme mesajı
                JOptionPane.showMessageDialog(panel,
                        "Diyet ve egzersiz verileri başarıyla güncellendi.",
                        "Bilgi",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        "Veriler yüklenirken hata oluştu: " + ex.getMessage(),
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        dateRangePanel.add(updateButton);
        summaryPanel.add(dateRangePanel);

        // Diyet Uyum Oranı - Progress Bar
        JPanel dietPanel = new JPanel(new BorderLayout(5, 5));
        dietPanel.add(new JLabel("Diyet Uyum Oranı:"), BorderLayout.WEST);
        dietPanel.add(dietProgressBar, BorderLayout.CENTER);
        summaryPanel.add(dietPanel);

        // Egzersiz Uyum Oranı - Progress Bar
        JPanel exercisePanel = new JPanel(new BorderLayout(5, 5));
        exercisePanel.add(new JLabel("Egzersiz Uyum Oranı:"), BorderLayout.WEST);
        exercisePanel.add(exerciseProgressBar, BorderLayout.CENTER);
        summaryPanel.add(exercisePanel);

        // Diyet Geçmişi Tablosu
        JPanel dietHistoryPanel = new JPanel(new BorderLayout());
        dietHistoryPanel.setBorder(BorderFactory.createTitledBorder("Diyet Geçmişi"));

        JTable dietTable = new JTable(dietModel);
        JScrollPane dietScroll = new JScrollPane(dietTable);
        dietHistoryPanel.add(dietScroll, BorderLayout.CENTER);

        // Evet/Hayır renklendir
        dietTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if ("Evet".equals(value)) {
                    c.setForeground(new Color(0, 128, 0)); // Yeşil
                } else if ("Hayır".equals(value)) {
                    c.setForeground(new Color(200, 0, 0)); // Kırmızı
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JLabel dietPlaceholderLabel = new JLabel("<html><center>Bu alanda hastanın diyet geçmişi gösterilecektir.<br>" +
                "Lütfen bir hasta seçip 'Güncelle' butonuna basın.</center></html>", JLabel.CENTER);
        dietPlaceholderLabel.setForeground(Color.GRAY);
        dietHistoryPanel.add(dietPlaceholderLabel, BorderLayout.NORTH);

        // Egzersiz Geçmişi Tablosu
        JPanel exerciseHistoryPanel = new JPanel(new BorderLayout());
        exerciseHistoryPanel.setBorder(BorderFactory.createTitledBorder("Egzersiz Geçmişi"));

        JTable exerciseTable = new JTable(exerciseModel);
        JScrollPane exerciseScroll = new JScrollPane(exerciseTable);
        exerciseHistoryPanel.add(exerciseScroll, BorderLayout.CENTER);

        // Evet/Hayır renklendir
        exerciseTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if ("Evet".equals(value)) {
                    c.setForeground(new Color(0, 128, 0)); // Yeşil
                } else if ("Hayır".equals(value)) {
                    c.setForeground(new Color(200, 0, 0)); // Kırmızı
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JLabel exercisePlaceholderLabel = new JLabel("<html><center>Bu alanda hastanın egzersiz geçmişi gösterilecektir.<br>" +
                "Lütfen bir hasta seçip 'Güncelle' butonuna basın.</center></html>", JLabel.CENTER);
        exercisePlaceholderLabel.setForeground(Color.GRAY);
        exerciseHistoryPanel.add(exercisePlaceholderLabel, BorderLayout.NORTH);

        // Tabloları panele ekle
        JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        tablesPanel.add(dietHistoryPanel);
        tablesPanel.add(exerciseHistoryPanel);

        panel.add(summaryPanel, BorderLayout.NORTH);
        panel.add(tablesPanel, BorderLayout.CENTER);

        return panel;
    }

    // Hasta bilgileri üst paneli
    private JPanel createPatientInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Hasta Bilgileri"));

        // Sol tarafta profil resmi
        JPanel profileImagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel patientImageLabel = new JLabel();
        patientImageLabel.setPreferredSize(new Dimension(100, 120));
        patientImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        patientImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        patientImageLabel.setText("Resim yok");
        patientImageLabel.setName("patientImageLabel"); // Sonra erişim için isim ver
        profileImagePanel.add(patientImageLabel);

        // Sağ tarafta hasta bilgileri
        JPanel patientInfoPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        patientInfoPanel.add(new JLabel("Ad Soyad: "));
        patientInfoPanel.add(new JLabel(""));
        patientInfoPanel.add(new JLabel("TC Kimlik: "));
        patientInfoPanel.add(new JLabel(""));
        patientInfoPanel.add(new JLabel("Yaş: "));
        patientInfoPanel.add(new JLabel(""));
        patientInfoPanel.add(new JLabel("E-posta: "));
        patientInfoPanel.add(new JLabel(""));

        // Ana panel içine yerleştir
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(profileImagePanel, BorderLayout.WEST);
        contentPanel.add(patientInfoPanel, BorderLayout.CENTER);

        infoPanel.add(contentPanel, BorderLayout.CENTER);

        // Geri dönüş butonu
        JButton backButton = new JButton("Listeye Dön");
        backButton.addActionListener(e -> {
            // Üst paneli tekrar görünür yap
            Component topComponent = DoctorDashboard.this.getComponent(0);
            topComponent.setVisible(true);

            // Ana sekme paneline dön
            cardLayout.show(centerPanel, TABS_PANEL);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);
        infoPanel.add(buttonPanel, BorderLayout.SOUTH);

        return infoPanel;
    }

    // Kan şekeri takip sekmesi - Sadece tablo bölümü
    private JPanel createBloodSugarPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filtreleme ve tarih seçimi kontrolleri
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Tarih Aralığı:"));

        // Tarih aralığı combo box'ı
        JComboBox<String> dateRangeCombo = new JComboBox<>(new String[] {
                "Son 7 Gün", "Son 30 Gün", "Son 3 Ay", "Özel Aralık"
        });
        controlPanel.add(dateRangeCombo);

        // Özel aralık için tarih seçme alanları - başlangıçta gizli
        JPanel customDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customDatePanel.setVisible(false);

        JLabel startDateLabel = new JLabel("Başlangıç:");
        JTextField startDateField = new JTextField(10);
        startDateField.setText(LocalDate.now().minusWeeks(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        JLabel endDateLabel = new JLabel("Bitiş:");
        JTextField endDateField = new JTextField(10);
        endDateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        customDatePanel.add(startDateLabel);
        customDatePanel.add(startDateField);
        customDatePanel.add(endDateLabel);
        customDatePanel.add(endDateField);

        controlPanel.add(customDatePanel);

        // Tarih aralığı seçimi değiştiğinde özel tarih alanlarını göster/gizle
        dateRangeCombo.addActionListener(e -> {
            boolean isCustomRange = "Özel Aralık".equals(dateRangeCombo.getSelectedItem());
            customDatePanel.setVisible(isCustomRange);
            controlPanel.revalidate();
            controlPanel.repaint();
        });

        JButton updateButton = new JButton("Güncelle");

        controlPanel.add(updateButton);

        panel.add(controlPanel, BorderLayout.NORTH);

        // Ölçüm verileri tablosu - "Durum" sütunu kaldırıldı
        String[] columnNames = {"Tarih", "Saat", "Ölçüm Değeri (mg/dL)", "Kategori"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabloyu salt okunur yap
            }
        };
        JTable measurementsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(measurementsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Kan Şekeri Ölçümleri"));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Güncelleme butonuna tıklandığında ölçümleri yükle
        updateButton.addActionListener(e -> {
            if (selectedPatient == null || selectedPatient.getPatient_id() == null) {
                JOptionPane.showMessageDialog(panel,
                        "Lütfen önce bir hasta seçin.",
                        "Hasta Seçilmedi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // Tarih aralığını belirle
                LocalDate startDate;
                LocalDate endDate = LocalDate.now();

                String selectedRange = (String) dateRangeCombo.getSelectedItem();
                if ("Özel Aralık".equals(selectedRange)) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                        startDate = LocalDate.parse(startDateField.getText(), formatter);
                        endDate = LocalDate.parse(endDateField.getText(), formatter);

                        if (endDate.isBefore(startDate)) {
                            JOptionPane.showMessageDialog(panel,
                                    "Bitiş tarihi başlangıç tarihinden önce olamaz.",
                                    "Tarih Hatası",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(panel,
                                "Lütfen geçerli bir tarih formatı girin (GG.AA.YYYY).",
                                "Format Hatası",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else if ("Son 7 Gün".equals(selectedRange)) {
                    startDate = endDate.minusDays(7);
                } else if ("Son 30 Gün".equals(selectedRange)) {
                    startDate = endDate.minusDays(30);
                } else { // "Son 3 Ay"
                    startDate = endDate.minusMonths(3);
                }

                // Tabloyu temizle
                model.setRowCount(0);

                // Verileri çek ve tabloyu doldur
                MeasurementDao measurementDao = new MeasurementDao();
                List<BloodSugarMeasurement> measurements = measurementDao.findByDateRange(
                        selectedPatient.getPatient_id(),
                        startDate,
                        endDate
                );

                // Verileri tabloya ekle - "Durum" sütunu çıkarıldı
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                for (BloodSugarMeasurement measurement : measurements) {
                    Object[] row = new Object[4]; // 4 sütun
                    row[0] = measurement.getOlcum_tarihi().toLocalDate().format(dateFormatter);
                    row[1] = measurement.getOlcum_tarihi().format(timeFormatter);
                    row[2] = measurement.getOlcum_degeri();

                    // Kategori belirleme
                    String category;
                    int value = measurement.getOlcum_degeri();
                    if (value < 70) {
                        category = "Düşük (Hipoglisemi)";
                    } else if (value <= 99) {
                        category = "Normal";
                    } else if (value <= 125) {
                        category = "Prediyabet";
                    } else {
                        category = "Yüksek (Diyabet)";
                    }
                    row[3] = category;

                    // "Durum" sütunu kaldırıldı

                    model.addRow(row);
                }

                // Sonuç mesajı
                if (measurements.isEmpty()) {
                    JOptionPane.showMessageDialog(panel,
                            "Seçilen tarih aralığında kan şekeri ölçümü bulunmamaktadır.",
                            "Sonuç",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(panel,
                            "Toplam " + measurements.size() + " adet ölçüm yüklendi.",
                            "Bilgi",
                            JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        "Ölçüm verileri yüklenirken hata oluştu: " + ex.getMessage(),
                        "Veritabanı Hatası",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        return panel;
    }

    /**
     * Analiz panelini oluşturur
     * @return Analiz paneli
     */
    private JPanel createAnalysisPanel() {
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Bu paneli sınıf düzeyinde bir değişkene atayalım ki diğer metodlardan erişebilelim
        final JPanel chartContainerPanel = new JPanel(new BorderLayout());

        // Basit bir metin alanı kullanalım - özet için
        final JTextArea summaryArea = new JTextArea(5, 20); // 5 satır
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("SansSerif", Font.PLAIN, 14)); // Yazı boyutunu büyüttük

        // Üst kontrol paneli
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Analiz Seçenekleri"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Analiz türü seçimi
        controlPanel.add(new JLabel("Analiz Türü:"));
        final JComboBox<String> analysisCombo = new JComboBox<>(new String[] {
                "Zaman Bazlı Kan Şekeri Değişimleri",
                "Kan Şekeri - Diyet İlişkisi",
                "Kan Şekeri - Egzersiz İlişkisi"
        });
        controlPanel.add(analysisCombo);

        // Zaman bazlı analiz için tarih seçici
        final JPanel timeBasedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeBasedPanel.add(new JLabel("Tarih:"));

        // Format: GG.AA.YYYY
        final JTextField dateField = new JTextField(10);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        timeBasedPanel.add(dateField);

        // Tarih seçici butonu - JDateChooser yerine manuel dialog kullanalım
        JButton datePickerButton = new JButton("...");
        datePickerButton.setPreferredSize(new Dimension(30, 25));
        datePickerButton.addActionListener(e -> {
            // Basit bir tarih seçici dialog göster
            JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), "Tarih Seç", true);

            // Yıl, ay, gün için spinner'lar
            JPanel datePanel = new JPanel(new GridLayout(0, 2));
            datePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Mevcut tarihi parse et
            LocalDate currentDate;
            try {
                currentDate = LocalDate.parse(dateField.getText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } catch (Exception ex) {
                currentDate = LocalDate.now();
            }

            // Gün spinner'ı
            datePanel.add(new JLabel("Gün:"));
            SpinnerModel dayModel = new SpinnerNumberModel(currentDate.getDayOfMonth(), 1, 31, 1);
            JSpinner daySpinner = new JSpinner(dayModel);
            datePanel.add(daySpinner);

            // Ay spinner'ı
            datePanel.add(new JLabel("Ay:"));
            SpinnerModel monthModel = new SpinnerNumberModel(currentDate.getMonthValue(), 1, 12, 1);
            JSpinner monthSpinner = new JSpinner(monthModel);
            datePanel.add(monthSpinner);

            // Yıl spinner'ı
            datePanel.add(new JLabel("Yıl:"));
            SpinnerModel yearModel = new SpinnerNumberModel(currentDate.getYear(), 2000, 2100, 1);
            JSpinner yearSpinner = new JSpinner(yearModel);
            datePanel.add(yearSpinner);

            JButton selectButton = new JButton("Seç");
            selectButton.addActionListener(event -> {
                try {
                    int day = (Integer) daySpinner.getValue();
                    int month = (Integer) monthSpinner.getValue();
                    int year = (Integer) yearSpinner.getValue();

                    // Tarihi doğrula
                    LocalDate selectedDate = LocalDate.of(year, month, day);
                    dateField.setText(selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Geçersiz tarih! Lütfen geçerli bir tarih girin.",
                            "Hata", JOptionPane.ERROR_MESSAGE);
                }
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(selectButton);

            dialog.setLayout(new BorderLayout());
            dialog.add(datePanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.pack();
            dialog.setLocationRelativeTo(datePickerButton);
            dialog.setResizable(false);
            dialog.setVisible(true);
        });
        timeBasedPanel.add(datePickerButton);
        controlPanel.add(timeBasedPanel);

        // Analiz türü değiştiğinde ilgili kontrolleri göster/gizle
        analysisCombo.addActionListener(e -> {
            int selectedIndex = analysisCombo.getSelectedIndex();

            // Zaman bazlı analiz için tarih seçiciyi göster
            timeBasedPanel.setVisible(selectedIndex == 0);

            // Kan şekeri - diyet veya egzersiz ilişkisi için tarih aralığı seçiciyi göster
            if (selectedIndex == 1 || selectedIndex == 2) { // <-- Burada değişiklik yaptım: 1 veya 2 için
                // Daha önce oluşturulmadıysa tarih aralığı seçici oluştur
                if (dateRangePanel == null) {
                    dateRangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                    dateRangePanel.add(new JLabel("Başlangıç:"));
                    startDateField = new JTextField(10);
                    startDateField.setText(LocalDate.now().minusDays(30).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    dateRangePanel.add(startDateField);

                    dateRangePanel.add(new JLabel("Bitiş:"));
                    endDateField = new JTextField(10);
                    endDateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    dateRangePanel.add(endDateField);

                    // Tarih seçici butonunu ekle
                    JButton startDatePickerButton = createDatePickerButton(startDateField);
                    dateRangePanel.add(startDatePickerButton);

                    JButton endDatePickerButton = createDatePickerButton(endDateField);
                    dateRangePanel.add(endDatePickerButton);

                    // Panele ekle
                    controlPanel.add(dateRangePanel);
                }

                dateRangePanel.setVisible(true);
            } else {
                if (dateRangePanel != null) {
                    dateRangePanel.setVisible(false);
                }
            }

            mainPanel.revalidate();
            mainPanel.repaint();
        });

        // Analiz butonu
        JButton analyzeButton = new JButton("Analiz Et");
        analyzeButton.addActionListener(e -> {
            if (selectedPatient == null) {
                JOptionPane.showMessageDialog(mainPanel,
                        "Lütfen önce bir hasta seçin.",
                        "Hasta Seçilmedi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int selectedIndex = analysisCombo.getSelectedIndex();

            if (selectedIndex == 0) {
                // Zaman bazlı kan şekeri grafiği
                try {
                    // Tarih formatını kontrol et
                    String dateStr = dateField.getText().trim();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    LocalDate selectedDate = LocalDate.parse(dateStr, formatter);

                    // Grafiği oluştur
                    updateBloodSugarTimeChart(selectedDate, chartContainerPanel, summaryArea);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel,
                            "Tarih formatı geçersiz. Lütfen GG.AA.YYYY formatında bir tarih girin.",
                            "Format Hatası",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } else if (selectedIndex == 1) {
                // Kan şekeri - diyet ilişkisi grafiği
                try {
                    // Tarih formatını kontrol et
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    LocalDate startDate = LocalDate.parse(startDateField.getText().trim(), formatter);
                    LocalDate endDate = LocalDate.parse(endDateField.getText().trim(), formatter);

                    if (endDate.isBefore(startDate)) {
                        JOptionPane.showMessageDialog(mainPanel,
                                "Bitiş tarihi başlangıç tarihinden önce olamaz.",
                                "Tarih Hatası",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Panel başlığını güncelle
                    chartContainerPanel.setBorder(BorderFactory.createTitledBorder(
                            "Kan Şekeri - Diyet İlişkisi (" +
                                    startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                                    " - " +
                                    endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                                    ")"));

                    // Grafiği oluştur
                    updateBloodSugarDietChart(chartContainerPanel, summaryArea, startDate, endDate);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel,
                            "Tarih formatı geçersiz. Lütfen GG.AA.YYYY formatında bir tarih girin.",
                            "Format Hatası",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } else if (selectedIndex == 2) {
                // Kan şekeri - egzersiz ilişkisi grafiği
                try {
                    // Tarih formatını kontrol et
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    LocalDate startDate = LocalDate.parse(startDateField.getText().trim(), formatter);
                    LocalDate endDate = LocalDate.parse(endDateField.getText().trim(), formatter);

                    if (endDate.isBefore(startDate)) {
                        JOptionPane.showMessageDialog(mainPanel,
                                "Bitiş tarihi başlangıç tarihinden önce olamaz.",
                                "Tarih Hatası",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Panel başlığını güncelle
                    chartContainerPanel.setBorder(BorderFactory.createTitledBorder(
                            "Kan Şekeri - Egzersiz İlişkisi (" +
                                    startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                                    " - " +
                                    endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                                    ")"));

                    // Grafiği oluştur
                    updateBloodSugarExerciseChart(chartContainerPanel, summaryArea, startDate, endDate);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel,
                            "Tarih formatı geçersiz. Lütfen GG.AA.YYYY formatında bir tarih girin.",
                            "Format Hatası",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
        controlPanel.add(analyzeButton);

        mainPanel.add(controlPanel, BorderLayout.NORTH);

        // Grafik alanı
        chartContainerPanel.setBorder(BorderFactory.createTitledBorder("Zaman Bazlı Kan Şekeri Değişimleri"));
        chartContainerPanel.setPreferredSize(new Dimension(600, 400));

        // Grafik bilgi etiketi
        JLabel chartInfoLabel = new JLabel("<html><center>Bu alanda seçilen tarihe ait kan şekeri değerlerinin<br>" +
                "gün içindeki değişimi gösterilecektir.<br>" +
                "Lütfen bir tarih seçip \"Analiz Et\" butonuna tıklayın.</center></html>", JLabel.CENTER);
        chartInfoLabel.setForeground(Color.GRAY);
        chartContainerPanel.add(chartInfoLabel, BorderLayout.CENTER);

        // Analiz özeti - daha okunaklı
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Analiz Özeti"));
        summaryPanel.setPreferredSize(new Dimension(-1, 150)); // Panel yüksekliğini arttırdık

        summaryArea.setText("Analiz henüz gerçekleştirilmemiştir. Lütfen analiz türünü ve tarihi seçerek 'Analiz Et' butonuna tıklayın.");
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // İç kenar boşluğu
        summaryPanel.add(summaryScroll, BorderLayout.CENTER);

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(chartContainerPanel, BorderLayout.CENTER);
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    /**
     * Tarih seçici buton oluşturur
     * @param targetField Seçilen tarihin yazılacağı metin alanı
     * @return Tarih seçici buton
     */
    private JButton createDatePickerButton(JTextField targetField) {
        JButton datePickerButton = new JButton("...");
        datePickerButton.setPreferredSize(new Dimension(30, 25));
        datePickerButton.addActionListener(e -> {
            // Basit bir tarih seçici dialog göster
            JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(mainPanel), "Tarih Seç", true);

            // Yıl, ay, gün için spinner'lar
            JPanel datePanel = new JPanel(new GridLayout(0, 2));
            datePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Mevcut tarihi parse et
            LocalDate currentDate;
            try {
                currentDate = LocalDate.parse(targetField.getText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } catch (Exception ex) {
                currentDate = LocalDate.now();
            }

            // Gün spinner'ı
            datePanel.add(new JLabel("Gün:"));
            SpinnerModel dayModel = new SpinnerNumberModel(currentDate.getDayOfMonth(), 1, 31, 1);
            JSpinner daySpinner = new JSpinner(dayModel);
            datePanel.add(daySpinner);

            // Ay spinner'ı
            datePanel.add(new JLabel("Ay:"));
            SpinnerModel monthModel = new SpinnerNumberModel(currentDate.getMonthValue(), 1, 12, 1);
            JSpinner monthSpinner = new JSpinner(monthModel);
            datePanel.add(monthSpinner);

            // Yıl spinner'ı
            datePanel.add(new JLabel("Yıl:"));
            SpinnerModel yearModel = new SpinnerNumberModel(currentDate.getYear(), 2000, 2100, 1);
            JSpinner yearSpinner = new JSpinner(yearModel);
            datePanel.add(yearSpinner);

            JButton selectButton = new JButton("Seç");
            selectButton.addActionListener(event -> {
                try {
                    int day = (Integer) daySpinner.getValue();
                    int month = (Integer) monthSpinner.getValue();
                    int year = (Integer) yearSpinner.getValue();

                    // Tarihi doğrula
                    LocalDate selectedDate = LocalDate.of(year, month, day);
                    targetField.setText(selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Geçersiz tarih! Lütfen geçerli bir tarih girin.",
                            "Hata", JOptionPane.ERROR_MESSAGE);
                }
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(selectButton);

            dialog.setLayout(new BorderLayout());
            dialog.add(datePanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.pack();
            dialog.setLocationRelativeTo(datePickerButton);
            dialog.setResizable(false);
            dialog.setVisible(true);
        });

        return datePickerButton;
    }

    /**
     * Seçilen tarih için zaman bazlı kan şekeri grafiğini günceller
     * @param selectedDate Seçilen tarih
     * @param chartContainerPanel Grafik paneli
     * @param summaryArea Özet metin alanı
     */
    private void updateBloodSugarTimeChart(LocalDate selectedDate, JPanel chartContainerPanel, JTextArea summaryArea) {
        // Mevcut grafiği temizle
        chartContainerPanel.removeAll();

        try {
            // MeasurementDao kullanarak seçilen gün için verileri al
            MeasurementDao measurementDao = new MeasurementDao();
            List<BloodSugarMeasurement> allMeasurements = measurementDao.findByDateRange(
                    selectedPatient.getPatient_id(),
                    selectedDate,
                    selectedDate
            );

            // SADECE GEÇERLİ ÖLÇÜMLERİ FİLTRELE
            List<BloodSugarMeasurement> validMeasurements = new ArrayList<>();
            for (BloodSugarMeasurement measurement : allMeasurements) {
                if (Boolean.TRUE.equals(measurement.getIs_valid_time())) {
                    validMeasurements.add(measurement);
                }
            }

            if (validMeasurements.isEmpty()) {
                JLabel noDataLabel = new JLabel("Seçilen tarih için geçerli kan şekeri ölçümü kaydedilmemiş", JLabel.CENTER);
                noDataLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                noDataLabel.setForeground(Color.GRAY);
                chartContainerPanel.add(noDataLabel, BorderLayout.CENTER);
            } else {
                // Ölçümleri saate göre sırala
                Collections.sort(validMeasurements, Comparator.comparing(m -> m.getOlcum_tarihi()));

                // Tek bir seri kullan, tüm noktaları içinde topla
                XYSeries series = new XYSeries("Kan Şekeri");

                // Periyotları sakla (hangi XY değeri hangi periyoda ait)
                final Map<Point2D, String> periodMap = new HashMap<>();

                // SADECE GEÇERLİ ÖLÇÜMLERİ GRAFİĞE EKLE
                for (BloodSugarMeasurement measurement : validMeasurements) {
                    LocalDateTime dateTime = measurement.getOlcum_tarihi();
                    long seconds = dateTime.getHour() * 3600L + dateTime.getMinute() * 60L;
                    double value = measurement.getOlcum_degeri();

                    // Veriyi seriye ekle
                    series.add(seconds, value);

                    // Periyot bilgisini sakla (nokta koordinatlarıyla ilişkilendir)
                    periodMap.put(new Point2D.Double(seconds, value), measurement.getOlcum_zamani());
                }

                // Veri setini oluştur
                XYSeriesCollection dataset = new XYSeriesCollection(series);

                // Grafiği oluştur
                JFreeChart chart = ChartFactory.createXYLineChart(
                        selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " - Kan Şekeri Değişimi",
                        "Saat",
                        "Kan Şekeri (mg/dL)",
                        dataset,
                        PlotOrientation.VERTICAL,
                        false,
                        true,
                        false
                );

                // Grafik görünümünü düzenle
                XYPlot plot = chart.getXYPlot();
                plot.setBackgroundPaint(Color.WHITE);
                plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
                plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

                // X ekseni (saat) formatını ayarla
                NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
                domainAxis.setNumberFormatOverride(new NumberFormat() {
                    @Override
                    public StringBuffer format(double seconds, StringBuffer toAppendTo, FieldPosition pos) {
                        int hours = (int) (seconds / 3600);
                        int minutes = (int) ((seconds % 3600) / 60);
                        return toAppendTo.append(String.format("%02d:%02d", hours, minutes));
                    }

                    @Override
                    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
                        return format((double) number, toAppendTo, pos);
                    }

                    @Override
                    public Number parse(String source, ParsePosition parsePosition) {
                        return null;
                    }
                });

                domainAxis.setRange(0, 24 * 3600);
                domainAxis.setTickUnit(new NumberTickUnit(2 * 3600));

                // Y ekseni ayarları
                NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                rangeAxis.setRange(30, 250);
                rangeAxis.setTickUnit(new NumberTickUnit(20));

                // Normal aralık işaretçisi
                IntervalMarker normalRange = new IntervalMarker(70, 110);
                normalRange.setPaint(new Color(200, 255, 200, 100));
                normalRange.setLabel("Normal Aralık (70-110)");
                normalRange.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
                normalRange.setLabelPaint(new Color(0, 120, 0));
                plot.addRangeMarker(normalRange);

                // Özel Renderer - Çizgiler tek renk, noktalar periyoda göre renkli
                XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer() {
                    // Noktaların renklerini belirler
                    @Override
                    public Paint getItemFillPaint(int series, int item) {
                        // Veri noktasının koordinatlarını al
                        XYDataset dataset = getPlot().getDataset();
                        double x = dataset.getXValue(series, item);
                        double y = dataset.getYValue(series, item);

                        // Bu koordinata karşılık gelen periyodu bul
                        String period = periodMap.get(new Point2D.Double(x, y));

                        // Periyoda göre renk döndür
                        if (period != null) {
                            switch (period) {
                                case "sabah": return new Color(0, 120, 215); // Mavi
                                case "ogle": return new Color(255, 140, 0);  // Turuncu
                                case "ikindi": return new Color(50, 150, 50); // Yeşil
                                case "aksam": return new Color(128, 0, 128); // Mor
                                case "gece": return new Color(50, 50, 50);   // Siyah
                            }
                        }
                        return super.getItemFillPaint(series, item);
                    }

                    // Noktaların kenarlık renklerini belirler
                    @Override
                    public Paint getItemOutlinePaint(int series, int item) {
                        return getItemFillPaint(series, item);
                    }
                };

                // Çizgi ve nokta stilini ayarla
                renderer.setDefaultShapesVisible(true);
                renderer.setDefaultShape(new Ellipse2D.Double(-5, -5, 10, 10)); // Büyük noktalar
                renderer.setDefaultLinesVisible(true); // Çizgileri göster
                renderer.setDrawOutlines(true);
                renderer.setUseFillPaint(true);
                renderer.setDefaultFillPaint(Color.WHITE);

                // Çizgilerin rengini ayarla (tüm çizgiler için tek renk)
                renderer.setSeriesPaint(0, new Color(240, 0, 0, 255)); // Kırmızı çizgi rengi
                renderer.setSeriesStroke(0, new BasicStroke(2.0f)); // Kalın çizgi

                // Renderer'ı ayarla
                plot.setRenderer(renderer);

                // Tooltip generator
                renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator() {
                    @Override
                    public String generateToolTip(XYDataset dataset, int series, int item) {
                        // Koordinatlar
                        double x = dataset.getXValue(series, item);
                        double y = dataset.getYValue(series, item);

                        // Saat formatı
                        int hours = (int) (x / 3600);
                        int minutes = (int) ((x % 3600) / 60);
                        String timeStr = String.format("%02d:%02d", hours, minutes);

                        // Kan şekeri değeri
                        int bloodSugar = (int) y;

                        // Periyot adını bul
                        String periodCode = periodMap.get(new Point2D.Double(x, y));
                        String periodName;
                        switch (periodCode) {
                            case "sabah": periodName = "Sabah"; break;
                            case "ogle": periodName = "Öğle"; break;
                            case "ikindi": periodName = "İkindi"; break;
                            case "aksam": periodName = "Akşam"; break;
                            case "gece": periodName = "Gece"; break;
                            default: periodName = periodCode; break;
                        }

                        return String.format("<html>Vakit: <b>%s</b><br>Saat: <b>%s</b><br>Kan Şekeri: <b>%d mg/dL</b></html>",
                                periodName, timeStr, bloodSugar);
                    }
                });

                // Periyot açıklaması için gösterge oluştur
                LegendTitle legend = new LegendTitle(new LegendItemSource() {
                    @Override
                    public LegendItemCollection getLegendItems() {
                        LegendItemCollection items = new LegendItemCollection();
                        items.add(new LegendItem("Sabah", new Color(0, 120, 215)));
                        items.add(new LegendItem("Öğle", new Color(255, 140, 0)));
                        items.add(new LegendItem("İkindi", new Color(50, 150, 50)));
                        items.add(new LegendItem("Akşam", new Color(128, 0, 128)));
                        items.add(new LegendItem("Gece", new Color(50, 50, 50)));
                        return items;
                    }
                });
                legend.setPosition(RectangleEdge.BOTTOM);
                chart.addLegend(legend);

                // Grafiği panele ekle
                ChartPanel jfreeChartPanel = new ChartPanel(chart);

                // Grafik boyutunu ayarla
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int chartHeight = (int)(screenSize.height * 0.4);
                jfreeChartPanel.setPreferredSize(new Dimension(-1, chartHeight));

                chartContainerPanel.add(jfreeChartPanel, BorderLayout.CENTER);

                // Ortalama ve özet bilgi hazırla - basitleştirilmiş versiyon
                try {
                    double avgValue = measurementDao.getDailyAverage(selectedPatient.getPatient_id(), selectedDate);

                    StringBuilder summary = new StringBuilder();

                    // Tarih ve hasta bilgisi
                    summary.append("TARİH: ")
                            .append(selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                            .append("\n");

                    summary.append("HASTA: ")
                            .append(selectedPatient.getAd()).append(" ").append(selectedPatient.getSoyad())
                            .append("\n");

                    if (avgValue > 0) {
                        // Ortalama kan şekeri değeri ve durumu
                        summary.append("ORTALAMA KAN ŞEKERİ: ")
                                .append(String.format("%.1f mg/dL", avgValue));

                        if (avgValue < 70) {
                            summary.append(" (DÜŞÜK - Hipoglisemi riski!)");
                        } else if (avgValue > 110) {
                            summary.append(" (YÜKSEK - Hiperglisemi riski!)");
                        } else {
                            summary.append(" (NORMAL aralıkta)");
                        }
                        summary.append("\n");

                        // Ölçüm sayısı
                        summary.append("ÖLÇÜM SAYISI: ")
                                .append(validMeasurements.size())
                                .append("\n");

                        // En düşük ve en yüksek değerleri de ekleyelim
                        int minValue = Integer.MAX_VALUE;
                        int maxValue = Integer.MIN_VALUE;
                        String minPeriod = "";
                        String maxPeriod = "";

                        for (BloodSugarMeasurement m : validMeasurements) {
                            if (m.getOlcum_degeri() < minValue) {
                                minValue = m.getOlcum_degeri();
                                minPeriod = m.getOlcum_zamani();
                            }
                            if (m.getOlcum_degeri() > maxValue) {
                                maxValue = m.getOlcum_degeri();
                                maxPeriod = m.getOlcum_zamani();
                            }
                        }

                        // Min değer
                        summary.append("EN DÜŞÜK DEĞER: ")
                                .append(minValue).append(" mg/dL (")
                                .append(convertPeriodToDisplayName(minPeriod))
                                .append(")\n");

                        // Max değer
                        summary.append("EN YÜKSEK DEĞER: ")
                                .append(maxValue).append(" mg/dL (")
                                .append(convertPeriodToDisplayName(maxPeriod))
                                .append(")");

                    } else {
                        summary.append("Bu tarih için yeterli ölçüm bulunamadı.");
                    }

                    summaryArea.setText(summary.toString());
                    summaryArea.setCaretPosition(0);
                } catch (Exception e) {
                    System.err.println("Ortalama hesaplanırken hata: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Grafik yüklenirken hata: " + e.getMessage(), JLabel.CENTER);
            errorLabel.setFont(new Font("Arial", Font.BOLD, 12));
            errorLabel.setForeground(Color.RED);
            chartContainerPanel.add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace();
        }

        // Panel güncellendi, yeniden çiz
        chartContainerPanel.revalidate();
        chartContainerPanel.repaint();
    }

    /**
     * Veritabanı periyot kodunu kullanıcı dostu görüntüleme adına dönüştürür
     * @param periodCode Periyot kodu (sabah, ogle vb.)
     * @return Görüntüleme adı (Sabah, Öğle vb.)
     */
    private String convertPeriodToDisplayName(String periodCode) {
        switch (periodCode) {
            case "sabah": return "Sabah";
            case "ogle": return "Öğle";
            case "ikindi": return "İkindi";
            case "aksam": return "Akşam";
            case "gece": return "Gece";
            default: return periodCode;
        }
    }

    /**
     * Kan şekeri ve diyet ilişkisi grafiğini günceller
     * @param chartContainerPanel Grafik paneli
     * @param summaryArea Özet metin alanı
     */
    private void updateBloodSugarDietChart(JPanel chartContainerPanel, JTextArea summaryArea, LocalDate startDate, LocalDate endDate) {
        // Mevcut grafiği temizle
        chartContainerPanel.removeAll();

        try {
            // Veri erişim nesneleri
            MeasurementDao measurementDao = new MeasurementDao();
            DietTrackingDao dietTrackingDao = new DietTrackingDao();

            // Ölçüm ve diyet verilerini al
            Map<LocalDate, Double> bloodSugarMap = new HashMap<>();
            Map<LocalDate, Boolean> dietStatusMap = new HashMap<>();

            // Her gün için kan şekeri ortalaması
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                try {
                    double dailyAvg = measurementDao.getDailyAverage(selectedPatient.getPatient_id(), date);
                    if (dailyAvg > 0) {
                        bloodSugarMap.put(date, dailyAvg);
                    }
                } catch (SQLException ex) {
                    System.err.println("Kan şekeri ortalaması hesaplanırken hata: " + ex.getMessage());
                }
            }

            // Diyet takip verisini al
            List<DietTracking> dietTrackings = dietTrackingDao.findAllByPatientId(selectedPatient.getPatient_id());
            for (DietTracking tracking : dietTrackings) {
                if (!tracking.getTakip_tarihi().isBefore(startDate) &&
                        !tracking.getTakip_tarihi().isAfter(endDate)) {
                    dietStatusMap.put(tracking.getTakip_tarihi(), tracking.getUygulandi_mi());

                    // Debug için yazdır
                    System.out.println("Diyet takip: " + tracking.getTakip_tarihi() + " - " +
                            (tracking.getUygulandi_mi() ? "Uygulandı" : "Uygulanmadı"));
                }
            }

            // TimeSeriesCollection için veri serileri
            TimeSeries bloodSugarSeries = new TimeSeries("Kan Şekeri");

            // Debug için konsola yazdır
            System.out.println("Toplam kan şekeri günlük ortalama kaydı sayısı: " + bloodSugarMap.size());
            for (Map.Entry<LocalDate, Double> entry : bloodSugarMap.entrySet()) {
                System.out.println("Tarih: " + entry.getKey() + ", Ortalama: " + entry.getValue());
            }

            System.out.println("Toplam diyet takip kaydı sayısı: " + dietStatusMap.size());
            for (Map.Entry<LocalDate, Boolean> entry : dietStatusMap.entrySet()) {
                System.out.println("Tarih: " + entry.getKey() + ", Uygulandı: " + entry.getValue());
            }

            // Her tarih için veriyi ekle
            for (Map.Entry<LocalDate, Double> entry : bloodSugarMap.entrySet()) {
                LocalDate date = entry.getKey();
                double value = entry.getValue();
                Day day = new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear());
                bloodSugarSeries.add(day, value);
            }

            // Verileri dataset'e ekle
            TimeSeriesCollection dataset = new TimeSeriesCollection();
            dataset.addSeries(bloodSugarSeries);

            // Grafik oluştur
            JFreeChart chart = ChartFactory.createTimeSeriesChart(
                    "Kan Şekeri ve Diyet İlişkisi",
                    "Tarih",
                    "Kan Şekeri (mg/dL)",
                    dataset,
                    true,
                    true,
                    false
            );

            // Plot'u al
            XYPlot plot = chart.getXYPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

            // Y ekseni formatını düzeltme
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setNumberFormatOverride(new DecimalFormat("0"));
            rangeAxis.setAutoRangeIncludesZero(false);
            rangeAxis.setRange(40, 250); // Kan şekeri değerleri genellikle bu aralıkta
            rangeAxis.setTickUnit(new NumberTickUnit(50)); // 50'şer adımlarla göster

            // Normal aralık işaretçisi
            IntervalMarker normalRange = new IntervalMarker(70, 110);
            normalRange.setPaint(new Color(200, 255, 200, 100));
            normalRange.setLabel("Normal Aralık (70-110)");
            normalRange.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
            normalRange.setLabelPaint(new Color(0, 120, 0));
            plot.addRangeMarker(normalRange);

            // Özel renderer - uygulandı/uygulanmadı durumuna göre nokta renkleri
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer() {
                @Override
                public Paint getItemPaint(int series, int item) {
                    if (series == 0) {
                        TimeSeriesDataItem dataItem = bloodSugarSeries.getDataItem(item);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(dataItem.getPeriod().getStart());

                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH 0-11 arası
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        LocalDate date = LocalDate.of(year, month, day);
                        Boolean applied = dietStatusMap.get(date);

                        if (applied != null) {
                            return applied ? new Color(46, 204, 113) : new Color(231, 76, 60); // Yeşil veya Kırmızı
                        }
                    }
                    return super.getItemPaint(series, item);
                }

                @Override
                public Shape getItemShape(int series, int item) {
                    if (series == 0) {
                        TimeSeriesDataItem dataItem = bloodSugarSeries.getDataItem(item);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(dataItem.getPeriod().getStart());

                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH) + 1;
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        LocalDate date = LocalDate.of(year, month, day);
                        Boolean applied = dietStatusMap.get(date);

                        if (applied != null) {
                            if (applied) {
                                // Diyet uygulandı - kare şekli
                                return new Rectangle2D.Double(-5, -5, 10, 10);
                            } else {
                                // Diyet uygulanmadı - daire şekli
                                return new Ellipse2D.Double(-5, -5, 10, 10);
                            }
                        }
                    }
                    return super.getItemShape(series, item);
                }
            };

            // Render ayarları
            renderer.setSeriesStroke(0, new BasicStroke(2.0f));
            renderer.setSeriesShapesVisible(0, true);
            renderer.setDefaultShapesFilled(true);
            renderer.setUseFillPaint(true);
            plot.setRenderer(renderer);

            // Tarih formatı
            DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setDateFormatOverride(new SimpleDateFormat("dd.MM"));
            dateAxis.setVerticalTickLabels(false);
            dateAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);

            // Açıklama ekleme
            LegendTitle legend = new LegendTitle(new LegendItemSource() {
                @Override
                public LegendItemCollection getLegendItems() {
                    LegendItemCollection items = new LegendItemCollection();
                    items.add(new LegendItem("Diyet Uygulandı", null, null, null,
                            new Rectangle2D.Double(-5, -5, 10, 10), new Color(46, 204, 113)));
                    items.add(new LegendItem("Diyet Uygulanmadı", null, null, null,
                            new Ellipse2D.Double(-5, -5, 10, 10), new Color(231, 76, 60)));
                    return items;
                }
            });
            legend.setPosition(RectangleEdge.BOTTOM);
            chart.addLegend(legend);

            // Grafiği panele ekle
            ChartPanel jfreeChartPanel = new ChartPanel(chart);
            jfreeChartPanel.setPreferredSize(new Dimension(-1, 400));
            chartContainerPanel.add(jfreeChartPanel, BorderLayout.CENTER);

            // Özet bilgisi hazırla
            StringBuilder summary = new StringBuilder();
            summary.append("TARİH ARALIĞI: ")
                    .append(startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    .append(" - ")
                    .append(endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    .append("\n\n");

            summary.append("HASTA: ")
                    .append(selectedPatient.getAd()).append(" ").append(selectedPatient.getSoyad())
                    .append("\n\n");

            // Diyet uygulandığı ve uygulanmadığı günlerdeki kan şekeri ortalamaları
            double appliedAvg = 0, notAppliedAvg = 0;
            int appliedCount = 0, notAppliedCount = 0;

            for (LocalDate date : bloodSugarMap.keySet()) {
                Boolean applied = dietStatusMap.get(date);
                if (applied != null) {
                    if (applied) {
                        appliedAvg += bloodSugarMap.get(date);
                        appliedCount++;
                    } else {
                        notAppliedAvg += bloodSugarMap.get(date);
                        notAppliedCount++;
                    }
                }
            }

            if (appliedCount > 0) {
                appliedAvg /= appliedCount;
            }
            if (notAppliedCount > 0) {
                notAppliedAvg /= notAppliedCount;
            }

            // Grafikteki uyumsuzluk için test - dietStatusMap tüm gerçek kayıt sayısını sayacağız
            long totalAppliedCount = dietStatusMap.values().stream().filter(val -> val).count();
            System.out.println("TÜM DİYET UYGULANDIĞI GÜNLER (VERİTABANI): " + totalAppliedCount);
            System.out.println("GRAFİKTE GÖSTERILEN DİYET UYGULANDIĞI GÜNLER: " + appliedCount);

            // Özet raporda gerçek veritabanı sayılarını kullan
            summary.append("DİYET UYGULANDIĞI GÜNLERİN SAYISI: ")
                    .append(totalAppliedCount)
                    .append("\n");

            summary.append("DİYET UYGULANDIĞI GÜNLERDEKİ ORTALAMA: ")
                    .append(String.format("%.1f mg/dL", appliedAvg));

            if (appliedAvg > 0) {
                if (appliedAvg < 70) {
                    summary.append(" (DÜŞÜK)");
                } else if (appliedAvg > 110) {
                    summary.append(" (YÜKSEK)");
                } else {
                    summary.append(" (NORMAL)");
                }
            }
            summary.append("\n");

            // Diyet uygulanmadığı günler
            long totalNotAppliedCount = dietStatusMap.values().stream().filter(val -> !val).count();

            summary.append("DİYET UYGULANMADIĞI GÜNLERİN SAYISI: ")
                    .append(totalNotAppliedCount)
                    .append("\n");

            summary.append("DİYET UYGULANMADIĞI GÜNLERDEKİ ORTALAMA: ")
                    .append(String.format("%.1f mg/dL", notAppliedAvg));

            if (notAppliedAvg > 0) {
                if (notAppliedAvg < 70) {
                    summary.append(" (DÜŞÜK)");
                } else if (notAppliedAvg > 110) {
                    summary.append(" (YÜKSEK)");
                } else {
                    summary.append(" (NORMAL)");
                }
            }

            summaryArea.setText(summary.toString());
            summaryArea.setCaretPosition(0);

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Grafik yüklenirken hata: " + e.getMessage(), JLabel.CENTER);
            errorLabel.setFont(new Font("Arial", Font.BOLD, 12));
            errorLabel.setForeground(Color.RED);
            chartContainerPanel.add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace();
        }

        // Panel güncellendi, yeniden çiz
        chartContainerPanel.revalidate();
        chartContainerPanel.repaint();
    }

    /**
     * Kan şekeri ve egzersiz ilişkisi grafiğini günceller
     * @param chartContainerPanel Grafik paneli
     * @param summaryArea Özet metin alanı
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     */
    private void updateBloodSugarExerciseChart(JPanel chartContainerPanel, JTextArea summaryArea, LocalDate startDate, LocalDate endDate) {
        // Mevcut grafiği temizle
        chartContainerPanel.removeAll();

        try {
            // Veri erişim nesneleri
            MeasurementDao measurementDao = new MeasurementDao();
            ExerciseTrackingDao exerciseTrackingDao = new ExerciseTrackingDao();

            // Ölçüm ve egzersiz verilerini al
            Map<LocalDate, Double> bloodSugarMap = new HashMap<>();
            Map<LocalDate, Boolean> exerciseStatusMap = new HashMap<>();

            // Her gün için kan şekeri ortalaması
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                try {
                    double dailyAvg = measurementDao.getDailyAverage(selectedPatient.getPatient_id(), date);
                    if (dailyAvg > 0) {
                        bloodSugarMap.put(date, dailyAvg);
                    }
                } catch (SQLException ex) {
                    System.err.println("Kan şekeri ortalaması hesaplanırken hata: " + ex.getMessage());
                }
            }

            // Egzersiz takip verisini al
            List<ExerciseTracking> exerciseTrackings = exerciseTrackingDao.findByPatientIdAndDateRange(
                    selectedPatient.getPatient_id(), startDate, endDate);

            for (ExerciseTracking tracking : exerciseTrackings) {
                if (!tracking.getTakip_tarihi().isBefore(startDate) &&
                        !tracking.getTakip_tarihi().isAfter(endDate)) {
                    exerciseStatusMap.put(tracking.getTakip_tarihi(), tracking.getUygulandi_mi());

                    // Debug için yazdır
                    System.out.println("Egzersiz takip: " + tracking.getTakip_tarihi() + " - " +
                            (tracking.getUygulandi_mi() ? "Yapıldı" : "Yapılmadı"));
                }
            }

            // TimeSeriesCollection için veri serileri
            TimeSeries bloodSugarSeries = new TimeSeries("Kan Şekeri");

            // Debug için konsola yazdır
            System.out.println("Toplam kan şekeri günlük ortalama kaydı sayısı: " + bloodSugarMap.size());
            for (Map.Entry<LocalDate, Double> entry : bloodSugarMap.entrySet()) {
                System.out.println("Tarih: " + entry.getKey() + ", Ortalama: " + entry.getValue());
            }

            System.out.println("Toplam egzersiz takip kaydı sayısı: " + exerciseStatusMap.size());
            for (Map.Entry<LocalDate, Boolean> entry : exerciseStatusMap.entrySet()) {
                System.out.println("Tarih: " + entry.getKey() + ", Uygulandı: " + entry.getValue());
            }

            // Her tarih için veriyi ekle
            for (Map.Entry<LocalDate, Double> entry : bloodSugarMap.entrySet()) {
                LocalDate date = entry.getKey();
                double value = entry.getValue();
                Day day = new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear());
                bloodSugarSeries.add(day, value);
            }

            // Verileri dataset'e ekle
            TimeSeriesCollection dataset = new TimeSeriesCollection();
            dataset.addSeries(bloodSugarSeries);

            // Grafik oluştur
            JFreeChart chart = ChartFactory.createTimeSeriesChart(
                    "Kan Şekeri ve Egzersiz İlişkisi",
                    "Tarih",
                    "Kan Şekeri (mg/dL)",
                    dataset,
                    true,
                    true,
                    false
            );

            // Plot'u al
            XYPlot plot = chart.getXYPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

            // Y ekseni formatını düzeltme
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setNumberFormatOverride(new DecimalFormat("0"));
            rangeAxis.setAutoRangeIncludesZero(false);
            rangeAxis.setRange(40, 250); // Kan şekeri değerleri genellikle bu aralıkta
            rangeAxis.setTickUnit(new NumberTickUnit(50)); // 50'şer adımlarla göster

            // Normal aralık işaretçisi
            IntervalMarker normalRange = new IntervalMarker(70, 110);
            normalRange.setPaint(new Color(200, 255, 200, 100));
            normalRange.setLabel("Normal Aralık (70-110)");
            normalRange.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
            normalRange.setLabelPaint(new Color(0, 120, 0));
            plot.addRangeMarker(normalRange);

            // Özel renderer - yapıldı/yapılmadı durumuna göre nokta renkleri
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer() {
                @Override
                public Paint getItemPaint(int series, int item) {
                    if (series == 0) {
                        TimeSeriesDataItem dataItem = bloodSugarSeries.getDataItem(item);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(dataItem.getPeriod().getStart());

                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH 0-11 arası
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        LocalDate date = LocalDate.of(year, month, day);
                        Boolean performed = exerciseStatusMap.get(date);

                        if (performed != null) {
                            return performed ? new Color(65, 105, 225) : new Color(220, 20, 60); // Mavi veya Kırmızı
                        }
                    }
                    return super.getItemPaint(series, item);
                }

                @Override
                public Shape getItemShape(int series, int item) {
                    if (series == 0) {
                        TimeSeriesDataItem dataItem = bloodSugarSeries.getDataItem(item);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(dataItem.getPeriod().getStart());

                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH) + 1;
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        LocalDate date = LocalDate.of(year, month, day);
                        Boolean performed = exerciseStatusMap.get(date);

                        if (performed != null) {
                            if (performed) {
                                // Egzersiz yapıldı - üçgen şekli
                                return new Polygon(
                                        new int[]{0, -6, 6},
                                        new int[]{-8, 4, 4},
                                        3
                                );
                            } else {
                                // Egzersiz yapılmadı - daire şekli
                                return new Ellipse2D.Double(-5, -5, 10, 10);
                            }
                        }
                    }
                    return super.getItemShape(series, item);
                }
            };

            // Render ayarları
            renderer.setSeriesStroke(0, new BasicStroke(2.0f));
            renderer.setSeriesShapesVisible(0, true);
            renderer.setDefaultShapesFilled(true);
            renderer.setUseFillPaint(true);
            plot.setRenderer(renderer);

            // Tarih formatı
            DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setDateFormatOverride(new SimpleDateFormat("dd.MM"));
            dateAxis.setVerticalTickLabels(false);
            dateAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);

            // Açıklama ekleme
            LegendTitle legend = new LegendTitle(new LegendItemSource() {
                @Override
                public LegendItemCollection getLegendItems() {
                    LegendItemCollection items = new LegendItemCollection();
                    items.add(new LegendItem("Egzersiz Yapıldı", null, null, null,
                            new Polygon(new int[]{0, -6, 6}, new int[]{-8, 4, 4}, 3),
                            new Color(65, 105, 225)));
                    items.add(new LegendItem("Egzersiz Yapılmadı", null, null, null,
                            new Ellipse2D.Double(-5, -5, 10, 10), new Color(220, 20, 60)));
                    return items;
                }
            });
            legend.setPosition(RectangleEdge.BOTTOM);
            chart.addLegend(legend);

            // Grafiği panele ekle
            ChartPanel jfreeChartPanel = new ChartPanel(chart);
            jfreeChartPanel.setPreferredSize(new Dimension(-1, 400));
            chartContainerPanel.add(jfreeChartPanel, BorderLayout.CENTER);

            // Özet bilgisi hazırla
            StringBuilder summary = new StringBuilder();
            summary.append("TARİH ARALIĞI: ")
                    .append(startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    .append(" - ")
                    .append(endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    .append("\n\n");

            summary.append("HASTA: ")
                    .append(selectedPatient.getAd()).append(" ").append(selectedPatient.getSoyad())
                    .append("\n\n");

            // Egzersiz yapıldığı ve yapılmadığı günlerdeki kan şekeri ortalamaları
            double performedAvg = 0, notPerformedAvg = 0;
            int performedCount = 0, notPerformedCount = 0;

            for (LocalDate date : bloodSugarMap.keySet()) {
                Boolean performed = exerciseStatusMap.get(date);
                if (performed != null) {
                    if (performed) {
                        performedAvg += bloodSugarMap.get(date);
                        performedCount++;
                    } else {
                        notPerformedAvg += bloodSugarMap.get(date);
                        notPerformedCount++;
                    }
                }
            }

            if (performedCount > 0) {
                performedAvg /= performedCount;
            }
            if (notPerformedCount > 0) {
                notPerformedAvg /= notPerformedCount;
            }

            // Grafikteki uyumsuzluk için test - veritabanındaki tüm egzersiz kayıt sayısını say
            long totalPerformedCount = exerciseStatusMap.values().stream().filter(val -> val).count();
            System.out.println("TÜM EGZERSİZ YAPILAN GÜNLER (VERİTABANI): " + totalPerformedCount);
            System.out.println("GRAFİKTE GÖSTERİLEN EGZERSİZ YAPILAN GÜNLER: " + performedCount);

            // Özet raporda gerçek veritabanı sayılarını kullan
            summary.append("EGZERSİZ YAPILDIĞI GÜNLERİN SAYISI: ")
                    .append(totalPerformedCount)
                    .append("\n");

            summary.append("EGZERSİZ YAPILDIĞI GÜNLERDEKİ ORTALAMA: ")
                    .append(String.format("%.1f mg/dL", performedAvg));

            if (performedAvg > 0) {
                if (performedAvg < 70) {
                    summary.append(" (DÜŞÜK)");
                } else if (performedAvg > 110) {
                    summary.append(" (YÜKSEK)");
                } else {
                    summary.append(" (NORMAL)");
                }
            }
            summary.append("\n");

            // Egzersiz yapılmadığı günler
            long totalNotPerformedCount = exerciseStatusMap.values().stream().filter(val -> !val).count();

            summary.append("EGZERSİZ YAPILMADIĞI GÜNLERİN SAYISI: ")
                    .append(totalNotPerformedCount)
                    .append("\n");

            summary.append("EGZERSİZ YAPILMADIĞI GÜNLERDEKİ ORTALAMA: ")
                    .append(String.format("%.1f mg/dL", notPerformedAvg));

            if (notPerformedAvg > 0) {
                if (notPerformedAvg < 70) {
                    summary.append(" (DÜŞÜK)");
                } else if (notPerformedAvg > 110) {
                    summary.append(" (YÜKSEK)");
                } else {
                    summary.append(" (NORMAL)");
                }
            }

            summaryArea.setText(summary.toString());
            summaryArea.setCaretPosition(0);

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Grafik yüklenirken hata: " + e.getMessage(), JLabel.CENTER);
            errorLabel.setFont(new Font("Arial", Font.BOLD, 12));
            errorLabel.setForeground(Color.RED);
            chartContainerPanel.add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace();
        }

        // Panel güncellendi, yeniden çiz
        chartContainerPanel.revalidate();
        chartContainerPanel.repaint();
    }


    // Uyarılar paneli - Hastaların güne göre uyarıları
    private JPanel createAlertsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filtreleme kontrolleri
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Uyarı Türü:"));

        // Uyarı türlerini veritabanından yükle
        DefaultComboBoxModel<String> alertTypeModel = new DefaultComboBoxModel<>();
        alertTypeModel.addElement("Tüm Uyarılar"); // Her zaman ilk eleman olarak ekleyelim

        try {
            // Veritabanından uyarı türlerini al
            AlertTypeDao alertTypeDao = new AlertTypeDao();
            List<AlertType> alertTypes = alertTypeDao.findAll();

            // Uyarı türlerini modele ekle
            for (AlertType alertType : alertTypes) {
                alertTypeModel.addElement(alertType.getTip_adi());
            }

        } catch (SQLException ex) {
            System.err.println("Uyarı türleri yüklenirken hata oluştu: " + ex.getMessage());
            ex.printStackTrace();

            // Hata durumunda manuel ekleme yap
            alertTypeModel.addElement("Kritik (Acil Düşük/Yüksek)");
            alertTypeModel.addElement("Bilgilendirme");
        }

        JComboBox<String> alertTypeCombo = new JComboBox<>(alertTypeModel);
        controlPanel.add(alertTypeCombo);

        controlPanel.add(new JLabel("  Tarih:"));
        JComboBox<String> dateRangeCombo = new JComboBox<>(new String[] {
                "Bugün",
                "Son 7 Gün",
                "Son 30 Gün"
        });
        controlPanel.add(dateRangeCombo);

        // Uyarılar tablosu - Durumu sütunu kaldırıldı
        String[] columns = {"Tarih", "Saat", "Uyarı Türü", "Uyarı Mesajı"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tablo hücrelerinin düzenlenmesini engelle
            }
        };
        JTable alertsTable = new JTable(model);

        // Bilgi etiketi - tablonun altına eklenecek
        JLabel infoLabel = new JLabel("Seçilen filtrelere uygun uyarı bulunmuyor.", SwingConstants.CENTER);
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setVisible(false); // Başlangıçta gizli

        // Detay panelini burada tanımla (tablo seçim dinleyicileri için gerekli)
        JTextArea detailArea = new JTextArea(4, 20);
        detailArea.setEditable(false);
        detailArea.setText("Detaylı bilgi görmek için bir uyarı seçin.");
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);

        // Tablo seçim olayını dinle
        alertsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && alertsTable.getSelectedRow() != -1) {
                // Seçilen satırdan mesajı al
                String alertMessage = (String) alertsTable.getValueAt(alertsTable.getSelectedRow(), 3);
                // Detay alanına göster
                detailArea.setText(alertMessage);
            }
        });

        // Uyarıları yenileme butonu - alertsTable tanımlandıktan sonra
        JButton refreshAlertsButton = new JButton("Yenile");
        refreshAlertsButton.addActionListener(e -> {
            // Buton tıklandığında popup göstermek için true parameteresi geçiyoruz
            refreshAlertsTable(alertTypeCombo, dateRangeCombo, (DefaultTableModel) alertsTable.getModel(),
                    detailArea, infoLabel, true);
        });
        controlPanel.add(refreshAlertsButton);

        panel.add(controlPanel, BorderLayout.NORTH);

        // Tablo ve bilgi etiketini bir container panele ekle
        JPanel tableContainer = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(alertsTable);
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        tableContainer.add(infoLabel, BorderLayout.SOUTH);
        tableContainer.setBorder(BorderFactory.createTitledBorder("Hasta İçin Oluşturulan Uyarılar"));

        panel.add(tableContainer, BorderLayout.CENTER);

        // Uyarı detay paneli
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("Seçili Uyarı Detayı"));

        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailPanel.add(detailScroll, BorderLayout.CENTER);

        panel.add(detailPanel, BorderLayout.SOUTH);

        // İlk kez yüklendiğinde varsayılan filtrelerle uyarıları getir - popup gösterme
        refreshAlertsTable(alertTypeCombo, dateRangeCombo, model, detailArea, infoLabel, false);

        return panel;
    }

    /**
     * Seçilen filtrelere göre uyarı tablosunu yeniler
     *
     * @param alertTypeCombo Seçilen uyarı türü
     * @param dateRangeCombo Seçilen tarih aralığı
     * @param tableModel Güncellenecek tablo modeli
     * @param detailArea Detay metin alanı
     * @param infoLabel Bilgi etiketi
     * @param showPopup Uyarı bulunamadığında popup gösterilsin mi?
     */
    private void refreshAlertsTable(JComboBox<String> alertTypeCombo, JComboBox<String> dateRangeCombo,
                                    DefaultTableModel tableModel, JTextArea detailArea,
                                    JLabel infoLabel, boolean showPopup) {
        // Tablo içeriğini temizle
        tableModel.setRowCount(0);

        // Detay alanını sıfırla
        detailArea.setText("Detaylı bilgi görmek için bir uyarı seçin.");

        try {
            // Doktor ID'si - loggedInDoctor yerine mevcut doctorId değişkenini kullan
            if (doctorId == null) {
                JOptionPane.showMessageDialog(null,
                        "Doktor bilgisi bulunamadı. Lütfen tekrar giriş yapın.",
                        "Oturum Hatası",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Seçilen filtreler
            String selectedAlertType = (String) alertTypeCombo.getSelectedItem();
            String selectedDateRange = (String) dateRangeCombo.getSelectedItem();

            // AlertDao ve AlertTypeDao oluştur
            AlertDao alertDao = new AlertDao();
            AlertTypeDao alertTypeDao = new AlertTypeDao();

            // Filtreleme koşullarını belirle
            List<Alert> filteredAlerts = new ArrayList<>();
            List<Alert> doctorAlerts = alertDao.findByDoctorId(doctorId);

            // Uyarı türüne göre filtrele
            if (selectedAlertType.equals("Tüm Uyarılar")) {
                filteredAlerts = doctorAlerts;
            } else {
                // Seçilen uyarı türünün ID'sini bul
                AlertType selectedType = alertTypeDao.findByName(selectedAlertType);

                if (selectedType != null) {
                    // Sadece seçilen türdeki uyarıları filtrele
                    for (Alert alert : doctorAlerts) {
                        if (alert.getAlertType().getAlert_type_id().equals(selectedType.getAlert_type_id())) {
                            filteredAlerts.add(alert);
                        }
                    }
                }
            }

            // Tarih aralığına göre filtrele
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate;

            switch (selectedDateRange) {
                case "Bugün":
                    startDate = now.toLocalDate().atStartOfDay();
                    break;
                case "Son 7 Gün":
                    startDate = now.minusDays(7);
                    break;
                case "Son 30 Gün":
                    startDate = now.minusDays(30);
                    break;
                default:
                    startDate = now.minusYears(1); // Varsayılan olarak son 1 yıl
            }

            // Son filtrelemeyi uygula ve tabloya ekle
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            List<Alert> finalFilteredAlerts = new ArrayList<>();
            for (Alert alert : filteredAlerts) {
                if (alert.getOlusturma_zamani().isAfter(startDate)) {
                    finalFilteredAlerts.add(alert);
                }
            }

            // Uyarıları tarihe göre sırala (en yeni en üstte)
            finalFilteredAlerts.sort((a1, a2) -> a2.getOlusturma_zamani().compareTo(a1.getOlusturma_zamani()));

            // Tabloya ekle
            for (Alert alert : finalFilteredAlerts) {
                tableModel.addRow(new Object[]{
                        alert.getOlusturma_zamani().format(dateFormatter),
                        alert.getOlusturma_zamani().format(timeFormatter),
                        alert.getAlertType().getTip_adi(),
                        alert.getMesaj()
                });
            }

            // Veri yoksa bilgi etiketini göster, varsa gizle
            boolean hasNoData = tableModel.getRowCount() == 0;
            infoLabel.setVisible(hasNoData);

            // Eğer hiç veri yoksa ve popup gösterilmesi isteniyorsa bilgi mesajı göster
            if (hasNoData && showPopup) {
                JOptionPane.showMessageDialog(null,
                        "Seçilen filtrelerle eşleşen uyarı bulunamadı.",
                        "Bilgi",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Uyarılar yüklenirken hata oluştu: " + ex.getMessage(),
                    "Veritabanı Hatası",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }



    /**
     * Seçilen hastanın detaylarını gösterir
     */
    private void showPatientDetail(String tcKimlik) {
        try {
            // Hasta bilgilerini getir
            Patient patient = null;
            for (Patient p : allPatients) {
                if (p.getTc_kimlik().equals(tcKimlik)) {
                    patient = p;
                    break;
                }
            }

            if (patient == null) {
                JOptionPane.showMessageDialog(this,
                        "Hasta bilgileri bulunamadı",
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            selectedPatient = patient;

            // Üst paneli gizle (doktor profil resmi ve hoş geldiniz yazısı)
            Component topComponent = getComponent(0); // BorderLayout.NORTH bileşenini al
            topComponent.setVisible(false); // Panel görünürlüğünü kapat

            // Hasta bilgileri panelini güncelle
            JPanel infoPanel = (JPanel) patientDetailPanel.getComponent(0);
            JPanel contentPanel = (JPanel) infoPanel.getComponent(0);

            // Profil resmi panel (sol taraf)
            JPanel profileImagePanel = (JPanel) contentPanel.getComponent(0);
            JLabel patientImageLabel = (JLabel) profileImagePanel.getComponent(0);

            // Hasta profil resmini göster
            if (patient.getProfil_resmi() != null && patient.getProfil_resmi().length > 0) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(patient.getProfil_resmi());
                    BufferedImage originalImage = ImageIO.read(bis);

                    if (originalImage != null) {
                        // Resmi uygun boyuta ölçeklendir
                        Image scaledImage = originalImage.getScaledInstance(90, 110, Image.SCALE_SMOOTH);
                        patientImageLabel.setIcon(new ImageIcon(scaledImage));
                        patientImageLabel.setText("");
                    } else {
                        // Varsayılan resim göster (baş harfler)
                        createDefaultPatientImage(patientImageLabel, patient);
                    }
                } catch (IOException e) {
                    System.err.println("Hasta profil resmi yüklenemedi: " + e.getMessage());
                    createDefaultPatientImage(patientImageLabel, patient);
                }
            } else {
                // Profil resmi yok, varsayılan resim göster
                createDefaultPatientImage(patientImageLabel, patient);
            }

            // Bilgi paneli (sağ taraf)
            JPanel patientInfoPanel = (JPanel) contentPanel.getComponent(1);

            JLabel nameLabel = (JLabel) patientInfoPanel.getComponent(1);
            JLabel tcLabel = (JLabel) patientInfoPanel.getComponent(3);
            JLabel ageLabel = (JLabel) patientInfoPanel.getComponent(5);
            JLabel emailLabel = (JLabel) patientInfoPanel.getComponent(7);

            // Yaşı hesapla
            int age = 0;
            if (patient.getDogum_tarihi() != null) {
                age = LocalDate.now().getYear() - patient.getDogum_tarihi().getYear();
            }

            nameLabel.setText(patient.getAd() + " " + patient.getSoyad());
            tcLabel.setText(patient.getTc_kimlik());
            ageLabel.setText(String.valueOf(age));
            emailLabel.setText(patient.getEmail());

            // Detay panelini göster
            cardLayout.show(centerPanel, DETAIL_PANEL);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Hasta detayları gösterilirken bir hata oluştu: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Varsayılan hasta profil resmi oluşturan yardımcı method
    private void createDefaultPatientImage(JLabel imageLabel, Patient patient) {
        // Resmi temizle
        imageLabel.setIcon(null);

        // Kişinin baş harflerinden görsel oluştur
        BufferedImage img = new BufferedImage(90, 90, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        // Arkaplan rengi
        g2.setColor(new Color(70, 130, 180)); // Steel Blue
        g2.fillRect(0, 0, 90, 90);

        // Baş harfler
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        g2.setColor(Color.WHITE);

        String initials = "";
        if (patient.getAd() != null && !patient.getAd().isEmpty()) {
            initials += patient.getAd().substring(0, 1);
        }
        if (patient.getSoyad() != null && !patient.getSoyad().isEmpty()) {
            initials += patient.getSoyad().substring(0, 1);
        }

        // Metni ortala
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(initials);
        int textHeight = fm.getHeight();
        g2.drawString(initials, (90 - textWidth) / 2, 45 + fm.getAscent() / 2 - textHeight / 4);

        g2.dispose();

        // Label'a ekle
        imageLabel.setIcon(new ImageIcon(img));
        imageLabel.setText(null);
    }

    /**
     * Hasta verilerini yükler ve tabloya ekler
     */
    private void loadPatientData() {
        // doctorId kullanarak hastaları al
        allPatients = patientService.getDoctorPatients(doctorId);
        filteredPatients = new ArrayList<>(allPatients);

        updatePatientTable(filteredPatients);

        // Hasta combo box'larını güncelle
        if(bloodMeasurePatientCombo != null) {
            bloodMeasurePatientCombo = updatePatientCombo(bloodMeasurePatientCombo);
        }

        if (dietPatientCombo != null) {
            dietPatientCombo = updatePatientCombo(dietPatientCombo);
        }

        if (exercisePatientCombo != null) {
            exercisePatientCombo = updatePatientCombo(exercisePatientCombo);
        }
    }

    /**
     * Hasta tablosunu günceller
     */
    private void updatePatientTable(List<Patient> patients) {
        patientTableModel.setRowCount(0); // Tabloyu temizle

        for (Patient patient : patients) {
            Object[] row = new Object[7];
            row[0] = patient.getTc_kimlik();
            row[1] = patient.getAd();
            row[2] = patient.getSoyad();
            row[3] = patient.getEmail();
            row[4] = patient.getCinsiyet() == 'E' ? "Erkek" : "Kadın";

            // Son ölçüm bilgisini getir
            String lastMeasurementText = "Veri yok";
            if (patient.getPatient_id() != null) {
                try {
                    // Yeni yöntem: MeasurementDao kullanarak doğrudan son ölçüm değerini al
                    MeasurementDao measurementDao = new MeasurementDao();
                    Integer lastValue = measurementDao.getLastMeasurementValue(patient.getPatient_id());
                    if (lastValue != null) {
                        lastMeasurementText = lastValue + " mg/dL";
                    }
                } catch (SQLException e) {
                    System.err.println("Hasta için son ölçüm bilgisi alınamadı: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            row[5] = lastMeasurementText;

            // Belirti sayısını getir
            String symptomCountText = "Veri yok";
            if (patient.getPatient_id() != null) {
                try {
                    List<PatientSymptom> symptoms = symptomService.getPatientSymptoms(patient.getPatient_id());
                    if (symptoms != null && !symptoms.isEmpty()) {
                        symptomCountText = symptoms.size() + " belirti";
                    }
                } catch (Exception e) {
                    System.err.println("Hasta için belirti sayısı alınamadı: " + e.getMessage());
                }
            }
            row[6] = symptomCountText;

            patientTableModel.addRow(row);
        }
    }

    /**
     * Filtreleri uygular
     */
    private void applyFilters() {
        String bloodLevelFilter = (String) bloodLevelFilterCombo.getSelectedItem();
        String symptomFilter = (String) symptomFilterCombo.getSelectedItem();

        System.out.println("Seçilen filtreler: Kan Şekeri = " + bloodLevelFilter + ", Belirti = " + symptomFilter);

        // Önce filtrelenecek hasta listesini tüm hastalardan başlat
        filteredPatients = new ArrayList<>(allPatients);
        List<Patient> tempFilteredList = new ArrayList<>();

        // Kan şekeri seviyesine göre filtreleme
        if (!"Tümü".equals(bloodLevelFilter)) {
            for (Patient patient : filteredPatients) {
                try {
                    // MeasurementDao kullanarak en son kan şekeri ölçümünü al
                    MeasurementDao measurementDao = new MeasurementDao();
                    Integer lastValue = measurementDao.getLastMeasurementValue(patient.getPatient_id());

                    if (lastValue != null) {
                        boolean matchesFilter = false;

                        if ("Düşük-Hipoglisemi (70 mg/dL altı)".equals(bloodLevelFilter) && lastValue < 70) {
                            matchesFilter = true;
                        } else if ("Normal (70-99 mg/dL)".equals(bloodLevelFilter) && lastValue >= 70 && lastValue <= 99) {
                            matchesFilter = true;
                        } else if ("Orta-Prediyabet (100-125 mg/dL)".equals(bloodLevelFilter) && lastValue >= 100 && lastValue <= 125) {
                            matchesFilter = true;
                        } else if ("Yüksek-Diyabet (126 mg/dL üstü)".equals(bloodLevelFilter) && lastValue > 125) {
                            matchesFilter = true;
                        }

                        if (matchesFilter) {
                            tempFilteredList.add(patient);
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Hasta için kan şekeri değeri alınamadı: " + e.getMessage());
                }
            }

            filteredPatients = new ArrayList<>(tempFilteredList);
            tempFilteredList.clear();
        }

        // Semptoma göre filtreleme - Service sınıfı kullanarak - Optimize edilmiş
        if (!"Tümü".equals(symptomFilter)) {
            System.out.println("Belirti filtreleme başlıyor: " + symptomFilter);

            // Direkt olarak belirti adına göre hasta ID'lerini al
            List<Integer> matchingPatientIds = symptomService.findPatientIdsBySymptomName(symptomFilter);
            System.out.println("Belirtiye sahip hasta ID sayısı: " + matchingPatientIds.size());

            // Mevcut filtrelenmiş listeden eşleşen ID'leri koru
            for (Patient patient : filteredPatients) {
                if (patient.getPatient_id() != null && matchingPatientIds.contains(patient.getPatient_id())) {
                    tempFilteredList.add(patient);
                }
            }

            filteredPatients = new ArrayList<>(tempFilteredList);
            System.out.println("Belirti filtreleme sonucu hasta sayısı: " + filteredPatients.size());
        }

        // Tabloyu güncelle
        updatePatientTable(filteredPatients);

        JOptionPane.showMessageDialog(this,
                "Filtreleme işlemi tamamlandı. Toplam " + filteredPatients.size() + " hasta bulundu.",
                "Filtreleme Sonucu",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Filtreleri temizler
     */
    private void clearFilters() {
        bloodLevelFilterCombo.setSelectedItem("Tümü");
        symptomFilterCombo.setSelectedItem("Tümü");

        filteredPatients = new ArrayList<>(allPatients);
        updatePatientTable(filteredPatients);

        JOptionPane.showMessageDialog(this,
                "Filtreler temizlendi. Tüm hastalar görüntüleniyor.",
                "Filtreler Temizlendi",
                JOptionPane.INFORMATION_MESSAGE);
    }



    private void refreshPatientList() {
        // Hasta listesi panelini yeniden oluştur
        tabbedPane.remove(patientListPanel);
        patientListPanel = createPatientListPanel();
        tabbedPane.insertTab("Hasta Listesi", null, patientListPanel, null, 0);
        tabbedPane.setSelectedIndex(0);
    }

    public void refreshData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Hoş Geldiniz, Dr. " + currentUser.getAd() + " " + currentUser.getSoyad());

        // Profil resmini güncelle
        try {
            // Üst paneli bulma
            Component[] components = this.getComponents();
            JPanel topPanel = null;

            for (Component component : components) {
                if (component instanceof JPanel && component.getClass().getName().equals("javax.swing.JPanel")) {
                    if (((JPanel)component).getLayout() instanceof BorderLayout) {
                        topPanel = (JPanel)component;
                        break;
                    }
                }
            }

            if (topPanel != null) {
                // Profil panelini bul (sol tarafta)
                Component leftComponent = ((BorderLayout)topPanel.getLayout()).getLayoutComponent(BorderLayout.WEST);
                if (leftComponent instanceof JPanel) {
                    JPanel profilePanel = (JPanel)leftComponent;

                    // Profil panelindeki ilk bileşen (profil resmi etiketi) olacak
                    if (profilePanel.getComponentCount() > 0 && profilePanel.getComponent(0) instanceof JLabel) {
                        profilePanel.remove(0); // Eski profil resmini kaldır
                        profilePanel.add(updateProfileImage(), 0); // Yeni profil resmini ekle
                        profilePanel.revalidate(); // Panel yerleşimini güncelle
                        profilePanel.repaint(); // Paneli yeniden çiz

                        System.out.println("Profil resmi güncellendi");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Profil resmi güncellenirken hata: " + e.getMessage());
            e.printStackTrace();
        }

        // Doktor ID'sini yeniden bul
        try {
            DoctorDao doctorDao = new DoctorDao();
            Doctor doctor = doctorDao.findByUserId(user.getUser_id());
            if (doctor != null) {
                this.doctorId = doctor.getDoctor_id();
                System.out.println("Doktor ID güncellendi: " + this.doctorId);
            }
        } catch (SQLException e) {
            System.err.println("Doktor bilgisi alınırken hata: " + e.getMessage());
        }

        refreshPatientList();
        loadPatientData(); // Hasta verilerini yenile
    }
}
