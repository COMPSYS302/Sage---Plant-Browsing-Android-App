package com.example.sage.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sage.LoginActivity;
import com.example.sage.R;
import com.example.sage.data.ImageCarouselAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.sage.data.FirestoreManager;

import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    // Request code for starting LoginActivity and receiving a result back
    private static final int LOGIN_REQUEST_CODE = 1001;

    // Flag to track if user tried to add to favourites but needs to login first
    private boolean pendingFavourite = false;

    //store the plantID
    private int plantId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        // Set the layout file for this activity
        setContentView(R.layout.activity_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.details_root), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the "Add to favourites" button in the layout
        Button favButton = findViewById(R.id.favButton);

        // Set click listener on the favourites button
        favButton.setOnClickListener(view -> {
            // Get the current logged-in Firebase user
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user == null) {
                // User is not logged in, so set flag and start LoginActivity for result
                pendingFavourite = true;
                Intent loginIntent = new Intent(DetailsActivity.this, LoginActivity.class);
                startActivityForResult(loginIntent, LOGIN_REQUEST_CODE);
            } else {
                // User is logged in, proceed to add to favourites immediately
                addToFavourites(); // Implement your Firestore logic here
            }
        });

        // Get plant images URLs passed from previous activity for the image carousel
        List<String> imageUrls = getIntent().getStringArrayListExtra("plant_images");

        // Setup the ViewPager2 to display images
        ViewPager2 viewPager = findViewById(R.id.image_carousel);
        ImageCarouselAdapter adapter = new ImageCarouselAdapter(imageUrls);
        viewPager.setAdapter(adapter);

        // Setup TabLayout to show dots indicating current image in carousel
        TabLayout tabLayout = findViewById(R.id.image_dots);

        // Connect TabLayout with ViewPager2 using TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            ImageView dot = new ImageView(this);
            dot.setImageResource(R.drawable.dot_inactive); // default state is inactive dot
            tab.setCustomView(dot);
        }).attach();

        // Update the dots as the user scrolls through images
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Loop through all tabs (dots)
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);

                    // Change dot drawable to active/inactive based on current page
                    if (tab != null && tab.getCustomView() instanceof ImageView) {
                        ((ImageView) tab.getCustomView()).setImageResource(
                                i == position ? R.drawable.dot_active : R.drawable.dot_inactive
                        );
                    }
                }
            }
        });

        // Retrieve plant details passed from the previous activity
        String name = getIntent().getStringExtra("plant_name");
        if (name == null) name = "Unknown Plant";  // fallback default

        double price = getIntent().getDoubleExtra("plant_price", 0.0);

        String sunlight = getIntent().getStringExtra("plant_sunlight");
        if (sunlight == null) sunlight = "N/A";

        String water = getIntent().getStringExtra("plant_water");
        if (water == null) water = "N/A";

        String season = getIntent().getStringExtra("plant_season");
        if (season == null) season = "N/A";

        String description = getIntent().getStringExtra("plant_description");
        if (description == null) description = "No description available.";

        // Find the TextViews in layout for displaying plant details
        TextView descriptionView = findViewById(R.id.plant_description);
        TextView nameView = findViewById(R.id.plant_name);
        TextView priceView = findViewById(R.id.plant_price);
        TextView sunlightView = findViewById(R.id.plant_sunlight);
        TextView waterView = findViewById(R.id.plant_water);
        TextView seasonView = findViewById(R.id.plant_season);

        // Set plant detail values into the views
        nameView.setText(name);
        priceView.setText(getString(R.string.price_format, price));
        sunlightView.setText(sunlight);
        waterView.setText(water);
        seasonView.setText(season);
        descriptionView.setText(description);

        //store the plantid for addToFavourites() method
        plantId = getIntent().getIntExtra("plant_id",-1);
    }

    // Handle result returned from LoginActivity when user logs in or cancels
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if this is the result from the login request and login was successful
        if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            if (pendingFavourite) {
                // User logged in, resume adding to favourites as intended before login
                addToFavourites();
            }
        }
    }

    // Method to add the current plant to favourites
    private void addToFavourites() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // User is not signed in, redirect to login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("redirectTo", "favourites"); // Optional: handle redirect after login
            startActivity(intent);
            return;
        }

        if (plantId == -1) {
            // Plant ID was not passed properly
            Toast.makeText(this, "Error: Plant ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user's email
        String email = currentUser.getEmail();
        if (email == null) {
            Toast.makeText(this, "Error: Email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add plant ID to user's favourites in Firestore
        FirestoreManager firestoreManager = new FirestoreManager();
        firestoreManager.addIdToFavourite(
                plantId,
                email,
                unused -> {
                    Toast.makeText(this, "Added to Favourites!", Toast.LENGTH_SHORT).show();
                    pendingFavourite = false;
                },
                error -> {
                    Toast.makeText(this, "Failed to add to favourites: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    pendingFavourite = false;
                }
        );
    }
}
