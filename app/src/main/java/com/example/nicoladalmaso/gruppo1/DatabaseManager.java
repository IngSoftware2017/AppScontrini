package com.example.nicoladalmaso.gruppo1;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Francesco Piccolo on 13/11/2017.
 * CLASSE DATABASE SEMPLIFICATO
 */

public class DatabaseManager extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "PhotosDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PHOTOS = "photos";
    private static final String KEY_PHOTOS_ID = "filename";
    //Singleton Pattern
    private static DatabaseManager sInstance;

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }//DatabaseManager

    public static synchronized DatabaseManager getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseManager(context.getApplicationContext());
        }//if
        return sInstance;
    }//getInstance

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }//onConfigure

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PHOTOS_TABLE = "CREATE TABLE " + TABLE_PHOTOS +
                "(" +
                KEY_PHOTOS_ID + " TEXT PRIMARY KEY,)";
        db.execSQL(CREATE_PHOTOS_TABLE);
    }//onCreate

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS);
            onCreate(db);
        }//if
    }//onUpgrade

    // Insert the photo's path into the db
    public void addPhoto(String path) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_PHOTOS_ID, path);
            db.insertOrThrow(TABLE_PHOTOS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("","error");
        } finally {
            db.endTransaction();
        }
    }

}//DatabaseManager
