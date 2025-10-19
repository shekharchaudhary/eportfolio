package com.example.inventorycontrolapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

import com.example.inventorycontrolapplication.data.helpers.SqlDbHelper;
import com.example.inventorycontrolapplication.data.helpers.PasswordHasher;
import com.example.inventorycontrolapplication.data.model.LoggedInUser;
import com.example.inventorycontrolapplication.data.model.SqlDbContract;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private SqlDbHelper DbHelper;

    public LoginDataSource(Context context) {
        DbHelper = new SqlDbHelper(context);
    }

    public Result<LoggedInUser> login(String username, String password) {
        try {
            if (username != null) username = username.trim();
            String[] projection = {
                    BaseColumns._ID,
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME,
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_PASSWORD,
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_LAST_LOGIN
            };
            String selection = SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME + " = ?";
            String[] selectionArgs = { username };
            Cursor cursor = queryLoginDatabase(projection, selection, selectionArgs, null);
            HashMap row = DbHelper.GetFirst(cursor);
            if (cursor != null) cursor.close();

            if (row == null || row.isEmpty()) {
                return new Result.Error(new IOException("Error logging in"));
            }

            String dbUsername = getString(row, SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME);
            String storedPassword = getString(row, SqlDbContract.AuthenticationEntry.COLUMN_NAME_PASSWORD);
            String userId = getString(row, SqlDbContract.AuthenticationEntry._ID);
            String lastLogin = getString(row, SqlDbContract.AuthenticationEntry.COLUMN_NAME_LAST_LOGIN);

            if (dbUsername == null || storedPassword == null) {
                return new Result.Error(new IOException("Error logging in"));
            }

            boolean authenticated = false;
            if (isHashedFormat(storedPassword)) {
                authenticated = PasswordHasher.verify(password, storedPassword);
            } else {
                // Legacy plaintext: verify then migrate
                authenticated = storedPassword.equals(password);
                if (authenticated) {
                    String newHash = PasswordHasher.hash(password);
                    updatePasswordAndLastLogin(username, newHash, now());
                }
            }

            if (!authenticated) {
                return new Result.Error(new IOException("Error logging in"));
            }

            // Update last login if not already updated via migration
            if (isHashedFormat(storedPassword)) {
                updatePasswordAndLastLogin(username, null, now());
            }

            return new Result.Success<>(new LoggedInUser(
                    userId,
                    dbUsername,
                    lastLogin == null ? "" : lastLogin
            ));
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    /*
        Function used to register the user in the database
     */
    public Result<LoggedInUser> register(String username, String password) {
        try {
            if (username != null) username = username.trim();
            // Projection
            String[] projection = {
                    BaseColumns._ID,
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME,
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_PASSWORD,
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_LAST_LOGIN
            };
            // Filter results WHERE
            String selection = SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME + " = ?";
            String[] selectionArgs = { username };
            // Query Db
            Cursor cursor = queryLoginDatabase(projection, selection, selectionArgs, null);
            // Get first row & null check
            HashMap row = DbHelper.GetFirst(cursor);
            if (cursor != null) cursor.close();
            if (row.isEmpty()) {
                // Insert new rows
                long id = insertLoginDatabase("", username, PasswordHasher.hash(password));
                // Return logged in user
                return new Result.Success<>(new LoggedInUser
                        (
                                String.valueOf(id),
                                username,
                                ""
                        )
                );
            } else {
                return new Result.Error(new IOException("Account already registered"));
            }
        } catch (Exception e) {
            return new Result.Error(new IOException("Error registering account", e));
        }
    }

    /*
        Function to log out the user
     */
    public void logout() {
        // TODO: revoke authentication
    }

    /*
        Private Helpers
     */
    private Cursor queryLoginDatabase(String[] projection, @Nullable String selection,
                                 @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get DB
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        // Query and return cursor
        return db.query(
                SqlDbContract.AuthenticationEntry.TABLE_NAME,          // The table to query
                projection,         // The array of columns to return (pass null to get all)
                selection,          // The columns for the WHERE clause
                selectionArgs,      // The values for the WHERE clause
                null,      // don't group the rows
                null,       // don't filter by row groups
                sortOrder           // The sort order
        );
    }

    private long insertLoginDatabase(String displayName, String username, String passwordOrHash) {
        // Get DB
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        // Create content values
        ContentValues values = new ContentValues();
        values.put(SqlDbContract.AuthenticationEntry.COLUMN_NAME_NAME, displayName);
        values.put(SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME, username);
        values.put(SqlDbContract.AuthenticationEntry.COLUMN_NAME_PASSWORD, passwordOrHash);
        values.put(SqlDbContract.AuthenticationEntry.COLUMN_NAME_LAST_LOGIN, now());
        // Insert the new row, returning the primary key value of the new row
        return db.insert(SqlDbContract.AuthenticationEntry.TABLE_NAME, null, values);
    }

    private void updatePasswordAndLastLogin(String username, String newPasswordHash, String newLastLogin) {
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (newPasswordHash != null) {
            values.put(SqlDbContract.AuthenticationEntry.COLUMN_NAME_PASSWORD, newPasswordHash);
        }
        if (newLastLogin != null) {
            values.put(SqlDbContract.AuthenticationEntry.COLUMN_NAME_LAST_LOGIN, newLastLogin);
        }
        db.update(
                SqlDbContract.AuthenticationEntry.TABLE_NAME,
                values,
                SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME + " = ?",
                new String[]{ username }
        );
    }

    private static boolean isHashedFormat(String stored) {
        return stored != null && stored.startsWith("PBKDF2WithHmacSHA256$");
    }

    private static String now() {
        return java.text.DateFormat.getDateTimeInstance().format(new Date());
    }

    private static String getString(HashMap row, String key) {
        Object v = row.get(key);
        return v == null ? null : v.toString();
    }
}
