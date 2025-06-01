package com.example.sage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button authButton;
    private TextView toggleText;

    private FirebaseAuth firebaseAuth;
    private boolean isLoginMode = true; // Toggle between Login and Sign Up

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Find views
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        authButton = findViewById(R.id.buttonAuth);
        toggleText = findViewById(R.id.textViewToggle);

        // Auth button click handler
        authButton.setOnClickListener(v -> handleAuth());

        // Toggle between Login and Sign Up
        toggleText.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            authButton.setText(isLoginMode ? "Login" : "Sign Up");
            toggleText.setText(isLoginMode ? "Don't have an account? Sign Up" : "Already have an account? Login");
        });
    }

    private void handleAuth() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLoginMode) {
            loginUser(email, password);
        } else {
            signUpUser(email, password);
        }
    }

    private void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        // Notify caller (DetailsActivity/MainActivity) login succeeded and finish
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signUpUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                        // Notify caller that signup/login succeeded, then finish
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Sign up failed.";
                        showSignUpError(error);
                    }
                });
    }


private void showSignUpError(String message) {
    if (message.contains("already in use")) {
        Toast.makeText(this, "This email is already registered. Please log in instead.", Toast.LENGTH_LONG).show();
    } else if (message.contains("badly formatted")) {
        Toast.makeText(this, "Invalid email format. Please check and try again.", Toast.LENGTH_LONG).show();
    } else if (message.contains("password")) {
        Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_LONG).show();
    } else {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

private void redirectToMain() {
    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    startActivity(intent);
    finish(); // Close LoginActivity
}
}
