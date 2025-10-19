package com.example.inventorycontrolapplication.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Patterns;

import com.example.inventorycontrolapplication.data.LoginRepository;
import com.example.inventorycontrolapplication.data.Result;
import com.example.inventorycontrolapplication.data.model.LoggedInUser;
import com.example.inventorycontrolapplication.R;
import com.example.inventorycontrolapplication.utils.AppLogger;

/**
 * ViewModel for login and registration.
 * Enhanced with logging and proper error handling.
 */
public class LoginViewModel extends ViewModel {

    private static final String TAG = "LoginViewModel";

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
        AppLogger.d(TAG, "LoginViewModel initialized");
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    /**
     * Login user with username and password
     */
    public void login(String username, String password) {
        AppLogger.logMethodEntry(TAG, "login");
        AppLogger.d(TAG, "Login attempt for user: " + username);

        try {
            // Delegate to repository
            Result<LoggedInUser> result = loginRepository.login(username, password);

            if (result instanceof Result.Success) {
                LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
                AppLogger.i(TAG, "Login successful");
            } else {
                AppLogger.w(TAG, "Login failed");
                loginResult.setValue(new LoginResult(R.string.login_failed));
            }
        } catch (Exception e) {
            AppLogger.e(TAG, "Login error", e);
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }

        AppLogger.logMethodExit(TAG, "login");
    }

    /**
     * Register new user with username and password
     */
    public void register(String username, String password) {
        AppLogger.logMethodEntry(TAG, "register");
        AppLogger.d(TAG, "Registration attempt for user: " + username);

        try {
            // Delegate to repository
            Result<LoggedInUser> result = loginRepository.register(username, password);

            if (result instanceof Result.Success) {
                LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
                AppLogger.i(TAG, "Registration successful");
            } else {
                AppLogger.w(TAG, "Registration failed");
                Exception error = ((Result.Error) result).getError();
                String errorMsg = error.getMessage();

                // Set appropriate error message
                if (errorMsg != null && errorMsg.contains("already registered")) {
                    loginResult.setValue(new LoginResult(R.string.registration_failed_duplicate));
                } else {
                    loginResult.setValue(new LoginResult(R.string.registration_failed));
                }
            }
        } catch (Exception e) {
            AppLogger.e(TAG, "Registration error", e);
            loginResult.setValue(new LoginResult(R.string.registration_failed));
        }

        AppLogger.logMethodExit(TAG, "register");
    }

    /**
     * Validate login/registration form data
     */
    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    /**
     * Validate username format
     */
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.trim().isEmpty()) {
            return false;
        }
        // Username must be at least 3 characters
        return username.trim().length() >= 3;
    }

    /**
     * Validate password strength
     */
    private boolean isPasswordValid(String password) {
        // Password must be at least 6 characters
        return password != null && password.length() >= 6;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        AppLogger.d(TAG, "LoginViewModel cleared");
    }
}
