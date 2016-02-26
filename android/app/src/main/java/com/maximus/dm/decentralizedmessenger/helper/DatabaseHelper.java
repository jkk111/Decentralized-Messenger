package com.maximus.dm.decentralizedmessenger.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Maximus on 19/02/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database
    private static final String DATABASE_NAME = "logged_in_user.db";
    private static final int DATABASE_VERSION = 1;

    // FriendsTab table
    public static final String TABLE_NAME_FRIENDS = "friends";
    public static final String COLUMN_FRIEND_ID = "_id";
    public static final String COLUMN_FRIEND_ID_ON_SERVER = "id_on_server";
    public static final String COLUMN_FRIEND_USERNAME = "friends_username";
    public static final String COLUMN_FRIEND_PENDING = "friend_pending";
    public static final String COLUMN_FRIEND_MESSAGES = "friend_messages";

    // Messages table
    public static final String TABLE_NAME_MESSAGES = "messages";
    public static final String COLUMN_MESSAGE_ID = "_id";
    public static final String COLUMN_MESSAGE_COUNT = "message_count";
    public static final String COLUMN_MESSAGE_MESSAGE = "message";

    // Helper strings
    private static final String CREATE_TABLE_FRIENDS = "CREATE TABLE " + TABLE_NAME_FRIENDS + "("
            + COLUMN_FRIEND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_FRIEND_ID_ON_SERVER + " INTEGER NOT NULL UNIQUE, "
            + COLUMN_FRIEND_USERNAME + " TEXT NOT NULL, "
            + COLUMN_FRIEND_PENDING + " INTEGER NOT NULL, "
            + COLUMN_FRIEND_MESSAGES + " TEXT"
            + ");";

    private static final String CREATE_TABLE_MESSAGES = "CREATE TABLE " + TABLE_NAME_MESSAGES + "("
            + COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_MESSAGE_COUNT + " INTEGER NOT NULL, "
            + COLUMN_MESSAGE_MESSAGE + " TEXT NOT NULL "
            + ");";

    // Messages table
    //TODO: Create messages table
    // Keep all conversations between current user and friends here

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Runs when DatabaseHelper instance in created
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FRIENDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Delete table if it exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_FRIENDS);

        // Recreate tables
        onCreate(db);
    }
}
