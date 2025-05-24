package com.example.sage;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.sage.data.Plant;
import com.example.sage.data.FirestoreManager;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firestore manager instance
        firestoreManager = new FirestoreManager();

        // Set up bottom navigation bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home); // Default selection

        // Handle navigation item clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_home) {
                // Already on the home screen
                return true;
            } else if (itemId == R.id.bottom_shop) {
                // Navigate to Shop screen
                startActivity(new Intent(MainActivity.this, ShopActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish(); // Prevents stacking multiple instances
                return true;
            } else if (itemId == R.id.bottom_favourites) {
                // Navigate to Favourites screen
                startActivity(new Intent(MainActivity.this, FavouritesActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });

        // Set up category button listeners
        LinearLayout indoor = findViewById(R.id.categoryIndoor);
        LinearLayout flowering = findViewById(R.id.categoryFlowering);
        LinearLayout edible = findViewById(R.id.categoryEdible);

        indoor.setOnClickListener(v -> openShopWithFilter("Indoor"));
        flowering.setOnClickListener(v -> openShopWithFilter("Flowering"));
        edible.setOnClickListener(v -> openShopWithFilter("Edible"));

        // Fetch a plant by its ID from Firestore
        firestoreManager.retrievePlantById(546,
                plant -> {
                    // On success, display the plant name in a Toast
                    Log.d("MainActivity", "Fetched plant: " + plant.getName());
                    Toast.makeText(MainActivity.this, "Loaded plant: " + plant.getName(), Toast.LENGTH_SHORT).show();
                },
                e -> {
                    // On failure, log the error and show a Toast
                    Log.e("MainActivity", "Failed to fetch plant", e);
                    Toast.makeText(MainActivity.this, "Failed to load plant: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }

    /**
     * Launches ShopActivity with a filter applied to show a specific category of plants.
     * @param category the category to filter by
     */
    private void openShopWithFilter(String category) {
        Intent intent = new Intent(MainActivity.this, ShopActivity.class);
        intent.putExtra("categoryFilter", category);
        startActivity(intent);
    }
}
