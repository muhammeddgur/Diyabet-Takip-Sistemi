package org.model;

import java.util.ArrayList;
import java.util.List;

public class Doctor extends User {
    private Integer doctor_id;
    private List<Patient> patients;

    // Constructors
    public Doctor() {
        super();
        this.patients = new ArrayList<>();
        setKullanici_tipi("doktor");
    }

    public Doctor(User user) {
        super(user.getTc_kimlik(), user.getPassword(), user.getEmail(), user.getAd(),
                user.getSoyad(), user.getDogum_tarihi(), user.getCinsiyet(), "doktor");
        this.setUser_id(user.getUser_id());
        this.setProfil_resmi(user.getProfil_resmi());
        this.setCreated_at(user.getCreated_at());
        this.setLast_login(user.getLast_login());
        this.patients = new ArrayList<>();
    }

    // Getters and Setters
    public Integer getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(Integer doctor_id) {
        this.doctor_id = doctor_id;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }

    // Helper methods
    public void addPatient(Patient patient) {
        if (!patients.contains(patient)) {
            patients.add(patient);
            patient.setDoctor(this);
        }
    }

    public void removePatient(Patient patient) {
        if (patients.contains(patient)) {
            patients.remove(patient);
            patient.setDoctor(null);
        }
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctor_id=" + doctor_id +
                ", user_id=" + getUser_id() +
                ", ad='" + getAd() + '\'' +
                ", soyad='" + getSoyad() + '\'' +
                ", patientCount=" + (patients != null ? patients.size() : 0) +
                '}';
    }
}