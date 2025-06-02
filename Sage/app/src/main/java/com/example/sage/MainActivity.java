package com.example.sage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sage.data.FirestoreManager;
import com.example.sage.data.Plant;
import com.example.sage.data.PlantAdapter;
import com.example.sage.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;


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




        // Category icons to open ShopActivity with filter
        ImageView categoryIndoor = findViewById(R.id.categoryIndoor);
        ImageView categoryFlowering = findViewById(R.id.categoryFlowering);
        ImageView categoryEdible = findViewById(R.id.categoryEdible);

        categoryIndoor.setOnClickListener(v -> openShopWithFilter("Indoor"));
        categoryFlowering.setOnClickListener(v -> openShopWithFilter("Flowering"));
        categoryEdible.setOnClickListener(v -> openShopWithFilter("Edible"));

        ImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(view -> showProfileMenu(view));

        // TEMPORARY: call createUserWithEmail directly for testing
        firestoreManager.createUserWithEmail(
                "testuser@example.com",           // Replace with test email
                "password123",                    // Replace with test password (min 6 characters)
                "TestUser",                       // Username
                unused -> {
                    Log.d("MainActivity", "✅ User created successfully.");
                },
                error -> {
                    Log.e("MainActivity", "❌ Failed to create user: " + error.getMessage());
                }
        );
    }

    private void openShopWithFilter(String category) {
        Intent intent = new Intent(this, ShopActivity.class);
        intent.putExtra("categoryFilter", category);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    private void showProfileMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = user != null;

        // Toggle menu items based on login state
        popup.getMenu().findItem(R.id.menu_login).setVisible(!isLoggedIn);
        popup.getMenu().findItem(R.id.menu_logout).setVisible(isLoggedIn);

        // Handle menu actions
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_login) {
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            } else if (id == R.id.menu_logout) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popup.show();
    }



}
