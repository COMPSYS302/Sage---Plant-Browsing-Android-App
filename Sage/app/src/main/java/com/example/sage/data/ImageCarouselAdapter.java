package com.example.sage.data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sage.R;

import java.util.List;

public class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.CarouselViewHolder> {

    private final List<String> imageUrls; // List of image URLs

    public ImageCarouselAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }


    /**
        This method is called when the ViewHolder needs to be created.
        It inflates the layout for each item and returns a new ViewHolder.
     */
    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carousel_image, parent, false);
        return new CarouselViewHolder(view);
    }

    /**
     * This method is called by the RecyclerView to display the data at the specified position.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        // Load image from URL using Glide
        String url = imageUrls.get(position);
        Glide.with(holder.imageView.getContext())
                .load(url)
                .placeholder(R.drawable.placeholder) // Show this while loading
                .error(R.drawable.image_failed)      // Show this on load error
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }


    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.carousel_image);
        }
    }
}
