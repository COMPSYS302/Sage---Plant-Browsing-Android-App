package com.example.sage;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.sage.data.FirestoreManager;

public class MainActivity extends AppCompatActivity {

    private FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firestore manager
        firestoreManager = new FirestoreManager();

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home); // Set default selected

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                return true; // Already in this view
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

        // Category icons to open ShopActivity with filter
        ImageView categoryIndoor = findViewById(R.id.categoryIndoor);
        ImageView categoryFlowering = findViewById(R.id.categoryFlowering);
        ImageView categoryEdible = findViewById(R.id.categoryEdible);

        categoryIndoor.setOnClickListener(v -> openShopWithFilter("Indoor"));
        categoryFlowering.setOnClickListener(v -> openShopWithFilter("Flowering"));
        categoryEdible.setOnClickListener(v -> openShopWithFilter("Edible"));


    }

    /**
     * Opens ShopActivity with the selected category as a filter.
     */
    private void openShopWithFilter(String category) {
        Intent intent = new Intent(MainActivity.this, ShopActivity.class);
        intent.putExtra("categoryFilter", category);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
