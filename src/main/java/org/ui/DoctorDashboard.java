package org.ui;

import org.dao.*;
import org.model.*;
import org.service.*;
import org.util.DateTimeUtil;
import org.util.ValidationUtil;
import org.util.PasswordUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private JTextField bloodSugarAverageField; // Ortalama kan şekeri değeri
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
        welcomeLabel = new JLabel("Hoş Geldiniz, Dr. " + currentUser.getAd() + " " + currentUser.getSoyad());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        logoutButton = new JButton("Çıkış Yap");
        logoutButton.addActionListener(e -> parent.showLoginPanel());
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

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
        bloodSugarPanel.setBorder(BorderFactory.createTitledBorder("Kan Şekeri Ölçümleri"));

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

        // Ortalama değer gösterimi
        bloodSugarGbc.gridx = 0;
        bloodSugarGbc.gridy = 2;
        bloodSugarInputPanel.add(new JLabel("Günlük Ortalama:"), bloodSugarGbc);

        bloodSugarGbc.gridx = 1;
        bloodSugarAverageField = new JTextField(10);
        bloodSugarAverageField.setEditable(false);
        bloodSugarInputPanel.add(bloodSugarAverageField, bloodSugarGbc);

        bloodSugarPanel.add(bloodSugarInputPanel, BorderLayout.NORTH);


        bloodSugarTableModel = new DefaultTableModel(new String[]{"Zaman Dilimi", "Ölçüm Değeri (mg/dL)"}, 0);
        JTable bloodSugarTable = new JTable(bloodSugarTableModel);
        JScrollPane tableScrollPane = new JScrollPane(bloodSugarTable);
        tableScrollPane.setPreferredSize(new Dimension(400, 100));
        bloodSugarPanel.add(tableScrollPane, BorderLayout.CENTER);

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

                // Aynı zaman dilimi için önceki kaydı bul
                TempMeasurement newMeasurement = new TempMeasurement(value, periodText);
                int existingIndex = -1;

                for (int i = 0; i < tempBloodSugarMeasurements.size(); i++) {
                    TempMeasurement measurement = tempBloodSugarMeasurements.get(i);
                    if (measurement.getPeriod().equals(periodText)) {
                        existingIndex = i;
                        break;
                    }
                }

                // Eğer aynı zaman dilimi için kayıt varsa, güncelle
                if (existingIndex >= 0) {
                    tempBloodSugarMeasurements.set(existingIndex, newMeasurement);

                    // Tablodaki ilgili satırı güncelle
                    for (int i = 0; i < bloodSugarTableModel.getRowCount(); i++) {
                        if (bloodSugarTableModel.getValueAt(i, 0).equals(periodText)) {
                            bloodSugarTableModel.setValueAt(value, i, 1);
                            break;
                        }
                    }

                    JOptionPane.showMessageDialog(bloodSugarPanel,
                            periodText + " için önceki değer güncellendi.",
                            "Değer Güncellendi",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Yeni bir zaman dilimi için kayıt ekle
                    tempBloodSugarMeasurements.add(newMeasurement);
                    bloodSugarTableModel.addRow(new Object[]{periodText, value});

                    JOptionPane.showMessageDialog(bloodSugarPanel,
                            "Yeni ölçüm geçici olarak kaydedildi. Hasta kayıt işlemi tamamlandığında veritabanına eklenecektir.",
                            "İşlem Başarılı",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                // Ortalamayı hesapla ve göster
                if (!tempBloodSugarMeasurements.isEmpty()) {
                    int sum = 0;
                    for (TempMeasurement measurement : tempBloodSugarMeasurements) {
                        sum += measurement.getValue();
                    }
                    double average = (double) sum / tempBloodSugarMeasurements.size();
                    bloodSugarAverageField.setText(String.format("%.1f", average));
                }

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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Hastayı Tüm Bilgilerle Kaydet");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.addActionListener(e -> savePatientWithAllInfo());
        buttonPanel.add(saveButton);

        mainPanel.add(buttonPanel);

        // Scrollable Panel
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(scrollPane, BorderLayout.CENTER);

        return containerPanel;
    }

    /**
     * Geçici olarak kan şekeri ölçümü değerini kaydeder
     */
    private void addMeasurement() {
        // Form kontrolü
        String valueStr = measurementValueField.getText().trim();
        if (valueStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
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

            // Geçici olarak ortalama değeri saklayalım
            // Burada tek bir ölçüm olduğu için değerin kendisi ortalama olacak
            bloodSugarAverageField.setText(String.valueOf(value));

            JOptionPane.showMessageDialog(this,
                    "Ölçüm değeri geçici olarak kaydedildi.\nHasta kaydı tamamlandığında veritabanına eklenecektir.",
                    "İşlem Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);

            // Form alanını temizleme
            measurementValueField.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Ölçüm değeri 1-999 arasında bir sayı olmalıdır!",
                    "Doğrulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateRecommendations() {
        // Kan şekeri kontrolü
        if (bloodSugarAverageField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen önce kan şekeri değerlerini hesaplayın.",
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

            // Kan şekeri ortalaması - DÜZELTME
            String bloodSugarText = bloodSugarAverageField.getText().trim();
            // Değer virgülle yazılmış olabilir, noktaya çevir
            bloodSugarText = bloodSugarText.replace(',', '.');

            // Önce double olarak parse et, sonra yuvarla
            double bloodSugarValue = Double.parseDouble(bloodSugarText);
            int bloodSugar = (int) Math.round(bloodSugarValue);

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

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Geçersiz kan şekeri değeri: " + bloodSugarAverageField.getText() +
                            "\nLütfen önce ortalama hesaplayın.",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Hata detayını konsola yazdır
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
        if (bloodSugarAverageField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen kan şekeri değerlerini hesaplayın.",
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

                // 8. Kan şekeri ölçümlerini ekle
                for (TempMeasurement temp : tempBloodSugarMeasurements) {
                    try {
                        BloodSugarMeasurement measurement = new BloodSugarMeasurement();
                        measurement.setPatient(addedPatient);
                        measurement.setPatient_id(addedPatient.getPatient_id());
                        measurement.setOlcum_degeri(temp.getValue());
                        measurement.setOlcum_zamani(temp.getPeriod());
                        measurement.setOlcum_tarihi(DateTimeUtil.getCurrentDateTime());
                        measurement.setIs_valid_time(true); // Geçerli ölçüm zamanı
                        measurement.setInsulin_miktari(0.0); // Başlangıç değeri

                        // Ölçümü veritabanına kaydet
                        boolean success = measurementService.addMeasurement(measurement);

                        if (success) {
                            try {
                                // İnsülin miktarını hesapla ve güncelle - mevcut addMeasurement metoduyla aynı mantık
                                MeasurementDao measurementDao = new MeasurementDao();
                                InsulinReferenceDao insulinReferenceDao = new InsulinReferenceDao();

                                // Ölçüm geçerli olarak işaretlenir
                                measurementDao.updateFlag(measurement.getMeasurement_id());

                                // Günlük ortalamayı hesapla
                                int averageValue = (int) measurementDao.getDailyAverage(
                                        measurement.getPatient_id(),
                                        measurement.getOlcum_tarihi().toLocalDate());

                                // Ortalamaya göre insülin referansını bul
                                InsulinReference insulinReference = insulinReferenceDao.findByBloodSugarValue(averageValue);

                                // İnsülin miktarını ayarla
                                if (insulinReference != null) {
                                    double insulinDose = insulinReference.getInsulin_dose();
                                    measurement.setInsulin_miktari(insulinDose);

                                    // Veritabanında insülin miktarını güncelle
                                    measurementDao.updateInsulinAmount(measurement.getMeasurement_id(), insulinDose);

                                    System.out.println("İnsülin dozu başarıyla kaydedildi: " + insulinDose +
                                            " - Ölçüm ID: " + measurement.getMeasurement_id());
                                } else {
                                    System.err.println("Kan şekeri değeri için insülin referansı bulunamadı: " + averageValue);
                                }
                            } catch (SQLException ex) {
                                System.err.println("İnsülin miktarı kaydedilirken hata: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        } else {
                            System.err.println("Ölçüm kaydedilemedi: " + temp.getValue() + " - " + temp.getPeriod());
                        }
                    } catch (Exception ex) {
                        System.err.println("Ölçüm kaydedilirken hata: " + ex.getMessage());
                        ex.printStackTrace();
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
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Şifre alanı boş bırakılamaz.",
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

        // Kan şekeri ölçümlerini sıfırla
        tempBloodSugarMeasurements.clear();
        bloodSugarTableModel.setRowCount(0);
        bloodSugarAverageField.setText("");
        measurementValueField.setText("");

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

    private void addPatient() {
        // Form alanlarını al
        String tcKimlik = tcKimlikField.getText();
        String ad = adField.getText();
        String soyad = soyadField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String cinsiyet = cinsiyetCombo.getSelectedItem().toString().substring(0, 1); // E veya K

        // Doğum tarihini al (JSpinner'dan Date olarak alınır, LocalDate'e çevrilir)
        Date spinnerDate = (Date) dogumTarihiSpinner.getValue();
        LocalDate dogumTarihi = spinnerDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Validasyon kontrolleri
        if (tcKimlik.isEmpty() || ad.isEmpty() || soyad.isEmpty() ||
                email.isEmpty() || password.isEmpty()) {
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

        // Patient nesnesi oluştur
        Patient patient = new Patient();
        patient.setTc_kimlik(tcKimlik);
        patient.setAd(ad);
        patient.setSoyad(soyad);
        patient.setEmail(email);
        patient.setDogum_tarihi(dogumTarihi);  // Doğum tarihini ekledik

        // Şifreyi hashle
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

        patient.setCinsiyet(cinsiyet.charAt(0));
        patient.setKullanici_tipi("hasta"); // Hasta kaydı

        // DoctorDao kullanarak mevcut kullanıcının doktor nesnesini bul
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

        // Kayıt işlemini gerçekleştir
        Patient addedPatient = patientService.addPatient(patient,password);

        if (addedPatient != null) {
            JOptionPane.showMessageDialog(this,
                    "Hasta başarıyla eklendi!",
                    "İşlem Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);
            clearAddPatientFields();
            refreshPatientList();
            loadPatientData(); // Yeni hasta verilerini yükle
            tabbedPane.setSelectedIndex(0); // Hasta listesi sekmesine geç
        } else {
            JOptionPane.showMessageDialog(this,
                    "Hasta eklenemedi. Bu TC Kimlik No veya e-posta zaten kullanılıyor olabilir.",
                    "İşlem Hatası",
                    JOptionPane.ERROR_MESSAGE);
        }
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

        // Kan seviyesi filtresi
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

        // Belirti filtresi
        gbc.gridx = 0;
        gbc.gridy = 1;
        filterPanel.add(new JLabel("Hastalık Belirtisi:"), gbc);

        gbc.gridx = 1;
        symptomFilterCombo = new JComboBox<>(new String[] {
                "Tümü", "Yorgunluk", "Aşırı Susama", "Sık İdrara Çıkma", "Bulanık Görme", "Kilo Kaybı"
        });
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

        // Hasta tablosu
        String[] columnNames = {
                "TC Kimlik",
                "Ad",
                "Soyad",
                "E-posta",
                "Cinsiyet",
                "Son Ölçüm",
                "Son Belirti"
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
        JPanel bloodSugarPanel = createBloodSugarChartPanel();
        patientDetailTabs.addTab("Kan Şekeri Takibi", bloodSugarPanel);

        // İlişki analizi sekmesi - Kan şekeri, diyet ve egzersiz ilişkisi grafiği
        JPanel analysisPanel = createAnalysisPanel();
        patientDetailTabs.addTab("İlişki Analizi", analysisPanel);

        // Uyarılar sekmesi - Hastaların güne göre uyarıları
        JPanel alertsPanel = createAlertsPanel();
        patientDetailTabs.addTab("Uyarılar", alertsPanel);

        panel.add(patientDetailTabs, BorderLayout.CENTER);

        return panel;
    }

    // Hasta bilgileri üst paneli
    private JPanel createPatientInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Hasta Bilgileri"));

        JPanel patientInfoPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        patientInfoPanel.add(new JLabel("Ad Soyad: "));
        patientInfoPanel.add(new JLabel("TC Kimlik: "));
        patientInfoPanel.add(new JLabel("Yaş: "));
        patientInfoPanel.add(new JLabel("E-posta: "));

        infoPanel.add(patientInfoPanel, BorderLayout.CENTER);

        // Geri dönüş butonu
        JButton backButton = new JButton("Listeye Dön");
        backButton.addActionListener(e -> {
            // Ana sekme paneline dön
            cardLayout.show(centerPanel, TABS_PANEL);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);
        infoPanel.add(buttonPanel, BorderLayout.SOUTH);

        return infoPanel;
    }

    // Kan şekeri grafiği paneli - Gün ve saat bazlı takip
    private JPanel createBloodSugarChartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Grafik alanı için panel
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("Kan Şekeri Değerlerinin Gün ve Saat Bazlı Grafiği"));
        chartPanel.setPreferredSize(new Dimension(600, 300));

        // Grafik alanı için bilgilendirme etiketi
        JLabel chartInfoLabel = new JLabel("<html><center>Bu alanda gün ve saat bazlı kan şekeri değerleri grafiği gösterilecektir.<br>" +
                "Veri olmadığı için grafik şu an boş görünmektedir.</center></html>", JLabel.CENTER);
        chartInfoLabel.setForeground(Color.GRAY);
        chartPanel.add(chartInfoLabel, BorderLayout.CENTER);

        panel.add(chartPanel, BorderLayout.CENTER);

        // Filtreleme ve tarih seçimi kontrolleri
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Tarih Aralığı:"));

        JComboBox<String> dateRangeCombo = new JComboBox<>(new String[] {
                "Son 7 Gün", "Son 30 Gün", "Son 3 Ay", "Özel Aralık"
        });
        controlPanel.add(dateRangeCombo);

        JButton updateButton = new JButton("Güncelle");
        updateButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(panel,
                    "Grafik henüz uygulanmamıştır. Veriler güncellenecektir.",
                    "Bilgi",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        controlPanel.add(updateButton);

        panel.add(controlPanel, BorderLayout.NORTH);

        // Ölçüm verileri tablosu
        String[] columnNames = {"Tarih", "Saat", "Ölçüm Değeri (mg/dL)", "Kategori", "Durum"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable measurementsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(measurementsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Kan Şekeri Ölçümleri"));

        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
    }

    // İlişki analizi paneli - Kan şekeri, diyet ve egzersiz ilişkisi grafiği
    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Analiz türü seçimi
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Analiz Türü:"));

        JComboBox<String> analysisCombo = new JComboBox<>(new String[] {
                "Kan Şekeri - Diyet İlişkisi",
                "Kan Şekeri - Egzersiz İlişkisi",
                "Çoklu Faktör Analizi"
        });
        controlPanel.add(analysisCombo);

        JButton analyzeButton = new JButton("Analiz Et");
        analyzeButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(panel,
                    "Analiz işlevi henüz uygulanmamıştır.",
                    "Bilgi",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        controlPanel.add(analyzeButton);

        panel.add(controlPanel, BorderLayout.NORTH);

        // Grafik alanı
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("İlişki Analizi Grafiği"));
        chartPanel.setPreferredSize(new Dimension(600, 400));

        // Grafik alanı için bilgilendirme etiketi
        JLabel chartInfoLabel = new JLabel("<html><center>Bu alanda kan şekeri değerleri ile diyet ve egzersiz<br>" +
                "arasındaki ilişkiyi gösteren grafikler yer alacaktır.<br>" +
                "Veri olmadığı için grafik şu an boş görünmektedir.</center></html>", JLabel.CENTER);
        chartInfoLabel.setForeground(Color.GRAY);
        chartPanel.add(chartInfoLabel, BorderLayout.CENTER);

        panel.add(chartPanel, BorderLayout.CENTER);

        // Analiz özeti
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Analiz Özeti"));

        JTextArea summaryArea = new JTextArea(4, 20);
        summaryArea.setEditable(false);
        summaryArea.setText("Analiz henüz gerçekleştirilmemiştir. Lütfen analiz türünü seçerek 'Analiz Et' butonuna tıklayın.");
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryPanel.add(summaryScroll, BorderLayout.CENTER);

        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }



    // Uyarılar paneli - Hastaların güne göre uyarıları
    private JPanel createAlertsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filtreleme kontrolleri
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Uyarı Türü:"));

        JComboBox<String> alertTypeCombo = new JComboBox<>(new String[] {
                "Tüm Uyarılar",
                "Kritik (Acil Düşük/Yüksek)",
                "Orta Derece",
                "Bilgilendirme"
        });
        controlPanel.add(alertTypeCombo);

        controlPanel.add(new JLabel("  Tarih:"));
        JComboBox<String> dateRangeCombo = new JComboBox<>(new String[] {
                "Bugün",
                "Son 7 Gün",
                "Son 30 Gün"
        });
        controlPanel.add(dateRangeCombo);

        JButton refreshAlertsButton = new JButton("Yenile");
        refreshAlertsButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(panel,
                    "Uyarılar henüz uygulanmamıştır.",
                    "Bilgi",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        controlPanel.add(refreshAlertsButton);

        panel.add(controlPanel, BorderLayout.NORTH);

        // Uyarılar tablosu
        String[] columns = {"Tarih", "Saat", "Uyarı Türü", "Uyarı Mesajı", "Durumu"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable alertsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(alertsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Hasta İçin Oluşturulan Uyarılar"));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Uyarı detay paneli
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("Seçili Uyarı Detayı"));

        JTextArea detailArea = new JTextArea(4, 20);
        detailArea.setEditable(false);
        detailArea.setText("Detaylı bilgi görmek için bir uyarı seçin.");
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailPanel.add(detailScroll, BorderLayout.CENTER);

        panel.add(detailPanel, BorderLayout.SOUTH);

        return panel;
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

            // Hasta bilgileri panelini güncelle
            JPanel infoPanel = (JPanel) patientDetailPanel.getComponent(0);
            JPanel patientInfoPanel = (JPanel) infoPanel.getComponent(0);

            JLabel nameLabel = (JLabel) patientInfoPanel.getComponent(0);
            JLabel tcLabel = (JLabel) patientInfoPanel.getComponent(1);
            JLabel ageLabel = (JLabel) patientInfoPanel.getComponent(2);
            JLabel emailLabel = (JLabel) patientInfoPanel.getComponent(3);

            // Yaşı hesapla
            int age = 0;
            if (patient.getDogum_tarihi() != null) {
                age = LocalDate.now().getYear() - patient.getDogum_tarihi().getYear();
            }

            nameLabel.setText("Ad Soyad: " + patient.getAd() + " " + patient.getSoyad());
            tcLabel.setText("TC Kimlik: " + patient.getTc_kimlik());
            ageLabel.setText("Yaş: " + age);
            emailLabel.setText("E-posta: " + patient.getEmail());

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
            row[5] = "Veri yok"; // Son ölçüm
            row[6] = "Veri yok"; // Son belirti

            patientTableModel.addRow(row);
        }
    }

    /**
     * Filtreleri uygular
     */
    private void applyFilters() {
        String bloodLevelFilter = (String) bloodLevelFilterCombo.getSelectedItem();
        String symptomFilter = (String) symptomFilterCombo.getSelectedItem();

        // Filtreleme başlıyor mesajı
        JOptionPane.showMessageDialog(this,
                "Kan şekeri seviyesi: " + bloodLevelFilter + "\n" +
                        "Belirti türü: " + symptomFilter + "\n\n" +
                        "Filtreleme işlevi henüz tam olarak uygulanmamıştır. " +
                        "Bu özellik, hastaları kan şekeri değerleri ve belirtilerine göre filtreleme yapacaktır.",
                "Filtreleme İşlevi",
                JOptionPane.INFORMATION_MESSAGE);

        // Bu noktada, gerçek bir uygulamada filtreleme kodu çalışacaktır
        // Örneğin: filteredPatients = patientService.filterPatientsByBloodSugarAndSymptoms(doctorId, bloodLevelFilter, symptomFilter);
        // updatePatientTable(filteredPatients);
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

    // Sınıf seviyesi değişkenler
    private List<TempMeasurement> tempBloodSugarMeasurements = new ArrayList<>();
    private DefaultTableModel bloodSugarTableModel;

    private class TempMeasurement {
        private int value;
        private String period;

        public TempMeasurement(int value, String period) {
            this.value = value;
            this.period = period;
        }

        public int getValue() { return value; }
        public String getPeriod() { return period; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TempMeasurement other = (TempMeasurement) obj;
            return period != null && period.equals(other.period);
        }

        @Override
        public int hashCode() {
            return period != null ? period.hashCode() : 0;
        }
    }
}

