package org.model;

import java.time.LocalDate;

public class ExerciseTracking {
    private Integer tracking_id;
    private Patient patient;
    private Exercise exercise;
    private LocalDate takip_tarihi;
    private Boolean yapildi_mi;

    // Constructors
    public ExerciseTracking() {
        this.takip_tarihi = LocalDate.now();
    }

    public ExerciseTracking(Patient patient, Exercise exercise, Boolean yapildi_mi) {
        this.patient = patient;
        this.exercise = exercise;
        this.takip_tarihi = LocalDate.now();
        this.yapildi_mi = yapildi_mi;
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

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public LocalDate getTakip_tarihi() {
        return takip_tarihi;
    }

    public void setTakip_tarihi(LocalDate takip_tarihi) {
        this.takip_tarihi = takip_tarihi;
    }

    public Boolean getYapildi_mi() {
        return yapildi_mi;
    }

    public void setYapildi_mi(Boolean yapildi_mi) {
        this.yapildi_mi = yapildi_mi;
    }

    @Override
    public String toString() {
        return "ExerciseTracking{" +
                "tracking_id=" + tracking_id +
                ", patient=" + (patient != null ? patient.getAd() + " " + patient.getSoyad() : "Bilinmeyen") +
                ", exercise=" + (exercise != null ? exercise.getExercise_adi() : "Bilinmeyen") +
                ", takip_tarihi=" + takip_tarihi +
                ", yapildı_mı=" + yapildi_mi +
                '}';
    }
}