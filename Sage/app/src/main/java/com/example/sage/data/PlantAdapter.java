package com.example.sage.data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sage.R;
import com.example.sage.data.Plant;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    // This list holds all the plants to be displayed
    private final List<Plant> plantList;

    // Constructor to initialize the adapter with a list of plants
    public PlantAdapter(List<Plant> plantList) {
        this.plantList = plantList;
    }

    // Inflates the layout for each plant item
    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    // Binds plant data to the item views
    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        holder.bindView(plantList.get(position));
    }

    // Returns the total number of plant items
    @Override
    public int getItemCount() {
        return plantList.size();
    }

    // ViewHolder class that holds and binds each plant item
    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        private final TextView plantName;
        private final ImageView plantImage;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantName = itemView.findViewById(R.id.plantName);
            plantImage = itemView.findViewById(R.id.plant_image);
        }

        // Binds the plant object to the item view
        public void bindView(Plant plant) {
            plantName.setText(plant.getName());

            // Set a default image. You can replace this with an image loading library later.
            plantImage.setImageResource(R.drawable.ic_logo);

            // Set click behavior for each plant item
            itemView.setOnClickListener(v -> {
                // You can replace this with an intent to go to a detail page
                Toast.makeText(v.getContext(), "Clicked: " + plant.getName(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
