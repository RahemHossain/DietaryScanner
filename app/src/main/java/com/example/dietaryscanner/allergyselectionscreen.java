package com.example.dietaryscanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch; // IMPORTANT: Import the Switch class
import androidx.appcompat.app.AppCompatActivity;

public class allergyselectionscreen extends AppCompatActivity {

    // --- SharedPreferences constants ---
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_HALAL_KEY = "halal_preference";
    private static final String PREF_KOSHER_KEY = "kosher_preference";
    private static final String PREF_VEGAN_KEY = "vegan_preference";
    private static final String PREF_VEGETARIAN_KEY = "vegetarian_preference";

    private SharedPreferences sharedPreferences;

    // --- UI component variables ---
    private Button doneButton;
    private Switch switchHalal;
    private Switch switchKosher;
    private Switch switchVegan;
    private Switch switchVegetarian;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allergyselection);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Find the UI components from the XML layout
        findViews();

        // Load the previously saved state of the switches
        loadSwitchStates();

        // Set up listeners for each switch to save changes as they happen
        setupSwitchListeners();

        // Set up the listener for the Done button
        setupDoneButtonListener();
    }

    /**
     * Finds and initializes all the UI components from the layout.
     */
    private void findViews() {
        try {
            doneButton = findViewById(R.id.done_button);
            switchHalal = findViewById(R.id.switch_halal);
            switchKosher = findViewById(R.id.switch_kosher);
            switchVegan = findViewById(R.id.switch_vegan);
            switchVegetarian = findViewById(R.id.switch_vegetarian);
        } catch (NullPointerException e) {
            // This catches cases where an ID is not found.
            // Helps in debugging if a view is missing.
            Log.e("AllergySelection", "Error finding a view by its ID: " + e.getMessage());
        }
    }

    /**
     * Loads the saved state of the switches from SharedPreferences and sets the UI.
     */
    private void loadSwitchStates() {
        if (switchHalal != null) {
            boolean isHalalEnabled = sharedPreferences.getBoolean(PREF_HALAL_KEY, false);
            switchHalal.setChecked(isHalalEnabled);
        }
        if (switchKosher != null) {
            boolean isKosherEnabled = sharedPreferences.getBoolean(PREF_KOSHER_KEY, false);
            switchKosher.setChecked(isKosherEnabled);
        }
        if (switchVegan != null) {
            boolean isVeganEnabled = sharedPreferences.getBoolean(PREF_VEGAN_KEY, false);
            switchVegan.setChecked(isVeganEnabled);
        }
        if (switchVegetarian != null) {
            boolean isVegetarianEnabled = sharedPreferences.getBoolean(PREF_VEGETARIAN_KEY, false);
            switchVegetarian.setChecked(isVegetarianEnabled);
        }
    }

    /**
     * Sets up the OnCheckedChangeListener for each switch to save its state.
     */
    private void setupSwitchListeners() {
        if (switchHalal != null) {
            switchHalal.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveBooleanPreference(PREF_HALAL_KEY, isChecked);
            });
        }
        if (switchKosher != null) {
            switchKosher.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveBooleanPreference(PREF_KOSHER_KEY, isChecked);
            });
        }
        if (switchVegan != null) {
            switchVegan.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveBooleanPreference(PREF_VEGAN_KEY, isChecked);
            });
        }
        if (switchVegetarian != null) {
            switchVegetarian.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveBooleanPreference(PREF_VEGETARIAN_KEY, isChecked);
            });
        }
    }

    /**
     * Helper method to save a boolean preference to SharedPreferences.
     */
    private void saveBooleanPreference(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply(); // Use apply() for asynchronous saving
    }

    /**
     * Sets up the OnClickListener for the Done button.
     */
    private void setupDoneButtonListener() {
            doneButton.setOnClickListener(v -> {
                // When the "Done" button is clicked, the state of the switches has already
                // been saved by the listeners. You can now proceed to the next Activity.
                Intent intent = new Intent(allergyselectionscreen.this, homescreen.class);
                startActivity(intent);
            });
    }
}