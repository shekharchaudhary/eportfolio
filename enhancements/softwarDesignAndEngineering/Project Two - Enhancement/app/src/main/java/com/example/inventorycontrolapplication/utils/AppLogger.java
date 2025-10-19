package com.example.inventorycontrolapplication.utils;

import android.util.Log;

/**
 * Centralized logging utility providing structured logging throughout the application.
 * Follows Single Responsibility Principle - handles all logging concerns.
 */
public final class AppLogger {

    private static final String TAG_PREFIX = "InventoryApp";
    private static boolean loggingEnabled = true;

    // Private constructor to prevent instantiation
    private AppLogger() {
        throw new AssertionError("Cannot instantiate AppLogger");
    }

    /**
     * Enable or disable logging (useful for production builds)
     */
    public static void setLoggingEnabled(boolean enabled) {
        loggingEnabled = enabled;
    }

    /**
     * Log debug message
     */
    public static void d(String tag, String message) {
        if (loggingEnabled) {
            Log.d(formatTag(tag), message);
        }
    }

    /**
     * Log debug message with throwable
     */
    public static void d(String tag, String message, Throwable throwable) {
        if (loggingEnabled) {
            Log.d(formatTag(tag), message, throwable);
        }
    }

    /**
     * Log info message
     */
    public static void i(String tag, String message) {
        if (loggingEnabled) {
            Log.i(formatTag(tag), message);
        }
    }

    /**
     * Log info message with throwable
     */
    public static void i(String tag, String message, Throwable throwable) {
        if (loggingEnabled) {
            Log.i(formatTag(tag), message, throwable);
        }
    }

    /**
     * Log warning message
     */
    public static void w(String tag, String message) {
        if (loggingEnabled) {
            Log.w(formatTag(tag), message);
        }
    }

    /**
     * Log warning message with throwable
     */
    public static void w(String tag, String message, Throwable throwable) {
        if (loggingEnabled) {
            Log.w(formatTag(tag), message, throwable);
        }
    }

    /**
     * Log error message
     */
    public static void e(String tag, String message) {
        if (loggingEnabled) {
            Log.e(formatTag(tag), message);
        }
    }

    /**
     * Log error message with throwable
     */
    public static void e(String tag, String message, Throwable throwable) {
        if (loggingEnabled) {
            Log.e(formatTag(tag), message, throwable);
        }
    }

    /**
     * Log verbose message
     */
    public static void v(String tag, String message) {
        if (loggingEnabled) {
            Log.v(formatTag(tag), message);
        }
    }

    /**
     * Log verbose message with throwable
     */
    public static void v(String tag, String message, Throwable throwable) {
        if (loggingEnabled) {
            Log.v(formatTag(tag), message, throwable);
        }
    }

    /**
     * Format tag with prefix for consistent logging
     */
    private static String formatTag(String tag) {
        return TAG_PREFIX + ":" + tag;
    }

    /**
     * Log method entry for debugging
     */
    public static void logMethodEntry(String tag, String methodName) {
        if (loggingEnabled) {
            d(tag, "→ Entering " + methodName);
        }
    }

    /**
     * Log method exit for debugging
     */
    public static void logMethodExit(String tag, String methodName) {
        if (loggingEnabled) {
            d(tag, "← Exiting " + methodName);
        }
    }

    /**
     * Log database operation
     */
    public static void logDatabaseOperation(String operation, String table, boolean success) {
        if (loggingEnabled) {
            String status = success ? "SUCCESS" : "FAILED";
            i("Database", String.format("Operation: %s | Table: %s | Status: %s",
                operation, table, status));
        }
    }

    /**
     * Log authentication event
     */
    public static void logAuthEvent(String event, String username, boolean success) {
        if (loggingEnabled) {
            String status = success ? "SUCCESS" : "FAILED";
            i("Auth", String.format("Event: %s | User: %s | Status: %s",
                event, username, status));
        }
    }
}
