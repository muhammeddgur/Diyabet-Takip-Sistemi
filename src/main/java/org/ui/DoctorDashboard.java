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

        // Kan Şekeri Ölçümü Kayıt Sekmesi
        measurementPanel = createMeasurementPanel();
        tabbedPane.addTab("Kan Şekeri Ölçümü", measurementPanel);

        // Diyet Planı Sekmesi
        dietPlanPanel = createDietPlanPanel();
        tabbedPane.addTab("Diyet Planı", dietPlanPanel);

        // Egzersiz Planı Sekmesi
        exercisePlanPanel = createExercisePlanPanel();
        tabbedPane.addTab("Egzersiz Planı", exercisePlanPanel);

        // Hasta Ekleme Sekmesi
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

    /**
     * Kan şekeri ölçümü kaydetme panelini oluşturur
     */
    private JPanel createMeasurementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Kan Şekeri Ölçümü Ekle"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Hasta seçimi
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Hasta:"), gbc);

        gbc.gridx = 1;
        bloodMeasurePatientCombo = new JComboBox<>();
        bloodMeasurePatientCombo = updatePatientCombo(bloodMeasurePatientCombo);
        formPanel.add(bloodMeasurePatientCombo, gbc);

        // Ölçüm değeri
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Ölçüm Değeri (mg/dL):"), gbc);

        gbc.gridx = 1;
        measurementValueField = new JTextField(10);
        formPanel.add(measurementValueField, gbc);

        // Periyot
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Ölçüm Zamanı:"), gbc);

        gbc.gridx = 1;
        periodCombo = new JComboBox<>(new String[] {
                "Sabah (07:00-09:00)",
                "Öğle (12:00-14:00)",
                "İkindi (15:00-17:00)",
                "Akşam (18:00-20:00)",
                "Gece (22:00-24:00)"
        });
        formPanel.add(periodCombo, gbc);

        // Ekle butonu
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton addMeasurementButton = new JButton("Ölçümü Kaydet");
        addMeasurementButton.addActionListener(e -> addMeasurement());
        formPanel.add(addMeasurementButton, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Ölçüm tablosu
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Son Ölçümler"));

        String[] columnNames = {
                "Hasta", "Ölçüm Değeri (mg/dL)", "Zaman Dilimi", "Tarih/Saat", "Kategori", "İnsülin Önerisi"
        };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable measurementsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(measurementsTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Ölçüm ekleme işlemini gerçekleştirir
     */
    //Kan şekeri ölçüm değerleri tabloya burada kaydedilir
    private void addMeasurement() {
        // Form kontrolü
        if (bloodMeasurePatientCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen bir hasta seçin!", "Form Hatası", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String valueStr = measurementValueField.getText().trim();
        if (valueStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen ölçüm değerini girin!", "Form Hatası", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int value = Integer.parseInt(valueStr);
            if (value <= 0 || value >= 1000) {
                throw new NumberFormatException();
            }
            // Seçili hastayı al
            Patient selectedPatient = (Patient) bloodMeasurePatientCombo.getSelectedItem();
            String periodText = (String) periodCombo.getSelectedItem();

            // Ölçüm nesnesi oluştur
            BloodSugarMeasurement measurement = new BloodSugarMeasurement();
            measurement.setPatient(selectedPatient);
            measurement.setPatient_id(selectedPatient.getPatient_id());
            measurement.setOlcum_degeri(value);
            measurement.setOlcum_zamani(periodText);
            measurement.setOlcum_tarihi(DateTimeUtil.getCurrentDateTime()); // Şu anki tarih ve saat

            measurement.setInsulin_miktari(0.0);


            //Service aracılığıyla Dao kullanarak tabloya ekler
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
                measurementValueField.setText("");
                periodCombo.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ölçüm kaydedilirken bir hata oluştu.",
                        "Kayıt Hatası",
                        JOptionPane.ERROR_MESSAGE);
            }

            // Form alanlarını temizle
            measurementValueField.setText("");
            periodCombo.setSelectedIndex(0);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Ölçüm değeri 1-999 arasında bir sayı olmalıdır!",
                    "Doğrulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Diyet planı oluşturma panelini oluşturur
     */
    private JPanel createDietPlanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Diyet Planı Oluştur"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Hasta seçimi
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Hasta:"), gbc);

        gbc.gridx = 1;
        dietPatientCombo = new JComboBox<>();
        dietPatientCombo = updatePatientCombo(dietPatientCombo);
        formPanel.add(dietPatientCombo, gbc);

        // Hasta seçildiğinde belirtileri yükle
        JLabel patientSymptomsLabel = new JLabel("Mevcut Belirtiler: ");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(patientSymptomsLabel, gbc);

        JLabel symptomsInfoLabel = new JLabel("(Belirtiler veritabanından otomatik yüklenir)");
        gbc.gridx = 1;
        formPanel.add(symptomsInfoLabel, gbc);

        // Kan şekeri değeri giriş alanı
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Kan Şekeri Değeri:"), gbc);

        gbc.gridx = 1;
        JTextField bloodSugarField = new JTextField();
        formPanel.add(bloodSugarField, gbc);

        // Diyet tipi seçimi - dinamik olarak doldurulacak
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Diyet Tipi:"), gbc);

        gbc.gridx = 1;
        JComboBox<Diet> dietTypeCombo = new JComboBox<>();
        // Tüm diyet tiplerini yükle
        try {
            List<Diet> allDiets = dietDao.findAll();
            for (Diet diet : allDiets) {
                dietTypeCombo.addItem(diet);
            }
        } catch (Exception e) {
            System.err.println("Diyet tipleri yüklenirken hata: " + e.getMessage());
        }
        formPanel.add(dietTypeCombo, gbc);

        // Diyet açıklaması
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Açıklama:"), gbc);

        gbc.gridx = 1;
        JTextArea dietDescArea = new JTextArea(3, 20);
        dietDescArea.setLineWrap(true);
        dietDescArea.setWrapStyleWord(true);
        JScrollPane dietDescScroll = new JScrollPane(dietDescArea);
        formPanel.add(dietDescScroll, gbc);

        // Diyet tipi seçildiğinde açıklamayı güncelle
        dietTypeCombo.addActionListener(e -> {
            Diet selectedDiet = (Diet) dietTypeCombo.getSelectedItem();
            if (selectedDiet != null) {
                dietDescArea.setText(selectedDiet.getAciklama());
            }
        });

        // Hasta seçildiğinde belirtileri güncelleme
        dietPatientCombo.addActionListener(e -> {
            Patient selectedPatient = (Patient) dietPatientCombo.getSelectedItem();
            if (selectedPatient != null) {
                try {
                    // Hastanın belirtilerini veritabanından çek
                    List<Map<String, Object>> patientSymptoms = symptomService.getPatientSymptomDetails(selectedPatient.getPatient_id());

                    if (patientSymptoms.isEmpty()) {
                        symptomsInfoLabel.setText("Bu hasta için kayıtlı belirti bulunmuyor.");
                    } else {
                        StringBuilder symptomsList = new StringBuilder("<html>");
                        for (int i = 0; i < patientSymptoms.size(); i++) {
                            if (i > 0) symptomsList.append(", ");
                            symptomsList.append(patientSymptoms.get(i).get("symptomName"));
                            if (i == 4 && patientSymptoms.size() > 5) {
                                symptomsList.append(" ve ").append(patientSymptoms.size() - 5).append(" diğer...");
                                break;
                            }
                        }
                        symptomsList.append("</html>");
                        symptomsInfoLabel.setText(symptomsList.toString());
                    }
                } catch (Exception ex) {
                    symptomsInfoLabel.setText("Belirtiler yüklenirken hata oluştu.");
                    System.err.println("Hasta belirtileri yüklenirken hata: " + ex.getMessage());
                }
            } else {
                symptomsInfoLabel.setText("(Belirtiler veritabanından otomatik yüklenir)");
            }
        });

        // Öneri oluştur butonu
        gbc.gridx = 0;
        gbc.gridy = 5;
        JButton recommendButton = new JButton("Öneri Oluştur");
        recommendButton.addActionListener(e -> {
            // Hasta seçilmiş mi kontrol et
            if (dietPatientCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this,
                        "Lütfen önce bir hasta seçin.",
                        "Hasta Seçilmedi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Patient selectedPatient = (Patient) dietPatientCombo.getSelectedItem();

            // Kan şekeri değeri
            Integer bloodSugar = null;
            if (!bloodSugarField.getText().trim().isEmpty()) {
                try {
                    bloodSugar = Integer.parseInt(bloodSugarField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Geçerli bir kan şekeri değeri girin.",
                            "Geçersiz Değer",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            try {
                // Diyet önerisi al - null gönderince belirtiler veritabanından otomatik çekilecek
                Diet recommendedDiet = recommendationService.recommendDiet(
                        selectedPatient.getPatient_id(),
                        bloodSugar,
                        null); // Belirtiler veritabanından otomatik yüklenecek

                if (recommendedDiet != null) {
                    // Öneriyi combobox'ta seç
                    for (int i = 0; i < dietTypeCombo.getItemCount(); i++) {
                        Diet diet = (Diet) dietTypeCombo.getItemAt(i);
                        if (diet.getDiet_adi().equals(recommendedDiet.getDiet_adi())) {
                            dietTypeCombo.setSelectedIndex(i);
                            break;
                        }
                    }

                    JOptionPane.showMessageDialog(this,
                            "Önerilen diyet: " + recommendedDiet.getDiet_adi(),
                            "Diyet Önerisi",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Uygun bir diyet önerisi bulunamadı.",
                            "Öneri Yok",
                            JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Diyet önerisi oluşturulurken hata: " + ex.getMessage(),
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        formPanel.add(recommendButton, gbc);

        // Ekle butonu
        gbc.gridx = 1;
        JButton addDietButton = new JButton("Diyet Planı Oluştur");
        addDietButton.addActionListener(e -> {
            // Hasta ve diyet seçimi kontrolü
            if (dietPatientCombo.getSelectedItem() == null || dietTypeCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this,
                        "Lütfen hasta ve diyet tipini seçin.",
                        "Form Hatası",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Patient selectedPatient = (Patient) dietPatientCombo.getSelectedItem();
            Diet selectedDiet = (Diet) dietTypeCombo.getSelectedItem();

            try {
                // Diyet planını oluştur
                boolean success = dietDao.assignDietToPatient(
                        selectedPatient.getPatient_id(),
                        selectedDiet.getDiet_id(),
                        doctorId
                );

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Diyet planı başarıyla oluşturuldu.",
                            "İşlem Başarılı",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Form alanlarını temizle
                    dietPatientCombo.setSelectedIndex(0);
                    dietTypeCombo.setSelectedIndex(0);
                    dietDescArea.setText("");
                    bloodSugarField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Diyet planı oluşturulurken bir hata oluştu.",
                            "İşlem Hatası",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Veritabanı hatası: " + ex.getMessage(),
                        "İşlem Hatası",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        formPanel.add(addDietButton, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Diyet planları tablosu
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Mevcut Diyet Planları"));

        String[] columnNames = {
                "Hasta", "Diyet Tipi", "Başlangıç Tarihi", "Açıklama"
        };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable dietsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(dietsTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Egzersiz planı oluşturma panelini oluşturur
     */
    private JPanel createExercisePlanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Egzersiz Planı Oluştur"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Hasta seçimi
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Hasta:"), gbc);

        gbc.gridx = 1;
        exercisePatientCombo = new JComboBox<>();
        exercisePatientCombo = updatePatientCombo(exercisePatientCombo);
        formPanel.add(exercisePatientCombo, gbc);

        // Hasta seçildiğinde belirtileri yükle
        JLabel patientSymptomsLabel = new JLabel("Mevcut Belirtiler: ");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(patientSymptomsLabel, gbc);

        JLabel symptomsInfoLabel = new JLabel("(Belirtiler veritabanından otomatik yüklenir)");
        gbc.gridx = 1;
        formPanel.add(symptomsInfoLabel, gbc);

        // Kan şekeri değeri giriş alanı
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Kan Şekeri Değeri:"), gbc);

        gbc.gridx = 1;
        JTextField bloodSugarField = new JTextField();
        formPanel.add(bloodSugarField, gbc);

        // Egzersiz tipi seçimi
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Egzersiz Tipi:"), gbc);

        gbc.gridx = 1;
        JComboBox<Exercise> exerciseTypeCombo = new JComboBox<>();
        // Tüm egzersiz tiplerini yükle
        try {
            List<Exercise> allExercises = exerciseDao.findAll();
            for (Exercise exercise : allExercises) {
                exerciseTypeCombo.addItem(exercise);
            }
        } catch (Exception e) {
            System.err.println("Egzersiz tipleri yüklenirken hata: " + e.getMessage());
        }
        formPanel.add(exerciseTypeCombo, gbc);

        // Egzersiz açıklaması
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Açıklama/Yönergeler:"), gbc);

        gbc.gridx = 1;
        JTextArea exerciseDescArea = new JTextArea(3, 20);
        exerciseDescArea.setLineWrap(true);
        exerciseDescArea.setWrapStyleWord(true);
        JScrollPane exerciseDescScroll = new JScrollPane(exerciseDescArea);
        formPanel.add(exerciseDescScroll, gbc);

        // Egzersiz tipi seçildiğinde açıklamayı güncelle
        exerciseTypeCombo.addActionListener(e -> {
            Exercise selectedExercise = (Exercise) exerciseTypeCombo.getSelectedItem();
            if (selectedExercise != null) {
                exerciseDescArea.setText(selectedExercise.getAciklama());
            }
        });

        // Hasta seçildiğinde belirtileri güncelleme
        exercisePatientCombo.addActionListener(e -> {
            Patient selectedPatient = (Patient) exercisePatientCombo.getSelectedItem();
            if (selectedPatient != null) {
                try {
                    // Hastanın belirtilerini veritabanından çek
                    List<Map<String, Object>> patientSymptoms = symptomService.getPatientSymptomDetails(selectedPatient.getPatient_id());

                    if (patientSymptoms.isEmpty()) {
                        symptomsInfoLabel.setText("Bu hasta için kayıtlı belirti bulunmuyor.");
                    } else {
                        StringBuilder symptomsList = new StringBuilder("<html>");
                        for (int i = 0; i < patientSymptoms.size(); i++) {
                            if (i > 0) symptomsList.append(", ");
                            symptomsList.append(patientSymptoms.get(i).get("symptomName"));
                            if (i == 4 && patientSymptoms.size() > 5) {
                                symptomsList.append(" ve ").append(patientSymptoms.size() - 5).append(" diğer...");
                                break;
                            }
                        }
                        symptomsList.append("</html>");
                        symptomsInfoLabel.setText(symptomsList.toString());
                    }
                } catch (Exception ex) {
                    symptomsInfoLabel.setText("Belirtiler yüklenirken hata oluştu.");
                    System.err.println("Hasta belirtileri yüklenirken hata: " + ex.getMessage());
                }
            } else {
                symptomsInfoLabel.setText("(Belirtiler veritabanından otomatik yüklenir)");
            }
        });

        // Öneri oluştur butonu
        gbc.gridx = 0;
        gbc.gridy = 5;
        JButton recommendButton = new JButton("Öneri Oluştur");
        recommendButton.addActionListener(e -> {
            // Hasta seçilmiş mi kontrol et
            if (exercisePatientCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this,
                        "Lütfen önce bir hasta seçin.",
                        "Hasta Seçilmedi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Patient selectedPatient = (Patient) exercisePatientCombo.getSelectedItem();

            // Kan şekeri değeri
            Integer bloodSugar = null;
            if (!bloodSugarField.getText().trim().isEmpty()) {
                try {
                    bloodSugar = Integer.parseInt(bloodSugarField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Geçerli bir kan şekeri değeri girin.",
                            "Geçersiz Değer",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            try {
                // Egzersiz önerisi al - null gönderince belirtiler veritabanından otomatik çekilecek
                Exercise recommendedExercise = recommendationService.recommendExercise(
                        selectedPatient.getPatient_id(),
                        bloodSugar,
                        null); // Belirtiler veritabanından otomatik yüklenecek

                if (recommendedExercise != null) {
                    // Öneriyi combobox'ta seç
                    for (int i = 0; i < exerciseTypeCombo.getItemCount(); i++) {
                        Exercise exercise = (Exercise) exerciseTypeCombo.getItemAt(i);
                        if (exercise.getExercise_adi().equals(recommendedExercise.getExercise_adi())) {
                            exerciseTypeCombo.setSelectedIndex(i);
                            break;
                        }
                    }

                    JOptionPane.showMessageDialog(this,
                            "Önerilen egzersiz: " + recommendedExercise.getExercise_adi(),
                            "Egzersiz Önerisi",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Uygun bir egzersiz önerisi bulunamadı.",
                            "Öneri Yok",
                            JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Egzersiz önerisi oluşturulurken hata: " + ex.getMessage(),
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        formPanel.add(recommendButton, gbc);

        // Ekle butonu
        gbc.gridx = 1;
        JButton addExerciseButton = new JButton("Egzersiz Planı Oluştur");
        addExerciseButton.addActionListener(e -> {
            // Hasta ve egzersiz seçimi kontrolü
            if (exercisePatientCombo.getSelectedItem() == null || exerciseTypeCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this,
                        "Lütfen hasta ve egzersiz tipini seçin.",
                        "Form Hatası",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Patient selectedPatient = (Patient) exercisePatientCombo.getSelectedItem();
            Exercise selectedExercise = (Exercise) exerciseTypeCombo.getSelectedItem();

            try {
                // Egzersiz planını oluştur
                boolean success = exerciseDao.assignExerciseToPatient(
                        selectedPatient.getPatient_id(),
                        selectedExercise.getExercise_id(),
                        doctorId
                );

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Egzersiz planı başarıyla oluşturuldu.",
                            "İşlem Başarılı",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Form alanlarını temizle
                    exercisePatientCombo.setSelectedIndex(0);
                    exerciseTypeCombo.setSelectedIndex(0);
                    exerciseDescArea.setText("");
                    bloodSugarField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Egzersiz planı oluşturulurken bir hata oluştu.",
                            "İşlem Hatası",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Veritabanı hatası: " + ex.getMessage(),
                        "İşlem Hatası",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        formPanel.add(addExerciseButton, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Egzersiz planları tablosu
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Mevcut Egzersiz Planları"));

        String[] columnNames = {
                "Hasta", "Egzersiz Tipi", "Başlangıç Tarihi", "Açıklama"
        };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable exercisesTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(exercisesTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JTextArea logArea;

    private void addDebugPanel() {
        JPanel debugPanel = new JPanel(new BorderLayout());
        debugPanel.setBorder(BorderFactory.createTitledBorder("Debug Bilgileri"));

        logArea = new JTextArea(5, 50);
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        debugPanel.add(scrollPane, BorderLayout.CENTER);

        JButton clearButton = new JButton("Temizle");
        clearButton.addActionListener(e -> logArea.setText(""));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(clearButton);
        debugPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(debugPanel, BorderLayout.SOUTH);
    }

    // Log ekleme metodu
    private void log(String message) {
        if (logArea != null) {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }

// RecommendationService sınıfında bu metodu çağırarak çalışma detaylarını loglayabiliriz

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

        // Diyet ve egzersiz takip sekmesi - Yüzde oranları görselleştirme
        JPanel dietExercisePanel = createDietExercisePanel();
        patientDetailTabs.addTab("Diyet ve Egzersiz Takibi", dietExercisePanel);

        // Belirtiler sekmesi
        JPanel symptomsPanel = createSymptomsPanel();
        patientDetailTabs.addTab("Belirtiler", symptomsPanel);

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

    // Diyet ve Egzersiz Paneli - Yüzde uygulanma görselleştirmesi
    private JPanel createDietExercisePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Diyet uyum oranı paneli
        JPanel dietPanel = new JPanel(new BorderLayout(10, 10));
        dietPanel.setBorder(BorderFactory.createTitledBorder("Diyet Takibi ve Uyum Oranı"));

        // Progress bar ile görselleştirme
        JPanel dietBarPanel = new JPanel(new BorderLayout());
        JProgressBar dietProgressBar = new JProgressBar(0, 100);
        dietProgressBar.setStringPainted(true);
        dietProgressBar.setForeground(new Color(0, 153, 51)); // Yeşil tonu
        dietBarPanel.add(dietProgressBar, BorderLayout.CENTER);
        dietBarPanel.add(new JLabel("Diyet Uyumu: Veri yok"), BorderLayout.NORTH);
        dietPanel.add(dietBarPanel, BorderLayout.NORTH);

        // Diyet listesi
        String[] dietColumns = {"Tarih", "Diyet Türü", "Açıklama", "Uygulandı mı"};
        DefaultTableModel dietModel = new DefaultTableModel(dietColumns, 0);
        JTable dietTable = new JTable(dietModel);
        JScrollPane dietScrollPane = new JScrollPane(dietTable);
        dietPanel.add(dietScrollPane, BorderLayout.CENTER);

        // Egzersiz uyum oranı paneli
        JPanel exercisePanel = new JPanel(new BorderLayout(10, 10));
        exercisePanel.setBorder(BorderFactory.createTitledBorder("Egzersiz Takibi ve Uyum Oranı"));

        // Progress bar ile görselleştirme
        JPanel exerciseBarPanel = new JPanel(new BorderLayout());
        JProgressBar exerciseProgressBar = new JProgressBar(0, 100);
        exerciseProgressBar.setStringPainted(true);
        exerciseProgressBar.setForeground(new Color(51, 102, 255)); // Mavi tonu
        exerciseBarPanel.add(exerciseProgressBar, BorderLayout.CENTER);
        exerciseBarPanel.add(new JLabel("Egzersiz Uyumu: Veri yok"), BorderLayout.NORTH);
        exercisePanel.add(exerciseBarPanel, BorderLayout.NORTH);

        // Egzersiz listesi
        String[] exerciseColumns = {"Tarih", "Egzersiz Türü", "Süre", "Yapıldı mı"};
        DefaultTableModel exerciseModel = new DefaultTableModel(exerciseColumns, 0);
        JTable exerciseTable = new JTable(exerciseModel);
        JScrollPane exerciseScrollPane = new JScrollPane(exerciseTable);
        exercisePanel.add(exerciseScrollPane, BorderLayout.CENTER);

        panel.add(dietPanel);
        panel.add(exercisePanel);

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

    /**
     * Belirtiler paneli
     */
    private JPanel createSymptomsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Belirti ekleme formu
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Belirti Ekle/Düzenle"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Belirti tipi - veritabanından çekilen değerlerle doldurulan ComboBox
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Belirti Tipi:"), gbc);

        gbc.gridx = 1;
        JComboBox<SymptomItem> symptomTypeCombo = new JComboBox<>();

        // Belirtileri veritabanından çek ve ComboBox'a ekle
        try {
            List<Symptom> allSymptoms = symptomService.getAllSymptoms();
            for (Symptom symptom : allSymptoms) {
                symptomTypeCombo.addItem(new SymptomItem(symptom));
            }

            // "Diğer" seçeneğini en sona ekle
            Symptom otherSymptom = new Symptom();
            otherSymptom.setSymptom_id(-1); // Özel ID
            otherSymptom.setSymptom_adi("Diğer");
            otherSymptom.setAciklama("Özel belirti eklemek için");
            symptomTypeCombo.addItem(new SymptomItem(otherSymptom));

        } catch (Exception e) {
            System.err.println("Belirtiler yüklenirken hata: " + e.getMessage());
            e.printStackTrace();
        }

        formPanel.add(symptomTypeCombo, gbc);

        // Özel belirti alanı - sadece "Diğer" seçildiğinde görünür
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel customSymptomLabel = new JLabel("Özel Belirti Adı:");
        customSymptomLabel.setVisible(false);
        formPanel.add(customSymptomLabel, gbc);

        gbc.gridx = 1;
        JTextField customSymptomField = new JTextField();
        customSymptomField.setVisible(false);
        formPanel.add(customSymptomField, gbc);

        // "Diğer" seçildiğinde özel belirti alanını göster/gizle
        symptomTypeCombo.addActionListener(e -> {
            SymptomItem selectedItem = (SymptomItem) symptomTypeCombo.getSelectedItem();
            boolean isOther = selectedItem != null && selectedItem.getSymptom().getSymptom_id() == -1;
            customSymptomLabel.setVisible(isOther);
            customSymptomField.setVisible(isOther);
        });

        // Başlangıç tarihi
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Başlangıç Tarihi:"), gbc);

        gbc.gridx = 1;
        // Güncel tarihi varsayılan değer olarak ayarla, DateTimeUtil kullanarak
        SpinnerDateModel dateModel = new SpinnerDateModel();
        // Güncel tarih için DateTimeUtil kullan
        Date currentDate = Date.from(DateTimeUtil.getCurrentDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        dateModel.setValue(currentDate);

        JSpinner startDateSpinner = new JSpinner(dateModel);
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "dd.MM.yyyy"));
        ((JSpinner.DefaultEditor)startDateSpinner.getEditor()).getTextField().setEditable(false);
        formPanel.add(startDateSpinner, gbc);

        // Notlar/Açıklama
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Açıklama:"), gbc);

        gbc.gridx = 1;
        JTextArea notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        formPanel.add(notesScroll, gbc);

        // ID alanı (gizli)
        final JTextField patientSymptomIdField = new JTextField();
        patientSymptomIdField.setVisible(false);
        formPanel.add(patientSymptomIdField, gbc);

        // Butonlar
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton addButton = new JButton("Belirti Ekle");
        addButton.addActionListener(e -> {
            // Hasta kontrolü
            if (selectedPatient == null) {
                JOptionPane.showMessageDialog(panel,
                        "Lütfen önce bir hasta seçin.",
                        "Hasta Seçilmedi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // Form verilerini al
                SymptomItem selectedItem = (SymptomItem) symptomTypeCombo.getSelectedItem();
                if (selectedItem == null) {
                    JOptionPane.showMessageDialog(panel,
                            "Lütfen bir belirti tipi seçin.",
                            "Belirti Tipi Seçilmedi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Date startDate = (Date) startDateSpinner.getValue();
                String notes = notesArea.getText();

                // Tarihi LocalDate'e dönüştür
                LocalDate reportDate = startDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                boolean success = false;

                // "Diğer" seçeneği için özel işlem
                if (selectedItem.getSymptom().getSymptom_id() == -1) {
                    String customSymptomName = customSymptomField.getText().trim();
                    if (customSymptomName.isEmpty()) {
                        JOptionPane.showMessageDialog(panel,
                                "Özel belirti adı girmelisiniz.",
                                "Eksik Bilgi",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Özel belirtiyi ekle
                    success = symptomService.addSymptomToPatientByName(
                            selectedPatient.getPatient_id(),
                            customSymptomName,
                            notes,
                            reportDate
                    );
                } else {
                    // Seçilen belirtiyi ekle
                    PatientSymptom patientSymptom = new PatientSymptom();
                    patientSymptom.setPatient_id(selectedPatient.getPatient_id());
                    patientSymptom.setSymptom_id(selectedItem.getSymptom().getSymptom_id());
                    patientSymptom.setBelirtilme_tarihi(reportDate);

                    try {
                        success = symptomService.savePatientSymptom(patientSymptom);
                    } catch (SQLException ex) {
                        System.err.println("Belirti eklenirken hata: " + ex.getMessage());
                        throw ex;
                    }
                }

                if (success) {
                    JOptionPane.showMessageDialog(panel,
                            "Belirti başarıyla eklendi.",
                            "İşlem Başarılı",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Formu temizle
                    symptomTypeCombo.setSelectedIndex(0);
                    startDateSpinner.setValue(Date.from(DateTimeUtil.getCurrentDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    notesArea.setText("");
                    patientSymptomIdField.setText("");
                    customSymptomField.setText("");
                    customSymptomField.setVisible(false);
                    customSymptomLabel.setVisible(false);

                    // Belirti tablosunu güncelle
                    refreshSymptomsList((JTable) ((JScrollPane) panel.getComponent(1)).getViewport().getView());
                } else {
                    JOptionPane.showMessageDialog(panel,
                            "Belirti eklenirken bir hata oluştu.",
                            "İşlem Başarısız",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        "İşlem sırasında bir hata oluştu: " + ex.getMessage(),
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        buttonPanel.add(addButton);

        JButton updateButton = new JButton("Güncelle");
        updateButton.addActionListener(e -> {
            // Hasta kontrolü
            if (selectedPatient == null) {
                JOptionPane.showMessageDialog(panel,
                        "Lütfen önce bir hasta seçin.",
                        "Hasta Seçilmedi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Seçilen belirti kontrolü
            String idText = patientSymptomIdField.getText();
            if (idText == null || idText.isEmpty()) {
                JOptionPane.showMessageDialog(panel,
                        "Lütfen önce güncellenecek bir belirti seçin.",
                        "Belirti Seçilmedi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // Form verilerini al
                Integer patientSymptomId = Integer.parseInt(idText);
                SymptomItem selectedItem = (SymptomItem) symptomTypeCombo.getSelectedItem();

                if (selectedItem == null) {
                    JOptionPane.showMessageDialog(panel,
                            "Lütfen bir belirti tipi seçin.",
                            "Belirti Tipi Seçilmedi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Date startDate = (Date) startDateSpinner.getValue();
                String notes = notesArea.getText();

                // Tarihi LocalDate'e dönüştür
                LocalDate reportDate = startDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                boolean success = false;

                // "Diğer" seçeneği için özel işlem
                if (selectedItem.getSymptom().getSymptom_id() == -1) {
                    String customSymptomName = customSymptomField.getText().trim();
                    if (customSymptomName.isEmpty()) {
                        JOptionPane.showMessageDialog(panel,
                                "Özel belirti adı girmelisiniz.",
                                "Eksik Bilgi",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Önce özel belirtiyi oluştur/al
                    Symptom symptom = symptomService.createOrGetSymptom(customSymptomName, notes);
                    success = symptomService.updatePatientSymptom(
                            patientSymptomId,
                            symptom.getSymptom_id(),
                            reportDate
                    );
                } else {
                    // Normal belirti güncellemesi
                    success = symptomService.updatePatientSymptom(
                            patientSymptomId,
                            selectedItem.getSymptom().getSymptom_id(),
                            reportDate
                    );
                }

                if (success) {
                    JOptionPane.showMessageDialog(panel,
                            "Belirti başarıyla güncellendi.",
                            "İşlem Başarılı",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Formu temizle
                    symptomTypeCombo.setSelectedIndex(0);
                    startDateSpinner.setValue(Date.from(DateTimeUtil.getCurrentDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    notesArea.setText("");
                    patientSymptomIdField.setText("");
                    customSymptomField.setText("");
                    customSymptomField.setVisible(false);
                    customSymptomLabel.setVisible(false);

                    // Belirti tablosunu güncelle
                    refreshSymptomsList((JTable) ((JScrollPane) panel.getComponent(1)).getViewport().getView());
                } else {
                    JOptionPane.showMessageDialog(panel,
                            "Belirti güncellenirken bir hata oluştu.",
                            "İşlem Başarısız",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Geçersiz belirti ID'si.",
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        "İşlem sırasında bir hata oluştu: " + ex.getMessage(),
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        buttonPanel.add(updateButton);

        JButton deleteButton = new JButton("Sil");
        deleteButton.addActionListener(e -> {
            // Seçilen belirti kontrolü
            String idText = patientSymptomIdField.getText();
            if (idText == null || idText.isEmpty()) {
                JOptionPane.showMessageDialog(panel,
                        "Lütfen önce silinecek bir belirti seçin.",
                        "Belirti Seçilmedi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Silme onayı
            int confirm = JOptionPane.showConfirmDialog(panel,
                    "Seçili belirtiyi silmek istediğinizden emin misiniz?",
                    "Silme Onayı",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                Integer patientSymptomId = Integer.parseInt(idText);
                boolean success = symptomService.deletePatientSymptom(patientSymptomId);

                if (success) {
                    JOptionPane.showMessageDialog(panel,
                            "Belirti başarıyla silindi.",
                            "İşlem Başarılı",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Formu temizle
                    symptomTypeCombo.setSelectedIndex(0);
                    startDateSpinner.setValue(Date.from(DateTimeUtil.getCurrentDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    notesArea.setText("");
                    patientSymptomIdField.setText("");
                    customSymptomField.setText("");
                    customSymptomField.setVisible(false);
                    customSymptomLabel.setVisible(false);

                    // Belirti tablosunu güncelle
                    refreshSymptomsList((JTable) ((JScrollPane) panel.getComponent(1)).getViewport().getView());
                } else {
                    JOptionPane.showMessageDialog(panel,
                            "Belirti silinirken bir hata oluştu.",
                            "İşlem Başarısız",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                        "Geçersiz belirti ID'si.",
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(deleteButton);

        JButton clearButton = new JButton("Temizle");
        clearButton.addActionListener(e -> {
            // Formu temizle
            symptomTypeCombo.setSelectedIndex(0);
            startDateSpinner.setValue(Date.from(DateTimeUtil.getCurrentDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            notesArea.setText("");
            patientSymptomIdField.setText("");
            customSymptomField.setText("");
            customSymptomField.setVisible(false);
            customSymptomLabel.setVisible(false);
        });
        buttonPanel.add(clearButton);

        formPanel.add(buttonPanel, gbc);

        // Belirti grafik alanı
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("Belirtilerin Zamana Göre Dağılımı"));
        chartPanel.setPreferredSize(new Dimension(600, 200));

        // Bilgilendirme etiketi
        JLabel chartInfoLabel = new JLabel("<html><center>Bu alanda hastanın belirtilerinin zaman içindeki dağılımını<br>" +
                "gösteren grafik yer alacaktır.<br>" +
                "Veri olmadığı için grafik şu an boş görünmektedir.</center></html>", JLabel.CENTER);
        chartInfoLabel.setForeground(Color.GRAY);
        chartPanel.add(chartInfoLabel, BorderLayout.CENTER);

        // Formu ve grafiği üst panele ekle
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.NORTH);
        topPanel.add(chartPanel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        // Belirtiler tablosu - Şiddet sütunu kaldırıldı
        String[] columns = {"ID", "Tarih", "Belirti", "Açıklama"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabloyu salt okunur yap
            }
        };
        JTable symptomsTable = new JTable(model);
        symptomsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && symptomsTable.getSelectedRow() != -1) {
                // Seçilen belirtiyi form alanlarına yükle
                int selectedRow = symptomsTable.getSelectedRow();

                // Görünmeyen ID sütunundaki değeri al
                String id = symptomsTable.getValueAt(selectedRow, 0).toString();
                patientSymptomIdField.setText(id);

                // Diğer değerleri al
                String tarih = symptomsTable.getValueAt(selectedRow, 1).toString();
                String belirti = symptomsTable.getValueAt(selectedRow, 2).toString();
                String aciklama = symptomsTable.getValueAt(selectedRow, 3).toString();

                // Belirti tipini ComboBox'ta bul
                boolean found = false;
                for (int i = 0; i < symptomTypeCombo.getItemCount(); i++) {
                    SymptomItem item = symptomTypeCombo.getItemAt(i);
                    if (item.toString().equals(belirti)) {
                        symptomTypeCombo.setSelectedIndex(i);
                        found = true;
                        break;
                    }
                }

                // Eğer ComboBox'ta bulunamadıysa "Diğer" seçeneğini seç ve özel alan olarak ayarla
                if (!found && symptomTypeCombo.getItemCount() > 0) {
                    for (int i = 0; i < symptomTypeCombo.getItemCount(); i++) {
                        SymptomItem item = symptomTypeCombo.getItemAt(i);
                        if (item.getSymptom().getSymptom_id() == -1) { // "Diğer" seçeneği
                            symptomTypeCombo.setSelectedIndex(i);
                            customSymptomField.setText(belirti);
                            break;
                        }
                    }
                }

                // Tarih ve açıklama alanlarını ayarla
                try {
                    LocalDate date = DateTimeUtil.parseDate(tarih);
                    if (date != null) {
                        Date javaDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        startDateSpinner.setValue(javaDate);
                    }
                } catch (Exception ex) {
                    System.err.println("Tarih dönüştürme hatası: " + ex.getMessage());
                    ex.printStackTrace();
                }

                notesArea.setText(aciklama);
            }
        });

        // İlk sütunu (ID) gizle
        symptomsTable.getColumnModel().getColumn(0).setMinWidth(0);
        symptomsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        symptomsTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(symptomsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Belirtilen Semptomlar"));

        panel.add(scrollPane, BorderLayout.CENTER);

        // İşlem sonuç paneli
        JPanel statusPanel = new JPanel(new BorderLayout());
        JLabel statusLabel = new JLabel("Hasta belirtileri doktor tarafından buradan yönetilebilir.");
        statusPanel.add(statusLabel, BorderLayout.WEST);

        JButton refreshButton = new JButton("Belirtileri Yenile");
        refreshButton.addActionListener(e -> refreshSymptomsList(symptomsTable));
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.add(refreshButton);
        statusPanel.add(refreshPanel, BorderLayout.EAST);

        panel.add(statusPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Belirti nesnelerini ComboBox'da göstermek için yardımcı sınıf
     */
    private class SymptomItem {
        private Symptom symptom;

        public SymptomItem(Symptom symptom) {
            this.symptom = symptom;
        }

        public Symptom getSymptom() {
            return symptom;
        }

        @Override
        public String toString() {
            return symptom.getSymptom_adi();
        }
    }

    /**
     * Belirtiler tablosunu yeniler - DateTimeUtil kullanarak
     * Şiddet sütunu kaldırıldı
     */
    private void refreshSymptomsList(JTable symptomsTable) {
        if (selectedPatient == null) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen önce bir hasta seçin.",
                    "Hasta Seçilmedi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Hastanın belirti detaylarını al
            List<Map<String, Object>> symptomDetails =
                    symptomService.getPatientSymptomDetails(selectedPatient.getPatient_id());

            DefaultTableModel model = (DefaultTableModel) symptomsTable.getModel();
            model.setRowCount(0); // Tabloyu temizle

            for (Map<String, Object> detail : symptomDetails) {
                Object[] row = new Object[4]; // Şiddet kaldırıldı, şimdi 4 sütun var
                row[0] = detail.get("patientSymptomId");

                // Tarihi formatla - DateTimeUtil kullanarak
                LocalDate reportDate = (LocalDate) detail.get("reportDate");
                row[1] = DateTimeUtil.formatDate(reportDate); // DateTimeUtil kullanımı

                row[2] = detail.get("symptomName");
                row[3] = detail.get("description");

                model.addRow(row);
            }

            if (symptomDetails.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Bu hastaya ait kayıtlı belirti bulunamadı.",
                        "Bilgi",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Belirtiler yüklenirken bir hata oluştu: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
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

        // Doğum Tarihi
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Doğum Tarihi:"), gbc);

        gbc.gridx = 1;
        // Bugünün tarihinden 30 yıl öncesi varsayılan değer olarak
        Date defaultDate = new Date();
        defaultDate.setYear(defaultDate.getYear() - 30);

        SpinnerDateModel dateModel = new SpinnerDateModel(defaultDate, null, new Date(), java.util.Calendar.DAY_OF_MONTH);
        dogumTarihiSpinner = new JSpinner(dateModel);
        dogumTarihiSpinner.setEditor(new JSpinner.DateEditor(dogumTarihiSpinner, "dd.MM.yyyy"));
        panel.add(dogumTarihiSpinner, gbc);

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
        addButton.addActionListener(e -> addPatient());
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

    private void clearAddPatientFields() {
        tcKimlikField.setText("");
        adField.setText("");
        soyadField.setText("");
        emailField.setText("");
        passwordField.setText("");
        cinsiyetCombo.setSelectedIndex(0);
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
}