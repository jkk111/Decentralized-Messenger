package com.maximus.dm.decentralizedmessenger.User;

/**
 * Created by Maximus on 26/02/2016.
 */
public class Friend extends User {

    private int friendshipId;
    private boolean pending, initiatedBySelf;
    private String publicKey;

    public Friend(int friendshipId, int userId, String username, boolean pending, boolean initiatedBySelf) {
        super(userId, username);
        this.friendshipId = friendshipId;
        this.pending = pending;
        this.initiatedBySelf = initiatedBySelf;
    }

    public Friend(int friendshipId, int userId, String username, boolean pending, String publicKey) {
        super(userId, username);
        this.friendshipId = friendshipId;
        this.pending = pending;
        this.publicKey = publicKey;
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

    public int getFriendshipId() {
        return friendshipId;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
