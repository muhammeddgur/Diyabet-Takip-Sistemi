package org.model;

import java.time.LocalDate;

public class PatientExercise {
    private Integer patientExerciseId;
    private Integer patientId;
    private Integer exerciseId;
    private Integer doctorId;
    private LocalDate baslangicTarihi;

    public PatientExercise() {
    }

    public PatientExercise(Integer patientId, Integer exerciseId, Integer doctorId, LocalDate baslangicTarihi) {
        this.patientId = patientId;
        this.exerciseId = exerciseId;
        this.doctorId = doctorId;
        this.baslangicTarihi = baslangicTarihi;
    }

    public Integer getPatientExerciseId() {
        return patientExerciseId;
    }

    public void setPatientExerciseId(Integer patientExerciseId) {
        this.patientExerciseId = patientExerciseId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Integer exerciseId) {
        this.exerciseId = exerciseId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getBaslangicTarihi() {
        return baslangicTarihi;
    }

    public void setBaslangicTarihi(LocalDate baslangicTarihi) {
        this.baslangicTarihi = baslangicTarihi;
    }

    @Override
    public String toString() {
        return "PatientDiet{" +
                "patientExerciseId=" + patientExerciseId +
                ", patientId=" + patientId +
                ", exerciseId=" + exerciseId +
                ", doctorId=" + doctorId +
                ", baslangicTarihi=" + baslangicTarihi +
                '}';
    }
}
