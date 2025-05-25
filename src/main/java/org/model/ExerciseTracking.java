package org.model;

import java.time.LocalDate;

public class ExerciseTracking {
    private Integer tracking_id;
    private Integer patient_exercise_id;
    private LocalDate takip_tarihi;
    private Boolean uygulandi_mi;

    // Constructors
    public ExerciseTracking() {
        this.takip_tarihi = LocalDate.now();
    }

    public ExerciseTracking(Integer patient_exercise_id, Boolean uygulandi_mi) {
        this.patient_exercise_id = patient_exercise_id;
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

    public Integer getPatient_exercise_id() {
        return patient_exercise_id;
    }

    public void setPatient_exercise_id(Integer patient_exercise_id) {
        this.patient_exercise_id = patient_exercise_id;
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
        return "ExerciseTracking{" +
                "tracking_id=" + tracking_id +
                ", takip_tarihi=" + takip_tarihi +
                ", yapildı_mı=" + uygulandi_mi +
                '}';
    }
}