package model;

import java.time.LocalDateTime;

public class BloodSugarMeasurement {
    private int id;
    private Patient patient;
    private double value;
    private LocalDateTime measuredAt;

    public BloodSugarMeasurement(int id, Patient patient, double value, LocalDateTime measuredAt) {
        this.id = id;
        this.patient = patient;
        this.value = value;
        this.measuredAt = measuredAt;
    }

    public Patient getPatient() {
        return patient;
    }

    public double getValue() {
        return value;
    }

    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }
}
