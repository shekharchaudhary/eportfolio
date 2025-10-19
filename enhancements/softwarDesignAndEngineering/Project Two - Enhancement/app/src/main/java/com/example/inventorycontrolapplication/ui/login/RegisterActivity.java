package com.example.inventorycontrolapplication.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.inventorycontrolapplication.MainActivity;
import com.example.inventorycontrolapplication.R;
import com.example.inventorycontrolapplication.utils.AppLogger;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Activity for user registration.
 * Enhanced with proper validation, error handling, and logging.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLogger.logMethodEntry(TAG, "onCreate");

        setContentView(R.layout.activity_register);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory(getApplicationContext()))
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final EditText confirmPasswordEditText = findViewById(R.id.confirm_password);
        final TextInputLayout usernameLayout = findViewById(R.id.username_layout);
        final TextInputLayout passwordLayout = findViewById(R.id.password_layout);
        final TextInputLayout confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
        final Button registerButton = findViewById(R.id.register);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        // Observe form state for validation
        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }

                // Enable register button only if username and password are valid
                boolean isValid = loginFormState.isDataValid();

                // Also check if passwords match
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();
                boolean passwordsMatch = password.equals(confirmPassword) && !password.isEmpty();

                registerButton.setEnabled(isValid && passwordsMatch);

                // Show username error if any
                if (loginFormState.getUsernameError() != null) {
                    if (usernameLayout != null) {
                        usernameLayout.setError(getString(loginFormState.getUsernameError()));
                    }
                } else if (usernameLayout != null) {
                    usernameLayout.setError(null);
                }

                // Show password error if any
                if (loginFormState.getPasswordError() != null) {
                    if (passwordLayout != null) {
                        passwordLayout.setError(getString(loginFormState.getPasswordError()));
                    }
                } else if (passwordLayout != null) {
                    passwordLayout.setError(null);
                }

                // Check if passwords match
                if (!password.isEmpty() && !confirmPassword.isEmpty() && !passwordsMatch) {
                    if (confirmPasswordLayout != null) {
                        confirmPasswordLayout.setError("Passwords do not match");
                    }
                } else if (confirmPasswordLayout != null) {
                    confirmPasswordLayout.setError(null);
                }
            }
        });

        // Observe login result for registration
        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }

                loadingProgressBar.setVisibility(View.GONE);

                if (loginResult.getError() != null) {
                    // Show error message
                    showRegistrationFailed(loginResult.getError());
                    AppLogger.w(TAG, "Registration failed");
                    return;
                }

                if (loginResult.getSuccess() != null) {
                    // Registration successful
                    updateUiWithUser(loginResult.getSuccess());
                    setResult(Activity.RESULT_OK);
                    AppLogger.i(TAG, "Registration successful for user: " +
                        loginResult.getSuccess().getDisplayName());

                    // Navigate to main activity
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // Text change listeners for validation
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(
                    usernameEditText.getText().toString(),
                    passwordEditText.getText().toString()
                );
            }
        };

        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        confirmPasswordEditText.addTextChangedListener(afterTextChangedListener);

        // Register button click listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();

                // Validate passwords match
                if (!password.equals(confirmPassword)) {
                    if (confirmPasswordLayout != null) {
                        confirmPasswordLayout.setError("Passwords do not match");
                    }
                    Toast.makeText(getApplicationContext(),
                        "Passwords do not match",
                        Toast.LENGTH_SHORT).show();
                    AppLogger.w(TAG, "Registration attempt with mismatched passwords");
                    return;
                }

                // Clear errors
                if (confirmPasswordLayout != null) {
                    confirmPasswordLayout.setError(null);
                }

                AppLogger.d(TAG, "Attempting to register user: " + username);
                loadingProgressBar.setVisibility(View.VISIBLE);

                // Call register method
                loginViewModel.register(username, password);
            }
        });

        // Login button (go back to login screen)
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppLogger.d(TAG, "Navigating back to login screen");
                finish(); // Go back to login activity
            }
        });

        AppLogger.logMethodExit(TAG, "onCreate");
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showRegistrationFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppLogger.d(TAG, "onDestroy");
    }
}
