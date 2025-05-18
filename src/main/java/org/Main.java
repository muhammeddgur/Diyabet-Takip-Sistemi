package org;

import org.dao.DatabaseConnectionManager;
import org.model.*;
import org.service.*;
import org.util.DateTimeUtil;
import org.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Diyabet Takip Sistemi ana sınıfı
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static AuthenticationService authService;
    private static PatientService patientService;
    private static MeasurementService measurementService;
    private static DietService dietService;
    private static ExerciseService exerciseService;
    private static AlertService alertService;
    private static User currentUser;

    public static void main(String[] args) {
        System.out.println("Diyabet Takip Sistemi Başlatılıyor...");

        // Veritabanı bağlantısını başlat
        try {
            DatabaseConnectionManager.getInstance();
            System.out.println("Veritabanı bağlantısı kuruldu.");
        } catch (Exception e) {
            System.err.println("Veritabanı bağlantısı kurulamadı: " + e.getMessage());
            return;
        }

        // Servisleri başlat
        initializeServices();

        // Ana menüyü göster
        showMainMenu();

        // Uygulama çıkışı
        System.out.println("Diyabet Takip Sistemi kapatılıyor...");
        scanner.close();
    }

    private static void initializeServices() {
        authService = new AuthenticationService();
        patientService = new PatientService();
        measurementService = new MeasurementService();
        dietService = new DietService();
        exerciseService = new ExerciseService();
        alertService = new AlertService();
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\n===== DIYABET TAKİP SİSTEMİ =====");
            System.out.println("1. Giriş Yap");
            System.out.println("2. Kayıt Ol");
            System.out.println("3. Şifremi Unuttum");
            System.out.println("0. Çıkış");
            System.out.print("Seçiminiz: ");

            int choice = readInt();

            switch (choice) {
                case 0:
                    return;
                case 1:
                    login();
                    break;
                case 2:
                    register();
                    break;
                case 3:
                    resetPassword();
                    break;
                default:
                    System.out.println("Geçersiz seçim, tekrar deneyin.");
            }
        }
    }

    private static void login() {
        System.out.println("\n===== GİRİŞ =====");
        System.out.print("TC Kimlik No: ");
        String tcKimlik = scanner.nextLine();

        System.out.print("Şifre: ");
        String password = scanner.nextLine();

        currentUser = authService.login(tcKimlik, password);

        if (currentUser != null) {
            System.out.println("Giriş başarılı! Hoş geldiniz, " + currentUser.getAd() + " " + currentUser.getSoyad());

            if ("hasta".equals(currentUser.getKullanici_tipi())) {
                showPatientMenu();
            } else if ("doktor".equals(currentUser.getKullanici_tipi())) {
                showDoctorMenu();
            } else if ("admin".equals(currentUser.getKullanici_tipi())) {
                showAdminMenu();
            }
        } else {
            System.out.println("Giriş başarısız. TC Kimlik No veya şifre hatalı.");
        }
    }

    private static void register() {
        System.out.println("\n===== KAYIT OL =====");

        User user = new User();

        boolean valid = false;
        while (!valid) {
            System.out.print("TC Kimlik No: ");
            String tcKimlik = scanner.nextLine();

            if (ValidationUtil.validateTcKimlik(tcKimlik)) {
                user.setTc_kimlik(tcKimlik);
                valid = true;
            } else {
                System.out.println("Geçersiz TC Kimlik No. Lütfen 11 haneli geçerli bir TC Kimlik No girin.");
            }
        }

        valid = false;
        while (!valid) {
            System.out.print("Ad: ");
            String ad = scanner.nextLine();

            if (ValidationUtil.validateName(ad)) {
                user.setAd(ad);
                valid = true;
            } else {
                System.out.println("Geçersiz ad. Lütfen en az 2 karakter ve sadece harf içeren bir ad girin.");
            }
        }

        valid = false;
        while (!valid) {
            System.out.print("Soyad: ");
            String soyad = scanner.nextLine();

            if (ValidationUtil.validateName(soyad)) {
                user.setSoyad(soyad);
                valid = true;
            } else {
                System.out.println("Geçersiz soyad. Lütfen en az 2 karakter ve sadece harf içeren bir soyad girin.");
            }
        }

        valid = false;
        while (!valid) {
            System.out.print("E-posta: ");
            String email = scanner.nextLine();

            if (ValidationUtil.validateEmail(email)) {
                user.setEmail(email);
                valid = true;
            } else {
                System.out.println("Geçersiz e-posta formatı. Lütfen geçerli bir e-posta adresi girin.");
            }
        }

        valid = false;
        while (!valid) {
            System.out.print("Şifre (en az 8 karakter, büyük/küçük harf, rakam ve özel karakter): ");
            String password = scanner.nextLine();

            if (ValidationUtil.validatePassword(password)) {
                user.setPassword(password);
                valid = true;
            } else {
                System.out.println("Şifre gereksinimleri karşılamıyor. Lütfen daha güçlü bir şifre girin.");
            }
        }

        System.out.print("Kullanıcı Tipi (hasta/doktor): ");
        String userType = scanner.nextLine().toLowerCase();
        user.setKullanici_tipi(userType);

        User registeredUser = authService.register(user);

        if (registeredUser != null) {
            System.out.println("Kayıt başarılı! Şimdi giriş yapabilirsiniz.");
        } else {
            System.out.println("Kayıt başarısız. Lütfen tekrar deneyin.");
        }
    }

    private static void resetPassword() {
        System.out.println("\n===== ŞİFREMİ SIFIRLA =====");

        System.out.print("E-posta adresiniz: ");
        String email = scanner.nextLine();

        if (!ValidationUtil.validateEmail(email)) {
            System.out.println("Geçersiz e-posta formatı.");
            return;
        }

        boolean result = authService.resetPassword(email);

        if (result) {
            System.out.println("Şifre sıfırlama bağlantısı e-posta adresinize gönderildi.");
        } else {
            System.out.println("Şifre sıfırlama başarısız. Bu e-posta ile kayıtlı kullanıcı bulunamadı.");
        }
    }

    private static void showPatientMenu() {
        // Hastayı al
        Patient patient = patientService.getPatient(currentUser.getUser_id());

        while (true) {
            System.out.println("\n===== HASTA MENÜSÜ =====");
            System.out.println("1. Kan Şekeri Ölçümü Ekle");
            System.out.println("2. Ölçümlerimi Görüntüle");
            System.out.println("3. Diyet Takibi Yap");
            System.out.println("4. Egzersiz Takibi Yap");
            System.out.println("5. Uyarılarımı Görüntüle");
            System.out.println("6. Şifremi Değiştir");
            System.out.println("0. Çıkış Yap");
            System.out.print("Seçiminiz: ");

            int choice = readInt();

            switch (choice) {
                case 0:
                    currentUser = null;
                    return;
                case 1:
                    addBloodSugarMeasurement(patient);
                    break;
                case 2:
                    viewMeasurements(patient);
                    break;
                case 3:
                    trackDiet(patient);
                    break;
                case 4:
                    trackExercise(patient);
                    break;
                case 5:
                    viewAlerts(patient);
                    break;
                case 6:
                    changePassword();
                    break;
                default:
                    System.out.println("Geçersiz seçim, tekrar deneyin.");
            }
        }
    }

    private static void addBloodSugarMeasurement(Patient patient) {
        System.out.println("\n===== KAN ŞEKERİ ÖLÇÜMÜ EKLE =====");

        System.out.print("Kan şekeri değeri (mg/dL): ");
        int bloodSugar = readInt();

        if (!ValidationUtil.validateBloodSugar(bloodSugar)) {
            System.out.println("Geçersiz kan şekeri değeri. 30 ile 600 mg/dL arasında olmalıdır.");
            return;
        }

        BloodSugarMeasurement measurement = new BloodSugarMeasurement();
        measurement.setPatient(patient);
        measurement.setOlcum_degeri(bloodSugar);
        measurement.setOlcum_tarihi(LocalDateTime.now());

        boolean result = measurementService.addMeasurement(measurement);

        if (result) {
            System.out.println("Kan şekeri ölçümü başarıyla kaydedildi.");

            // Kan şekeri değeri çok düşük veya çok yüksekse uyarı ver
            if (bloodSugar < 70) {
                System.out.println("UYARI: Kan şekeri değeriniz düşük! Hemen bir şeyler yemelisiniz.");
            } else if (bloodSugar > 200) {
                System.out.println("UYARI: Kan şekeri değeriniz yüksek! Doktorunuzla iletişime geçmelisiniz.");
            }
        } else {
            System.out.println("Kan şekeri ölçümü kaydedilemedi.");
        }
    }

    private static void viewMeasurements(Patient patient) {
        System.out.println("\n===== ÖLÇÜMLER =====");
        System.out.println("1. Günlük Ölçümler");
        System.out.println("2. Haftalık Ölçümler");
        System.out.println("3. Aylık Ölçümler");
        System.out.println("0. Geri");
        System.out.print("Seçiminiz: ");

        int choice = readInt();

        switch (choice) {
            case 0:
                return;
            case 1:
                showDailyMeasurements(patient);
                break;
            case 2:
                showWeeklyMeasurements(patient);
                break;
            case 3:
                showMonthlyMeasurements(patient);
                break;
            default:
                System.out.println("Geçersiz seçim, tekrar deneyin.");
        }
    }

    private static void showDailyMeasurements(Patient patient) {
        System.out.println("\n===== GÜNLÜK ÖLÇÜMLER =====");

        LocalDate today = DateTimeUtil.getCurrentDate();
        List<BloodSugarMeasurement> measurements = measurementService.getDailyMeasurements(patient.getPatient_id(), today);

        if (measurements.isEmpty()) {
            System.out.println("Bugün için kaydedilmiş ölçüm bulunmamaktadır.");
        } else {
            System.out.println("Tarih\t\tSaat\t\tDeğer (mg/dL)");
            System.out.println("--------------------------------------");

            for (BloodSugarMeasurement measurement : measurements) {
                System.out.println(
                        DateTimeUtil.formatDate(measurement.getOlcum_tarihi().toLocalDate()) + "\t" +
                                DateTimeUtil.formatTime(measurement.getOlcum_tarihi().toLocalTime()) + "\t" +
                                measurement.getOlcum_degeri()
                );
            }

            double average = measurementService.calculateDailyAverage(patient.getPatient_id(), today);
            System.out.println("\nGünlük ortalama: " + average + " mg/dL");
        }
    }

    private static void showWeeklyMeasurements(Patient patient) {
        System.out.println("\n===== HAFTALIK ÖLÇÜMLER =====");

        List<BloodSugarMeasurement> measurements = measurementService.getWeeklyMeasurements(patient.getPatient_id());

        if (measurements.isEmpty()) {
            System.out.println("Son hafta için kaydedilmiş ölçüm bulunmamaktadır.");
        } else {
            System.out.println("Tarih\t\tSaat\t\tDeğer (mg/dL)");
            System.out.println("--------------------------------------");

            for (BloodSugarMeasurement measurement : measurements) {
                System.out.println(
                        DateTimeUtil.formatDate(measurement.getOlcum_tarihi().toLocalDate()) + "\t" +
                                DateTimeUtil.formatTime(measurement.getOlcum_tarihi().toLocalTime()) + "\t" +
                                measurement.getOlcum_degeri()
                );
            }

            // Haftalık ortalama hesaplama - basit bir örnek
            double sum = 0;
            for (BloodSugarMeasurement measurement : measurements) {
                sum += measurement.getOlcum_degeri();
            }
            double average = sum / measurements.size();

            System.out.println("\nHaftalık ortalama: " + average + " mg/dL");
        }
    }

    private static void showMonthlyMeasurements(Patient patient) {
        System.out.println("\n===== AYLIK ÖLÇÜMLER =====");

        List<BloodSugarMeasurement> measurements = measurementService.getMonthlyMeasurements(patient.getPatient_id());

        if (measurements.isEmpty()) {
            System.out.println("Son ay için kaydedilmiş ölçüm bulunmamaktadır.");
        } else {
            System.out.println("Tarih\t\tSaat\t\tDeğer (mg/dL)");
            System.out.println("--------------------------------------");

            for (BloodSugarMeasurement measurement : measurements) {
                System.out.println(
                        DateTimeUtil.formatDate(measurement.getOlcum_tarihi().toLocalDate()) + "\t" +
                                DateTimeUtil.formatTime(measurement.getOlcum_tarihi().toLocalTime()) + "\t" +
                                measurement.getOlcum_degeri()
                );
            }

            // Aylık ortalama hesaplama - basit bir örnek
            double sum = 0;
            for (BloodSugarMeasurement measurement : measurements) {
                sum += measurement.getOlcum_degeri();
            }
            double average = sum / measurements.size();

            System.out.println("\nAylık ortalama: " + average + " mg/dL");
        }
    }

    private static void trackDiet(Patient patient) {
        System.out.println("\n===== DİYET TAKİBİ =====");

        // Hastaya atanan diyetleri listele
        System.out.println("Atanan Diyetleriniz:");
        List<Diet> diets = dietService.getAllDiets();

        if (diets.isEmpty()) {
            System.out.println("Atanmış diyet bulunmamaktadır.");
            return;
        }

        for (int i = 0; i < diets.size(); i++) {
            Diet diet = diets.get(i);
            System.out.println((i+1) + ". " + diet.getDiet_adi());
        }

        System.out.print("\nHangi diyeti takip ettiğinizi işaretlemek için numara seçin (0: Geri): ");
        int choice = readInt();

        if (choice == 0 || choice > diets.size()) {
            return;
        }

        Diet selectedDiet = diets.get(choice - 1);

        System.out.print("Diyeti bugün uyguladınız mı? (e/h): ");
        String response = scanner.nextLine().trim().toLowerCase();

        DietTracking tracking = new DietTracking();
        tracking.setPatient(patient);
        tracking.setDiet(selectedDiet);
        tracking.setTakip_tarihi(LocalDate.now());
        tracking.setUygulandi_mi(response.startsWith("e"));

        boolean result = dietService.trackDiet(tracking);

        if (result) {
            System.out.println("Diyet takibi başarıyla kaydedildi.");
        } else {
            System.out.println("Diyet takibi kaydedilemedi.");
        }
    }

    private static void trackExercise(Patient patient) {
        System.out.println("\n===== EGZERSİZ TAKİBİ =====");

        // Hastaya atanan egzersizleri listele
        System.out.println("Atanan Egzersizleriniz:");
        List<Exercise> exercises = exerciseService.getAllExercises();

        if (exercises.isEmpty()) {
            System.out.println("Atanmış egzersiz bulunmamaktadır.");
            return;
        }

        for (int i = 0; i < exercises.size(); i++) {
            Exercise exercise = exercises.get(i);
            System.out.println((i+1) + ". " + exercise.getExercise_adi());
        }

        System.out.print("\nHangi egzersizi yaptığınızı işaretlemek için numara seçin (0: Geri): ");
        int choice = readInt();

        if (choice == 0 || choice > exercises.size()) {
            return;
        }

        Exercise selectedExercise = exercises.get(choice - 1);

        System.out.print("Egzersizi bugün yaptınız mı? (e/h): ");
        String response = scanner.nextLine().trim().toLowerCase();

        ExerciseTracking tracking = new ExerciseTracking();
        tracking.setPatient(patient);
        tracking.setExercise(selectedExercise);
        tracking.setTakip_tarihi(LocalDate.now());
        tracking.setYapildi_mi(response.startsWith("e"));

        boolean result = exerciseService.trackExercise(tracking);

        if (result) {
            System.out.println("Egzersiz takibi başarıyla kaydedildi.");
        } else {
            System.out.println("Egzersiz takibi kaydedilemedi.");
        }
    }

    private static void viewAlerts(Patient patient) {
        System.out.println("\n===== UYARILAR =====");

        List<Alert> alerts = alertService.getPatientAlerts(patient.getPatient_id());

        if (alerts.isEmpty()) {
            System.out.println("Herhangi bir uyarı bulunmamaktadır.");
        } else {
            System.out.println("Tarih\t\t\tTip\t\t\tMesaj\t\t\tOkundu");
            System.out.println("-----------------------------------------------------------");

            for (Alert alert : alerts) {
                System.out.println(
                        DateTimeUtil.formatDateTime(alert.getOlusturma_zamani()) + "\t" +
                                alert.getAlertType().getTip_adi() + "\t" +
                                alert.getMesaj() + "\t" +
                                (alert.getOkundu_mu() ? "Evet" : "Hayır")
                );

                // Okunmamış uyarıyı okundu olarak işaretle
                if (!alert.getOkundu_mu()) {
                    alertService.markAlertAsRead(alert.getAlert_id());
                }
            }
        }
    }

    private static void changePassword() {
        System.out.println("\n===== ŞİFRE DEĞİŞTİR =====");

        System.out.print("Eski şifre: ");
        String oldPassword = scanner.nextLine();

        System.out.print("Yeni şifre (en az 8 karakter, büyük/küçük harf, rakam ve özel karakter): ");
        String newPassword = scanner.nextLine();

        if (!ValidationUtil.validatePassword(newPassword)) {
            System.out.println("Yeni şifre gereksinimleri karşılamıyor.");
            return;
        }

        boolean result = authService.changePassword(currentUser.getUser_id(), oldPassword, newPassword);

        if (result) {
            System.out.println("Şifre başarıyla değiştirildi.");
        } else {
            System.out.println("Şifre değiştirilemedi. Eski şifrenizi doğru girdiğinizden emin olun.");
        }
    }

    private static void showDoctorMenu() {
        while (true) {
            System.out.println("\n===== DOKTOR MENÜSÜ =====");
            System.out.println("1. Hastalarımı Listele");
            System.out.println("2. Hasta Ekle");
            System.out.println("3. Hasta Bilgilerini Güncelle");
            System.out.println("4. Hasta Ölçümlerini Görüntüle");
            System.out.println("5. Diyet Ata");
            System.out.println("6. Egzersiz Ata");
            System.out.println("7. Uyarıları Görüntüle");
            System.out.println("8. Şifremi Değiştir");
            System.out.println("0. Çıkış Yap");
            System.out.print("Seçiminiz: ");

            int choice = readInt();

            switch (choice) {
                case 0:
                    currentUser = null;
                    return;
                case 1:
                    listPatients();
                    break;
                case 2:
                    addPatient();
                    break;
                case 3:
                    updatePatient();
                    break;
                case 4:
                    viewPatientMeasurements();
                    break;
                case 5:
                    assignDiet();
                    break;
                case 6:
                    assignExercise();
                    break;
                case 7:
                    viewDoctorAlerts();
                    break;
                case 8:
                    changePassword();
                    break;
                default:
                    System.out.println("Geçersiz seçim, tekrar deneyin.");
            }
        }
    }

    private static void listPatients() {
        System.out.println("\n===== HASTALARIM =====");

        List<Patient> patients = patientService.getDoctorPatients(currentUser.getUser_id());

        if (patients.isEmpty()) {
            System.out.println("Kayıtlı hastanız bulunmamaktadır.");
        } else {
            System.out.println("ID\tAdı\tSoyadı\tTC Kimlik\tYaş\tCinsiyet");
            System.out.println("-----------------------------------------------------------");

            for (Patient patient : patients) {
                System.out.println(
                        patient.getPatient_id() + "\t" +
                                patient.getAd() + "\t" +
                                patient.getSoyad() + "\t" +
                                patient.getTc_kimlik() + "\t" +
                                patient.getCinsiyet()
                );
            }
        }
    }

    private static void addPatient() {
        // Benzer kayıt işlemleri, doktor ataması otomatik yapılır
        System.out.println("\n===== HASTA EKLE =====");

        Patient patient = new Patient();

        // TC Kimlik doğrulama
        boolean valid = false;
        while (!valid) {
            System.out.print("TC Kimlik No: ");
            String tcKimlik = scanner.nextLine();

            if (ValidationUtil.validateTcKimlik(tcKimlik)) {
                patient.setTc_kimlik(tcKimlik);
                valid = true;
            } else {
                System.out.println("Geçersiz TC Kimlik No. Lütfen 11 haneli geçerli bir TC Kimlik No girin.");
            }
        }

        // İsim doğrulama
        valid = false;
        while (!valid) {
            System.out.print("Ad: ");
            String ad = scanner.nextLine();

            if (ValidationUtil.validateName(ad)) {
                patient.setAd(ad);
                valid = true;
            } else {
                System.out.println("Geçersiz ad. Lütfen en az 2 karakter ve sadece harf içeren bir ad girin.");
            }
        }

        // Soyad doğrulama
        valid = false;
        while (!valid) {
            System.out.print("Soyad: ");
            String soyad = scanner.nextLine();

            if (ValidationUtil.validateName(soyad)) {
                patient.setSoyad(soyad);
                valid = true;
            } else {
                System.out.println("Geçersiz soyad. Lütfen en az 2 karakter ve sadece harf içeren bir soyad girin.");
            }
        }

        // E-posta doğrulama
        valid = false;
        while (!valid) {
            System.out.print("E-posta: ");
            String email = scanner.nextLine();

            if (ValidationUtil.validateEmail(email)) {
                patient.setEmail(email);
                valid = true;
            } else {
                System.out.println("Geçersiz e-posta formatı. Lütfen geçerli bir e-posta adresi girin.");
            }
        }

        System.out.print("Şifre: ");
        patient.setPassword(scanner.nextLine());

        System.out.print("Cinsiyet (E/K): ");
        patient.setCinsiyet(scanner.nextLine().charAt(0));

        // Kullanıcı tipini otomatik olarak hasta yap
        patient.setKullanici_tipi("hasta");

        // Doktor olarak kendini ata
        Doctor doctor = new Doctor();
        doctor.setDoctor_id(currentUser.getUser_id());
        patient.setDoctor(doctor);

        Patient addedPatient = patientService.addPatient(patient);

        if (addedPatient != null) {
            System.out.println("Hasta başarıyla eklendi!");
        } else {
            System.out.println("Hasta eklenemedi. Lütfen girdiğiniz bilgileri kontrol edin.");
        }
    }

    private static void updatePatient() {
        System.out.println("\n===== HASTA BİLGİLERİNİ GÜNCELLE =====");

        System.out.print("Güncellenecek hastanın ID'si: ");
        int patientId = readInt();

        Patient patient = patientService.getPatient(patientId);

        if (patient == null) {
            System.out.println("Hasta bulunamadı.");
            return;
        }

        // Hastanın mevcut bilgilerini göster
        System.out.println("\nMevcut Bilgiler:");
        System.out.println("Ad: " + patient.getAd());
        System.out.println("Soyad: " + patient.getSoyad());
        System.out.println("TC Kimlik: " + patient.getTc_kimlik());
        System.out.println("E-posta: " + patient.getEmail());
        System.out.println("Cinsiyet: " + patient.getCinsiyet());

        System.out.println("\nGüncellenecek alanı seçin:");
        System.out.println("1. Ad");
        System.out.println("2. Soyad");
        System.out.println("3. E-posta");
        System.out.println("4. Cinsiyet");
        System.out.println("0. İptal");
        System.out.print("Seçiminiz: ");

        int choice = readInt();

        switch (choice) {
            case 0:
                return;
            case 1:
                System.out.print("Yeni ad: ");
                String newName = scanner.nextLine();
                if (ValidationUtil.validateName(newName)) {
                    patient.setAd(newName);
                } else {
                    System.out.println("Geçersiz ad formatı.");
                    return;
                }
                break;
            case 2:
                System.out.print("Yeni soyad: ");
                String newSurname = scanner.nextLine();
                if (ValidationUtil.validateName(newSurname)) {
                    patient.setSoyad(newSurname);
                } else {
                    System.out.println("Geçersiz soyad formatı.");
                    return;
                }
                break;
            case 3:
                System.out.print("Yeni e-posta: ");
                String newEmail = scanner.nextLine();
                if (ValidationUtil.validateEmail(newEmail)) {
                    patient.setEmail(newEmail);
                } else {
                    System.out.println("Geçersiz e-posta formatı.");
                    return;
                }
                break;
            case 4:
                System.out.print("Yeni cinsiyet (E/K): ");
                patient.setCinsiyet(scanner.nextLine().charAt(0));
                break;
            default:
                System.out.println("Geçersiz seçim.");
                return;
        }

        boolean result = patientService.updatePatient(patient);

        if (result) {
            System.out.println("Hasta bilgileri başarıyla güncellendi.");
        } else {
            System.out.println("Hasta bilgileri güncellenemedi.");
        }
    }

    private static void viewPatientMeasurements() {
        System.out.println("\n===== HASTA ÖLÇÜMLERİNİ GÖRÜNTÜLE =====");

        System.out.print("Hastanın ID'si: ");
        int patientId = readInt();

        Patient patient = patientService.getPatient(patientId);

        if (patient == null) {
            System.out.println("Hasta bulunamadı.");
            return;
        }

        // Mevcut ölçüm görüntüleme metodlarını yeniden kullan
        System.out.println("\n" + patient.getAd() + " " + patient.getSoyad() + " için ölçümler:");

        viewMeasurements(patient);
    }

    private static void assignDiet() {
        System.out.println("\n===== DİYET ATA =====");

        System.out.print("Hastanın ID'si: ");
        int patientId = readInt();

        Patient patient = patientService.getPatient(patientId);

        if (patient == null) {
            System.out.println("Hasta bulunamadı.");
            return;
        }

        // Tüm diyetleri listele
        System.out.println("\nMevcut Diyetler:");
        List<Diet> diets = dietService.getAllDiets();

        if (diets.isEmpty()) {
            System.out.println("Kayıtlı diyet bulunmamaktadır.");
            return;
        }

        for (int i = 0; i < diets.size(); i++) {
            Diet diet = diets.get(i);
            System.out.println((i+1) + ". " + diet.getDiet_adi());
        }

        System.out.print("\nAtanacak diyeti seçin (0: İptal): ");
        int choice = readInt();

        if (choice == 0 || choice > diets.size()) {
            return;
        }

        Diet selectedDiet = diets.get(choice - 1);

        boolean result = dietService.assignDiet(patient.getPatient_id(), selectedDiet.getDiet_id(), currentUser.getUser_id());

        if (result) {
            System.out.println("Diyet başarıyla atandı.");
        } else {
            System.out.println("Diyet atanamadı.");
        }
    }

    private static void assignExercise() {
        System.out.println("\n===== EGZERSİZ ATA =====");

        System.out.print("Hastanın ID'si: ");
        int patientId = readInt();

        Patient patient = patientService.getPatient(patientId);

        if (patient == null) {
            System.out.println("Hasta bulunamadı.");
            return;
        }

        // Tüm egzersizleri listele
        System.out.println("\nMevcut Egzersizler:");
        List<Exercise> exercises = exerciseService.getAllExercises();

        if (exercises.isEmpty()) {
            System.out.println("Kayıtlı egzersiz bulunmamaktadır.");
            return;
        }

        for (int i = 0; i < exercises.size(); i++) {
            Exercise exercise = exercises.get(i);
            System.out.println((i+1) + ". " + exercise.getExercise_adi());
        }

        System.out.print("\nAtanacak egzersizi seçin (0: İptal): ");
        int choice = readInt();

        if (choice == 0 || choice > exercises.size()) {
            return;
        }

        Exercise selectedExercise = exercises.get(choice - 1);

        boolean result = exerciseService.assignExercise(patient.getPatient_id(), selectedExercise.getExercise_id(), currentUser.getUser_id());

        if (result) {
            System.out.println("Egzersiz başarıyla atandı.");
        } else {
            System.out.println("Egzersiz atanamadı.");
        }
    }

    private static void viewDoctorAlerts() {
        System.out.println("\n===== UYARILAR =====");

        List<Alert> alerts = alertService.getDoctorAlerts(currentUser.getUser_id());

        if (alerts.isEmpty()) {
            System.out.println("Herhangi bir uyarı bulunmamaktadır.");
        } else {
            System.out.println("Tarih\t\t\tTip\t\tHasta\t\tMesaj\t\t\tOkundu");
            System.out.println("-------------------------------------------------------------------------");

            for (Alert alert : alerts) {
                System.out.println(
                        DateTimeUtil.formatDateTime(alert.getOlusturma_zamani()) + "\t" +
                                alert.getAlertType().getTip_adi() + "\t" +
                                alert.getPatient().getAd() + " " + alert.getPatient().getSoyad() + "\t" +
                                alert.getMesaj() + "\t" +
                                (alert.getOkundu_mu() ? "Evet" : "Hayır")
                );

                // Okunmamış uyarıyı okundu olarak işaretle
                if (!alert.getOkundu_mu()) {
                    alertService.markAlertAsRead(alert.getAlert_id());
                }
            }
        }
    }

    private static void showAdminMenu() {
        System.out.println("\n===== YÖNETİCİ MENÜSÜ =====");
        System.out.println("Admin işlevleri henüz uygulanmamıştır.");
    }

    // Input yardımcı metodu
    private static int readInt() {
        try {
            String input = scanner.nextLine();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}