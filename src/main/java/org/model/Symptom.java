package org.model;

public class Symptom {
    private Integer symptom_id;
    private String symptom_adi;
    private String aciklama;

    // Constructors
    public Symptom() {
    }

    public Symptom(String symptom_adi, String aciklama) {
        this.symptom_adi = symptom_adi;
        this.aciklama = aciklama;
    }

    // Getters and Setters
    public Integer getSymptom_id() {
        return symptom_id;
    }

    public void setSymptom_id(Integer symptom_id) {
        this.symptom_id = symptom_id;
    }

    public String getSymptom_adi() {
        return symptom_adi;
    }

    public void setSymptom_adi(String symptom_adi) {
        this.symptom_adi = symptom_adi;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    @Override
    public String toString() {
        return "Symptom{" +
                "symptom_id=" + symptom_id +
                ", symptom_adi='" + symptom_adi + '\'' +
                ", aciklama='" + aciklama + '\'' +
                '}';
    }
}