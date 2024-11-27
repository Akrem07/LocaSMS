package com.example.findfriends;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "positions.db";
    private static final int DATABASE_VERSION = 3; // Increment version for schema change
    private static final String TABLE_NAME = "positions";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_NAME = "name";  // Optional column for friend name
    private static final String COLUMN_PHONE = "phone"; // New column for phone number

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
                COLUMN_NAME + " TEXT, " +
                COLUMN_PHONE + " TEXT UNIQUE);"; // Phone should be unique
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DATABASE_VERSION) {
            // Add 'name' column if not already present
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_NAME + " TEXT;");
        }
    }


    // Insert or update position
//    public boolean savePosition(String phone, double latitude, double longitude, String timestamp) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_LATITUDE, latitude);
//        values.put(COLUMN_LONGITUDE, longitude);
//        values.put(COLUMN_TIMESTAMP, timestamp);
//        values.put(COLUMN_PHONE, phone);
//
//        // Check if the phone already exists
//        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_PHONE + " = ?", new String[]{phone}, null, null, null);
//
//        if (cursor != null && cursor.getCount() > 0) {
//            // Update existing record
//            int rowsAffected = db.update(TABLE_NAME, values, COLUMN_PHONE + " = ?", new String[]{phone});
//            cursor.close();
//            return rowsAffected > 0;
//        } else {
//            // Insert new record
//            long result = db.insert(TABLE_NAME, null, values);
//            if (cursor != null) cursor.close();
//            return result != -1;
//        }
//    }

    public Cursor getLastUnnamedPosition() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM positions WHERE name IS NULL OR name = '' ORDER BY timestamp DESC LIMIT 1";
        return db.rawQuery(query, null);
    }

    public Cursor getAllNamedPositions() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM positions WHERE name IS NOT NULL AND name != ''";
        return db.rawQuery(query, null);
    }




    public boolean savePosition(String phone, double latitude, double longitude, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_PHONE, phone);

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_PHONE + " = ?", new String[]{phone}, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            int rowsAffected = db.update(TABLE_NAME, values, COLUMN_PHONE + " = ?", new String[]{phone});
            cursor.close();
            return rowsAffected > 0;
        } else {
            long result = db.insert(TABLE_NAME, null, values);
            if (cursor != null) cursor.close();
            return result != -1;
        }
    }

    // Get all positions
    public Cursor getAllPositions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }

    // Insert position with name
    public boolean insertPositionWithName(double latitude, double longitude, String timestamp, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_TIMESTAMP, timestamp);  // You can set the current time or pass a timestamp if needed
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




    // Update position name
//    public boolean updatePositionName(int id, String newName) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_NAME, newName);
//        int rowsAffected = db.update(TABLE_NAME, values, "id = ?", new String[]{String.valueOf(id)});
//        return rowsAffected > 0;
//    }

    // Update position name
//    public boolean updatePositionName(int id, String newName) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_NAME, newName);
//        int rowsAffected = db.update(TABLE_NAME, values, "id = ?", new String[]{String.valueOf(id)});
//        return rowsAffected > 0;
//    }

    public boolean updatePositionName(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        int rowsAffected = db.update("positions", values, "id=?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }


    // Get position by ID
    public Cursor getPositionById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
    }

    // Delete position by ID
//    public boolean deletePosition(String phone) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        int rowsAffected = db.delete(TABLE_NAME, COLUMN_PHONE + " = ?", new String[]{phone});
//        return rowsAffected > 0; // Return true if at least one row was deleted
//    }

    // DatabaseHelper.java
//    public boolean deletePosition(String phone) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        int rowsDeleted = db.delete(TABLE_NAME, COLUMN_PHONE + " = ?", new String[]{phone});
//        db.close();
//        return rowsDeleted > 0;
//    }
    public boolean deletePosition(String positionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{positionId});
        db.close();
        return rowsDeleted > 0;
    }

    public boolean insertNamedPosition(String name, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);

        long result = db.insert("positions", null, contentValues); // Assuming "positions" is the table name
        db.close();
        return result != -1;
    }


    public void insertNamedPosition(double latitude, double longitude, String positionName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("name", positionName);

        db.insert("positions", null, values);
        db.close();
    }



    public String getNameByPhone(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_NAME}, COLUMN_PHONE + " = ?", new String[]{phone}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            cursor.close();
            return name;
        }
        if (cursor != null) cursor.close();
        return null;
    }




    public boolean savePosition(String phone, double latitude, double longitude, String timestamp, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Populate ContentValues with provided data
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_NAME, name); // Add name

        // Check if an entry already exists for the given phone number
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_PHONE + " = ?", new String[]{phone}, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            // Update existing entry
            int rowsAffected = db.update(TABLE_NAME, values, COLUMN_PHONE + " = ?", new String[]{phone});
            cursor.close(); // Close the cursor to release resources
            return rowsAffected > 0; // Return true if update was successful
        } else {
            // Insert a new entry
            long result = db.insert(TABLE_NAME, null, values);
            if (cursor != null) cursor.close(); // Close the cursor if not null
            return result != -1; // Return true if insert was successful
        }
    }



}