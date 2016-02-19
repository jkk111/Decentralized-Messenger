package com.maximus.dm.decentralizedmessenger.User;

/**
 * Created by Maximus on 29/01/2016.
 */
public class User {

    String userId, username;

    public User(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public User(String username) {
        new User(null, username);
    }

    public String getUserId() { return userId; }

    public String getUsername() { return username; }

}
