package com.example.inventorycontrolapplication.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId;
    private String displayName;
    private String lastLoginTime;

    public LoggedInUser(String userId, String displayName, String logonTime) {
        this.userId = userId;
        this.displayName = displayName;
        this.lastLoginTime = logonTime;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }
}