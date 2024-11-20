package com.example.findfriends;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "positions.db";
    private static final int DATABASE_VERSION = 2; // Incremented version for schema change
    private static final String TABLE_NAME = "positions";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_NAME = "name";  // Column for position name

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_TIMESTAMP + " TEXT, " +
                COLUMN_NAME + " TEXT);";  // Include name column
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_NAME + " TEXT;");
        }
    }

    // Insert position with name
    public boolean insertPositionWithName(double latitude, double longitude, String timestamp, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_NAME, name);  // Insert name
        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;  // Return true if insertion is successful
    }

    // Insert position without name
    public boolean insertPosition(double latitude, double longitude, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_TIMESTAMP, timestamp);
        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    // Get all positions
    public Cursor getAllPositions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }

    // Update position name
    public boolean updatePositionName(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, newName);
        int rowsAffected = db.update(TABLE_NAME, values, "id = ?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }

    // Get position by ID
    public Cursor getPositionById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
    }

    // Delete position by ID
    public boolean deletePosition(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(id)});
        return rowsDeleted > 0;
    }
}
