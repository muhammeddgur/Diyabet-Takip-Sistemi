package org.model;

import org.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

public class User {
    private Integer user_id;
    private String tc_kimlik;
    private String password;
    private String email;
    private String ad;
    private String soyad;
    private LocalDate dogum_tarihi;
    private char cinsiyet;
    private byte[] profil_resmi;
    private String kullanici_tipi;
    private LocalDateTime created_at;
    private LocalDateTime last_login;

    // Constructors
    public User() {
    }

    public User(String tc_kimlik, String password, String email, String ad, String soyad,
                LocalDate dogum_tarihi, char cinsiyet, String kullanici_tipi) {
        this.tc_kimlik = tc_kimlik;
        this.password = password;
        this.email = email;
        this.ad = ad;
        this.soyad = soyad;
        this.dogum_tarihi = dogum_tarihi;
        this.cinsiyet = cinsiyet;
        this.kullanici_tipi = kullanici_tipi;
        this.created_at = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getTc_kimlik() {
        return tc_kimlik;
    }

    public void setTc_kimlik(String tc_kimlik) {
        if (!ValidationUtil.validateTcKimlik(tc_kimlik)) {
            throw new IllegalArgumentException("Geçersiz TC kimlik numarası formatı");
        }
        this.tc_kimlik = tc_kimlik;
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
        if (!ValidationUtil.validateEmail(email)) {
            throw new IllegalArgumentException("Geçersiz e-posta formatı");
        }
        this.email = email;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getSoyad() {
        return soyad;
    }

    public void setSoyad(String soyad) {
        this.soyad = soyad;
    }

    public LocalDate getDogum_tarihi() {
        return dogum_tarihi;
    }

    public void setDogum_tarihi(LocalDate dogum_tarihi) {
        this.dogum_tarihi = dogum_tarihi;
    }

    public char getCinsiyet() {
        return cinsiyet;
    }

    public void setCinsiyet(char cinsiyet) {
        this.cinsiyet = cinsiyet;
    }

    public byte[] getProfil_resmi() {
        return profil_resmi;
    }

    public void setProfil_resmi(byte[] profil_resmi) {
        this.profil_resmi = profil_resmi;
    }

    public String getKullanici_tipi() {
        return kullanici_tipi;
    }

    public void setKullanici_tipi(String kullanici_tipi) {
        this.kullanici_tipi = kullanici_tipi;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getLast_login() {
        return last_login;
    }

    public void setLast_login(LocalDateTime last_login) {
        this.last_login = last_login;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id=" + user_id +
                ", tc_kimlik='" + tc_kimlik + '\'' +
                ", email='" + email + '\'' +
                ", ad='" + ad + '\'' +
                ", soyad='" + soyad + '\'' +
                ", dogum_tarihi=" + dogum_tarihi +
                ", cinsiyet=" + cinsiyet +
                ", kullanici_tipi='" + kullanici_tipi + '\'' +
                ", created_at=" + created_at +
                ", last_login=" + last_login +
                '}';
    }
}