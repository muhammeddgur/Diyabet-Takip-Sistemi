package org.model;

import java.time.LocalDate;

/**
 * Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): 2025-05-14 15:56:25
 * Current User's Login: Emirhan-Karabulut
 *
 * Hasta bilgilerini tutan model sınıfı
 * height ve weight alanları kaldırıldı
 */
public class Patient {
    private Integer patientId;
    private Integer userId;
    private Integer doctorId;
    private LocalDate diagnosisDate;
    private String diabetesType;
    private String notes;
    private User user; // İlişki için referans
    private Doctor doctor; // İlişki için referans

    // Constructors
    public Patient() {}

    public Patient(Integer patientId, Integer userId, Integer doctorId, LocalDate diagnosisDate,
                   String diabetesType, String notes) {
        this.patientId = patientId;
        this.userId = userId;
        this.doctorId = doctorId;
        this.diagnosisDate = diagnosisDate;
        this.diabetesType = diabetesType;
        this.notes = notes;
    }

    // Getters and Setters
    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getDiagnosisDate() {
        return diagnosisDate;
    }

    public void setDiagnosisDate(LocalDate diagnosisDate) {
        this.diagnosisDate = diagnosisDate;
    }

    public String getDiabetesType() {
        return diabetesType;
    }

    public void setDiabetesType(String diabetesType) {
        this.diabetesType = diabetesType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", userId=" + userId +
                ", doctorId=" + doctorId +
                ", diagnosisDate=" + diagnosisDate +
                ", diabetesType='" + diabetesType + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}