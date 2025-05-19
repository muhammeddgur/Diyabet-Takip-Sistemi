package org.model;

public class BloodSugarCategory {
    private Integer category_id;
    private String category_name;
    private Integer min_value;
    private Integer max_value;
    private String description;
    private String alert_level;
    private String color_code;

    // Constructors
    public BloodSugarCategory() {
    }

    public BloodSugarCategory(String category_name, Integer min_value, Integer max_value,
                              String description, String alert_level, String color_code) {
        this.category_name = category_name;
        this.min_value = min_value;
        this.max_value = max_value;
        this.description = description;
        this.alert_level = alert_level;
        this.color_code = color_code;
    }

    // Getters and Setters
    public Integer getCategory_id() {
        return category_id;
    }

    public void setCategory_id(Integer category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public Integer getMin_value() {
        return min_value;
    }

    public void setMin_value(Integer min_value) {
        this.min_value = min_value;
    }

    public Integer getMax_value() {
        return max_value;
    }

    public void setMax_value(Integer max_value) {
        this.max_value = max_value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAlert_level() {
        return alert_level;
    }

    public void setAlert_level(String alert_level) {
        this.alert_level = alert_level;
    }

    public String getColor_code() {
        return color_code;
    }

    public void setColor_code(String color_code) {
        this.color_code = color_code;
    }

    // Bir ölçüm değerinin bu kategoriye ait olup olmadığını kontrol eden yardımcı metod
    public boolean containsValue(Integer value) {
        return value >= min_value && value <= max_value;
    }

    @Override
    public String toString() {
        return "BloodSugarCategory{" +
                "category_id=" + category_id +
                ", category_name='" + category_name + '\'' +
                ", min_value=" + min_value +
                ", max_value=" + max_value +
                ", description='" + description + '\'' +
                ", alert_level='" + alert_level + '\'' +
                ", color_code='" + color_code + '\'' +
                '}';
    }
}