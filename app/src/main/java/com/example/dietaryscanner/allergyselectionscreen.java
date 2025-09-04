package com.example.dietaryscanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.HashSet;
import java.util.Set;

public class allergyselectionscreen extends AppCompatActivity {

    // --- SharedPreferences constants ---
    private static final String PREFS_NAME = "dietary_preferences";
    private static final String PREF_SET_KEY = "selected_preferences";

    private SharedPreferences sharedPreferences;

    // --- UI component variables ---
    private Button doneButton;
    private CardView customCard;
    private Switch switchHalal;
    private Switch switchKosher;
    private Switch switchVegan;
    private Switch switchVegetarian;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allergyselection);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        findViews();
        loadSwitchStates();
        setupSwitchListeners();
        setupDoneButtonListener();

        // This method will now work correctly
        setupCustomButtonListener();
    }

    /**
     * Finds and initializes all the UI components from the layout.
     */
    private void findViews() {
        try {
            doneButton = findViewById(R.id.done_button);
            customCard = findViewById(R.id.card_add_custom); // Find the CardView by its new ID
            switchHalal = findViewById(R.id.switch_halal);
            switchKosher = findViewById(R.id.switch_kosher);
            switchVegan = findViewById(R.id.switch_vegan);
            switchVegetarian = findViewById(R.id.switch_vegetarian);
        } catch (NullPointerException e) {
            Log.e("AllergySelection", "Error finding a view by its ID: " + e.getMessage());
        }
    }

    /**
     * Sets up the OnClickListener for the Custom card to launch the CustomPreferencesActivity.
     */
    private void setupCustomButtonListener() {
        if (customCard != null) {
            customCard.setOnClickListener(v -> {
                Log.d("AllergySelection", "Custom card clicked. Launching CustomPreferencesActivity.");
                Intent intent = new Intent(allergyselectionscreen.this, CustomPreferencesActivity.class);
                startActivity(intent);
            });
        }
    }

    /**
     * Loads the saved state of the switches from SharedPreferences and sets the UI.
     */
    private void loadSwitchStates() {
        Set<String> savedPreferences = sharedPreferences.getStringSet(PREF_SET_KEY, new HashSet<>());

        Log.d("AllergySelection", "Loading saved preferences: " + savedPreferences.toString());

        if (switchHalal != null) {
            boolean isHalalEnabled = savedPreferences.contains("halal");
            switchHalal.setChecked(isHalalEnabled);
            Log.d("AllergySelection", "Halal switch set to: " + isHalalEnabled);
        }
        if (switchKosher != null) {
            boolean isKosherEnabled = savedPreferences.contains("kosher");
            switchKosher.setChecked(isKosherEnabled);
            Log.d("AllergySelection", "Kosher switch set to: " + isKosherEnabled);
        }
        if (switchVegan != null) {
            boolean isVeganEnabled = savedPreferences.contains("vegan");
            switchVegan.setChecked(isVeganEnabled);
            Log.d("AllergySelection", "Vegan switch set to: " + isVeganEnabled);
        }
        if (switchVegetarian != null) {
            boolean isVegetarianEnabled = savedPreferences.contains("vegetarian");
            switchVegetarian.setChecked(isVegetarianEnabled);
            Log.d("AllergySelection", "Vegetarian switch set to: " + isVegetarianEnabled);
        }
    }

    /**
     * Sets up the OnCheckedChangeListener for each switch to save its state.
     */
    private void setupSwitchListeners() {
        if (switchHalal != null) {
            switchHalal.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updatePreferenceSet("halal", isChecked);
            });
        }
        if (switchKosher != null) {
            switchKosher.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updatePreferenceSet("kosher", isChecked);
            });
        }
        if (switchVegan != null) {
            switchVegan.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updatePreferenceSet("vegan", isChecked);
            });
        }
        if (switchVegetarian != null) {
            switchVegetarian.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updatePreferenceSet("vegetarian", isChecked);
            });
        }
    }

    /**
     * Helper method to update the preference set.
     */
    private void updatePreferenceSet(String preference, boolean isEnabled) {
        Set<String> currentPreferences = new HashSet<>(sharedPreferences.getStringSet(PREF_SET_KEY, new HashSet<>()));

        if (isEnabled) {
            currentPreferences.add(preference);
            Log.d("AllergySelection", "Added preference: " + preference);
        } else {
            currentPreferences.remove(preference);
            Log.d("AllergySelection", "Removed preference: " + preference);
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(PREF_SET_KEY, currentPreferences);
        editor.apply();

        Log.d("AllergySelection", "Updated preferences set: " + currentPreferences.toString());
    }

    /**
     * Sets up the OnClickListener for the Done button.
     */
    private void setupDoneButtonListener() {
        if (doneButton != null) {
            doneButton.setOnClickListener(v -> {
                Set<String> finalPreferences = sharedPreferences.getStringSet(PREF_SET_KEY, new HashSet<>());
                Log.d("AllergySelection", "Final saved preferences: " + finalPreferences.toString());

                Intent intent = new Intent(allergyselectionscreen.this, homescreen.class);
                startActivity(intent);
                finish();
            });
        }
    }
}