package com.example.inventorycontrolapplication.data.model;

import android.provider.BaseColumns;

public final class SqlDbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SqlDbContract() {}

    /* Authentication Table */
    public static class AuthenticationEntry implements BaseColumns {
        public static final String TABLE_NAME = "authentication";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_PASSWORD = "password";
        public static final String COLUMN_NAME_LAST_LOGIN = "lastLogin";
    }

    /* Inventory Table */
    public static class InventoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "inventory";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_COUNT = "count";
        public static final String COLUMN_NAME_DATE = "dateAdded";
    }
}
