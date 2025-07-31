package com.example.dietaryscanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PreferencesFile";
    private static final String KEY_HALAL = "halal";
    private static final String KEY_KOSHER = "kosher";
    private static final String KEY_VEGAN = "vegan";
    private static final String KEY_VEGETARIAN = "vegetarian";

    private Switch switchHalal, switchKosher, switchVegan, switchVegetarian;
    private Button doneButton;
    private View addCustomCard;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadPreferences();
        setListeners();
    }

    private void initViews() {
        switchHalal = findViewById(R.id.switch_halal);
        switchKosher = findViewById(R.id.switch_kosher);
        switchVegan = findViewById(R.id.switch_vegan);
        switchVegetarian = findViewById(R.id.switch_vegetarian);
        doneButton = findViewById(R.id.done_button);

        // Find the "Add Custom" card - it's the last CardView in the options section
        LinearLayout optionsSection = findViewById(R.id.options_section);
        addCustomCard = optionsSection.getChildAt(4); // The 5th child (0-indexed)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void loadPreferences() {
        switchHalal.setChecked(sharedPreferences.getBoolean(KEY_HALAL, false));
        switchKosher.setChecked(sharedPreferences.getBoolean(KEY_KOSHER, false));
        switchVegan.setChecked(sharedPreferences.getBoolean(KEY_VEGAN, false));
        switchVegetarian.setChecked(sharedPreferences.getBoolean(KEY_VEGETARIAN, false));

        updateDoneButtonState();
    }

    private void setListeners() {
        // Switch listeners
        switchHalal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_HALAL, isChecked);
            updateDoneButtonState();
            updateSwitchColors(switchHalal, isChecked);
        });

        switchKosher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_KOSHER, isChecked);
            updateDoneButtonState();
            updateSwitchColors(switchKosher, isChecked);
        });

        switchVegan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_VEGAN, isChecked);
            updateDoneButtonState();
            updateSwitchColors(switchVegan, isChecked);
        });

        switchVegetarian.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_VEGETARIAN, isChecked);
            updateDoneButtonState();
            updateSwitchColors(switchVegetarian, isChecked);
        });

        // Done button listener
        doneButton.setOnClickListener(v -> {
            if (hasAnyPreferenceSelected()) {
                // Handle done action - you can add your logic here
                Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show();
                // For example: finish() or navigate to next screen
            } else {
                Toast.makeText(this, "Please select at least one preference", Toast.LENGTH_SHORT).show();
            }
        });

        // Add Custom card click listener
        addCustomCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CustomPreferencesActivity.class);
            startActivity(intent);
        });
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void updateSwitchColors(Switch switchView, boolean isChecked) {
        if (isChecked) {
            switchView.getThumbDrawable().setTint(getResources().getColor(android.R.color.holo_blue_bright));
            switchView.getTrackDrawable().setTint(getResources().getColor(android.R.color.holo_blue_light));
        } else {
            switchView.getThumbDrawable().setTint(getResources().getColor(android.R.color.darker_gray));
            switchView.getTrackDrawable().setTint(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private boolean hasAnyPreferenceSelected() {
        return switchHalal.isChecked() || switchKosher.isChecked() ||
                switchVegan.isChecked() || switchVegetarian.isChecked();
    }

    private void updateDoneButtonState() {
        if (hasAnyPreferenceSelected()) {
            doneButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(android.R.color.holo_blue_bright)));
            doneButton.setEnabled(true);
        } else {
            doneButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(android.R.color.darker_gray)));
            doneButton.setEnabled(true); // Keep enabled but visually different
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload preferences in case custom preferences were added
        loadPreferences();
    }
}