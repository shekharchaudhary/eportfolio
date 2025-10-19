package com.example.inventorycontrolapplication.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import com.example.inventorycontrolapplication.data.Result;
import com.example.inventorycontrolapplication.data.helpers.SqlDbHelper;
import com.example.inventorycontrolapplication.data.model.SqlDbContract;
import com.example.inventorycontrolapplication.utils.AppLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Repository implementation for inventory data operations.
 * Follows Single Responsibility Principle - handles only inventory data access.
 * Follows Open/Closed Principle - open for extension through interface, closed for modification.
 * Implements comprehensive error handling and structured logging.
 */
public class InventoryRepository implements IInventoryRepository {

    private static final String TAG = "InventoryRepository";
    private final SqlDbHelper dbHelper;

    /**
     * Constructor with dependency injection
     * @param context Application context
     */
    public InventoryRepository(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.dbHelper = new SqlDbHelper(context);
        AppLogger.d(TAG, "InventoryRepository initialized");
    }

    @Override
    public Result<List<HashMap>> getAllItems() {
        AppLogger.logMethodEntry(TAG, "getAllItems");
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            String[] projection = {
                    BaseColumns._ID,
                    SqlDbContract.InventoryEntry.COLUMN_NAME_NAME,
                    SqlDbContract.InventoryEntry.COLUMN_NAME_TYPE,
                    SqlDbContract.InventoryEntry.COLUMN_NAME_COUNT,
                    SqlDbContract.InventoryEntry.COLUMN_NAME_DATE
            };

            cursor = db.query(
                    SqlDbContract.InventoryEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            ArrayList<HashMap> items = SqlDbHelper.GetAll(cursor);
            AppLogger.logDatabaseOperation("SELECT_ALL", SqlDbContract.InventoryEntry.TABLE_NAME, true);
            AppLogger.i(TAG, "Retrieved " + items.size() + " items from database");
            AppLogger.logMethodExit(TAG, "getAllItems");

            return new Result.Success<>(items);

        } catch (SQLiteException e) {
            AppLogger.e(TAG, "Database error getting all items", e);
            AppLogger.logDatabaseOperation("SELECT_ALL", SqlDbContract.InventoryEntry.TABLE_NAME, false);
            return new Result.Error(new Exception("Failed to retrieve inventory items", e));
        } catch (Exception e) {
            AppLogger.e(TAG, "Unexpected error getting all items", e);
            return new Result.Error(new Exception("Unexpected error retrieving inventory", e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            AppLogger.logMethodExit(TAG, "getAllItems");
        }
    }

    @Override
    public Result<HashMap> getItemById(String itemId) {
        AppLogger.logMethodEntry(TAG, "getItemById");
        AppLogger.d(TAG, "Fetching item with ID: " + itemId);

        if (itemId == null || itemId.trim().isEmpty()) {
            AppLogger.w(TAG, "Invalid item ID provided");
            return new Result.Error(new IllegalArgumentException("Item ID cannot be null or empty"));
        }

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            String[] projection = {
                    BaseColumns._ID,
                    SqlDbContract.InventoryEntry.COLUMN_NAME_NAME,
                    SqlDbContract.InventoryEntry.COLUMN_NAME_TYPE,
                    SqlDbContract.InventoryEntry.COLUMN_NAME_COUNT,
                    SqlDbContract.InventoryEntry.COLUMN_NAME_DATE
            };

            String selection = BaseColumns._ID + " = ?";
            String[] selectionArgs = { itemId };

            cursor = db.query(
                    SqlDbContract.InventoryEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            HashMap item = SqlDbHelper.GetFirst(cursor);

            if (item == null || item.isEmpty()) {
                AppLogger.w(TAG, "Item not found with ID: " + itemId);
                return new Result.Error(new Exception("Item not found"));
            }

            AppLogger.logDatabaseOperation("SELECT_BY_ID", SqlDbContract.InventoryEntry.TABLE_NAME, true);
            AppLogger.i(TAG, "Successfully retrieved item: " + itemId);

            return new Result.Success<>(item);

        } catch (SQLiteException e) {
            AppLogger.e(TAG, "Database error getting item by ID: " + itemId, e);
            AppLogger.logDatabaseOperation("SELECT_BY_ID", SqlDbContract.InventoryEntry.TABLE_NAME, false);
            return new Result.Error(new Exception("Failed to retrieve item", e));
        } catch (Exception e) {
            AppLogger.e(TAG, "Unexpected error getting item: " + itemId, e);
            return new Result.Error(new Exception("Unexpected error", e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            AppLogger.logMethodExit(TAG, "getItemById");
        }
    }

    @Override
    public Result<Long> addItem(String productName, String productType, String productCount) {
        AppLogger.logMethodEntry(TAG, "addItem");
        AppLogger.d(TAG, String.format("Adding item: %s, Type: %s, Count: %s",
            productName, productType, productCount));

        // Input validation
        if (productName == null || productName.trim().isEmpty()) {
            AppLogger.w(TAG, "Product name is required");
            return new Result.Error(new IllegalArgumentException("Product name cannot be empty"));
        }
        if (productType == null || productType.trim().isEmpty()) {
            AppLogger.w(TAG, "Product type is required");
            return new Result.Error(new IllegalArgumentException("Product type cannot be empty"));
        }
        if (productCount == null || productCount.trim().isEmpty()) {
            AppLogger.w(TAG, "Product count is required");
            return new Result.Error(new IllegalArgumentException("Product count cannot be empty"));
        }

        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(SqlDbContract.InventoryEntry.COLUMN_NAME_NAME, productName.trim());
            values.put(SqlDbContract.InventoryEntry.COLUMN_NAME_TYPE, productType.trim());
            values.put(SqlDbContract.InventoryEntry.COLUMN_NAME_COUNT, productCount.trim());
            values.put(SqlDbContract.InventoryEntry.COLUMN_NAME_DATE,
                java.text.DateFormat.getDateTimeInstance().format(new Date()));

            long newRowId = db.insert(SqlDbContract.InventoryEntry.TABLE_NAME, null, values);

            if (newRowId == -1) {
                AppLogger.e(TAG, "Failed to insert item into database");
                AppLogger.logDatabaseOperation("INSERT", SqlDbContract.InventoryEntry.TABLE_NAME, false);
                return new Result.Error(new Exception("Failed to add item to inventory"));
            }

            AppLogger.logDatabaseOperation("INSERT", SqlDbContract.InventoryEntry.TABLE_NAME, true);
            AppLogger.i(TAG, "Successfully added item with ID: " + newRowId);

            return new Result.Success<>(newRowId);

        } catch (SQLiteException e) {
            AppLogger.e(TAG, "Database error adding item", e);
            AppLogger.logDatabaseOperation("INSERT", SqlDbContract.InventoryEntry.TABLE_NAME, false);
            return new Result.Error(new Exception("Database error adding item", e));
        } catch (Exception e) {
            AppLogger.e(TAG, "Unexpected error adding item", e);
            return new Result.Error(new Exception("Unexpected error", e));
        } finally {
            AppLogger.logMethodExit(TAG, "addItem");
        }
    }

    @Override
    public Result<Boolean> updateItemCount(String itemId, String newCount) {
        AppLogger.logMethodEntry(TAG, "updateItemCount");
        AppLogger.d(TAG, String.format("Updating item %s to count: %s", itemId, newCount));

        // Input validation
        if (itemId == null || itemId.trim().isEmpty()) {
            AppLogger.w(TAG, "Item ID is required");
            return new Result.Error(new IllegalArgumentException("Item ID cannot be empty"));
        }
        if (newCount == null || newCount.trim().isEmpty()) {
            AppLogger.w(TAG, "New count is required");
            return new Result.Error(new IllegalArgumentException("Count cannot be empty"));
        }

        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(SqlDbContract.InventoryEntry.COLUMN_NAME_COUNT, newCount.trim());

            String selection = BaseColumns._ID + " = ?";
            String[] selectionArgs = { itemId };

            int rowsAffected = db.update(
                    SqlDbContract.InventoryEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
            );

            boolean success = rowsAffected > 0;

            if (success) {
                AppLogger.logDatabaseOperation("UPDATE", SqlDbContract.InventoryEntry.TABLE_NAME, true);
                AppLogger.i(TAG, "Successfully updated item: " + itemId);
            } else {
                AppLogger.w(TAG, "No rows updated for item: " + itemId);
                AppLogger.logDatabaseOperation("UPDATE", SqlDbContract.InventoryEntry.TABLE_NAME, false);
            }

            return new Result.Success<>(success);

        } catch (SQLiteException e) {
            AppLogger.e(TAG, "Database error updating item: " + itemId, e);
            AppLogger.logDatabaseOperation("UPDATE", SqlDbContract.InventoryEntry.TABLE_NAME, false);
            return new Result.Error(new Exception("Database error updating item", e));
        } catch (Exception e) {
            AppLogger.e(TAG, "Unexpected error updating item: " + itemId, e);
            return new Result.Error(new Exception("Unexpected error", e));
        } finally {
            AppLogger.logMethodExit(TAG, "updateItemCount");
        }
    }

    @Override
    public Result<Boolean> deleteItem(String itemId) {
        AppLogger.logMethodEntry(TAG, "deleteItem");
        AppLogger.d(TAG, "Deleting item: " + itemId);

        if (itemId == null || itemId.trim().isEmpty()) {
            AppLogger.w(TAG, "Item ID is required");
            return new Result.Error(new IllegalArgumentException("Item ID cannot be empty"));
        }

        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            String selection = BaseColumns._ID + " = ?";
            String[] selectionArgs = { itemId };

            int rowsDeleted = db.delete(
                    SqlDbContract.InventoryEntry.TABLE_NAME,
                    selection,
                    selectionArgs
            );

            boolean success = rowsDeleted > 0;

            if (success) {
                AppLogger.logDatabaseOperation("DELETE", SqlDbContract.InventoryEntry.TABLE_NAME, true);
                AppLogger.i(TAG, "Successfully deleted item: " + itemId);
            } else {
                AppLogger.w(TAG, "No rows deleted for item: " + itemId);
                AppLogger.logDatabaseOperation("DELETE", SqlDbContract.InventoryEntry.TABLE_NAME, false);
            }

            return new Result.Success<>(success);

        } catch (SQLiteException e) {
            AppLogger.e(TAG, "Database error deleting item: " + itemId, e);
            AppLogger.logDatabaseOperation("DELETE", SqlDbContract.InventoryEntry.TABLE_NAME, false);
            return new Result.Error(new Exception("Database error deleting item", e));
        } catch (Exception e) {
            AppLogger.e(TAG, "Unexpected error deleting item: " + itemId, e);
            return new Result.Error(new Exception("Unexpected error", e));
        } finally {
            AppLogger.logMethodExit(TAG, "deleteItem");
        }
    }

    @Override
    public Result<Boolean> deleteAllItems() {
        AppLogger.logMethodEntry(TAG, "deleteAllItems");
        AppLogger.w(TAG, "Attempting to delete ALL items from inventory");

        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            int rowsDeleted = db.delete(
                    SqlDbContract.InventoryEntry.TABLE_NAME,
                    null,
                    null
            );

            AppLogger.logDatabaseOperation("DELETE_ALL", SqlDbContract.InventoryEntry.TABLE_NAME, true);
            AppLogger.i(TAG, "Deleted " + rowsDeleted + " items from inventory");

            return new Result.Success<>(true);

        } catch (SQLiteException e) {
            AppLogger.e(TAG, "Database error deleting all items", e);
            AppLogger.logDatabaseOperation("DELETE_ALL", SqlDbContract.InventoryEntry.TABLE_NAME, false);
            return new Result.Error(new Exception("Database error deleting all items", e));
        } catch (Exception e) {
            AppLogger.e(TAG, "Unexpected error deleting all items", e);
            return new Result.Error(new Exception("Unexpected error", e));
        } finally {
            AppLogger.logMethodExit(TAG, "deleteAllItems");
        }
    }
}
