package com.example.inventorycontrolapplication.data.repository;

import com.example.inventorycontrolapplication.data.Result;
import com.example.inventorycontrolapplication.data.model.LoggedInUser;

/**
 * Interface for authentication repository operations.
 * Follows Interface Segregation Principle - clients only depend on methods they use.
 * Follows Dependency Inversion Principle - high-level modules depend on abstractions.
 */
public interface IAuthRepository {

    /**
     * Authenticate user with credentials
     * @param username User's username
     * @param password User's password
     * @return Result containing LoggedInUser on success or Error on failure
     */
    Result<LoggedInUser> login(String username, String password);

    /**
     * Register new user
     * @param username Desired username
     * @param password User's password
     * @return Result containing LoggedInUser on success or Error on failure
     */
    Result<LoggedInUser> register(String username, String password);

    /**
     * Log out current user
     */
    void logout();

    /**
     * Check if user is currently logged in
     * @return true if user is logged in, false otherwise
     */
    boolean isLoggedIn();

    /**
     * Get currently logged in user
     * @return LoggedInUser or null if not logged in
     */
    LoggedInUser getCurrentUser();
}
