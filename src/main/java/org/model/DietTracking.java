package org.model;

import java.time.LocalDate;

public class DietTracking {
    private Integer tracking_id;
    private Patient patient;
    private Diet diet;
    private LocalDate takip_tarihi;
    private Boolean uygulandi_mi;

    // Constructors
    public DietTracking() {
        this.takip_tarihi = LocalDate.now();
    }

    public DietTracking(Patient patient, Diet diet, Boolean uygulandi_mi) {
        this.patient = patient;
        this.diet = diet;
        this.takip_tarihi = LocalDate.now();
        this.uygulandi_mi = uygulandi_mi;
    }

    // Getters and Setters
    public Integer getTracking_id() {
        return tracking_id;
    }

    public void setTracking_id(Integer tracking_id) {
        this.tracking_id = tracking_id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Diet getDiet() {
        return diet;
    }

    public void setDiet(Diet diet) {
        this.diet = diet;
    }

    public LocalDate getTakip_tarihi() {
        return takip_tarihi;
    }

    public void setTakip_tarihi(LocalDate takip_tarihi) {
        this.takip_tarihi = takip_tarihi;
    }

    public Boolean getUygulandi_mi() {
        return uygulandi_mi;
    }

    public void setUygulandi_mi(Boolean uygulandi_mi) {
        this.uygulandi_mi = uygulandi_mi;
    }

    @Override
    public String toString() {
        return "DietTracking{" +
                "tracking_id=" + tracking_id +
                ", patient=" + (patient != null ? patient.getAd() + " " + patient.getSoyad() : "Bilinmeyen") +
                ", diet=" + (diet != null ? diet.getDiet_adi() : "Bilinmeyen") +
                ", takip_tarihi=" + takip_tarihi +
                ", uygulandı_mı=" + uygulandi_mi +
                '}';
    }
}