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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sage.data.FirestoreManager;
import com.example.sage.data.Plant;
import com.example.sage.data.PlantAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
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

        // Search Bar
        AutoCompleteTextView searchAutoComplete = findViewById(R.id.searchAutoComplete);

        SearchBarHelper.setupLiveSearchBar(
                this,
                searchAutoComplete,
                firestoreManager,
                filteredPlants -> {
                    // No RecyclerView to update in MainActivity
                }
        );

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_shop) {
                startActivity(new Intent(this, ShopActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (id == R.id.bottom_favourites) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    // Not signed in, go to login
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("redirectTo", "favourites"); // Redirect back after login
                    startActivity(intent);
                } else {
                    startActivity(new Intent(this, FavouritesActivity.class));
                }
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (id == R.id.bottom_home) {
                // Already on home
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

        // Profile icon click opens popup menu
        ImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(this::showProfileMenu);



    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load and display top 3 most viewed plants whenever activity resumes
        loadTopPicks();
    }

    private void loadTopPicks() {
        firestoreManager.getAllPlants(plants -> {
            if (plants != null && !plants.isEmpty()) {
                // Randomize first to break ties
                Collections.shuffle(plants);
                // Sort by views descending
                Collections.sort(plants, (p1, p2) -> Integer.compare(p2.getViews(), p1.getViews()));

                List<Plant> top3Plants = plants.subList(0, Math.min(3, plants.size()));
                PlantAdapter topPicksAdapter = new PlantAdapter(top3Plants, firestoreManager, R.layout.top_pick_card,false);
                topPicksRecyclerView.setAdapter(topPicksAdapter);
            } else {
                Toast.makeText(this, "No plants found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openShopWithFilter(String category) {
        Intent intent = new Intent(this, ShopActivity.class);
        intent.putExtra("categoryFilter", category);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // Show profile menu with options depending on login state
    private void showProfileMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = user != null;

        // Show/hide menu options based on whether user is signed in
        popup.getMenu().findItem(R.id.menu_login).setVisible(!isLoggedIn);
        popup.getMenu().findItem(R.id.menu_logout).setVisible(isLoggedIn);
        popup.getMenu().findItem(R.id.menu_delete_account).setVisible(isLoggedIn); // Show Delete Account only if signed in
        popup.getMenu().findItem(R.id.menu_signup).setVisible(!isLoggedIn);

        // Handle menu option clicks
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_login) {
                // Navigate to login screen
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            } else if (id == R.id.menu_signup) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("mode", "signup");
                startActivity(intent);
                return true;
            } else if (id == R.id.menu_logout) {
                // Sign out user and show message
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_delete_account) {
                // Confirm and delete user account
                confirmDeleteAccount();
                return true;
            }
            return false;
        });

        popup.show();
    }

    // Show confirmation dialog before deleting user account
    private void confirmDeleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        // The following method was written by Chatgpt
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    user.delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();

                            // Restart MainActivity fresh
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // Finish current instance
                        } else {
                            Toast.makeText(this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();

    }
}


