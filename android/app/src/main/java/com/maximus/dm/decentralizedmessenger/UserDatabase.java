package com.maximus.dm.decentralizedmessenger;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Maximus on 30/01/2016.
 */
public class UserDatabase {

    public static String USER_DB = "userDatabase";
    public static String USER_ID = "0";
    public static String USERNAME = "username";
    public static String EMAIL = "email";
    public static String PASSWORD = "password";
    SharedPreferences userDatabase;

    public UserDatabase(Context context) {
        userDatabase = context.getSharedPreferences(USER_DB, 0);
    }

    public void storeUser(User user) {
        SharedPreferences.Editor spEditor = userDatabase.edit();
        spEditor.putString(USER_ID + USERNAME, user.username);
        spEditor.putString(USER_ID + EMAIL, user.email);
        spEditor.putString(USER_ID + PASSWORD, user.password);
        spEditor.commit();
    }

    public User getUser(String userId) {
        String username = userDatabase.getString(userId + USERNAME, "");
        String email = userDatabase.getString(userId + EMAIL, "");
        String password = userDatabase.getString(userId + PASSWORD, "");

        return new User(username, email, password);
    }

    public void clearAll() {
        SharedPreferences.Editor spEditor = userDatabase.edit();
        spEditor.clear();
        spEditor.commit();
    }

}
