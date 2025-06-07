package com.example.sage.data;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sage.R;
import com.example.sage.ui.DetailsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    private final boolean showDelete;
    private List<Integer> favouriteIds = new ArrayList<>(); // Optional, empty by default


    // Constructor initializes the adapter with the plant list, FirestoreManager, and layout ID
    public PlantAdapter(List<Plant> plantList, FirestoreManager firestoreManager, int layoutId,boolean showDelete) {
        this.plantList = new ArrayList<>(plantList);
        this.fullPlantList = new ArrayList<>(plantList); // Store a full copy for filtering
        this.firestoreManager = firestoreManager;
        this.layoutId = layoutId;
        this.showDelete = showDelete;
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
    // used to remove favouriate item from the page
    public void removeItem(int position) {
        plantList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, plantList.size());
    }

    // Inflates the item layout and returns a new ViewHolder
    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false); // Use dynamic layout ID
        return new PlantViewHolder(view, firestoreManager,this,showDelete);
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

        private final ImageButton deleteButton;
        private final FirestoreManager firestoreManager;
        private final PlantAdapter adapter;

        private final Boolean showDelete;

        public PlantViewHolder(@NonNull View itemView, FirestoreManager firestoreManager, PlantAdapter adapter,boolean showDelete) {
            super(itemView);
            this.firestoreManager = firestoreManager;
            this.adapter = adapter;
            this.showDelete = showDelete;
            // View binding
            plantName = itemView.findViewById(R.id.plantName);
            plantCategory = itemView.findViewById(R.id.plantCategory);
            plantPrice = itemView.findViewById(R.id.plantPrice);
            plantImage = itemView.findViewById(R.id.plant_image);
            plantViews = itemView.findViewById(R.id.plantViews);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        // Populates the plant item with data
        public void bindView(Plant plant) {
            Log.d("DELETE_BUTTON", "showDelete: " + showDelete);

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

            if (deleteButton != null) {
                Log.d("DELETE_BUTTON", "setting visibility, showDelete = " + showDelete);
                deleteButton.setVisibility(showDelete ? View.VISIBLE : View.GONE);
                if (showDelete) {
                    // Click to delete
                    deleteButton.setOnClickListener(v -> {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            // no need for currentUser check or email validity check, as user should already login
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String email = currentUser.getEmail();
                            firestoreManager.deleteFavourite(
                                    plant.getPlantid(),
                                    email,
                                    unused -> {
                                        Toast.makeText(itemView.getContext(), "Removed from Favourites!", Toast.LENGTH_SHORT).show();

                                    },
                                    error -> {
                                        Toast.makeText(itemView.getContext(), "Failed to remove favourite: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                                    });
                            adapter.removeItem(pos); // Remove from list
                        }
                    });
                }
            }



            // Handles click on a plant card
            itemView.setOnClickListener(v -> {
                // Increment the view count in Firestore
                firestoreManager.incrementPlantViews(plant.getPlantid());

                // Start DetailsActivity with plant data
                Intent intent = new Intent(v.getContext(), DetailsActivity.class);
                intent.putExtra("plant_id", plant.getPlantid());

                intent.putExtra("plant_name", plant.getName());
                intent.putExtra("plant_description",plant.getDescription());
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
