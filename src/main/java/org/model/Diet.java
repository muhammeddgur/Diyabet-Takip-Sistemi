package org.model;

public class Diet {
    private Integer diet_id;
    private String diet_adi;
    private String aciklama;

    // Constructors
    public Diet() {
    }

    public Diet(String diet_adi, String aciklama) {
        this.diet_adi = diet_adi;
        this.aciklama = aciklama;
    }

    // Getters and Setters
    public Integer getDiet_id() {
        return diet_id;
    }

    public void setDiet_id(Integer diet_id) {
        this.diet_id = diet_id;
    }

    public String getDiet_adi() {
        return diet_adi;
    }

    public void setDiet_adi(String diet_adi) {
        this.diet_adi = diet_adi;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    @Override
    public String toString() {
        return diet_adi;
    }
}