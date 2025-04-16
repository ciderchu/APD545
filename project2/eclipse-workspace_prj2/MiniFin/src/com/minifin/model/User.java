package com.minifin.model;

public class User {
    private final long id;
    private final String username;
    private final String password;
    private final String email;

    public User(long id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }
    
    public User(String username, String password, String email) {
        this(-1, username, password, email);
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}