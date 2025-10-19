package com.example.inventorycontrolapplication.data.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.example.inventorycontrolapplication.data.model.SqlDbContract;

public final class MigrationUtil {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_PW_MIGRATED_V1 = "pw_migrated_v1";

    private MigrationUtil() {}

    public static void runPasswordHashMigrationOnce(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_PW_MIGRATED_V1, false)) return;

        SqlDbHelper dbHelper = new SqlDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();
        try {
            String[] projection = new String[] {
                    BaseColumns._ID,
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME,
                    SqlDbContract.AuthenticationEntry.COLUMN_NAME_PASSWORD
            };
            Cursor c = db.query(
                    SqlDbContract.AuthenticationEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            while (c.moveToNext()) {
                String id = c.getString(c.getColumnIndexOrThrow(BaseColumns._ID));
                String username = c.getString(c.getColumnIndexOrThrow(SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME));
                String stored = c.getString(c.getColumnIndexOrThrow(SqlDbContract.AuthenticationEntry.COLUMN_NAME_PASSWORD));
                // Skip already-hashed and null/empty entries
                if (stored == null || stored.isEmpty() || PasswordHasher.hasEncodedFormat(stored)) {
                    continue;
                }
                String newHash = PasswordHasher.hash(stored);
                ContentValues values = new ContentValues();
                values.put(SqlDbContract.AuthenticationEntry.COLUMN_NAME_PASSWORD, newHash);
                db.update(
                        SqlDbContract.AuthenticationEntry.TABLE_NAME,
                        values,
                        SqlDbContract.AuthenticationEntry.COLUMN_NAME_USERNAME + " = ?",
                        new String[]{ username }
                );
            }
            c.close();
            db.setTransactionSuccessful();
            prefs.edit().putBoolean(KEY_PW_MIGRATED_V1, true).apply();
        } catch (Exception ignored) {
            // Do not mark as migrated to retry next launch
        } finally {
            db.endTransaction();
        }
    }
}

