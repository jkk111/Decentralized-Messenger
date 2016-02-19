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

    public static final String LOGGED_IN = "loggedIn";
    public static final String TOKEN = "token";

    SharedPreferences currentUserDatabse;

    public UserDatabase(Context context) {
        currentUserDatabse = context.getSharedPreferences(USER_DB, 0);
    }

    // Call on login
    public void storeUser(User currentUser, String token) {
        SharedPreferences.Editor spEditor = currentUserDatabse.edit();
        spEditor.putString(USER_ID_LOCAL + USER_ID, currentUser.getUserId());
        spEditor.putString(USER_ID_LOCAL + USERNAME, currentUser.getUsername());
        spEditor.putString(USER_ID_LOCAL + TOKEN, token);
        spEditor.commit();
    }

    // Call for info on current user
    public User getCurrentUser() {
        String userId = currentUserDatabse.getString(USER_ID_LOCAL + USER_ID, "");
        String username = currentUserDatabse.getString(USER_ID_LOCAL + USERNAME, "");
        return new User(userId, username);
    }

    // Token operations are performed separately
    public String getToken() {
        return currentUserDatabse.getString(USER_ID_LOCAL + TOKEN, "");
    }

    public void setToken(String token) {
        SharedPreferences.Editor spEditor = currentUserDatabse.edit();
        spEditor.putString(USER_ID_LOCAL + TOKEN, token);
        spEditor.commit();
    }

    /*
    public void setLoggedIn(User user) {
        SharedPreferences.Editor spEditor = currentUserDatabse.edit();
        spEditor.putString(LOGGED_IN + USERNAME, user.getUsername());
        spEditor.commit();
    }
    */

    /*
    public User getLoggedIn() {
        String username = currentUserDatabse.getString(LOGGED_IN + USERNAME, "");
        return new User(username, email);
    }
    */

    public void clearAll() {
        SharedPreferences.Editor spEditor = currentUserDatabse.edit();
        spEditor.clear();
        spEditor.commit();
    }

}
