package org.model;

import java.util.ArrayList;
import java.util.List;

public class RecommendationRule {
    private Integer rule_id;
    private Integer minBloodSugar;
    private Integer maxBloodSugar;
    private List<Symptom> symptoms;
    private Diet recommendedDiet;
    private Exercise recommendedExercise;

    // Constructors
    public RecommendationRule() {
        this.symptoms = new ArrayList<>();
    }

    public RecommendationRule(Integer minBloodSugar, Integer maxBloodSugar, Diet recommendedDiet, Exercise recommendedExercise) {
        this.minBloodSugar = minBloodSugar;
        this.maxBloodSugar = maxBloodSugar;
        this.recommendedDiet = recommendedDiet;
        this.recommendedExercise = recommendedExercise;
        this.symptoms = new ArrayList<>();
    }

    // Getters and Setters
    public Integer getRule_id() {
        return rule_id;
    }

    public void setRule_id(Integer rule_id) {
        this.rule_id = rule_id;
    }

    public Integer getMinBloodSugar() {
        return minBloodSugar;
    }

    public void setMinBloodSugar(Integer minBloodSugar) {
        this.minBloodSugar = minBloodSugar;
    }

    public Integer getMaxBloodSugar() {
        return maxBloodSugar;
    }

    public void setMaxBloodSugar(Integer maxBloodSugar) {
        this.maxBloodSugar = maxBloodSugar;
    }

    public List<Symptom> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<Symptom> symptoms) {
        this.symptoms = symptoms;
    }

    public Diet getRecommendedDiet() {
        return recommendedDiet;
    }

    public void setRecommendedDiet(Diet recommendedDiet) {
        this.recommendedDiet = recommendedDiet;
    }

    public Exercise getRecommendedExercise() {
        return recommendedExercise;
    }

    public void setRecommendedExercise(Exercise recommendedExercise) {
        this.recommendedExercise = recommendedExercise;
    }

    // Helper methods
    public void addSymptom(Symptom symptom) {
        if (!symptoms.contains(symptom)) {
            symptoms.add(symptom);
        }
    }

    public boolean isApplicable(Integer bloodSugar) {
        return bloodSugar >= minBloodSugar && bloodSugar <= maxBloodSugar;
    }

    @Override
    public String toString() {
        return "RecommendationRule{" +
                "rule_id=" + rule_id +
                ", minBloodSugar=" + minBloodSugar +
                ", maxBloodSugar=" + maxBloodSugar +
                ", symptomsCount=" + symptoms.size() +
                ", recommendedDiet=" + (recommendedDiet != null ? recommendedDiet.getDiet_adi() : "Bilinmeyen") +
                ", recommendedExercise=" + (recommendedExercise != null ? recommendedExercise.getExercise_adi() : "Bilinmeyen") +
                '}';
    }
}