package com.example.sage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favourites);

        recyclerView = findViewById(R.id.recyclerViewFavourites);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        totalValueText = findViewById(R.id.totalValueText);

        setupBottomNavigation();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.putExtra("redirectTo", "favourites");
            startActivity(loginIntent);
            finish();
            return;
        }

        String email = currentUser.getEmail();
        if (email == null) {
            Toast.makeText(this, "User email not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreManager.getFavouriteIdsByEmail(email,
                favouriteIds -> {
                    FirestoreManager.retrieveAllPlants(allPlants -> {
                        List<Plant> favourites = new ArrayList<>();
                        double total = 0.0;

                        for (Plant plant : allPlants) {
                            if (favouriteIds.contains(plant.getPlantid())) {
                                favourites.add(plant);
                                total += plant.getPrice();
                            }
                        }

                        totalValueText.setText("Total Value: $" + String.format("%.2f", total));

                        adapter = new PlantAdapter(favourites, firestoreManager, R.layout.item_plant);
                        recyclerView.setAdapter(adapter);

                    }, e -> Toast.makeText(this, "Failed to retrieve plants: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                },
                e -> Toast.makeText(this, "Failed to retrieve favourites: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_favourites);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_favourites) {
                return true;
            } else if (id == R.id.bottom_shop) {
                startActivity(new Intent(this, ShopActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (id == R.id.bottom_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }
}
