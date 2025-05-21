package org.model;

import java.time.LocalDate;

public class PatientSymptom {
    private Integer patient_symptom_id;
    private Integer patient_id;
    private Integer symptom_id;
    private LocalDate belirtilme_tarihi;

    // İlişkili nesneler
    private Patient patient;
    private Symptom symptom;

    // Getters and Setters
    public Integer getPatient_symptom_id() {
        return patient_symptom_id;
    }

    public void setPatient_symptom_id(Integer patient_symptom_id) {
        this.patient_symptom_id = patient_symptom_id;
    }

    public Integer getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(Integer patient_id) {
        this.patient_id = patient_id;
    }

    public Integer getSymptom_id() {
        return symptom_id;
    }

    public void setSymptom_id(Integer symptom_id) {
        this.symptom_id = symptom_id;
    }

    public LocalDate getBelirtilme_tarihi() {
        return belirtilme_tarihi;
    }

    public void setBelirtilme_tarihi(LocalDate belirtilme_tarihi) {
        this.belirtilme_tarihi = belirtilme_tarihi;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            this.patient_id = patient.getPatient_id();
        }
    }

    public Symptom getSymptom() {
        return symptom;
    }

    public void setSymptom(Symptom symptom) {
        this.symptom = symptom;
        if (symptom != null) {
            this.symptom_id = symptom.getSymptom_id();
        }
    }
}