package com.example.sage.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

    private boolean pendingFavourite = false;
    private int plantId = -1;
    private Button favButton;
    private boolean isCurrentlyFavourite = false;

    private ActivityResultLauncher<Intent> loginLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.green));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.green));


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.details_root), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Register ActivityResultLauncher
        loginLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && pendingFavourite) {
                        addToFavourites();
                    }
                }
        );

        favButton = findViewById(R.id.favButton);
        favButton.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user == null) {
                pendingFavourite = true;
                Intent loginIntent = new Intent(DetailsActivity.this, LoginActivity.class);
                loginLauncher.launch(loginIntent);
            } else {
                addToFavourites();
            }
        });

        // Carousel
        List<String> imageUrls = getIntent().getStringArrayListExtra("plant_images");
        ViewPager2 viewPager = findViewById(R.id.image_carousel);
        ImageCarouselAdapter adapter = new ImageCarouselAdapter(imageUrls);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.image_dots);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            ImageView dot = new ImageView(this);
            dot.setImageResource(R.drawable.dot_inactive);
            tab.setCustomView(dot);
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null && tab.getCustomView() instanceof ImageView) {
                        ((ImageView) tab.getCustomView()).setImageResource(
                                i == position ? R.drawable.dot_active : R.drawable.dot_inactive
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

        // Check if already in favourites
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && plantId != -1) {
            new FirestoreManager().checkFavourite(plantId, user.getEmail(), isFav -> {
                isCurrentlyFavourite = isFav;
                updateFavButtonText();
            }, error -> Toast.makeText(this, "Error checking favourites", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateFavButtonText() {
        favButton.setText(isCurrentlyFavourite ? "Remove from Favourites" : "Add to Favourites");
    }

    private void addToFavourites() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("redirectTo", "favourites");
            loginLauncher.launch(intent);
            return;
        }

        if (plantId == -1) {
            Toast.makeText(this, "Error: Plant ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = currentUser.getEmail();
        if (email == null) {
            Toast.makeText(this, "Error: Email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirestoreManager firestoreManager = new FirestoreManager();

        firestoreManager.checkFavourite(
                plantId,
                email,
                isFavourite -> {
                    if (isFavourite) {
                        firestoreManager.deleteFavourite(
                                plantId,
                                email,
                                unused -> {
                                    Toast.makeText(this, "Removed from Favourites!", Toast.LENGTH_SHORT).show();
                                    isCurrentlyFavourite = false;
                                    updateFavButtonText();
                                    pendingFavourite = false;
                                },
                                error -> {
                                    Toast.makeText(this, "Failed to remove favourite: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    pendingFavourite = false;
                                }
                        );
                    } else {
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
