package com.example.sage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.sage.data.FirestoreManager;
import com.example.sage.data.Plant;
import com.example.sage.ui.DetailsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchBarHelper {

    public interface SearchUpdateListener {
        void onResultsUpdated(List<Plant> filteredPlants);
    }

    public static void setupLiveSearchBar(
            Activity activity,
            AutoCompleteTextView searchView,
            FirestoreManager firestoreManager,
            SearchUpdateListener updateListener // can be null if you don’t want live updates
    ) {
        firestoreManager.getAllPlants(allPlants -> {
            if (allPlants == null || allPlants.isEmpty()) return;

            Map<String, Plant> plantMap = new HashMap<>();
            for (Plant plant : allPlants) {
                plantMap.put(plant.getName(), plant);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    activity,
                    android.R.layout.simple_dropdown_item_1line,
                    new ArrayList<>()
            );
            searchView.setAdapter(adapter);
            searchView.setThreshold(1); // Show suggestions after 1 character
            searchView.setDropDownHeight(300);

            // Live filter as user types
            searchView.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void afterTextChanged(Editable s) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().toLowerCase().trim();

                    List<Plant> filteredPlants = new ArrayList<>();
                    List<String> topMatches = new ArrayList<>();

                    if (!query.isEmpty()) {
                        for (Plant plant : allPlants) {
                            if (plant.getName().toLowerCase().contains(query)) {
                                filteredPlants.add(plant);
                                topMatches.add(plant.getName());
                            }
                            if (topMatches.size() == 3) break;
                        }
                    }

                    adapter.clear();
                    if (!topMatches.isEmpty()) {
                        adapter.addAll(topMatches);
                        searchView.showDropDown();
                        if (updateListener != null) updateListener.onResultsUpdated(filteredPlants);
                    } else {
                        adapter.add("No results found");
                        searchView.showDropDown();
                        if (updateListener != null) updateListener.onResultsUpdated(new ArrayList<>());
                    }
                    adapter.notifyDataSetChanged();
                }
            });

            // Handle suggestion click
            searchView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedName = (String) parent.getItemAtPosition(position);
                Plant selectedPlant = plantMap.get(selectedName);
                if (selectedPlant == null) return; // Ignore "No results found"

                ArrayList<String> imageUrls = new ArrayList<>(selectedPlant.getImages());
                Intent intent = new Intent(activity, DetailsActivity.class);
                intent.putExtra("plant_id", selectedPlant.getPlantid());
                intent.putExtra("plant_name", selectedPlant.getName());
                intent.putExtra("plant_category", selectedPlant.getCategory());
                intent.putExtra("plant_price", selectedPlant.getPrice());
                intent.putExtra("plant_sunlight", selectedPlant.getSunlight());
                intent.putExtra("plant_water", selectedPlant.getWater());
                intent.putExtra("plant_season", selectedPlant.getSeason());
                intent.putExtra("plant_description", selectedPlant.getDescription());
                intent.putStringArrayListExtra("plant_images", imageUrls);

                try {
                    activity.startActivity(intent);
                } catch (Exception e) {
                    Log.e("SearchBarHelper", "Failed to open DetailsActivity", e);
                    Toast.makeText(activity, "Error: couldn't open DetailsActivity", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
