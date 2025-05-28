package com.example.sage;

import android.content.ClipData;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.example.sage.data.Plant;
import com.example.sage.data.PlantAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.sage.data.FirestoreManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SearchView searchView;
//    private List<Plant> itemList;

    private FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.searchView);
        searchView.clearFocus();
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                // Handle search query submission
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                // Handle search query text change
//                fileList(newText);
//                return true;
//            }
//        });
//
//        itemList = new ArrayList<>();

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

//    private void fileList(String text) {
//        List<Plant> filteredList = new ArrayList<>();
//        for (Plant plant : itemList) {
//            if (plant.getName().toLowerCase().contains(text.toLowerCase())) {
//                filteredList.add(plant);
//            }
//        }
//
//        if (filteredList.isEmpty()) {
//            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show();
//        } else {
//            PlantAdapter.setFilteredList(filteredList);
//        }
//    }

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
