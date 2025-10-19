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
import com.example.inventorycontrolapplication.data.helpers.MigrationUtil;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MigrationUtil.runPasswordHashMigrationOnce(getApplicationContext());
        setContentView(R.layout.activity_register);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory(getApplicationContext()))
                .get(LoginViewModel.class);

        final EditText nameEditText = findViewById(R.id.name);
        final EditText emailEditText = findViewById(R.id.email);
        final EditText passwordEditText = findViewById(R.id.password);
        final EditText confirmEditText = findViewById(R.id.confirm_password);
        final TextInputLayout emailLayout = findViewById(R.id.email_layout);
        final TextInputLayout passwordLayout = findViewById(R.id.password_layout);
        final TextInputLayout confirmLayout = findViewById(R.id.confirm_layout);
        final Button createButton = findViewById(R.id.create_account);
        final Button cancelButton = findViewById(R.id.cancel);
        final ProgressBar loading = findViewById(R.id.loading);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                // Simple inline validation
                String email = emailEditText.getText().toString().trim();
                String pw = passwordEditText.getText().toString();
                String c = confirmEditText.getText().toString();

                boolean emailOk = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
                boolean pwOk = pw != null && pw.length() > 5;
                boolean match = pw.equals(c);

                emailLayout.setError(emailOk ? null : getString(R.string.invalid_username));
                passwordLayout.setError(pwOk ? null : getString(R.string.invalid_password));
                confirmLayout.setError(match ? null : getString(R.string.passwords_do_not_match));

                createButton.setEnabled(emailOk && pwOk && match);
            }
        };
        emailEditText.addTextChangedListener(watcher);
        passwordEditText.addTextChangedListener(watcher);
        confirmEditText.addTextChangedListener(watcher);

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) return;
                loading.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showMessage(loginResult.getError());
                    return;
                }
                if (loginResult.getSuccess() != null) {
                    // Go to main on success
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.setVisibility(View.VISIBLE);
                String email = emailEditText.getText().toString().trim();
                String pw = passwordEditText.getText().toString();
                loginViewModel.register(email, pw);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showMessage(@StringRes int msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}

