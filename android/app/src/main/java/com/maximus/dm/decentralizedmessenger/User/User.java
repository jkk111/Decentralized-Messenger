package com.maximus.dm.decentralizedmessenger.User;

/**
 * Created by Maximus on 29/01/2016.
 */
public class User {

    String username, email;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

}
