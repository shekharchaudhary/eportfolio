package com.example.inventorycontrolapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.inventorycontrolapplication.data.helpers.SqlDbHelper;
import com.example.inventorycontrolapplication.data.helpers.PasswordHasher;
import com.example.inventorycontrolapplication.data.model.LoggedInUser;
import com.example.inventorycontrolapplication.data.model.SqlDbContract;
import com.example.inventorycontrolapplication.utils.AppLogger;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * Data source class that handles authentication with login credentials.
 * Follows Single Responsibility Principle - handles only database authentication operations.
 * Implements comprehensive error handling and structured logging.
 */
public class LoginDataSource {

    private static final String TAG = "LoginDataSource";
    private final SqlDbHelper DbHelper;

    public LoginDataSource(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        DbHelper = new SqlDbHelper(context);
        AppLogger.d(TAG, "LoginDataSource initialized");
    }

    public Result<LoggedInUser> login(String username, String password) {
        AppLogger.logMethodEntry(TAG, "login");
        AppLogger.d(TAG, "Attempting to authenticate user: " + username);

        Cursor cursor = null;
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

            cursor = queryLoginDatabase(projection, selection, selectionArgs, null);
            HashMap row = DbHelper.GetFirst(cursor);

            if (row == null || row.isEmpty()) {
                AppLogger.w(TAG, "User not found: " + username);
                AppLogger.logDatabaseOperation("SELECT_USER", SqlDbContract.AuthenticationEntry.TABLE_NAME, false);
                return new Result.Error(new IOException("Invalid username or password"));
            }

            AppLogger.logDatabaseOperation("SELECT_USER", SqlDbContract.AuthenticationEntry.TABLE_NAME, true);

            String dbUsername = getString(row, SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME);
            String storedPassword = getString(row, SqlDbContract.AuthenticationEntry.COLUMN_NAME_PASSWORD);
            String userId = getString(row, SqlDbContract.AuthenticationEntry._ID);
            String lastLogin = getString(row, SqlDbContract.AuthenticationEntry.COLUMN_NAME_LAST_LOGIN);

            if (dbUsername == null || storedPassword == null) {
                AppLogger.e(TAG, "Database record missing required fields for user: " + username);
                return new Result.Error(new IOException("Invalid user data"));
            }

            boolean authenticated = false;
            if (isHashedFormat(storedPassword)) {
                AppLogger.d(TAG, "Verifying hashed password");
                authenticated = PasswordHasher.verify(password, storedPassword);
            } else {
                // Legacy plaintext: verify then migrate
                AppLogger.w(TAG, "Found legacy plaintext password, migrating to hash");
                authenticated = storedPassword.equals(password);
                if (authenticated) {
                    try {
                        String newHash = PasswordHasher.hash(password);
                        updatePasswordAndLastLogin(username, newHash, now());
                        AppLogger.i(TAG, "Successfully migrated password to hash for user: " + username);
                    } catch (Exception e) {
                        AppLogger.e(TAG, "Failed to migrate password to hash", e);
                        // Continue with login even if migration fails
                    }
                }
            }

            if (!authenticated) {
                AppLogger.w(TAG, "Authentication failed for user: " + username);
                return new Result.Error(new IOException("Invalid username or password"));
            }

            // Update last login if not already updated via migration
            if (isHashedFormat(storedPassword)) {
                try {
                    updatePasswordAndLastLogin(username, null, now());
                } catch (Exception e) {
                    AppLogger.w(TAG, "Failed to update last login time", e);
                    // Non-critical, continue with login
                }
            }

            AppLogger.i(TAG, "User successfully authenticated: " + username);
            return new Result.Success<>(new LoggedInUser(
                    userId,
                    dbUsername,
                    lastLogin == null ? "" : lastLogin
            ));

        } catch (SQLiteException e) {
            AppLogger.e(TAG, "Database error during login for user: " + username, e);
            return new Result.Error(new IOException("Database error during login", e));
        } catch (Exception e) {
            AppLogger.e(TAG, "Unexpected error during login for user: " + username, e);
            return new Result.Error(new IOException("Login failed due to unexpected error", e));
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    AppLogger.w(TAG, "Error closing cursor", e);
                }
            }
            AppLogger.logMethodExit(TAG, "login");
        }
    }

    /**
     * Register a new user in the database
     * @param username The username for the new account
     * @param password The password for the new account
     * @return Result containing LoggedInUser on success or Error on failure
     */
    public Result<LoggedInUser> register(String username, String password) {
        AppLogger.logMethodEntry(TAG, "register");
        AppLogger.d(TAG, "Attempting to register new user: " + username);

        Cursor cursor = null;
        try {
            if (username != null) username = username.trim();

            // Projection
            String[] projection = {
                    BaseColumns._ID,
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME,
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_PASSWORD,
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_LAST_LOGIN
            };

            // Check if username already exists
            String selection = SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME + " = ?";
            String[] selectionArgs = { username };

            cursor = queryLoginDatabase(projection, selection, selectionArgs, null);
            HashMap row = DbHelper.GetFirst(cursor);

            if (row != null && !row.isEmpty()) {
                AppLogger.w(TAG, "Registration failed - username already exists: " + username);
                AppLogger.logDatabaseOperation("INSERT_USER", SqlDbContract.AuthenticationEntry.TABLE_NAME, false);
                return new Result.Error(new IOException("Username already registered"));
            }

            AppLogger.d(TAG, "Username available, creating account");

            // Hash the password before storing
            String hashedPassword;
            try {
                hashedPassword = PasswordHasher.hash(password);
                AppLogger.d(TAG, "Password successfully hashed");
            } catch (Exception e) {
                AppLogger.e(TAG, "Failed to hash password", e);
                return new Result.Error(new IOException("Failed to secure password", e));
            }

            // Insert new user
            long id = insertLoginDatabase("", username, hashedPassword);

            if (id == -1) {
                AppLogger.e(TAG, "Failed to insert user into database: " + username);
                AppLogger.logDatabaseOperation("INSERT_USER", SqlDbContract.AuthenticationEntry.TABLE_NAME, false);
                return new Result.Error(new IOException("Failed to create account"));
            }

            AppLogger.logDatabaseOperation("INSERT_USER", SqlDbContract.AuthenticationEntry.TABLE_NAME, true);
            AppLogger.i(TAG, "Successfully registered new user: " + username + " with ID: " + id);

            return new Result.Success<>(new LoggedInUser(
                    String.valueOf(id),
                    username,
                    ""
            ));

        } catch (SQLiteException e) {
            AppLogger.e(TAG, "Database error during registration for user: " + username, e);
            return new Result.Error(new IOException("Database error during registration", e));
        } catch (Exception e) {
            AppLogger.e(TAG, "Unexpected error during registration for user: " + username, e);
            return new Result.Error(new IOException("Registration failed due to unexpected error", e));
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    AppLogger.w(TAG, "Error closing cursor", e);
                }
            }
            AppLogger.logMethodExit(TAG, "register");
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
