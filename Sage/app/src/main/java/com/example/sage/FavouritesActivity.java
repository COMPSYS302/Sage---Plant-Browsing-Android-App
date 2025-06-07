package com.example.sage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sage.data.FirestoreManager;
import com.example.sage.data.Plant;
import com.example.sage.data.PlantAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView totalValueText;
    private PlantAdapter adapter;
    private FirestoreManager firestoreManager = new FirestoreManager();
    private String email;
    private double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favourites);

        // Setup RecyclerView to show plant cards in a 2-column grid
        recyclerView = findViewById(R.id.recyclerViewFavourites);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // TextView to display the total value of favourite plants
        totalValueText = findViewById(R.id.totalValueText);

        // Setup the bottom navigation menu
        setupBottomNavigation();

        // Check if user is logged in; if not, redirect to LoginActivity
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.putExtra("redirectTo", "favourites");
            startActivity(loginIntent);
            finish();
            return;
        }

        // Get the user's email
        email = currentUser.getEmail();
        if (email == null) {
            Toast.makeText(this, "User email not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load the user's favourite plants initially
        loadFavourites();
    }

    /**
     * Loads the user's favourites from Firestore and updates the UI.
     * This method can be called after adding/removing favourites to refresh the list.
     */
    public void loadFavourites() {
        firestoreManager.getFavouriteIdsByEmail(email,
                favouriteIds -> FirestoreManager.retrieveAllPlants(allPlants -> {
                    List<Plant> favourites = new ArrayList<>();
                    // Filter plants that match the user's favourite IDs
                    for (Plant plant : allPlants) {
                        if (favouriteIds.contains(plant.getPlantid())) {
                            favourites.add(plant);
                        }
                    }

                    // Set the adapter with the list of favourite plants
                    adapter = new PlantAdapter(favourites, firestoreManager, R.layout.item_plant, true, price -> {
                        updateTotalValue(); // Update total when price changes
                    });

                    recyclerView.setAdapter(adapter);

                    updateTotalValue(); // Initial total value calculation and display

                }, e -> Toast.makeText(this, "Failed to retrieve plants: " + e.getMessage(), Toast.LENGTH_SHORT).show()),
                e -> Toast.makeText(this, "Failed to retrieve favourites: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Calculates and updates the total value of the favourite plants.
     */
    private void updateTotalValue() {
        double sum = 0.0;
        if (adapter != null) {
            for (Plant plant : adapter.getCurrentData()) {
                sum += plant.getPrice();
            }
        }
        total = sum;
        totalValueText.setText("Total Value: $" + String.format("%.2f", total));
    }

    /**
     * Initializes the bottom navigation bar and sets the current tab as selected.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_favourites);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_favourites) {
                // Already on Favourites screen
                return true;
            } else if (id == R.id.bottom_shop) {
                // Navigate to Shop screen
                startActivity(new Intent(this, ShopActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (id == R.id.bottom_home) {
                // Navigate to Home screen
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }

    /**
     * Automatically refresh favourites when the user returns to this screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadFavourites();
    }
}
