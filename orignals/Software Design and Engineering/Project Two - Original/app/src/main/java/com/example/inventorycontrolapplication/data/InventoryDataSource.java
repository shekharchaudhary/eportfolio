package com.example.inventorycontrolapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.inventorycontrolapplication.data.helpers.SqlDbHelper;
import com.example.inventorycontrolapplication.data.model.SqlDbContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class InventoryDataSource {

    private SqlDbHelper DbHelper;

    public InventoryDataSource(Context context) {
        DbHelper = new SqlDbHelper(context);
    }

    /*
        Private Helpers
    */
    public Cursor queryInventoryDatabase(String[] projection, @Nullable String selection,
                                      @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get DB
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        // Query and return cursor
        return db.query(
                SqlDbContract.InventoryEntry.TABLE_NAME,          // The table to query
                projection,         // The array of columns to return (pass null to get all)
                selection,          // The columns for the WHERE clause
                selectionArgs,      // The values for the WHERE clause
                null,      // don't group the rows
                null,       // don't filter by row groups
                sortOrder           // The sort order
        );
    }

    public long insertInventoryDatabase(String productName, String productType, String productCount) {
        // Get DB
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // Create content values
        ContentValues values = new ContentValues();
        values.put(SqlDbContract.InventoryEntry.COLUMN_NAME_NAME, productName);
        values.put(SqlDbContract.InventoryEntry.COLUMN_NAME_TYPE, productType);
        values.put(SqlDbContract.InventoryEntry.COLUMN_NAME_COUNT, productCount);
        values.put(SqlDbContract.InventoryEntry.COLUMN_NAME_DATE, java.text.DateFormat.getDateTimeInstance().format(new Date()));
        // Insert the new row, returning the primary key value of the new row
        return db.insert(SqlDbContract.InventoryEntry.TABLE_NAME, null, values);
    }

    public boolean updateInventoryDatabase(String productId, String newCount) {
        // Get DB
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // New value for count column
        ContentValues values = new ContentValues();
        values.put(SqlDbContract.InventoryEntry.COLUMN_NAME_COUNT, newCount);
        // Which row to update, based on the title
        String selection = SqlDbContract.InventoryEntry._ID + " LIKE ?";
        String[] selectionArgs = { productId };
        // Call for Update
        return db.update(
                SqlDbContract.InventoryEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs) > 0;
    }

    public boolean deleteInventoryDatabase(String productId) {
        // Get DB
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // Build Delete Query
        String selection = SqlDbContract.InventoryEntry._ID + " LIKE ?";
        String[] selectionArgs = { productId };
        // Call for Delete
        return db.delete(
                SqlDbContract.InventoryEntry.TABLE_NAME,
                selection,
                selectionArgs) > 0;
    }

    public boolean deleteAllInventoryData() {
        // Get DB
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // Call for Delete
        return db.delete(
                SqlDbContract.InventoryEntry.TABLE_NAME,
                null,
                null) > 0;
    }
}
