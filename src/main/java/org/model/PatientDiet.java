package org.model;

import java.time.LocalDate;

/**
 * Hasta-Diyet ilişki model sınıfı.
 */
public class PatientDiet {

    private Integer patientDietId;
    private Integer patientId;
    private Integer dietId;
    private Integer doctorId;
    private LocalDate baslangicTarihi;

    public PatientDiet() {
    }

    public PatientDiet(Integer patientId, Integer dietId, Integer doctorId, LocalDate baslangicTarihi) {
        this.patientId = patientId;
        this.dietId = dietId;
        this.doctorId = doctorId;
        this.baslangicTarihi = baslangicTarihi;
    }

    public Integer getPatientDietId() {
        return patientDietId;
    }

    public void setPatientDietId(Integer patientDietId) {
        this.patientDietId = patientDietId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getDietId() {
        return dietId;
    }

    public void setDietId(Integer dietId) {
        this.dietId = dietId;
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
                "patientDietId=" + patientDietId +
                ", patientId=" + patientId +
                ", dietId=" + dietId +
                ", doctorId=" + doctorId +
                ", baslangicTarihi=" + baslangicTarihi +
                '}';
    }
}
