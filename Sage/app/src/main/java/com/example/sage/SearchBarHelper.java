
package com.example.sage;

import android.app.Activity;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.example.sage.data.FirestoreManager;
import com.example.sage.data.Plant;
import com.example.sage.ui.DetailsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchBarHelper {

    public static void setupPlantSearchBar(
            Activity activity,
            AutoCompleteTextView searchView,
            FirestoreManager firestoreManager
    ) {
        firestoreManager.getAllPlants(plants -> {
            if (plants != null && !plants.isEmpty()) {
                List<String> plantNames = new ArrayList<>();
                Map<String, Plant> plantMap = new HashMap<>();

                for (Plant plant : plants) {
                    plantNames.add(plant.getName());
                    plantMap.put(plant.getName(), plant);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        activity,
                        android.R.layout.simple_dropdown_item_1line,
                        plantNames
                );
                searchView.setAdapter(adapter);
                searchView.setThreshold(1);
                searchView.setDropDownHeight(200);

                searchView.setOnItemClickListener((parent, view, position, id) -> {
                    String selectedName = (String) parent.getItemAtPosition(position);
                    Plant selectedPlant = plantMap.get(selectedName);
                    if (selectedPlant != null) {
                        Intent intent = new Intent(activity, DetailsActivity.class);
                        intent.putExtra("plant_name", selectedPlant.getName());
                        intent.putExtra("plant_category", selectedPlant.getCategory());
                        intent.putExtra("plant_price", selectedPlant.getPrice());
                        intent.putExtra("plant_sunlight", selectedPlant.getSunlight());
                        intent.putExtra("plant_water", selectedPlant.getWater());
                        intent.putExtra("plant_season", selectedPlant.getSeason());
                        activity.startActivity(intent);
                    }
                });
            }
        });
    }
}
