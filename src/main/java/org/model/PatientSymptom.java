package org.model;

import org.util.DateTimeUtil;

import java.time.LocalDate;

/**
 * Hasta ve belirtisi arasındaki ilişkiyi temsil eden sınıf
 */
public class PatientSymptom {
    private Integer patient_symptom_id;
    private Integer patient_id;
    private Integer symptom_id;
    private LocalDate belirtilme_tarihi;

    // Lazy loading için
    private Patient patient;
    private Symptom symptom;

    // Constructors
    public PatientSymptom() {
        this.belirtilme_tarihi = DateTimeUtil.getCurrentDate();
    }

    public PatientSymptom(Integer patient_id, Integer symptom_id, LocalDate belirtilme_tarihi) {
        this.patient_id = patient_id;
        this.symptom_id = symptom_id;
        this.belirtilme_tarihi = belirtilme_tarihi != null ? belirtilme_tarihi : DateTimeUtil.getCurrentDate();
    }

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
        this.patient_id = patient != null ? patient.getPatient_id() : null;
    }

    public Symptom getSymptom() {
        return symptom;
    }

    public void setSymptom(Symptom symptom) {
        this.symptom = symptom;
        this.symptom_id = symptom != null ? symptom.getSymptom_id() : null;
    }

    @Override
    public String toString() {
        return "PatientSymptom{" +
                "patient_symptom_id=" + patient_symptom_id +
                ", patient_id=" + patient_id +
                ", symptom_id=" + symptom_id +
                ", belirtilme_tarihi=" + belirtilme_tarihi +
                '}';
    }
}