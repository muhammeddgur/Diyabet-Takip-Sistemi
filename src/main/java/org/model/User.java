package org.model;

public class User {
    private String tcKimlik;
    private String email;
    private String passwordHash;
    private String role;

    public User(String tcKimlik, String email, String passwordHash, String role) {
        this.tcKimlik = tcKimlik;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters and setters
    public String getTcKimlik() { return tcKimlik; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
}