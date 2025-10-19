package com.example.inventorycontrolapplication.data.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

// https://developer.android.com/training/data-storage/sqlite#java
public class SqlDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Inventory.db";

    public SqlDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SqlCommands.CREATE_AUTH_TABLE);
        db.execSQL(SqlCommands.CREATE_INV_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Ensure required tables exist (idempotent). For schema changes, add ALTERs per version.
        db.execSQL(SqlCommands.CREATE_AUTH_TABLE);
        db.execSQL(SqlCommands.CREATE_INV_TABLE);
    }

    /*
        Helper Functions
     */
    public static HashMap GetFirst(Cursor cursor) {
        // Row Type
        int colCount = cursor.getColumnCount();
        HashMap row = new HashMap(colCount);
        // Check if cursor has data
        if (cursor.moveToNext()) {
            for (int i = 0; i < colCount; i++) {
                // Switch out based on type
                switch (cursor.getType(i)) {
                    case Cursor.FIELD_TYPE_FLOAT:
                        row.put(cursor.getColumnName(i), cursor.getFloat(i));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        row.put(cursor.getColumnName(i), cursor.getInt(i));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        row.put(cursor.getColumnName(i), cursor.getString(i));
                        break;
                }
            }
        }
        return row;
    }

    public static ArrayList<HashMap> GetAll(Cursor cursor) {
        // INIT
        int colCount = cursor.getColumnCount();
        ArrayList<HashMap> formattedList = new ArrayList<HashMap>();
        // Check if cursor has data
        while (cursor.moveToNext()) {
            // Create Row
            HashMap row = new HashMap(colCount);
            // Get Types
            for (int i = 0; i < colCount; i++) {
                // Switch out based on type
                switch (cursor.getType(i)) {
                    case Cursor.FIELD_TYPE_FLOAT:
                        row.put(cursor.getColumnName(i), cursor.getFloat(i));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        row.put(cursor.getColumnName(i), cursor.getInt(i));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        row.put(cursor.getColumnName(i), cursor.getString(i));
                        break;
                }
            }
            formattedList.add(row);
        }
        return formattedList;
    }
}
