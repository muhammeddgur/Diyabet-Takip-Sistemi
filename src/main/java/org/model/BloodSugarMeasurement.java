package org.model;

import java.time.LocalDateTime;

public class BloodSugarMeasurement {
    private Integer measurement_id;
    private Patient patient;
    private Integer olcum_degeri;
    private String olcum_zamani;
    private LocalDateTime olcum_tarihi;
    private Double insulin_miktari;

    // Constructors
    public BloodSugarMeasurement() {
    }

    public BloodSugarMeasurement(Patient patient, Integer olcum_degeri, String olcum_zamani) {
        this.patient = patient;
        this.olcum_degeri = olcum_degeri;
        this.olcum_zamani = olcum_zamani;
        this.olcum_tarihi = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getMeasurement_id() {
        return measurement_id;
    }

    public void setMeasurement_id(Integer measurement_id) {
        this.measurement_id = measurement_id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Integer getOlcum_degeri() {
        return olcum_degeri;
    }

    public void setOlcum_degeri(Integer olcum_degeri) {
        this.olcum_degeri = olcum_degeri;
    }

    public String getOlcum_zamani() {
        return olcum_zamani;
    }

    public void setOlcum_zamani(String olcum_zamani) {
        // Olçum zamanının geçerli olduğundan emin olalım
        if (!olcum_zamani.equals("sabah") && !olcum_zamani.equals("ogle") &&
                !olcum_zamani.equals("ikindi") && !olcum_zamani.equals("aksam") &&
                !olcum_zamani.equals("gece")) {
            throw new IllegalArgumentException("Geçersiz ölçüm zamanı. Geçerli değerler: sabah, ogle, ikindi, aksam, gece");
        }
        this.olcum_zamani = olcum_zamani;
    }

    public LocalDateTime getOlcum_tarihi() {
        return olcum_tarihi;
    }

    public void setOlcum_tarihi(LocalDateTime olcum_tarihi) {
        this.olcum_tarihi = olcum_tarihi;
    }

    public Double getInsulin_miktari() {
        return insulin_miktari;
    }

    public void setInsulin_miktari(Double insulin_miktari) {
        this.insulin_miktari = insulin_miktari;
    }

    @Override
    public String toString() {
        return "BloodSugarMeasurement{" +
                "measurement_id=" + measurement_id +
                ", patient=" + (patient != null ? patient.getAd() + " " + patient.getSoyad() : "Bilinmeyen") +
                ", olcum_degeri=" + olcum_degeri +
                ", olcum_zamani='" + olcum_zamani + '\'' +
                ", olcum_tarihi=" + olcum_tarihi +
                ", insulin_miktari=" + insulin_miktari +
                '}';
    }
}