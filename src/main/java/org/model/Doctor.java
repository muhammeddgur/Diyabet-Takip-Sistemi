package org.model;

/**
 * Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): 2025-05-14 15:53:19
 * Current User's Login: Emirhan-Karabulut
 *
 * Doktor bilgilerini tutan model sınıfı
 * Lisans numarası, hastane ve uzmanlık alanı kaldırıldı
 */
public class Doctor {
    private Integer doctorId;
    private Integer userId;
    private User user; // İlişki için referans

    // Constructors
    public Doctor() {}

    public Doctor(Integer doctorId, Integer userId) {
        this.doctorId = doctorId;
        this.userId = userId;
    }

    // Getters and Setters
    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctorId=" + doctorId +
                ", userId=" + userId +
                '}';
    }

    /**
     * Doktorun tam adını döndürür
     */
    public String getFullTitle() {
        if (user != null) {
            return "Dr. " + user.getFullName();
        }
        return "Dr. (Bilgi yok)";
    }
}