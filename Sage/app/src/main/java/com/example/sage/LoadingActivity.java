package com.example.sage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.sage.data.FirestoreManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.sage.data.Plant;

import java.util.List;

public class LoadingActivity extends Activity {

    private FirestoreManager firestoreManager;
    private long startTime;
    private static final long MIN_DELAY = 5000; // 5 seconds in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Disabled dark mode as this isn't implemented and also set status bar colour to green to match loading screen
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.green));
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        firestoreManager = new FirestoreManager();
        startTime = System.currentTimeMillis(); // Record start time

        // Fetch data from Firestore and check if it's ready after a minimum 5s delay
        FirestoreManager.retrieveAllPlants(new OnSuccessListener<List<Plant>>() {
            @Override
            public void onSuccess(List<Plant> plants) {
                long elapsed = System.currentTimeMillis() - startTime; // Calculate elapsed time
                long remaining = MIN_DELAY - elapsed; // Calculate remaining time

                if (plants != null && !plants.isEmpty()) { // Check if data is ready
                    if (remaining <= 0) {
                        // Minimum delay passed, proceed immediately
                        proceedToMain();
                    } else {
                        // Delay remaining time, then proceed
                        new Handler(getMainLooper()).postDelayed(() -> proceedToMain(), remaining);
                    }
                } else {
                    // Could not fetch data, show message
                    Toast.makeText(LoadingActivity.this, "Failed to load data. Check connection.", Toast.LENGTH_LONG).show();
                }
            }
        }, e -> {
            // Could not fetch data due to failure, show message
            Toast.makeText(LoadingActivity.this, "Error loading plants: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Starts the MainActivity and applies fade in/out animations.
     */
    private void proceedToMain() {
        startActivity(new Intent(this, MainActivity.class));
        // Apply fade in for main activity, fade out for this activity
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
