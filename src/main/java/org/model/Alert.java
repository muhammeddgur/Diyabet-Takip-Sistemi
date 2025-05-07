package org.model;

import java.time.LocalDateTime;

public class Alert {
    private int id;
    private Patient patient;
    private String type;
    private String message;
    private LocalDateTime createdAt;

    public Alert(int id, Patient patient, String type, String message, LocalDateTime createdAt) {
        this.id = id;
        this.patient = patient;
        this.type = type;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Patient getPatient() {
        return patient;
    }
}
