package org.model;

import java.time.LocalDate;

public class Patient {
    private int id;
    private User user;       // ilişkilendirilmiş User
    private Doctor doctor;   // tanı koyan doktor
    private LocalDate createdAt;

    public Patient(int id, User user, Doctor doctor, LocalDate createdAt) {
        this.id = id;
        this.user = user;
        this.doctor = doctor;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }
}
