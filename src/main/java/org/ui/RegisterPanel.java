package org.ui;

import org.model.User;
import org.service.AuthenticationService;
import org.util.ValidationUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
    private JButton selectImageButton;
    private JLabel imagePreviewLabel;
    private byte[] selectedImageBytes; // Seçilen resmin byte dizisi
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

// Profil Resmi bölümünü güncelleyin
        gbc.gridx = 0;
        gbc.gridy = 9;
        add(new JLabel("Profil Resmi:"), gbc);

        gbc.gridx = 1;
        JPanel imagePanel = new JPanel(new BorderLayout(5, 5));

        // Profil resmi önizleme etiketi - daha kompakt boyutlama
        imagePreviewLabel = new JLabel();
        imagePreviewLabel.setPreferredSize(new Dimension(100, 120)); // Boy/en oranını koruyacak şekilde ayarlayın
        imagePreviewLabel.setBorder(BorderFactory.createEmptyBorder()); // Çerçeveyi kaldırın
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setText("Resim yok");

        // Resim seçme butonu
        selectImageButton = new JButton("Resim Seç");
        selectImageButton.addActionListener(e -> selectImage());

        // Panelleri düzenle - daha kompakt yerleşim
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.add(selectImageButton);

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.add(imagePreviewLabel, BorderLayout.CENTER);
        previewPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); // Daha açık gri, ince çerçeve

        JPanel combinedPanel = new JPanel(new BorderLayout(5, 5));
        combinedPanel.add(buttonPanel, BorderLayout.NORTH);
        combinedPanel.add(previewPanel, BorderLayout.CENTER);

        add(combinedPanel, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Kayıt ol butonu
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        registerButton = new JButton("Kaydı Tamamla");
        registerButton.addActionListener(e -> register());
        add(registerButton, gbc);

        // Geri dön butonu
        gbc.gridy = 11;
        backButton = new JButton("Giriş Ekranına Dön");
        backButton.addActionListener(e -> parent.showLoginPanel());
        add(backButton, gbc);
    }

    // Register metodu güncellendi
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
        user.setDogum_tarihi(dogumTarihi);
        user.setCinsiyet(cinsiyet.charAt(0));
        user.setKullanici_tipi("doktor");

        // Profil resmini ayarla
        if (selectedImageBytes != null) {
            user.setProfil_resmi(selectedImageBytes);
        }

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

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Profil Resmi Seç");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Resim Dosyaları", "jpg", "jpeg", "png"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();

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

                // Önizleme için resmi yeniden boyutlandır (en boy oranını koru)
                BufferedImage resizedImage = resizeImageWithAspectRatio(originalImage, 200, 200);

                // Önizleme için resmi daha küçük boyuta getir (en boy oranını koru)
                int previewWidth = 100;
                int previewHeight = 100;

                // Görüntü en boy oranını hesapla
                double aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
                if (aspectRatio > 1) {  // Yatay görüntü
                    previewHeight = (int) (previewWidth / aspectRatio);
                } else {  // Dikey görüntü
                    previewWidth = (int) (previewHeight * aspectRatio);
                }

                Image scaledImg = resizedImage.getScaledInstance(previewWidth, previewHeight, Image.SCALE_SMOOTH);
                imagePreviewLabel.setIcon(new ImageIcon(scaledImg));
                imagePreviewLabel.setText(null); // Metin yerine resmi göster

                // Resmi byte dizisine dönüştür
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "png", baos);
                selectedImageBytes = baos.toByteArray();
                System.out.println("Resim byte dizisine dönüştürüldü: " + selectedImageBytes.length + " bytes");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Resim yüklenirken bir hata oluştu: " + e.getMessage(),
                        "Resim Yükleme Hatası",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();

                // Hata durumunda resmi temizle
                selectedImageBytes = null;
                imagePreviewLabel.setIcon(null);
                imagePreviewLabel.setText("Resim yok");
            }
        }
    }

    // En-boy oranını koruyan yeni boyutlandırma metodu
    private BufferedImage resizeImageWithAspectRatio(BufferedImage originalImage, int targetWidth, int targetHeight) {
        // Görüntü boyutlarını ve en boy oranını hesapla
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        float ratio = (float) originalWidth / originalHeight;

        int width = targetWidth;
        int height = targetHeight;

        // En-boy oranını koru
        if (width / height > ratio) {
            width = (int) (height * ratio);
        } else {
            height = (int) (width / ratio);
        }

        // Yeni boyutta resmi oluştur
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
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

        // Profil resmi bilgilerini temizle
        selectedImageBytes = null;
        imagePreviewLabel.setIcon(null);
        imagePreviewLabel.setText("Resim yok");
    }
}