package com.example.sage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button authButton;
    private TextView toggleText, errorText, forgotPasswordText;

    private FirebaseAuth firebaseAuth;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.green));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.green));

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Link UI elements
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        authButton = findViewById(R.id.buttonAuth);
        toggleText = findViewById(R.id.textViewToggle);
        errorText = findViewById(R.id.textViewError);
        forgotPasswordText = findViewById(R.id.textViewForgotPassword);

        String mode = getIntent().getStringExtra("mode");
        if ("signup".equals(mode)) {
            isLoginMode = false;
            authButton.setText("Sign Up");
            toggleText.setText("Already have an account? Login");
            confirmPasswordEditText.setVisibility(View.VISIBLE);
            forgotPasswordText.setVisibility(View.GONE);
        }

        // Enable edge-to-edge
        EdgeToEdge.enable(this);

        // Set initial button click listener for login/sign-up
        authButton.setOnClickListener(v -> handleAuth());

        // Toggle between Login and Sign Up modes
        toggleText.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            authButton.setText(isLoginMode ? "Login" : "Sign Up");
            toggleText.setText(isLoginMode ? "Don't have an account? Sign Up" : "Already have an account? Login");
            errorText.setText("");

            // Confirm password visible only in Sign Up mode
            confirmPasswordEditText.setVisibility(isLoginMode ? View.GONE : View.VISIBLE);

            // Forgot password visible only in Login mode
            forgotPasswordText.setVisibility(isLoginMode ? View.VISIBLE : View.GONE);
        });

        // Forgot password functionality
        forgotPasswordText.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                showError("Please enter your email to reset your password.");
            } else {
                firebaseAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                showError("Password reset email sent. Check your inbox.");
                            } else {
                                Exception e = task.getException();
                                if (e instanceof FirebaseAuthInvalidUserException) {
                                    showError("No user found with this email.");
                                } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                    showError("Invalid email format.");
                                } else {
                                    showError("Failed to send reset email: " + (e != null ? e.getMessage() : "Unknown error."));
                                }
                            }
                        });
            }
        });
    }

    /**
     * Handles login or sign up depending on the current mode (login vs signup)
     */
    private void handleAuth() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (isLoginMode) {
            // Login mode validation
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                showError("Please enter both email and password.");
                return;
            }
            loginUser(email, password);
        } else {
            // Sign up mode validation
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                showError("Please fill in all fields.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match.");
                return;
            }
            signUpUser(email, password);
        }
    }

    /**
     * Attempts to log in the user with Firebase Authentication.
     * Provides detailed error messages based on the exception type.
     *
     * @param email The user's email.
     * @param password The user's password.
     *
     * This method was created with the help of ChatGpt
     */
    private void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Login successful, finish activity and return OK
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Usually means wrong password or invalid email format
                            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                            if (msg.contains("password")) {
                                showError("Incorrect password. Please try again.");
                            } else {
                                showError("Invalid login credentials. Please check and try again.");
                            }
                        } else if (e instanceof FirebaseAuthInvalidUserException) {
                            // User does not exist or disabled
                            showError("No account found with this email.");
                        } else {
                            // General failure
                            showError("Login failed: " + (e != null ? e.getMessage() : "Unknown error."));
                        }
                    }
                });
    }

    /**
     * Attempts to create a new user with Firebase Authentication.
     * Provides detailed error messages based on the exception type.
     *
     * @param email The user's email.
     * @param password The user's password.
     *
     *  This method was created with the help of ChatGpt
     */
    private void signUpUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        String userId = auth.getCurrentUser().getUid();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("favourites", new ArrayList<Integer>()); // Empty list initially

                        db.collection("users")
                                .document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    showError("Failed to save user data: " + e.getMessage());
                                });

                    } else {
                        Exception e = task.getException();
                        showError("Sign-up failed: " + (e != null ? e.getMessage() : "Unknown error."));

                    }
                });
    }


    /**
     * Displays an error message in the errorText TextView.
     */
    private void showError(String message) {
        errorText.setText(message);
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);  // fade out LoginActivity
    }


}
