package com.example.sage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sage.data.FirestoreManager;
import com.example.sage.data.Plant;
import com.example.sage.data.PlantAdapter;
import com.example.sage.SearchBarHelper;
import com.example.sage.ui.DetailsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirestoreManager firestoreManager;
    private RecyclerView topPicksRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firestore manager
        firestoreManager = new FirestoreManager();

        // Set up Top Picks RecyclerView
        topPicksRecyclerView = findViewById(R.id.topPicksRecyclerView);
        topPicksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load and display top 3 most viewed plants
        firestoreManager.getAllPlants(plants -> {
            if (plants != null && !plants.isEmpty()) {
                // Randomize first to break ties
                Collections.shuffle(plants);
                // Sort by views descending
                Collections.sort(plants, (p1, p2) -> Integer.compare(p2.getViews(), p1.getViews()));

                List<Plant> top3Plants = plants.subList(0, Math.min(3, plants.size()));
                PlantAdapter topPicksAdapter = new PlantAdapter(top3Plants, firestoreManager, R.layout.top_pick_card);
                topPicksRecyclerView.setAdapter(topPicksAdapter);
            } else {
                Toast.makeText(this, "No plants found", Toast.LENGTH_SHORT).show();
            }
        });

        // Search Bar
        AutoCompleteTextView searchAutoComplete = findViewById(R.id.searchAutoComplete);
        SearchBarHelper.setupPlantSearchBar(this, searchAutoComplete, firestoreManager);

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) return true;
            if (itemId == R.id.bottom_shop) {
                startActivity(new Intent(this, ShopActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_favourites) {
                startActivity(new Intent(this, FavouritesActivity.class));
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

    private void openShopWithFilter(String category) {
        Intent intent = new Intent(this, ShopActivity.class);
        intent.putExtra("categoryFilter", category);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
