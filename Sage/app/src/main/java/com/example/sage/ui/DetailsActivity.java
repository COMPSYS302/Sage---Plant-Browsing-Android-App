package com.example.sage.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sage.R;
import com.example.sage.data.ImageCarouselAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.details_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Carousel setup
        List<Integer> carouselImages = Arrays.asList(
                R.drawable.ic_flowering,
                R.drawable.ic_flowering,
                R.drawable.ic_flowering
        );

        ViewPager2 viewPager = findViewById(R.id.image_carousel);
        ImageCarouselAdapter adapter = new ImageCarouselAdapter(carouselImages);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.image_dots);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            ImageView dot = new ImageView(this);
            dot.setImageResource(R.drawable.dot_inactive); // default dot
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
        if (name == null) name = "Unknown Plant";

        double price = getIntent().getDoubleExtra("plant_price", 0.0);

        String sunlight = getIntent().getStringExtra("plant_sunlight");
        if (sunlight == null) sunlight = "N/A";

        String water = getIntent().getStringExtra("plant_water");
        if (water == null) water = "N/A";

        String season = getIntent().getStringExtra("plant_season");
        if (season == null) season = "N/A";

        String description = getIntent().getStringExtra("plant_description");
        if (description == null) description = "No description available.";

        TextView descriptionView = findViewById(R.id.plant_description);
        TextView nameView = findViewById(R.id.plant_name);
        TextView priceView = findViewById(R.id.plant_price);
        TextView sunlightView = findViewById(R.id.plant_sunlight);
        TextView waterView = findViewById(R.id.plant_water);
        TextView seasonView = findViewById(R.id.plant_season);

        nameView.setText(name);
        priceView.setText(getString(R.string.price_format, price));
        sunlightView.setText(sunlight);
        waterView.setText(water);
        seasonView.setText(season);
        descriptionView.setText(description);
    }
}
