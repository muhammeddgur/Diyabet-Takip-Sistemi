package org.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): 2025-05-14 15:39:15
 * Current User's Login: Emirhan-Karabulut
 *
 * Kullanıcı bilgilerini tutan model sınıfı
 */
public class User {
    private Integer userId;
    private String tcIdentity;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private LocalDate birthDate;
    private String gender;
    private String userType; // DOCTOR, PATIENT
    private LocalDateTime createdAt;

    // Profil resmi için byte dizisi (veritabanındaki adıyla uyumlu)
    private byte[] profilePhoto;

    // Getter ve Setter metotları
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTcIdentity() {
        return tcIdentity;
    }

    public void setTcIdentity(String tcIdentity) {
        this.tcIdentity = tcIdentity;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public byte[] getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(byte[] profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    // Tam adı döndürür
    public String getFullName() {
        return firstName + " " + lastName;
    }
}