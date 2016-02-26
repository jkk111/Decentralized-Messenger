package com.maximus.dm.decentralizedmessenger.User;

/**
 * Created by Maximus on 26/02/2016.
 */
public class Friend extends User {

    boolean pending, initiatedBySelf;

    public Friend(int userId, String username, boolean pending, boolean initiatedBySelf) {
        super(userId, username);
        this.pending = pending;
        this.initiatedBySelf = initiatedBySelf;
    }

    // Getters / Setters
    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public boolean isInitiatedBySelf() {
        return initiatedBySelf;
    }

    public void setInitiatedBySelf(boolean initiatedBySelf) {
        this.initiatedBySelf = initiatedBySelf;
    }
}
