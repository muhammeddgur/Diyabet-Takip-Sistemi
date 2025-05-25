package org.model;

import java.time.LocalDate;

public class DietTracking {
    private Integer tracking_id;
    private Integer patient_diet_id;
    private LocalDate takip_tarihi;
    private Boolean uygulandi_mi;

    // Constructors
    public DietTracking() {
        this.takip_tarihi = LocalDate.now();
    }

    public DietTracking(Integer patient_diet_id, Boolean uygulandi_mi) {
        this.patient_diet_id = patient_diet_id;
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

    public Integer getPatient_diet_id() {
        return patient_diet_id;
    }

    public void setPatient_diet_id(Integer patient_diet_id) {
        this.patient_diet_id = patient_diet_id;
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
                ", takip_tarihi=" + takip_tarihi +
                ", uygulandı_mı=" + uygulandi_mi +
                '}';
    }
}