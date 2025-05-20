package org.model;

import java.util.ArrayList;
import java.util.List;

public class Patient extends User {
    private Integer patient_id;
    private Doctor doctor;
    private List<BloodSugarMeasurement> measurements;
    private List<Diet> dietPlans;
    private List<Exercise> exercises;
    private List<Symptom> symptoms;

    // Constructors
    public Patient() {
        super();
        this.measurements = new ArrayList<>();
        this.dietPlans = new ArrayList<>();
        this.exercises = new ArrayList<>();
        this.symptoms = new ArrayList<>();
        setKullanici_tipi("hasta");
    }

    public Patient(User user) {
        super(user.getTc_kimlik(), user.getPassword(), user.getEmail(), user.getAd(),
                user.getSoyad(), user.getDogum_tarihi(), user.getCinsiyet(), "hasta");
        this.setUser_id(user.getUser_id());
        this.setProfil_resmi(user.getProfil_resmi());
        this.setCreated_at(user.getCreated_at());
        this.setLast_login(user.getLast_login());
        this.measurements = new ArrayList<>();
        this.dietPlans = new ArrayList<>();
        this.exercises = new ArrayList<>();
        this.symptoms = new ArrayList<>();
    }

    // Getters and Setters
    public Integer getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(Integer patient_id) {
        this.patient_id = patient_id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public List<BloodSugarMeasurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<BloodSugarMeasurement> measurements) {
        this.measurements = measurements;
    }

    public List<Diet> getDietPlans() {
        return dietPlans;
    }

    public void setDietPlans(List<Diet> dietPlans) {
        this.dietPlans = dietPlans;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public List<Symptom> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<Symptom> symptoms) {
        this.symptoms = symptoms;
    }

    // Helper methods
    public void addMeasurement(BloodSugarMeasurement measurement) {
        if (!measurements.contains(measurement)) {
            measurements.add(measurement);
            measurement.setPatient(this);
        }
    }

    public void addDiet(Diet diet) {
        if (!dietPlans.contains(diet)) {
            dietPlans.add(diet);
        }
    }

    public void addExercise(Exercise exercise) {
        if (!exercises.contains(exercise)) {
            exercises.add(exercise);
        }
    }

    public void addSymptom(Symptom symptom) {
        if (!symptoms.contains(symptom)) {
            symptoms.add(symptom);
        }
    }

    @Override
    public String toString() {
        return getAd() + " " + getSoyad();
    }
}