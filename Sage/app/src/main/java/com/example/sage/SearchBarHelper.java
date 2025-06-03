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

    // Listener interface to notify when filtered results are updated
    public interface SearchUpdateListener {
        void onResultsUpdated(List<Plant> filteredPlants);
    }

    /**
     * Sets up the live search bar with filtering and dropdown suggestions.
     *
     * @param activity The current activity context.
     * @param searchView The AutoCompleteTextView to attach the search logic.
     * @param firestoreManager Manager to fetch plant data from Firestore.
     * @param updateListener Listener to notify filtered results updates (can be null).
     */
    public static void setupLiveSearchBar(
            Activity activity,
            AutoCompleteTextView searchView,
            FirestoreManager firestoreManager,
            SearchUpdateListener updateListener
    ) {
        // Fetch all plants from Firestore
        firestoreManager.getAllPlants(allPlants -> {
            if (allPlants == null || allPlants.isEmpty()) return;

            // Map plant names to Plant objects for quick lookup on item click
            Map<String, Plant> plantMap = new HashMap<>();
            for (Plant plant : allPlants) {
                plantMap.put(plant.getName(), plant);
            }

            // Create a custom ArrayAdapter for the dropdown suggestions
            // Override getFilter to disable internal filtering so "No results found" shows as an item
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    activity,
                    android.R.layout.simple_dropdown_item_1line,
                    new ArrayList<>()
            ) {
                @Override
                public android.widget.Filter getFilter() {
                    return new android.widget.Filter() {
                        @Override
                        protected FilterResults performFiltering(CharSequence constraint) {
                            FilterResults results = new FilterResults();
                            // Instead of filtering internally, just return the current adapter list as-is
                            results.values = getOriginalValues();
                            results.count = getOriginalValues().size();
                            return results;
                        }

                        @Override
                        protected void publishResults(CharSequence constraint, FilterResults results) {
                            clear();
                            if (results != null && results.values != null) {
                                // Update adapter data with provided values (could include "No results found")
                                addAll((List<String>) results.values);
                            }
                            notifyDataSetChanged();
                        }

                        // Helper method to get current items in the adapter as a list
                        private List<String> getOriginalValues() {
                            List<String> list = new ArrayList<>();
                            for (int i = 0; i < getCount(); i++) {
                                list.add(getItem(i));
                            }
                            return list;
                        }
                    };
                }
            };

            // Set the adapter and dropdown configurations on the search view
            searchView.setAdapter(adapter);
            searchView.setThreshold(1); // Show suggestions after typing 1 character
            searchView.setDropDownHeight(300); // Limit dropdown height

            // Add a TextWatcher to perform live filtering on user input
            searchView.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void afterTextChanged(Editable s) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().toLowerCase().trim(); // Normalize input for matching

                    List<Plant> filteredPlants = new ArrayList<>(); // Filtered Plant objects
                    List<String> topMatches = new ArrayList<>(); // Corresponding plant names (max 3)

                    if (!query.isEmpty()) {
                        // Search for plants whose names contain the query substring (case-insensitive)
                        for (Plant plant : allPlants) {
                            if (plant.getName().toLowerCase().contains(query)) {
                                filteredPlants.add(plant);
                                topMatches.add(plant.getName());
                            }
                            if (topMatches.size() == 3) break; // Limit to top 3 matches
                        }
                    }

                    adapter.clear();
                    if (!topMatches.isEmpty()) {
                        // Add matching plant names to the dropdown suggestions
                        adapter.addAll(topMatches);
                        searchView.showDropDown();
                        // Notify listener with filtered plant objects
                        if (updateListener != null) updateListener.onResultsUpdated(filteredPlants);
                    } else {
                        // No matches found: show "No results found" as an item in the dropdown
                        adapter.add("No results found");
                        searchView.showDropDown();
                        if (updateListener != null) updateListener.onResultsUpdated(new ArrayList<>());
                    }
                    adapter.notifyDataSetChanged(); // Refresh the dropdown
                }
            });

            // Handle what happens when user clicks on a dropdown suggestion
            searchView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedName = (String) parent.getItemAtPosition(position);
                Plant selectedPlant = plantMap.get(selectedName);
                if (selectedPlant == null) return;

                // Prepare data for DetailsActivity
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

                // Start the DetailsActivity
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
