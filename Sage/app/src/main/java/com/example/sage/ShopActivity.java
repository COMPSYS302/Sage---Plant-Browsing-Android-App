package com.example.sage;

import android.content.Intent;
import android.graphics.Color;
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
import android.widget.AutoCompleteTextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sage.data.PlantAdapter;
import com.example.sage.data.FirestoreManager;
import com.example.sage.data.Plant;
import com.example.sage.SearchBarHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PlantAdapter adapter;
    private ImageView filterIcon;
    private PopupWindow popupWindow;

    private List<Plant> allPlants = new ArrayList<>(); // Full list of plants from Firestore
    private boolean isFilterActive = false; // Tracks if a category filter is applied
    private String selectedCategory = "All"; // Currently selected filter category

    private FirestoreManager firestoreManager = new FirestoreManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);

        // Setup RecyclerView with 2 column grid layout
        recyclerView = findViewById(R.id.recyclerViewPlants);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Search Bar
        AutoCompleteTextView searchBar = findViewById(R.id.searchBar);
        SearchBarHelper.setupLiveSearchBar(
                this,
                searchBar,
                firestoreManager,
                filteredPlants -> {
                    // live update RecyclerView
                    adapter.updateData(filteredPlants);
                }
        );

        // Initialize filter icon
        filterIcon = findViewById(R.id.filterIcon);

        // Get the filter category from the intent
        String categoryFilter = getIntent().getStringExtra("categoryFilter");
        if (categoryFilter != null) {
            selectedCategory = categoryFilter;
            isFilterActive = !categoryFilter.equalsIgnoreCase("All");
        }

        setupFilterDropdown();      // Set up filter popup behavior
        setupBottomNavigation();    // Configure bottom nav bar
        loadPlants();               // Load plant data and apply filter if needed
        updateFilterIconColor();    // Reflect filter icon state
    }

    private void applyCategoryFilter(String category) {
        selectedCategory = category; // Remember selection
        isFilterActive = !category.equalsIgnoreCase("All");

        FirestoreManager.retrieveAllPlants(
                plants -> {
                    allPlants = plants; // Store full list

                    List<Plant> filtered;
                    if (category.equalsIgnoreCase("All")) {
                        filtered = plants;
                    } else {
                        filtered = new ArrayList<>();
                        for (Plant plant : plants) {
                            if (plant.getCategory().equalsIgnoreCase(category)) {
                                filtered.add(plant);
                            }
                        }
                    }

                    if (adapter == null) {
                        adapter = new PlantAdapter(filtered, firestoreManager, R.layout.item_plant,false);
                        recyclerView.setAdapter(adapter);
                    } else {
                        adapter.updateData(filtered);
                    }

                    updateFilterIconColor(); // Update icon color based on state
                },
                e -> {
                    Toast.makeText(this, "Failed to load plants: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * Sets up the behavior of the filter icon.
     * Tapping it shows or hides the category filter popup.
     */
    private void setupFilterDropdown() {
        filterIcon.setOnClickListener(v -> {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            } else {
                showFilterPopup(v);
            }
        });
    }

    /**
     * Displays the filter popup under the filter icon with green highlight on the selected item.
     */
    private void showFilterPopup(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.filter_popup, null);

        ListView categoryListView = popupView.findViewById(R.id.categoryFilterList);
        String[] categories = {"All", "Indoor", "Flowering", "Edible"};

        // Adapter that highlights the selected category
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categories) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Highlight selected category in green
                if (categories[position].equalsIgnoreCase(selectedCategory)) {
                    textView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
                } else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        categoryListView.setAdapter(adapter);

        // Create and configure the popup window
        popupWindow = new PopupWindow(
                popupView,
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setElevation(10f);
        popupWindow.showAsDropDown(anchorView, 0, 10);

        // Handle category selection
        categoryListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = categories[position]; // Track selection
            applyFilter(selectedCategory);
            popupWindow.dismiss();
        });
    }

    /**
     * Filters the plant list based on the selected category.
     * Updates the filter active state and icon color.
     */
    private void applyFilter(String category) {
        selectedCategory = category;

        if (category.equalsIgnoreCase("All")) {
            isFilterActive = false;
            adapter.updateData(allPlants); // Show all plants
        } else {
            isFilterActive = true;
            List<Plant> filteredList = allPlants.stream()
                    .filter(p -> p.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
            adapter.updateData(filteredList);
        }

        updateFilterIconColor(); // Reflect new filter state on ui
    }

    /**
     * Updates the filter icon color based on whether a filter is active.
     * Green = active filter, Grey = no filter.
     */
    private void updateFilterIconColor() {
        int color = isFilterActive
                ? ContextCompat.getColor(this, R.color.green)
                : ContextCompat.getColor(this, R.color.grey);
        filterIcon.setColorFilter(color);
    }

    /**
     * Loads all plant data from Firestore and initializes the adapter.
     */
    private void loadPlants() {
        FirestoreManager.retrieveAllPlants(
                plantList -> {
                    allPlants = plantList;

                    List<Plant> displayList;

                    if (selectedCategory != null && !selectedCategory.equalsIgnoreCase("All")) {
                        isFilterActive = true;
                        displayList = allPlants.stream()
                                .filter(p -> p.getCategory().equalsIgnoreCase(selectedCategory))
                                .collect(Collectors.toList());
                    } else {
                        isFilterActive = false;
                        displayList = allPlants;
                    }

                    if (adapter == null) {
                        adapter = new PlantAdapter(displayList, firestoreManager, R.layout.item_plant,false); // Removed redundant FirestoreManager instantiation
                        recyclerView.setAdapter(adapter);
                    } else {
                        adapter.updateData(displayList);
                    }

                    updateFilterIconColor(); // Update filter on ui
                },
                e -> {
                    Toast.makeText(this, "Failed to load plants: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * Sets up the bottom navigation bar to navigate between activities.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_shop);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_shop) return true;
            else if (itemId == R.id.bottom_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_favourites) {
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
            }
            return false;
        });
    }
}
