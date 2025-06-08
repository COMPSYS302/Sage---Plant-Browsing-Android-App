package com.example.sage.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sage.LoginActivity;
import com.example.sage.R;
import com.example.sage.data.ImageCarouselAdapter;
import com.example.sage.data.FirestoreManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    private boolean pendingFavourite = false; // flag to track if the user is trying to add a favourite
    private int plantId = -1;
    private Button favButton;
    private boolean isCurrentlyFavourite = false; //  Boolean to track if the plant is a favourite

    private Animation scaleUp; // Animation for scaling up
    private Animation scaleDown; // Animation for scaling down


    private ActivityResultLauncher<Intent> loginLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.details_root), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);

        // Register ActivityResultLauncher
        loginLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && pendingFavourite) {
                        addToFavourites();
                    }
                }
        );

        favButton = findViewById(R.id.favButton); // Initialise favButton
        favButton.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // Check if user is logged in

            if (user == null) { // If not, launch login activity
                pendingFavourite = true;
                Intent loginIntent = new Intent(DetailsActivity.this, LoginActivity.class);
                loginLauncher.launch(loginIntent);
            } else {
                addToFavourites(); // If user is logged in, add to favourites
            }
        });

        // Carousel
        List<String> imageUrls = getIntent().getStringArrayListExtra("plant_images");
        ViewPager2 viewPager = findViewById(R.id.image_carousel);
        ImageCarouselAdapter adapter = new ImageCarouselAdapter(imageUrls); // Pass the image URLs
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.image_dots); // Initialise tabLayout indicators for carousel
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            ImageView dot = new ImageView(this); // Create a new ImageView for each dot
            dot.setImageResource(R.drawable.dot_inactive); // Set the inactive dot image
            tab.setCustomView(dot);
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() { // Update the dot images when the page changes

            /**
             * Called when the selected page has been changed.
             * @param position Position index of the new selected page.
             */
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                for (int i = 0; i < tabLayout.getTabCount(); i++) {  // Loop through all tabs
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null && tab.getCustomView() instanceof ImageView) { // Check if the custom view is an ImageView
                        ((ImageView) tab.getCustomView()).setImageResource(
                                i == position ? R.drawable.dot_active : R.drawable.dot_inactive // Set the active or inactive dot image
                        );
                    }
                }
            }
        });

        // Plant details
        String name = getIntent().getStringExtra("plant_name");
        String category = getIntent().getStringExtra("plant_category");
        double price = getIntent().getDoubleExtra("plant_price", 0.0);
        String sunlight = getIntent().getStringExtra("plant_sunlight");
        String water = getIntent().getStringExtra("plant_water");
        String season = getIntent().getStringExtra("plant_season");
        String description = getIntent().getStringExtra("plant_description");
        plantId = getIntent().getIntExtra("plant_id", -1);

        ((TextView) findViewById(R.id.plant_name)).setText(name != null ? name : "Unknown Plant");
        ((TextView) findViewById(R.id.plant_category)).setText(getIntent().getStringExtra("plant_category"));
        ((TextView) findViewById(R.id.plant_price)).setText(getString(R.string.price_format, price));
        ((TextView) findViewById(R.id.plant_sunlight)).setText(sunlight != null ? sunlight : "N/A");
        ((TextView) findViewById(R.id.plant_water)).setText(water != null ? water : "N/A");
        ((TextView) findViewById(R.id.plant_season)).setText(season != null ? season : "N/A");
        ((TextView) findViewById(R.id.plant_description)).setText(description != null ? description : "No description available.");


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // Check if user is logged in
        if (user != null && plantId != -1) { // Check if user is logged in and plant ID is valid
            new FirestoreManager().checkFavourite(plantId, user.getEmail(), isFav -> { // Check if plant is a favourite
                isCurrentlyFavourite = isFav;
                updateFavButtonText(); // Update the button text
            }, error -> Toast.makeText(this, "Error checking favourites", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Animates the favourite button text change with a zoom animation
     */
    private void updateFavButtonText() {
        // Set correct text immediately
        favButton.setText(isCurrentlyFavourite ? "Remove from Favourites" : "Add to Favourites");// Set the button text, Add if not favourite, Remove if favourite

        // zoom animation, scale up then scale down
        favButton.startAnimation(scaleUp);
        scaleUp.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {
                favButton.startAnimation(scaleDown);
            }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
    }



    /**
     * Adds the plant to the user's favourites
     * If the user is not logged in, they will be redirected to the login page
     */
    private void addToFavourites() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {// If not logged in, redirect to login
            Intent intent = new Intent(this, LoginActivity.class); // Launch login activity
            intent.putExtra("redirectTo", "favourites");// Redirect back to
            loginLauncher.launch(intent);
            return;
        }

        if (plantId == -1) { // If plant ID is missing, show error message
            Toast.makeText(this, "Error: Plant ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = currentUser.getEmail();// Get the user's email
        if (email == null) {
            Toast.makeText(this, "Error: Email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirestoreManager firestoreManager = new FirestoreManager(); // Create a FirestoreManager instance

        // Check if the plant is already a favourite
        firestoreManager.checkFavourite(
                plantId,
                email,
                isFavourite -> {
                    if (isFavourite) { // If it is, remove it
                        firestoreManager.deleteFavourite(
                                plantId,
                                email,
                                unused -> {
                                    Toast.makeText(this, "Removed from Favourites!", Toast.LENGTH_SHORT).show();
                                    isCurrentlyFavourite = false;
                                    updateFavButtonText();
                                    pendingFavourite = false; // Reset flag
                                },
                                error -> {
                                    Toast.makeText(this, "Failed to remove favourite: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    pendingFavourite = false;
                                }
                        );
                    } else { // If it isn't, add it
                        firestoreManager.addIdToFavourite(
                                plantId,
                                email,
                                unused -> {
                                    Toast.makeText(this, "Added to Favourites!", Toast.LENGTH_SHORT).show();
                                    isCurrentlyFavourite = true;
                                    updateFavButtonText();
                                    pendingFavourite = false;
                                },
                                error -> {
                                    Toast.makeText(this, "Failed to add to favourites: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    pendingFavourite = false;
                                }
                        );
                    }
                },
                error -> {
                    Toast.makeText(this, "Error checking favourites: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    pendingFavourite = false;
                }
        );
    }
}
