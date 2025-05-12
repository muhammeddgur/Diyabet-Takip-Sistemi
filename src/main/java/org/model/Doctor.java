package org.model;

public class Doctor {
    private int id;
    private User user;

    public Doctor(int id, User user) {
        this.id = id;
        this.user = user;
    }

    // Default constructor for new doctors (ID will be set after DB insert)
    public Doctor(User user) {
        this.user = user;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}