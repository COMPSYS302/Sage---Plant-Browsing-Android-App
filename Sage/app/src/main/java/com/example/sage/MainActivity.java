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
        FirestoreManager firestoreManager = new FirestoreManager();

// Indoor Plants
//        Plant indoor1 = new Plant();
//        indoor1.setPlantid(1001);
//        indoor1.setName("Peace Lily");
//        indoor1.setCategory("Indoor");
//        indoor1.setPrice(12.99);
//        indoor1.setImages(Arrays.asList("https://example.com/peace_lily1.jpg"));
//        indoor1.setSunlight("Low Light");
//        indoor1.setSeason("Spring");
//        indoor1.setWater("Once a week");
//        indoor1.setDescription("A beautiful and easy-care plant perfect for indoors.");
//        indoor1.setViews(0);
//        firestoreManager.uploadPlant(indoor1);

//        Plant indoor2 = new Plant();
//        indoor2.setPlantid(1002);
//        indoor2.setName("Snake Plant");
//        indoor2.setCategory("Indoor");
//        indoor2.setPrice(9.99);
//        indoor2.setImages(Arrays.asList("https://example.com/snake_plant1.jpg"));
//        indoor2.setSunlight("Low to Bright Light");
//        indoor2.setSeason("All Year");
//        indoor2.setWater("Every 2 weeks");
//        indoor2.setDescription("Hard to kill, the snake plant thrives in any condition.");
//        indoor2.setViews(0);
//        firestoreManager.uploadPlant(indoor2);
//
//        Plant indoor3 = new Plant();
//        indoor3.setPlantid(1003);
//        indoor3.setName("ZZ Plant");
//        indoor3.setCategory("Indoor");
//        indoor3.setPrice(14.49);
//        indoor3.setImages(Arrays.asList("https://example.com/zz_plant1.jpg"));
//        indoor3.setSunlight("Indirect Light");
//        indoor3.setSeason("Summer");
//        indoor3.setWater("Twice a month");
//        indoor3.setDescription("Glossy leaves and minimal care make this a favorite.");
//        indoor3.setViews(0);
//        firestoreManager.uploadPlant(indoor3);
//
//// Flowering Plants
//        Plant flower1 = new Plant();
//        flower1.setPlantid(2001);
//        flower1.setName("Lavender");
//        flower1.setCategory("Flowering");
//        flower1.setPrice(7.99);
//        flower1.setImages(Arrays.asList("https://example.com/lavender1.jpg"));
//        flower1.setSunlight("Full Sun");
//        flower1.setSeason("Summer");
//        flower1.setWater("Twice a week");
//        flower1.setDescription("Beautiful purple blooms with a soothing scent.");
//        flower1.setViews(0);
//        firestoreManager.uploadPlant(flower1);
//
//        Plant flower2 = new Plant();
//        flower2.setPlantid(2002);
//        flower2.setName("Marigold");
//        flower2.setCategory("Flowering");
//        flower2.setPrice(4.99);
//        flower2.setImages(Arrays.asList("https://example.com/marigold1.jpg"));
//        flower2.setSunlight("Full Sun");
//        flower2.setSeason("Spring");
//        flower2.setWater("Every 3 days");
//        flower2.setDescription("Bright orange flowers that deter pests.");
//        flower2.setViews(0);
//        firestoreManager.uploadPlant(flower2);
//
//        Plant flower3 = new Plant();
//        flower3.setPlantid(2003);
//        flower3.setName("Petunia");
//        flower3.setCategory("Flowering");
//        flower3.setPrice(5.49);
//        flower3.setImages(Arrays.asList("https://example.com/petunia1.jpg"));
//        flower3.setSunlight("Partial to Full Sun");
//        flower3.setSeason("Spring to Autumn");
//        flower3.setWater("Every 4 days");
//        flower3.setDescription("Colorful and fragrant blooms that last all season.");
//        flower3.setViews(0);
//        firestoreManager.uploadPlant(flower3);
//
//// Edible Plants
//        Plant edible1 = new Plant();
//        edible1.setPlantid(3001);
//        edible1.setName("Basil");
//        edible1.setCategory("Edible");
//        edible1.setPrice(3.99);
//        edible1.setImages(Arrays.asList("https://example.com/basil1.jpg"));
//        edible1.setSunlight("Full Sun");
//        edible1.setSeason("Summer");
//        edible1.setWater("Every 2 days");
//        edible1.setDescription("Popular herb perfect for pasta, salads, and more.");
//        edible1.setViews(0);
//        firestoreManager.uploadPlant(edible1);
//
//        Plant edible2 = new Plant();
//        edible2.setPlantid(3002);
//        edible2.setName("Mint");
//        edible2.setCategory("Edible");
//        edible2.setPrice(4.49);
//        edible2.setImages(Arrays.asList("https://example.com/mint1.jpg"));
//        edible2.setSunlight("Partial Shade");
//        edible2.setSeason("Spring");
//        edible2.setWater("Every 2 days");
//        edible2.setDescription("Great for drinks, desserts, and breath-freshening.");
//        edible2.setViews(0);
//        firestoreManager.uploadPlant(edible2);
//
//        Plant edible3 = new Plant();
//        edible3.setPlantid(3003);
//        edible3.setName("Cherry Tomato");
//        edible3.setCategory("Edible");
//        edible3.setPrice(6.99);
//        edible3.setImages(Arrays.asList("https://example.com/tomato1.jpg"));
//        edible3.setSunlight("Full Sun");
//        edible3.setSeason("Spring to Summer");
//        edible3.setWater("Every 3 days");
//        edible3.setDescription("Sweet and juicy tomatoes perfect for salads.");
//        edible3.setViews(0);
//        firestoreManager.uploadPlant(edible3);


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