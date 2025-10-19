package com.example.inventorycontrolapplication.data.helpers;

import com.example.inventorycontrolapplication.data.model.SqlDbContract;

public final class SqlCommands {
    public static final String CREATE_DB_SCHEMA ="";
    public static final String CREATE_INV_TABLE =
            "CREATE TABLE IF NOT EXISTS " + SqlDbContract.InventoryEntry.TABLE_NAME + " (" +
                    SqlDbContract.InventoryEntry._ID + " INTEGER PRIMARY KEY," +
                    SqlDbContract.InventoryEntry.COLUMN_NAME_NAME + " TEXT," +
                    SqlDbContract.InventoryEntry.COLUMN_NAME_TYPE + " TEXT," +
                    SqlDbContract.InventoryEntry.COLUMN_NAME_COUNT + " TEXT," +
                    SqlDbContract.InventoryEntry.COLUMN_NAME_DATE + " TEXT)";
    public static final String CREATE_AUTH_TABLE =
            "CREATE TABLE IF NOT EXISTS " + SqlDbContract.AuthenticationEntry.TABLE_NAME + " (" +
                    SqlDbContract.AuthenticationEntry._ID + " INTEGER PRIMARY KEY," +
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_NAME + " TEXT," +
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME + " TEXT," +
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_PASSWORD + " TEXT," +
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_LAST_LOGIN + " TEXT)";
}
