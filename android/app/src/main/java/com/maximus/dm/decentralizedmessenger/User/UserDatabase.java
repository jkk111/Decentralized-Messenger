package com.maximus.dm.decentralizedmessenger.User;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Maximus on 30/01/2016.
 */
public class UserDatabase {

    public static final String USER_DB = "userDatabase";
    public static final String USER_ID = "0";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public static final String LOGGED_IN = "loggedIn";

    SharedPreferences userDatabase;

    public UserDatabase(Context context) {
        userDatabase = context.getSharedPreferences(USER_DB, 0);
    }

    public void storeUser(User user) {
        SharedPreferences.Editor spEditor = userDatabase.edit();
        spEditor.putString(USER_ID + USERNAME, user.username);
        spEditor.putString(USER_ID + EMAIL, user.email);
        spEditor.commit();
    }

    public User getUser(String userId) {
        String username = userDatabase.getString(userId + USERNAME, "");
        String email = userDatabase.getString(userId + EMAIL, "");

        return new User(username, email);
    }

    public void setLoggedIn(User user) {
        SharedPreferences.Editor spEditor = userDatabase.edit();
        spEditor.putString(LOGGED_IN + USERNAME, user.getUsername());
        spEditor.putString(LOGGED_IN + EMAIL, user.getEmail());
        spEditor.commit();
    }

    public User getLoggedIn() {
        String username = userDatabase.getString(LOGGED_IN + USERNAME, "");
        String email = userDatabase.getString(LOGGED_IN + EMAIL, "");
        return new User(username, email);
    }

    public void clearAll() {
        SharedPreferences.Editor spEditor = userDatabase.edit();
        spEditor.clear();
        spEditor.commit();
    }

}
