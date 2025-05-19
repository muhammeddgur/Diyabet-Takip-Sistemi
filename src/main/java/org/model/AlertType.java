package org.model;

public class AlertType {
    private Integer alert_type_id;
    private String tip_adi;
    private String aciklama;

    // Constructors
    public AlertType() {
    }

    public AlertType(String tip_adi, String aciklama) {
        this.tip_adi = tip_adi;
        this.aciklama = aciklama;
    }

    // Getters and Setters
    public Integer getAlert_type_id() {
        return alert_type_id;
    }

    public void setAlert_type_id(Integer alert_type_id) {
        this.alert_type_id = alert_type_id;
    }

    public String getTip_adi() {
        return tip_adi;
    }

    public void setTip_adi(String tip_adi) {
        this.tip_adi = tip_adi;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    @Override
    public String toString() {
        return "AlertType{" +
                "alert_type_id=" + alert_type_id +
                ", tip_adi='" + tip_adi + '\'' +
                ", aciklama='" + aciklama + '\'' +
                '}';
    }
}