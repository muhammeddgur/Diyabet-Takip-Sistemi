package org.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): 2025-05-14 15:39:15
 * Current User's Login: Emirhan-Karabulut
 *
 * Kan şekeri ölçümü bilgilerini tutan model sınıfı
 */
public class BloodSugarMeasurement {
    private Integer measurementId;
    private Integer patientId;
    private BigDecimal measurementValue;
    private LocalDateTime measurementTime;
    private String measurementType; // "MORNING", "NOON", "AFTERNOON", "EVENING", "NIGHT"
    private boolean isValidTime; // Ölçüm saatinin geçerli saat aralığında olup olmadığı
    private String notes;
    private LocalDateTime createdAt;
    private Patient patient; // İlişki için referans

    // Constructors
    public BloodSugarMeasurement() {}

    public BloodSugarMeasurement(Integer measurementId, Integer patientId, BigDecimal measurementValue,
                                 LocalDateTime measurementTime, String measurementType,
                                 boolean isValidTime, String notes) {
        this.measurementId = measurementId;
        this.patientId = patientId;
        this.measurementValue = measurementValue;
        this.measurementTime = measurementTime;
        this.measurementType = measurementType;
        this.isValidTime = isValidTime;
        this.notes = notes;
    }

    // Getters and Setters
    public Integer getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(Integer measurementId) {
        this.measurementId = measurementId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public BigDecimal getMeasurementValue() {
        return measurementValue;
    }

    public void setMeasurementValue(BigDecimal measurementValue) {
        this.measurementValue = measurementValue;
    }

    public LocalDateTime getMeasurementTime() {
        return measurementTime;
    }

    public void setMeasurementTime(LocalDateTime measurementTime) {
        this.measurementTime = measurementTime;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(String measurementType) {
        this.measurementType = measurementType;
    }

    public boolean isValidTime() {
        return isValidTime;
    }

    public void setValidTime(boolean validTime) {
        isValidTime = validTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @Override
    public String toString() {
        return "BloodSugarMeasurement{" +
                "measurementId=" + measurementId +
                ", patientId=" + patientId +
                ", measurementValue=" + measurementValue +
                ", measurementTime=" + measurementTime +
                ", measurementType='" + measurementType + '\'' +
                ", isValidTime=" + isValidTime +
                ", notes='" + notes + '\'' +
                '}';
    }

    /**
     * Ölçüm değerini mg/dL olarak döndürür
     */
    public String getValueWithUnit() {
        return measurementValue + " mg/dL";
    }

    /**
     * Ölçüm değerine göre durumu döndürür
     */
    public String getStatus() {
        double value = measurementValue.doubleValue();

        if (value < 70) {
            return "Hipoglisemi";
        } else if (value <= 110) {
            return "Normal";
        } else if (value <= 150) {
            return "Orta Yüksek";
        } else if (value <= 200) {
            return "Yüksek";
        } else {
            return "Çok Yüksek (Hiperglisemi)";
        }
    }
}