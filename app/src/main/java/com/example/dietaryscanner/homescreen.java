package com.example.dietaryscanner; // Or your actual package name

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

// 1. Import the generated binding class for homescreen.xml
import com.example.dietaryscanner.databinding.HomescreenBinding;

public class homescreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 3. Inflate the layout using the HomescreenBinding class
        // 2. Declare a variable for the binding class
        // Changed from ActivityMainBinding
        HomescreenBinding binding = HomescreenBinding.inflate(getLayoutInflater());

        // 4. Set the content view to the root of the binding
        setContentView(binding.getRoot());

        // Initial fragment replacement (if you still need this)
        if (savedInstanceState == null) { // Good practice to only add initial fragment if savedInstanceState is null
            replaceFragment(new HomeFragment());
        }


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.homeFragment) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.savedFragment) {
                replaceFragment(new SavedFragment());
            } else if (itemId == R.id.settingsFragment) {
                replaceFragment(new SettingsFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Make sure homescreen.xml has a FrameLayout with the ID "frame_layout"
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
