package com.maximus.dm.decentralizedmessenger.User;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Maximus on 30/01/2016.
 */
public class UserDatabase {

    public static final String USER_DB = "currentUserDatabase";

    // Things to be stored in SharedPrefs.
    public static final String USER_ID_LOCAL = "0";
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String TOKEN = "token";
    public static final String PUBLIC_KEY = "publicKey";

    SharedPreferences currentUserDatabse;

    public UserDatabase(Context context) {
        currentUserDatabse = context.getSharedPreferences(USER_DB, 0);
    }

    // "SETTERS". Call on login!
    public void storeUser(String currentUserUsername, int currentUserId) {
        SharedPreferences.Editor spEditor = currentUserDatabse.edit();
        spEditor.putString(USER_ID_LOCAL + USERNAME, currentUserUsername);
        spEditor.putInt(USER_ID_LOCAL + USER_ID, currentUserId);
        spEditor.commit();
    }

    public void setToken(String token) {
        SharedPreferences.Editor spEditor = currentUserDatabse.edit();
        spEditor.putString(USER_ID_LOCAL + TOKEN, token);
        spEditor.commit();
    }

    public void setPublicKey(String publicKey) {
        SharedPreferences.Editor spEditor = currentUserDatabse.edit();
        spEditor.putString(USER_ID_LOCAL + PUBLIC_KEY, publicKey);
        spEditor.commit();
    }

    // GETTERS
    public String getCurrentUsername() { return currentUserDatabse.getString(USER_ID_LOCAL + USERNAME, ""); }
    public int getCurrentId() {
        return currentUserDatabse.getInt(USER_ID_LOCAL + USER_ID, -1);
    }
    public String getToken() { return currentUserDatabse.getString(USER_ID_LOCAL + TOKEN, ""); }
    public String getPublicKey() { return currentUserDatabse.getString(USER_ID_LOCAL + PUBLIC_KEY, ""); }

    // Call on log out
    public void clearAll() {
        SharedPreferences.Editor spEditor = currentUserDatabse.edit();
        spEditor.clear();
        spEditor.commit();
    }

}
