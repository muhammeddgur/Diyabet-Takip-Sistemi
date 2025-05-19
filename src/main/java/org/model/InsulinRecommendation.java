package org.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InsulinRecommendation {
    private Integer recommendation_id;
    private Patient patient;
    private LocalDate recommendation_date;
    private Double averageValue;
    private Integer measuredCount;
    private Double recommendedInsulin;
    private Boolean applied;
    private LocalDateTime created_at;

    // Constructors
    public InsulinRecommendation() {
        this.created_at = LocalDateTime.now();
        this.applied = false;
    }

    public InsulinRecommendation(Patient patient, Double averageValue, Integer measuredCount, Double recommendedInsulin) {
        this.patient = patient;
        this.recommendation_date = LocalDate.now();
        this.averageValue = averageValue;
        this.measuredCount = measuredCount;
        this.recommendedInsulin = recommendedInsulin;
        this.created_at = LocalDateTime.now();
        this.applied = false;
    }

    // Getters and Setters
    public Integer getRecommendation_id() {
        return recommendation_id;
    }

    public void setRecommendation_id(Integer recommendation_id) {
        this.recommendation_id = recommendation_id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public LocalDate getRecommendation_date() {
        return recommendation_date;
    }

    public void setRecommendation_date(LocalDate recommendation_date) {
        this.recommendation_date = recommendation_date;
    }

    public Double getAverageValue() {
        return averageValue;
    }

    public void setAverageValue(Double averageValue) {
        this.averageValue = averageValue;
    }

    public Integer getMeasuredCount() {
        return measuredCount;
    }

    public void setMeasuredCount(Integer measuredCount) {
        this.measuredCount = measuredCount;
    }

    public Double getRecommendedInsulin() {
        return recommendedInsulin;
    }

    public void setRecommendedInsulin(Double recommendedInsulin) {
        this.recommendedInsulin = recommendedInsulin;
    }

    public Boolean getApplied() {
        return applied;
    }

    public void setApplied(Boolean applied) {
        this.applied = applied;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "InsulinRecommendation{" +
                "recommendation_id=" + recommendation_id +
                ", patient=" + (patient != null ? patient.getAd() + " " + patient.getSoyad() : "Bilinmeyen") +
                ", recommendation_date=" + recommendation_date +
                ", averageValue=" + averageValue +
                ", measuredCount=" + measuredCount +
                ", recommendedInsulin=" + recommendedInsulin +
                ", applied=" + applied +
                ", created_at=" + created_at +
                '}';
    }
}