package com.example.inventorycontrolapplication.data;

import com.example.inventorycontrolapplication.data.model.LoggedInUser;
import com.example.inventorycontrolapplication.data.repository.IAuthRepository;
import com.example.inventorycontrolapplication.utils.AppLogger;

/**
 * Repository class that handles authentication operations.
 * Implements IAuthRepository interface for dependency inversion.
 * Follows Single Responsibility Principle - manages authentication state and delegates operations.
 * Maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository implements IAuthRepository {

    private static final String TAG = "LoginRepository";
    private static volatile LoginRepository instance;

    private final LoginDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("LoginDataSource cannot be null");
        }
        this.dataSource = dataSource;
        AppLogger.d(TAG, "LoginRepository initialized");
    }

    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if (instance == null) {
            synchronized (LoginRepository.class) {
                if (instance == null) {
                    instance = new LoginRepository(dataSource);
                }
            }
        }
        return instance;
    }

    @Override
    public boolean isLoggedIn() {
        boolean loggedIn = user != null;
        AppLogger.d(TAG, "isLoggedIn: " + loggedIn);
        return loggedIn;
    }

    @Override
    public void logout() {
        AppLogger.logMethodEntry(TAG, "logout");
        if (user != null) {
            AppLogger.logAuthEvent("LOGOUT", user.getDisplayName(), true);
            user = null;
        }
        dataSource.logout();
        AppLogger.logMethodExit(TAG, "logout");
    }

    @Override
    public LoggedInUser getCurrentUser() {
        return user;
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        if (user != null) {
            AppLogger.i(TAG, "User logged in: " + user.getDisplayName());
        }
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    @Override
    public Result<LoggedInUser> login(String username, String password) {
        AppLogger.logMethodEntry(TAG, "login");
        AppLogger.d(TAG, "Attempting login for user: " + username);

        try {
            // Input validation
            if (username == null || username.trim().isEmpty()) {
                AppLogger.w(TAG, "Login attempt with empty username");
                return new Result.Error(new IllegalArgumentException("Username cannot be empty"));
            }
            if (password == null || password.isEmpty()) {
                AppLogger.w(TAG, "Login attempt with empty password");
                return new Result.Error(new IllegalArgumentException("Password cannot be empty"));
            }

            // Delegate to data source
            Result<LoggedInUser> result = dataSource.login(username, password);

            if (result instanceof Result.Success) {
                LoggedInUser loggedInUser = ((Result.Success<LoggedInUser>) result).getData();
                setLoggedInUser(loggedInUser);
                AppLogger.logAuthEvent("LOGIN", username, true);
            } else {
                AppLogger.logAuthEvent("LOGIN", username, false);
            }

            return result;

        } catch (Exception e) {
            AppLogger.e(TAG, "Unexpected error during login", e);
            AppLogger.logAuthEvent("LOGIN", username, false);
            return new Result.Error(new Exception("Login failed", e));
        } finally {
            AppLogger.logMethodExit(TAG, "login");
        }
    }

    @Override
    public Result<LoggedInUser> register(String username, String password) {
        AppLogger.logMethodEntry(TAG, "register");
        AppLogger.d(TAG, "Attempting registration for user: " + username);

        try {
            // Input validation
            if (username == null || username.trim().isEmpty()) {
                AppLogger.w(TAG, "Registration attempt with empty username");
                return new Result.Error(new IllegalArgumentException("Username cannot be empty"));
            }
            if (password == null || password.isEmpty()) {
                AppLogger.w(TAG, "Registration attempt with empty password");
                return new Result.Error(new IllegalArgumentException("Password cannot be empty"));
            }

            // Delegate to data source
            Result<LoggedInUser> result = dataSource.register(username, password);

            if (result instanceof Result.Success) {
                LoggedInUser loggedInUser = ((Result.Success<LoggedInUser>) result).getData();
                setLoggedInUser(loggedInUser);
                AppLogger.logAuthEvent("REGISTER", username, true);
            } else {
                AppLogger.logAuthEvent("REGISTER", username, false);
            }

            return result;

        } catch (Exception e) {
            AppLogger.e(TAG, "Unexpected error during registration", e);
            AppLogger.logAuthEvent("REGISTER", username, false);
            return new Result.Error(new Exception("Registration failed", e));
        } finally {
            AppLogger.logMethodExit(TAG, "register");
        }
    }
}