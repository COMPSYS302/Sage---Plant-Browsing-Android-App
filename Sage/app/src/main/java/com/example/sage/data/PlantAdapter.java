package com.example.sage.data;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sage.R;
import com.example.sage.ui.DetailsActivity;

import java.util.ArrayList;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    // Holds the current list of plants to display
    private List<Plant> plantList;

    // Backup full list for filtering purposes
    private final List<Plant> fullPlantList;

    // Reference to FirestoreManager for incrementing views
    private final FirestoreManager firestoreManager;

    // Layout resource ID for inflating different layouts
    private final int layoutId;

    // Constructor initializes the adapter with the plant list, FirestoreManager, and layout ID
    public PlantAdapter(List<Plant> plantList, FirestoreManager firestoreManager, int layoutId) {
        this.plantList = new ArrayList<>(plantList);
        this.fullPlantList = new ArrayList<>(plantList); // Store a full copy for filtering
        this.firestoreManager = firestoreManager;
        this.layoutId = layoutId;
    }

    // Replaces the existing data with a new filtered list
    public void updateData(List<Plant> newPlantList) {
        this.plantList = new ArrayList<>(newPlantList);
        notifyDataSetChanged(); // Notify the RecyclerView to refresh
    }

    // Filters the list based on a search query
    public void setFilteredList(String query) {
        List<Plant> filteredList = new ArrayList<>();

        for (Plant plant : fullPlantList) {
            if (plant.getName().toLowerCase().contains(query.toLowerCase()) ||
                    plant.getCategory().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(plant);
            }
        }

        plantList = filteredList;
        notifyDataSetChanged();
    }

    // Inflates the item layout and returns a new ViewHolder
    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false); // Use dynamic layout ID
        return new PlantViewHolder(view, firestoreManager);
    }

    // Binds each plant in the list to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        holder.bindView(plantList.get(position));
    }

    // Returns the number of items in the plant list
    @Override
    public int getItemCount() {
        return plantList.size();
    }

    // ViewHolder for individual plant cards
    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        private final TextView plantName;
        private final TextView plantCategory;
        private final TextView plantPrice;
        private final ImageView plantImage;

        private final TextView plantViews;
        private final FirestoreManager firestoreManager;

        public PlantViewHolder(@NonNull View itemView, FirestoreManager firestoreManager) {
            super(itemView);
            this.firestoreManager = firestoreManager;

            // View binding
            plantName = itemView.findViewById(R.id.plantName);
            plantCategory = itemView.findViewById(R.id.plantCategory);
            plantPrice = itemView.findViewById(R.id.plantPrice);
            plantImage = itemView.findViewById(R.id.plant_image);
            plantViews = itemView.findViewById(R.id.plantViews);
        }

        // Populates the plant item with data
        public void bindView(Plant plant) {
            plantName.setText(plant.getName());
            plantCategory.setText(plant.getCategory());
            plantPrice.setText("$" + String.format("%.2f", plant.getPrice()));
            plantViews.setText(String.valueOf(plant.getViews()) + " views");

            // Load the plant image from Firestore using Glide
            if (plant.getImages() != null && !plant.getImages().isEmpty()) {
                String imageUrl = plant.getImages().get(0); // Use the first image as shop preview
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder) // Show while loading
                        .error(R.drawable.image_failed)      // Show if loading fails
                        .into(plantImage);
            } else {
                plantImage.setImageResource(R.drawable.placeholder);
            }

            // Handles click on a plant card
            itemView.setOnClickListener(v -> {
                // Increment the view count in Firestore
                firestoreManager.incrementPlantViews(plant.getPlantid());

                // Start DetailsActivity with plant data
                Intent intent = new Intent(v.getContext(), DetailsActivity.class);
                intent.putExtra("plant_id", plant.getPlantid());
                intent.putExtra("plant_name", plant.getName());
                intent.putExtra("plant_category", plant.getCategory());
                intent.putExtra("plant_price", plant.getPrice());
                intent.putExtra("plant_sunlight", plant.getSunlight());
                intent.putExtra("plant_water", plant.getWater());
                intent.putExtra("plant_season", plant.getSeason());
                // intent.putExtra("plant_image", plant.getImageResource()); // not implemented yet
                if (plant.getImages() != null && !plant.getImages().isEmpty()) {
                    intent.putStringArrayListExtra("plant_images", new ArrayList<>(plant.getImages()));
                }
                v.getContext().startActivity(intent);
            });
        }
    }
}
