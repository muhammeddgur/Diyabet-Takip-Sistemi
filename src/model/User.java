package model;

public class User {
    private int id;
    private String tcKimlik;
    private String email;
    private String passwordHash;
    private String role; // "DOCTOR" veya "PATIENT"

    // Constructor
    public User(int id, String tcKimlik, String email, String passwordHash, String role) {
        this.id = id;
        this.tcKimlik = tcKimlik;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getId() { return id; }
    public String getTcKimlik() { return tcKimlik; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
}
