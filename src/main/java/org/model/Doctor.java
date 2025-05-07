package model;

public class Doctor {
    private int id;
    private User user;
    private String specialty;

    public Doctor(int id, User user, String specialty) {
        this.id = id;
        this.user = user;
        this.specialty = specialty;
    }
}
