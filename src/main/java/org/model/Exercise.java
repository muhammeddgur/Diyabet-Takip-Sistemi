package org.model;

public class Exercise {
    private Integer exercise_id;
    private String exercise_adi;
    private String aciklama;

    // Constructors
    public Exercise() {
    }

    public Exercise(String exercise_adi, String aciklama) {
        this.exercise_adi = exercise_adi;
        this.aciklama = aciklama;
    }

    // Getters and Setters
    public Integer getExercise_id() {
        return exercise_id;
    }

    public void setExercise_id(Integer exercise_id) {
        this.exercise_id = exercise_id;
    }

    public String getExercise_adi() {
        return exercise_adi;
    }

    public void setExercise_adi(String exercise_adi) {
        this.exercise_adi = exercise_adi;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "exercise_id=" + exercise_id +
                ", exercise_adi='" + exercise_adi + '\'' +
                ", aciklama='" + aciklama + '\'' +
                '}';
    }
}