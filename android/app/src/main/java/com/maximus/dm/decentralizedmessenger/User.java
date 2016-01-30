package com.maximus.dm.decentralizedmessenger;

/**
 * Created by Maximus on 29/01/2016.
 */
public class User {

    String username, email, password;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
