package com.maximus.dm.decentralizedmessenger.User;

/**
 * Created by Maximus on 29/01/2016.
 */
public class User {
    // user id fields
    int userId;
    String username;

    public User(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUsername() { return username; }
    public int getUserId() { return userId; }
}
