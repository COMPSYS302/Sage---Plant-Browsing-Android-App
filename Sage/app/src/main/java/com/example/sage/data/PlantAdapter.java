package com.example.sage.data;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sage.R;
import com.example.sage.ui.DetailsActivity;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    // Holds the current list of plants to display
    private List<Plant> plantList;

    // Constructor initializes the adapter with the plant list
    public PlantAdapter(List<Plant> plantList) {
        this.plantList = plantList;
    }

    // Replaces the existing data with a new filtered list
    public void updateData(List<Plant> newPlantList) {
        this.plantList = newPlantList;
        notifyDataSetChanged(); // Notify the RecyclerView to refresh
    }

    // Inflates the item layout and returns a new ViewHolder
    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
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

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantName = itemView.findViewById(R.id.plantName);
            plantCategory = itemView.findViewById(R.id.plantCategory);
            plantPrice = itemView.findViewById(R.id.plantPrice);
            plantImage = itemView.findViewById(R.id.plant_image);
        }

        // Populates the plant item with data
        public void bindView(Plant plant) {
            plantName.setText(plant.getName());
            plantCategory.setText(plant.getCategory());
            plantPrice.setText("$" + String.format("%.2f", plant.getPrice()));

            // Set plant image dynamically (replace placeholder)
            //plantImage.setImageResource(plant.getImageResource());

            // Handles click on a plant card
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), DetailsActivity.class);
                intent.putExtra("plant_name", plant.getName());
                intent.putExtra("plant_category", plant.getCategory());
                intent.putExtra("plant_price", plant.getPrice());
                intent.putExtra("plant_sunlight", plant.getSunlight());
                intent.putExtra("plant_water", plant.getWater());
                intent.putExtra("plant_season", plant.getSeason());
                // intent.putExtra("plant_image", plant.getImageResource()); // not implemented yet

                v.getContext().startActivity(intent);
            });
        }
    }
}
