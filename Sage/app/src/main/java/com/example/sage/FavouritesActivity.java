package com.example.sage;

import android.content.Intent;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sage.data.PlantAdapter;
import com.example.sage.data.FirestoreManager;
import com.example.sage.data.Plant;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.android.material.bottomnavigation.BottomNavigationView;



public class FavouritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private PlantAdapter adapter;
    private ImageView filterIcon;
    private PopupWindow popupWindow;

    private List<Plant> allPlants = new ArrayList<>(); // Full list of plants from Firestore
    private boolean isFilterActive = false; // Tracks if a category filter is applied
    private String selectedCategory = "All"; // Currently selected filter category

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favourites);
//
//        recyclerView = findViewById(R.id.recyclerViewFavourites);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this, 2));
        // currently an error ^

        // Linking bottom navigation to views
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_favourites);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_favourites) {
                return true;
            } else if (itemId == R.id.bottom_shop) {
                startActivity(new Intent(FavouritesActivity.this, ShopActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_home) {
                startActivity(new Intent(FavouritesActivity.this, MainActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });

    }
}