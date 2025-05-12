package org.model;

import java.time.LocalDate;

public class Patient {
    private int id;
    private User user;
    private Doctor doctor;
    private LocalDate createdAt;

    public Patient(int id, User user, Doctor doctor, LocalDate createdAt) {
        this.id = id;
        this.user = user;
        this.doctor = doctor;
        this.createdAt = createdAt;
    }

    // Getter metotları
    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setId(int id) {
        this.id = id;
    }
}