package org.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class BloodSugarMeasurement {
    private Integer measurement_id;
    private Integer patient_id;
    private Patient patient;
    private Integer olcum_degeri;
    private String olcum_zamani;
    private LocalDateTime olcum_tarihi;
    private Double insulin_miktari;
    private Boolean is_valid_time;
    private Integer category_id; // Kategori ID eklendi

    // Constructors
    public BloodSugarMeasurement() {
        this.is_valid_time = true; // Varsayılan olarak true
    }

    public BloodSugarMeasurement(Patient patient, Integer olcum_degeri, String olcum_zamani) {
        this.patient = patient;
        this.patient_id = patient != null ? patient.getPatient_id() : null;
        this.olcum_degeri = olcum_degeri;
        this.olcum_zamani = olcum_zamani;
        this.olcum_tarihi = LocalDateTime.now();
        this.is_valid_time = validateMeasurementTime(this.olcum_zamani, this.olcum_tarihi.toLocalTime());
    }

    // Getters and Setters
    public Integer getMeasurement_id() {
        return measurement_id;
    }

    public void setMeasurement_id(Integer measurement_id) {
        this.measurement_id = measurement_id;
    }

    public Integer getPatient_id() {
        return patient_id;
    }
    public void setPatient_id(Integer patient_id) {
        this.patient_id = patient_id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        this.patient_id = patient != null ? patient.getPatient_id() : null;
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
        String timekey = "";
        // Olçum zamanının geçerli olduğundan emin olalım
        if(olcum_zamani.contains("Sabah"))
            timekey = "sabah";
        else if(olcum_zamani.contains("Öğle"))
            timekey = "ogle";
        else if(olcum_zamani.contains("İkindi"))
            timekey = "ikindi";
        else if(olcum_zamani.contains("Akşam"))
            timekey = "aksam";
        else if(olcum_zamani.contains("Gece"))
            timekey = "gece";
        else
            throw new IllegalArgumentException("Geçersiz ölçüm zamanı. Geçerli değerler: sabah, ogle, ikindi, aksam, gece");

        this.olcum_zamani = timekey;

        // Zaman değiştiğinde is_valid_time'ı da güncelleyelim
        if (this.olcum_tarihi != null) {
            this.is_valid_time = validateMeasurementTime(timekey, this.olcum_tarihi.toLocalTime());
        }
    }

    public LocalDateTime getOlcum_tarihi() {
        return olcum_tarihi;
    }

    public void setOlcum_tarihi(LocalDateTime olcum_tarihi) {
        this.olcum_tarihi = olcum_tarihi;

        // Tarih değiştiğinde is_valid_time'ı da güncelleyelim
        if (this.olcum_zamani != null) {
            this.is_valid_time = validateMeasurementTime(this.olcum_zamani, olcum_tarihi.toLocalTime());
        }
    }

    public Double getInsulin_miktari() {
        return insulin_miktari;
    }

    public void setInsulin_miktari(Double insulin_miktari) {
        this.insulin_miktari = insulin_miktari;
    }

    public Boolean getIs_valid_time() {
        return is_valid_time;
    }

    public void setIs_valid_time(Boolean is_valid_time) {
        this.is_valid_time = is_valid_time;
    }

    public Integer getCategory_id() {
        return category_id;
    }

    public void setCategory_id(Integer category_id) {
        this.category_id = category_id;
    }

    /**
     * Ölçüm saatinin, belirtilen zaman aralığı için geçerli olup olmadığını kontrol eder
     * @param timeKey Ölçüm zamanı (sabah, ogle, ikindi, aksam, gece)
     * @param time Ölçüm saati
     * @return Geçerli ise true, değilse false
     */
    private Boolean validateMeasurementTime(String timeKey, LocalTime time) {
        switch (timeKey) {
            case "sabah":
                return time.isAfter(LocalTime.of(7, 0)) && time.isBefore(LocalTime.of(8, 0));
            case "ogle":
                return time.isAfter(LocalTime.of(12, 0)) && time.isBefore(LocalTime.of(13, 0));
            case "ikindi":
                return time.isAfter(LocalTime.of(15, 0)) && time.isBefore(LocalTime.of(16, 0));
            case "aksam":
                return time.isAfter(LocalTime.of(18, 0)) && time.isBefore(LocalTime.of(19, 0));
            case "gece":
                return time.isAfter(LocalTime.of(22, 0)) && time.isBefore(LocalTime.of(23, 0));
            default:
                return false;
        }
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
                ", is_valid_time=" + is_valid_time +
                ", category_id=" + category_id +
                '}';
    }
}