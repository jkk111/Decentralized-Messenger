package com.maximus.dm.decentralizedmessenger.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.maximus.dm.decentralizedmessenger.User.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maximus on 19/02/2016.
 */
public class FriendsDBManager {

    /* This class will change asap. Open/close will be automatic and I will probably
        have a single manager for both FriendsTab and Messages tables. */

    // but for now
    // 3 SIMPLE STEPS:
    // 1. Open
    // 2. Use
    // 3. Close

    private Context mContext;
    private SQLiteOpenHelper mDBHelper;
    private SQLiteDatabase mDatabase;


    public FriendsDBManager(Context context) {
        mContext = context;

        mDBHelper = new DatabaseHelper(context);
    }

    public void openRead() { mDatabase = mDBHelper.getReadableDatabase(); }

    public void openWrite() { mDatabase = mDBHelper.getWritableDatabase(); }

    public void close() { mDatabase.close(); }


    // If new friend added successfully, returns true
    // Else returns false
    /*
    public boolean addFriend(User userToAdd) {
        mDatabase = mDBHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_FRIEND_ID_ON_SERVER, userToAdd.getUserId());
        contentValues.put(DatabaseHelper.COLUMN_FRIEND_USERNAME, userToAdd.getUsername());
        long responseCode = mDatabase.insert(DatabaseHelper.TABLE_NAME_FRIENDS, null, contentValues);

        return ((responseCode == -1)? false : true);
    }
    */
    /*
    public boolean removeFriend(User userToRemove) {
        int rowsAffected = mDatabase.delete(DatabaseHelper.TABLE_NAME_FRIENDS, "WHERE "
                + DatabaseHelper.COLUMN_FRIEND_ID_ON_SERVER + "=? AND"
                + DatabaseHelper.COLUMN_FRIEND_USERNAME + "=?",
                new String[] {userToRemove.getUserId(), userToRemove.getUsername()});

        return ((rowsAffected > 0)? true : false);
    }
    */
    public List<User> getAllFriends() {
        Cursor cursorReturned;
        List<User> allFriends = new ArrayList<User>();

        // get everything from friends table
        cursorReturned = mDatabase.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME_FRIENDS, null);

        while(cursorReturned.moveToNext()) {
            //allFriends.add(new User(cursorReturned.getString(1), cursorReturned.getString(2), false));
        }

        return allFriends;
    }

    /*
    public User getFriend(User userToGet) {
        Cursor cursorReturned;
        User userToReturn;

        cursorReturned = mDatabase.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_FRIENDS
                    + " WHERE " + DatabaseHelper.COLUMN_FRIENDS_ID_ON_SERVER + "=?", new String[] {userToGet.getUsername()});


        return userToReturn;
    }
    */
}
