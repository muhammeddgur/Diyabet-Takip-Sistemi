package org.model;

import java.time.LocalDateTime;

public class Alert {
    private Integer alertId;
    private Integer patientId;
    private String alertType;
    private String alertMessage;
    private boolean isRead;
    private boolean isUrgent;
    private LocalDateTime createdAt;
    private Patient patient; // İlişki için referans

    // Constructors
    public Alert() {}

    public Alert(Integer alertId, Integer patientId, String alertType, String alertMessage,
                 boolean isRead, boolean isUrgent) {
        this.alertId = alertId;
        this.patientId = patientId;
        this.alertType = alertType;
        this.alertMessage = alertMessage;
        this.isRead = isRead;
        this.isUrgent = isUrgent;
    }

    // Getters and Setters
    public Integer getAlertId() {
        return alertId;
    }

    public void setAlertId(Integer alertId) {
        this.alertId = alertId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
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
        return "Alert{" +
                "alertId=" + alertId +
                ", patientId=" + patientId +
                ", alertType='" + alertType + '\'' +
                ", alertMessage='" + alertMessage + '\'' +
                ", isRead=" + isRead +
                ", isUrgent=" + isUrgent +
                ", createdAt=" + createdAt +
                '}';
    }
}