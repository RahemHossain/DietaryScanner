package com.example.dietaryscanner; // Or your actual package name

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

// 1. Import the generated binding class for homescreen.xml
import com.example.dietaryscanner.databinding.HomescreenBinding;

public class homescreen extends AppCompatActivity {

    private HomescreenBinding binding;
    private ImageView navSaved, navHome, navBarcode, navMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 3. Inflate the layout using the HomescreenBinding class
        // 2. Declare a variable for the binding class
        // Changed from ActivityMainBinding
        binding = HomescreenBinding.inflate(getLayoutInflater());

        // 4. Set the content view to the root of the binding
        setContentView(binding.getRoot());

        // Initialize navigation ImageViews
        navSaved = binding.navSaved;
        navHome = binding.navHome;
        navBarcode = binding.navBarcode;
        navMenu = binding.navMenu;

        // Initial fragment replacement (if you still need this)
        if (savedInstanceState == null) { // Good practice to only add initial fragment if savedInstanceState is null
            replaceFragment(new HomeFragment());
        }

        // Set up click listeners for navigation items
        setupNavigationListeners();
    }

    private void setupNavigationListeners() {
        navSaved.setOnClickListener(v -> {
            replaceFragment(new SavedFragment());
            updateNavigationTints(navSaved);
        });

        navHome.setOnClickListener(v -> {
            replaceFragment(new HomeFragment());
            updateNavigationTints(navHome);
        });

        navBarcode.setOnClickListener(v -> {
            // Navigate to camera screen
            Intent intent = new Intent(homescreen.this, cameraActivity.class);
            startActivity(intent);
            // Note: Don't update tints for barcode since it's launching a new activity
        });

        navMenu.setOnClickListener(v -> {
            replaceFragment(new SettingsFragment());
            updateNavigationTints(navMenu);
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Make sure homescreen.xml has a FrameLayout with the ID "frame_layout"
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    // Helper method to update navigation item tints
    private void updateNavigationTints(ImageView selectedItem) {
        // Reset all to gray
        navSaved.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));
        navHome.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));
        navBarcode.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));
        navMenu.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));

        // Set selected to blue
        selectedItem.setColorFilter(Color.parseColor("#007AFF"));
    }
}