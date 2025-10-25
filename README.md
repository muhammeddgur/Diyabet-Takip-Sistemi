Java Swing ve PostgreSQL veritabanı kullanarak kapsamlı bir diyabet hasta takip sistemi geliştirildi. Projede 3NF (Üçüncü Normal Form) normalizasyon prensiplerine uygun veritabanı tasarımı gerçekleştirildi ve Tailscale VPN teknolojisi kullanarak yerel veritabanı yerine merkezi sunucu üzerinde ortak veritabanı erişimi sağlanıldı. DAO (Data Access Object) tasarım deseni, Singleton pattern, BCrypt ile şifre güvenliği, JFreeChart ile veri görselleştirme ve JDBC ile veritabanı işlemlerini uygulayarak hastaların kan şekeri, diyet, egzersiz ve semptom takiplerini yöneten tam özellikli bir sağlık bilgi sistemi oluşturuldu. veri uyarı sistemleri ve Türkçe karakter desteği ile birlikte GUI üzerinden veri görselleştirmeleri ve doktor-hasta etkileşimi sağlandı. 



-Kullanılan Teknolojiler: 

Dil & Framework: Java, Swing (GUI Framework) 

Veritabanı: PostgreSQL (3NF normalizasyon ile tasarlanmış) 

Bağımlılık Yönetimi: Maven 

Kütüphaneler:JDBC (PostgreSQL Driver)JFreeChart (Grafik ve veri görselleştirme)BCrypt (Şifre hashleme ve güvenlik) 

Network: Tailscale VPN (Merkezi veritabanı erişimi) 

 

-Veritabanı ve Mimari: 

3NF Normalizasyon: Veri tutarlılığı ve bütünlüğünü sağlamak için üçüncü normal form kurallarına uygun tablo tasarımı 

Merkezi Veritabanı Erişimi: Tailscale kullanarak local veritabanı yerine host üzerinde ortak PostgreSQL sunucusu erişimi 

DAO Pattern: Her entity için ayrı DAO sınıfları ile veri erişim katmanı soyutlaması 

 

-Yazılım Mühendisliği Becerileri: 

Tasarım Desenleri: Singleton Pattern (DatabaseConnectionManager)DAO Pattern (Veri erişim katmanı)Service Layer Pattern (İş mantığı katmanı) 

SOLID Prensipleri: Interface-based design, dependency injectionGüvenlik: BCrypt ile password hashing, güvenli şifre oluşturma 

Veri Görselleştirme: JFreeChart ile kan şekeri, diyet ve egzersiz takibi grafikleri 

 

-Özellikler: 

Hasta kayıt ve kimlik doğrulama sistemi, Kan şekeri ölçüm takibi ve grafik görselleştirme, Diyet planlama ve uyum takibi, Egzersiz programları ve takibi, Semptom kayıt ve analiz sistemi, Uyarı ve bildirim yönetimi, Tarih bazlı raporlama ve istatistiksel analiz 

 
