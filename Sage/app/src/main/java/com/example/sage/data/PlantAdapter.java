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
import com.example.sage.FavouritesActivity;
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
    private final OnItemRemovedListener removeListener;
    private List<Integer> favouriteIds = new ArrayList<>(); // Optional, empty by default


    // Constructor initializes the adapter with the plant list, FirestoreManager, and layout ID
    public PlantAdapter(List<Plant> plantList, FirestoreManager firestoreManager, int layoutId,boolean showDelete,OnItemRemovedListener removeListener) {
        this.plantList = new ArrayList<>(plantList);
        this.fullPlantList = new ArrayList<>(plantList); // Store a full copy for filtering
        this.firestoreManager = firestoreManager;
        this.layoutId = layoutId;
        this.showDelete = showDelete;
        this.removeListener = removeListener;
    }
    public interface OnItemRemovedListener {
        void onItemRemoved(double price);
    }
    public List<Plant> getCurrentData() {
        return new ArrayList<>(plantList);
    }

    /** Replaces the existing data with a new filtered list
     *
     * @param newPlantList The new list of plants to display
     */
    public void updateData(List<Plant> newPlantList) {
        this.plantList = new ArrayList<>(newPlantList);
        notifyDataSetChanged(); // Notify the RecyclerView to refresh
    }

    /** Filters the list based on a search query
     *
     * @param query The search query
     */
    public void setFilteredList(String query) {
        List<Plant> filteredList = new ArrayList<>();
        // Loop through the full list and add matching plants to the filtered list
        for (Plant plant : fullPlantList) {
            if (plant.getName().toLowerCase().contains(query.toLowerCase()) ||
                    plant.getCategory().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(plant);
            }
        }

        plantList = filteredList;
        notifyDataSetChanged();
    }

    /**
     * Removes an item from the list and notifies the adapter
     * @param position The position of the item to remove
     */
    public void removeItem(int position) {
        plantList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, plantList.size());
    }

    /** Inflates the item layout and returns a new ViewHolder
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false); // Use dynamic layout ID
        return new PlantViewHolder(view, firestoreManager,this,showDelete);
    }

    /** Binds each plant in the list to the ViewHolder
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        holder.bindView(plantList.get(position));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
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

        // Constructor initialises the ViewHolder with the view and FirestoreManager
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

        /**
         * Binds the plant data to the view
         * @param plant
         */
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
                plantImage.setImageResource(R.drawable.placeholder);// Placeholder error image
            }

            // Set the visibility of the delete button
            if (deleteButton != null) {
                Log.d("DELETE_BUTTON", "setting visibility, showDelete = " + showDelete);
                deleteButton.setVisibility(showDelete ? View.VISIBLE : View.GONE);
                if (showDelete) {
                    // Click to delete
                    deleteButton.setOnClickListener(v -> {
                        int pos = getBindingAdapterPosition(); // Get the position of the item
                        if (pos != RecyclerView.NO_POSITION) {
                            // user should already be logged in
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String email = currentUser.getEmail();
                            // Shake animation on the card before delete
                            itemView.animate()
                                    .translationXBy(25f) // Move right
                                    .setDuration(50)
                                    .withEndAction(() -> itemView.animate()
                                            .translationXBy(-50f) // Move left
                                            .setDuration(100)
                                            .withEndAction(() -> itemView.animate()
                                                    .translationXBy(25f) // Move back to center
                                                    .setDuration(50)
                                                    .withEndAction(() -> {
                                                        // Remove from favourites
                                                        firestoreManager.deleteFavourite(
                                                                plant.getPlantid(),
                                                                email,
                                                                unused -> {
                                                                    Toast.makeText(itemView.getContext(), "Removed from Favourites!", Toast.LENGTH_SHORT).show();
                                                                    if (adapter.removeListener != null) {
                                                                        adapter.removeListener.onItemRemoved(plant.getPrice());
                                                                    }
                                                                },
                                                                error -> {
                                                                    Toast.makeText(itemView.getContext(), "Failed to remove favourite: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                });
                                                        adapter.removeItem(pos); // Remove from list
                                                    })
                                                    .start())
                                            .start())
                                    .start();
                        }
                    });
                }
            }




            // Handles click on a plant card
            itemView.setOnClickListener(v -> {
                // Increment the view count in Firestore
                firestoreManager.incrementPlantViews(plant.getPlantid());

                // Launch DetailsActivity with plant data
                Intent intent = new Intent(v.getContext(), DetailsActivity.class);
                intent.putExtra("plant_id", plant.getPlantid());
                intent.putExtra("plant_name", plant.getName());
                intent.putExtra("plant_description",plant.getDescription());
                intent.putExtra("plant_category", plant.getCategory());
                intent.putExtra("plant_price", plant.getPrice());
                intent.putExtra("plant_sunlight", plant.getSunlight());
                intent.putExtra("plant_water", plant.getWater());
                intent.putExtra("plant_season", plant.getSeason());
                if (plant.getImages() != null && !plant.getImages().isEmpty()) {
                    intent.putStringArrayListExtra("plant_images", new ArrayList<>(plant.getImages()));
                }
                v.getContext().startActivity(intent);
            });


        }
    }
}
