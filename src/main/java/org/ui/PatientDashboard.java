package org.ui;
import org.dao.*;
import org.jfree.chart.*;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.model.*;
import org.service.*;
import org.util.DateTimeUtil;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.jfree.chart.plot.XYPlot;
import java.awt.BasicStroke;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.awt.geom.Ellipse2D;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import java.util.List;

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

    // Ana sayfadaki bileşenler
    private JLabel avgValueLabel;
    private JProgressBar dietProgress;
    private JProgressBar exerciseProgress;
    private DefaultTableModel dailyValuesModel;

    // Kan şekeri ölçüm tablosu
    private DefaultTableModel measurementsTableModel;
    private JTextField startDateField;
    private JTextField endDateField;

    // Tarih formatı
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // Ölçüm zamanı için eşleştirme tablosu
    private Map<String, String> periodMap;

    // Sınıf düzeyinde grafik paneli için değişken
    private JPanel chartPanel;

    public PatientDashboard(User user, AuthenticationService authService, PatientService patientService, MainFrame parent) {
        this.currentUser = user;
        this.authService = authService;
        this.patientService = patientService;
        this.patient = patientService.getPatientByUserId(this.currentUser.getUser_id());
        this.measurementService = new MeasurementService();
        this.dietService = new DietService();
        this.exerciseService = new ExerciseService();
        this.parent = parent;

        // Ölçüm zamanı eşleştirme tablosunu oluştur
        initPeriodMap();

        setLayout(new BorderLayout());
        initComponents();

        try {
            loadData(); // Verileri yükle
        } catch (Exception e) {
            System.err.println("Veri yükleme hatası: " + e.getMessage());
        }
    }

    /**
     * Ölçüm zamanı eşleştirme haritasını oluşturur
     */
    private void initPeriodMap() {
        periodMap = new HashMap<>();
        periodMap.put("Sabah (07:00-08:00)", "sabah");
        periodMap.put("Öğle (12:00-13:00)", "ogle");
        periodMap.put("İkindi (15:00-16:00)", "ikindi");
        periodMap.put("Akşam (18:00-19:00)", "aksam");
        periodMap.put("Gece (22:00-23:00)", "gece");

        // Tersine eşleştirmeler - görüntüleme için
        periodMap.put("sabah", "Sabah");
        periodMap.put("ogle", "Öğle");
        periodMap.put("ikindi", "İkindi");
        periodMap.put("aksam", "Akşam");
        periodMap.put("gece", "Gece");
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
                    // Resmi boyutlandır
                    Image scaledImage = originalImage.getScaledInstance(75, 75, Image.SCALE_SMOOTH);
                    profileImageLabel.setIcon(new ImageIcon(scaledImage));
                } else {
                    createDefaultProfileImage(profileImageLabel);
                }
            } catch (IOException e) {
                System.err.println("Profil resmi yüklenemedi: " + e.getMessage());
                createDefaultProfileImage(profileImageLabel);
            }
        } else {
            createDefaultProfileImage(profileImageLabel);
        }

        return profileImageLabel;
    }

    /**
     * Varsayılan profil resmi oluşturur
     */
    private void createDefaultProfileImage(JLabel imageLabel) {
        BufferedImage img = new BufferedImage(75, 75, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        g2.setColor(new Color(70, 130, 180)); // Steel blue
        g2.fillRect(0, 0, 75, 75);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 32));

        // Baş harfler
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
        avgValueLabel = new JLabel("Veri yok");
        avgValueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        avgPanel.add(avgValueLabel, BorderLayout.CENTER);
        infoPanel.add(avgPanel);

        // Diyet uyum oranı
        JPanel dietPanel = new JPanel(new BorderLayout());
        dietPanel.add(new JLabel("Diyet Uyum Oranı:"), BorderLayout.NORTH);
        dietProgress = new JProgressBar(0, 100);
        dietProgress.setValue(0);
        dietProgress.setStringPainted(true);
        dietProgress.setString("Veri yok");
        dietPanel.add(dietProgress, BorderLayout.CENTER);
        infoPanel.add(dietPanel);

        // Günlük kan şekeri değerleri
        JPanel dailyMeasurementsPanel = new JPanel(new BorderLayout());
        dailyMeasurementsPanel.add(new JLabel("Günlük Kan Şekeri Değerleri:"), BorderLayout.NORTH);

        // Günlük değerleri gösteren mini tablo
        dailyValuesModel = new DefaultTableModel(
                new Object[][]{{"Sabah", "Öğle", "İkindi", "Akşam", "Gece"},
                        {"--", "--", "--", "--", "--"}},
                new String[]{"", "", "", "", ""}
        );
        JTable miniTable = new JTable(dailyValuesModel);
        miniTable.setEnabled(false);
        miniTable.setRowHeight(25);
        JScrollPane miniScrollPane = new JScrollPane(miniTable);
        miniScrollPane.setPreferredSize(new Dimension(200, 60));

        dailyMeasurementsPanel.add(miniScrollPane, BorderLayout.CENTER);
        infoPanel.add(dailyMeasurementsPanel);

        // Egzersiz uyum oranı
        JPanel exercisePanel = new JPanel(new BorderLayout());
        exercisePanel.add(new JLabel("Egzersiz Uyum Oranı:"), BorderLayout.NORTH);
        exerciseProgress = new JProgressBar(0, 100);
        exerciseProgress.setValue(0);
        exerciseProgress.setStringPainted(true);
        exerciseProgress.setString("Veri yok");
        exercisePanel.add(exerciseProgress, BorderLayout.CENTER);
        infoPanel.add(exercisePanel);

        panel.add(infoPanel, BorderLayout.NORTH);

        // Orta kısım - Grafik alanı
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("Kan Şekeri Değerleri"));

        // Grafiğin boyutunu ayarlama
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int chartHeight = (int)(screenSize.height * 0.4); // Ekran yüksekliğinin %40'ı kadar
        chartPanel.setPreferredSize(new Dimension(600, chartHeight));

        // Grafiği güncelle
        updateBloodSugarChart();

        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Kan şekeri grafiğini günceller
     */
    private void updateBloodSugarChart() {
        // chartPanel'in içeriğini temizle
        chartPanel.removeAll();

        try {
            // Bugünün tarihini al
            LocalDate today = LocalDate.now();

            // MeasurementDao kullanarak bugünün verilerini al
            MeasurementDao measurementDao = new MeasurementDao();
            List<BloodSugarMeasurement> todaysMeasurements = measurementDao.findByDateRange(
                    patient.getPatient_id(),
                    today,
                    today
            );

            if (todaysMeasurements.isEmpty()) {
                JLabel noDataLabel = new JLabel("Bugün için kan şekeri ölçümü kaydedilmemiş", JLabel.CENTER);
                noDataLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                noDataLabel.setForeground(Color.GRAY);
                chartPanel.add(noDataLabel, BorderLayout.CENTER);
            } else {
                // Ölçümleri saate göre sırala
                Collections.sort(todaysMeasurements, Comparator.comparing(m -> m.getOlcum_tarihi()));

                // Tek bir seri kullan, tüm noktaları içinde topla
                XYSeries series = new XYSeries("Kan Şekeri");

                // Periyotları sakla (hangi XY değeri hangi periyoda ait)
                final Map<Point2D, String> periodMap = new HashMap<>();

                // Tüm ölçümleri tek seride topla
                for (BloodSugarMeasurement measurement : todaysMeasurements) {
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
                        "Bugün (" + today.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ")",
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

                // Özel Renderer - Periyoda göre farklı renk ama hepsi tek seride
                XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer() {
                    @Override
                    public Paint getItemPaint(int series, int item) {
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
                        return super.getItemPaint(series, item);
                    }
                };

                // Çizgi ve nokta stilini ayarla
                renderer.setDefaultShapesVisible(true);
                renderer.setDefaultShape(new Ellipse2D.Double(-5, -5, 10, 10)); // Büyük noktalar
                renderer.setDefaultLinesVisible(true); // Çizgileri göster
                renderer.setDrawOutlines(true);
                renderer.setUseFillPaint(true);
                renderer.setDefaultFillPaint(Color.WHITE);
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

                // Grafik boyutunu ayarla - ekranın önemli bir kısmını kaplasın
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int chartHeight = (int)(screenSize.height * 0.4);
                jfreeChartPanel.setPreferredSize(new Dimension(-1, chartHeight));

                chartPanel.add(jfreeChartPanel, BorderLayout.CENTER);

                // Ortalamayı güncelle
                try {
                    double avgValue = measurementDao.getDailyAverage(patient.getPatient_id(), today);
                    if (avgValue > 0) {
                        avgValueLabel.setText(String.format("%.1f mg/dL", avgValue));

                        // Değere göre renklendirme
                        if (avgValue < 70) {
                            avgValueLabel.setForeground(new Color(220, 0, 0)); // Kırmızı - düşük
                        } else if (avgValue > 110) {
                            avgValueLabel.setForeground(new Color(220, 0, 0)); // Kırmızı - yüksek
                        } else {
                            avgValueLabel.setForeground(new Color(0, 150, 0)); // Yeşil - normal
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Ortalama hesaplanırken hata: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Grafik yüklenirken hata: " + e.getMessage(), JLabel.CENTER);
            errorLabel.setFont(new Font("Arial", Font.BOLD, 12));
            errorLabel.setForeground(Color.RED);
            chartPanel.add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace();
        }

        // Panel güncellendi, yeniden çiz
        chartPanel.revalidate();
        chartPanel.repaint();
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
        startDateField = new JTextField(10);
        startDateField.setText(LocalDate.now().minusWeeks(1).format(dateFormatter)); // Son 1 hafta
        filterPanel.add(startDateField);

        filterPanel.add(new JLabel("Bitiş Tarihi:"));
        endDateField = new JTextField(10);
        endDateField.setText(LocalDate.now().format(dateFormatter)); // Bugün
        filterPanel.add(endDateField);

        JButton filterButton = new JButton("Filtrele");
        filterButton.addActionListener(e -> filterMeasurements());
        filterPanel.add(filterButton);

        centerPanel.add(filterPanel, BorderLayout.NORTH);

        // Ölçüm listesi tablosu
        String[] columnNames = {"Tarih", "Saat", "Değer (mg/dL)", "Periyot", "İnsülin Dozu", "Durum"};
        measurementsTableModel = new DefaultTableModel(columnNames, 0);
        JTable measurementsTable = new JTable(measurementsTableModel);
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

            // ComboBox'tan seçilen değeri direkt VT uyumlu hale getir
            String selectedPeriod = (String) periodCombo.getSelectedItem();
            String dbPeriod = periodMap.get(selectedPeriod);

            // Ölçüm nesnesi oluştur
            BloodSugarMeasurement measurement = new BloodSugarMeasurement();
            measurement.setPatient(patient);
            measurement.setPatient_id(patient.getPatient_id());
            measurement.setOlcum_degeri(value);
            measurement.setOlcum_zamani(dbPeriod); // VT uyumlu format
            measurement.setOlcum_tarihi(DateTimeUtil.getCurrentDateTime());
            measurement.setInsulin_miktari(0.0);
            measurement.setIs_valid_time(true);

            // Service aracılığıyla Dao kullanarak tabloya ekler
            boolean success = measurementService.addMeasurement(measurement);

            try {
                MeasurementDao measurementDao = new MeasurementDao();
                InsulinReferenceDao insulinReferenceDao = new InsulinReferenceDao();

                measurementDao.updateFlag(measurement.getMeasurement_id());
                int averageValue = (int) measurementDao.getDailyAverage(measurement.getPatient_id(), measurement.getOlcum_tarihi().toLocalDate());
                InsulinReference insulinReference = insulinReferenceDao.findByBloodSugarValue(averageValue);
                measurement.setInsulin_miktari(insulinReference.getInsulin_dose());
                measurementDao.updateInsulinAmount(measurement.getMeasurement_id(), insulinReference.getInsulin_dose());
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage(), "Veritabanı Hatası", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return;
            }

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Kan şekeri ölçümü başarıyla kaydedildi.",
                        "İşlem Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);

                // Form alanlarını temizle
                measurementField.setText("");
                periodCombo.setSelectedIndex(0);

                // Verileri yenile
                loadData();

                // Grafiği güncelle
                updateBloodSugarChart();
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
     * Ölçümleri tarih aralığına göre filtreler
     */
    private void filterMeasurements() {
        try {
            LocalDate startDate = LocalDate.parse(startDateField.getText(), dateFormatter);
            LocalDate endDate = LocalDate.parse(endDateField.getText(), dateFormatter);

            if (endDate.isBefore(startDate)) {
                JOptionPane.showMessageDialog(this,
                        "Bitiş tarihi başlangıç tarihinden önce olamaz.",
                        "Tarih Hatası",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Filtreleme işlemi
            loadMeasurementsByDateRange(startDate, endDate);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen tarihleri doğru formatta giriniz (GG.AA.YYYY).",
                    "Format Hatası",
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
        String[] dietStatus = {"Evet, uyguladım", "Hayır, uygulamadım"};
        JComboBox<String> dietStatusCombo = new JComboBox<>(dietStatus);
        reportPanel.add(dietStatusCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton reportButton = new JButton("Bildir");
        reportButton.addActionListener(e -> {
            addDietTracking(dietStatusCombo, panel);

        });
        reportPanel.add(reportButton, gbc);

        panel.add(reportPanel, BorderLayout.CENTER);

        return panel;
    }

    private void addDietTracking(JComboBox<String> dietStatusCombo, JPanel panel) {
        PatientDietsDao patientDietsDao = new PatientDietsDao();
        DietTracking dietTracking = new DietTracking();
        DietTrackingDao dietTrackingDao = new DietTrackingDao();

        try {
            Integer latestDietId = patientDietsDao.findLatestPatientDietIdByPatientId(patient.getPatient_id());
            dietTracking.setPatient_diet_id(latestDietId);

            dietTracking.setTakip_tarihi(LocalDate.now());

            if (dietStatusCombo.getSelectedItem().toString().toLowerCase().contains("evet"))
                dietTracking.setUygulandi_mi(true);
            else
                dietTracking.setUygulandi_mi(false);

            dietTrackingDao.save(dietTracking);

            JOptionPane.showMessageDialog(panel,
                    "Diyet durumu başarıyla bildirildi:\n" + dietStatusCombo.getSelectedItem(),
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Veritabanı hatası: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
        }
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
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton reportButton = new JButton("Bildir");
        reportButton.addActionListener(e -> {
            addExerciseTracking(exerciseStatusCombo, panel);

        });
        reportPanel.add(reportButton, gbc);

        panel.add(reportPanel, BorderLayout.CENTER);

        return panel;
    }

    private void addExerciseTracking(JComboBox<String> exerciseStatusCombo, JPanel panel) {
        PatientExercisesDao patientExercisesDao = new PatientExercisesDao();
        ExerciseTracking exerciseTracking = new ExerciseTracking();
        ExerciseTrackingDao exerciseTrackingDao = new ExerciseTrackingDao();

        try {
            Integer latestExerciseId = patientExercisesDao.findLatestPatientExerciseIdByPatientId(patient.getPatient_id());
            exerciseTracking.setPatient_exercise_id(latestExerciseId);

            exerciseTracking.setTakip_tarihi(LocalDate.now());

            if (exerciseStatusCombo.getSelectedItem().toString().toLowerCase().contains("evet"))
                exerciseTracking.setUygulandi_mi(true);
            else
                exerciseTracking.setUygulandi_mi(false);

            exerciseTrackingDao.save(exerciseTracking);

            JOptionPane.showMessageDialog(panel,
                    "Egzersiz durumu başarıyla bildirildi.",
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Veritabanı hatası: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Verileri yükler ve panelleri günceller
     */
    private void loadData() {
        if (patient == null || patient.getPatient_id() == null) {
            return;
        }

        // Ana sayfa verilerini yükle
        loadDashboardData();

        // Kan şekeri ölçümlerini yükle
        try {
            LocalDate startDate = LocalDate.now().minusWeeks(1);
            LocalDate endDate = LocalDate.now();

            // Tarih alanları zaten doldurulmuş olabilir
            try {
                startDate = LocalDate.parse(startDateField.getText(), dateFormatter);
                endDate = LocalDate.parse(endDateField.getText(), dateFormatter);
            } catch (Exception e) {
                // Tarih alanları henüz doldurulmamış olabilir
                System.out.println("Tarih alanları henüz doldurulmadı, varsayılan değerler kullanılıyor.");
            }

            loadMeasurementsByDateRange(startDate, endDate);
        } catch (Exception e) {
            System.err.println("Kan şekeri ölçümleri yüklenirken hata: " + e.getMessage());
        }
    }

    /**
     * Ana sayfa verilerini yükler
     */
    private void loadDashboardData() {
        if (patient == null || patient.getPatient_id() == null) {
            return;
        }

        try {
            // Günlük ortalama kan şekeri değeri
            MeasurementDao measurementDao = new MeasurementDao();
            double dailyAverage = 0;

            try {
                // Bugünün tarihini al
                LocalDate today = LocalDate.now();
                dailyAverage = measurementDao.getDailyAverage(patient.getPatient_id(), today);
            } catch (Exception e) {
                System.err.println("Günlük ortalama hesaplanamadı: " + e.getMessage());
            }

            if (dailyAverage > 0) {
                avgValueLabel.setText(String.format("%.1f mg/dL", dailyAverage));
            } else {
                avgValueLabel.setText("Veri yok");
            }

            // Günlük ölçüm değerlerini yükle - SADECE BUGÜNÜN VERİLERİ
            try {
                // Bugünün tarihini al
                LocalDate today = LocalDate.now();

                // Bugüne ait ölçümleri al
                List<BloodSugarMeasurement> todaysMeasurements = measurementDao.findByDateRange(
                        patient.getPatient_id(),
                        today,    // Bugünün başlangıcı
                        today     // Bugünün sonu
                );

                // Periyot bazında değer haritası
                Map<String, Integer> valuesByPeriod = new HashMap<>();
                String[] dbPeriods = {"sabah", "ogle", "ikindi", "aksam", "gece"};

                // Bugünün ölçümlerini haritaya ekle
                for (BloodSugarMeasurement measurement : todaysMeasurements) {
                    String period = measurement.getOlcum_zamani();
                    valuesByPeriod.put(period, measurement.getOlcum_degeri());
                }

                // Tablo modelini güncelle
                for (int i = 0; i < dbPeriods.length; i++) {
                    String value = "--";
                    Integer measuredValue = valuesByPeriod.get(dbPeriods[i]);
                    if (measuredValue != null) {
                        value = measuredValue.toString();
                    }
                    dailyValuesModel.setValueAt(value, 1, i);
                }

            } catch (Exception e) {
                System.err.println("Günlük ölçüm değerleri yüklenirken hata: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("Ana sayfa verileri yüklenirken hata: " + e.getMessage());
        }
    }

    /**
     * Belirli bir tarih aralığındaki ölçümleri yükler
     */
    private void loadMeasurementsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (patient == null || patient.getPatient_id() == null) {
            return;
        }

        try {
            // Tabloyu temizle
            measurementsTableModel.setRowCount(0);

            try {
                // MeasurementDao kullanarak tarih aralığına göre ölçümleri al
                MeasurementDao measurementDao = new MeasurementDao();
                List<BloodSugarMeasurement> measurements = measurementDao.findByDateRange(
                        patient.getPatient_id(),
                        startDate,
                        endDate
                );

                // Tabloya ekle (özel alan değer dönüşümleriyle)
                for (BloodSugarMeasurement measurement : measurements) {
                    String dateStr = measurement.getOlcum_tarihi().toLocalDate().format(dateFormatter);
                    String timeStr = String.format("%02d:%02d",
                            measurement.getOlcum_tarihi().getHour(),
                            measurement.getOlcum_tarihi().getMinute()
                    );

                    // İnsülin dozu
                    String insulinDose = "0.0";
                    if (measurement.getInsulin_miktari() != null) {
                        insulinDose = String.format("%.1f", measurement.getInsulin_miktari());
                    }

                    // Durum
                    String status = measurement.getIs_valid_time() != null &&
                            measurement.getIs_valid_time() ? "Geçerli" : "Geçersiz Saat";

                    // Periyot adını okunabilir formata dönüştür
                    String displayPeriod = periodMap.getOrDefault(
                            measurement.getOlcum_zamani(),
                            measurement.getOlcum_zamani()
                    );

                    // Tabloya ekle
                    Object[] row = {
                            dateStr,
                            timeStr,
                            measurement.getOlcum_degeri(),
                            displayPeriod,
                            insulinDose,
                            status
                    };

                    measurementsTableModel.addRow(row);
                }
            } catch (Exception e) {
                System.err.println("Ölçüm verileri yüklenirken hata: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("Ölçümler yüklenirken genel hata: " + e.getMessage());
        }
    }

    /**
     * Hasta bilgilerini günceller
     */
    public void refreshData(User user) {
        this.currentUser = user;
        this.patient = patientService.getPatientByUserId(this.currentUser.getUser_id());

        // Hoşgeldiniz mesajını güncelle
        welcomeLabel.setText("Hoş Geldiniz, " + currentUser.getAd() + " " + currentUser.getSoyad());

        // Profil resmini güncelle
        Component[] components = ((JPanel)((JPanel)getComponent(0)).getComponent(0)).getComponents();
        for (Component c : components) {
            if (c instanceof JLabel && c != welcomeLabel) {
                JLabel imageLabel = (JLabel)c;
                imageLabel.setIcon(null);

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
        // Verileri yenile
        loadData();
    }
}