package org.model;

import java.time.LocalDateTime;

public class Alert {
    private Integer alert_id;
    private Patient patient;
    private Doctor doctor;
    private AlertType alertType;
    private String mesaj;
    private LocalDateTime olusturma_zamani;
    private Boolean okundu_mu;
    private LocalDateTime okunma_zamani;

    // Constructors
    public Alert() {
        this.olusturma_zamani = LocalDateTime.now();
        this.okundu_mu = false;
    }

    public Alert(Patient patient, Doctor doctor, AlertType alertType, String mesaj) {
        this.patient = patient;
        this.doctor = doctor;
        this.alertType = alertType;
        this.mesaj = mesaj;
        this.olusturma_zamani = LocalDateTime.now();
        this.okundu_mu = false;
    }

    // Getters and Setters
    public Integer getAlert_id() {
        return alert_id;
    }

    public void setAlert_id(Integer alert_id) {
        this.alert_id = alert_id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public String getMesaj() {
        return mesaj;
    }

    public void setMesaj(String mesaj) {
        this.mesaj = mesaj;
    }

    public LocalDateTime getOlusturma_zamani() {
        return olusturma_zamani;
    }

    public void setOlusturma_zamani(LocalDateTime olusturma_zamani) {
        this.olusturma_zamani = olusturma_zamani;
    }

    public Boolean getOkundu_mu() {
        return okundu_mu;
    }

    public void setOkundu_mu(Boolean okundu_mu) {
        this.okundu_mu = okundu_mu;
        if (okundu_mu) {
            this.okunma_zamani = LocalDateTime.now();
        }
    }

    public LocalDateTime getOkunma_zamani() {
        return okunma_zamani;
    }

    public void setOkunma_zamani(LocalDateTime okunma_zamani) {
        this.okunma_zamani = okunma_zamani;
    }

    // Uyarıyı okundu olarak işaretlemek için yardımcı metod
    public void markAsRead() {
        this.okundu_mu = true;
        this.okunma_zamani = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Alert{" +
                "alert_id=" + alert_id +
                ", patient=" + (patient != null ? patient.getAd() + " " + patient.getSoyad() : "Bilinmeyen") +
                ", doctor=" + (doctor != null ? doctor.getAd() + " " + doctor.getSoyad() : "Bilinmeyen") +
                ", alertType=" + (alertType != null ? alertType.getTip_adi() : "Bilinmeyen") +
                ", mesaj='" + mesaj + '\'' +
                ", olusturma_zamani=" + olusturma_zamani +
                ", okundu_mu=" + okundu_mu +
                ", okunma_zamani=" + okunma_zamani +
                '}';
    }
}