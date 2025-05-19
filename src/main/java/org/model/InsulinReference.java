package org.model;

public class InsulinReference {
    private Integer reference_id;
    private Integer min_blood_sugar;
    private Integer max_blood_sugar;
    private Double insulin_dose;
    private String description;

    // Constructors
    public InsulinReference() {
    }

    public InsulinReference(Integer min_blood_sugar, Integer max_blood_sugar, Double insulin_dose, String description) {
        this.min_blood_sugar = min_blood_sugar;
        this.max_blood_sugar = max_blood_sugar;
        this.insulin_dose = insulin_dose;
        this.description = description;
    }

    // Getters and Setters
    public Integer getReference_id() {
        return reference_id;
    }

    public void setReference_id(Integer reference_id) {
        this.reference_id = reference_id;
    }

    public Integer getMin_blood_sugar() {
        return min_blood_sugar;
    }

    public void setMin_blood_sugar(Integer min_blood_sugar) {
        this.min_blood_sugar = min_blood_sugar;
    }

    public Integer getMax_blood_sugar() {
        return max_blood_sugar;
    }

    public void setMax_blood_sugar(Integer max_blood_sugar) {
        this.max_blood_sugar = max_blood_sugar;
    }

    public Double getInsulin_dose() {
        return insulin_dose;
    }

    public void setInsulin_dose(Double insulin_dose) {
        this.insulin_dose = insulin_dose;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Bir kan şekeri değeri için önerilen insülin dozunu belirten yardımcı metod
    public boolean isApplicableFor(Integer bloodSugarValue) {
        return bloodSugarValue >= min_blood_sugar && bloodSugarValue <= max_blood_sugar;
    }

    @Override
    public String toString() {
        return "InsulinReference{" +
                "reference_id=" + reference_id +
                ", min_blood_sugar=" + min_blood_sugar +
                ", max_blood_sugar=" + max_blood_sugar +
                ", insulin_dose=" + insulin_dose +
                ", description='" + description + '\'' +
                '}';
    }
}