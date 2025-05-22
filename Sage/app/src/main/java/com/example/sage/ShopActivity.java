package com.example.sage;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sage.data.PlantAdapter;
import com.example.sage.data.FirestoreManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ShopActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PlantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);

        recyclerView = findViewById(R.id.recyclerViewPlants);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        FirestoreManager.retrieveAllPlants(
                plantList -> {
                    adapter = new PlantAdapter(plantList);
                    recyclerView.setAdapter(adapter);
                },
                e -> {
                    // You can log or show an error message here
                    e.printStackTrace();
                }
        );
        // Linking bottom navigation to views
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_shop);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_shop) {
                return true;
            } else if (itemId == R.id.bottom_home) {
                startActivity(new Intent(ShopActivity.this, MainActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_favourites) {
                startActivity(new Intent(ShopActivity.this, FavouritesActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });

    }
}