package com.example.sage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.example.sage.data.FirestoreManager;

// This code was written using ChatGpt

public class LoadingActivity extends Activity {

    private FirestoreManager firestoreManager;
    private long startTime;
    private static final long MIN_DELAY = 5000; // 5 seconds in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        firestoreManager = new FirestoreManager();
        startTime = System.currentTimeMillis();

        // Fetch data from Firestore and check if it's ready after a minimum 5s delay
        firestoreManager.getAllPlants(plants -> {
            long elapsed = System.currentTimeMillis() - startTime;
            long remaining = MIN_DELAY - elapsed;

            if (plants != null && !plants.isEmpty()) {
                if (remaining <= 0) {
                    // Minimum delay passed, proceed immediately
                    proceedToMain();
                } else {
                    // Delay remaining time, then proceed
                    new Handler(getMainLooper()).postDelayed(this::proceedToMain, remaining);
                }
            } else {
                // Could not fetch data, show message
                Toast.makeText(this, "Failed to load data. Check connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void proceedToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
