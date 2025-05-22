package com.example.sage;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.graphics.Typeface;
import androidx.core.content.res.ResourcesCompat;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.sage.data.Plant;
import com.example.sage.data.FirestoreManager;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_nav, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        // Linking bottom navigation to views
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_home) {
                return true;
            } else if (itemId == R.id.bottom_shop) {
                startActivity(new Intent(MainActivity.this, ShopActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_favourites) {
                startActivity(new Intent(MainActivity.this, FavouritesActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
        Plant testPlant = new Plant();
        testPlant.setPlantid(1234);
        testPlant.setName("Demo Plant");
        testPlant.setCategory("Indoor");
        testPlant.setPrice(15.99);
        testPlant.setImages(Arrays.asList("https://example.com/image1.jpg", "https://example.com/image2.jpg"));
        testPlant.setSunlight("Partial Shade");
        testPlant.setSeason("Autumn");
        testPlant.setWater("Twice a week");
        testPlant.setDescription("Demo plant for testing Firestore upload.");
        testPlant.setViews(0);

        FirestoreManager firestoreManager = new FirestoreManager();
        firestoreManager.uploadPlant(testPlant);

        // 🔽 Fetch a plant from Firestore by ID
        firestoreManager.retrievePlantById(546,
                plant -> {
                    // ✅ Success: show plant info
                    Log.d("MainActivity", "Fetched plant: " + plant.getName());
                    Toast.makeText(MainActivity.this, "Loaded plant: " + plant.getName(), Toast.LENGTH_SHORT).show();
                },
                e -> {
                    // ❌ Failure: log error
                    Log.e("MainActivity", "Failed to fetch plant", e);
                    Toast.makeText(MainActivity.this, "Failed to load plant: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }

}