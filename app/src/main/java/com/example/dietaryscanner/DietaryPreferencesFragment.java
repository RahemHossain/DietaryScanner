package com.example.dietaryscanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView; // Make sure this import is present if you use CardViews

public class DietaryPreferencesFragment extends Fragment {

    private static final String PREFS_NAME = "PreferencesFile";
    private static final String KEY_HALAL = "halal";
    private static final String KEY_KOSHER = "kosher";
    private static final String KEY_VEGAN = "vegan";
    private static final String KEY_VEGETARIAN = "vegetarian";

    private Switch switchHalal, switchKosher, switchVegan, switchVegetarian;
    private Button doneButton;
    private View addCustomCard;
    private SharedPreferences sharedPreferences;

    // This method is the new 'onCreate' for a Fragment
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment. This is where the old setContentView goes.
        return inflater.inflate(R.layout.fragment_dietary_preferences, container, false);
    }

    // This method is called after onCreateView and is the correct place to initialize views
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Your old onCreate logic goes here
        initViews(view);
        loadPreferences();
        setListeners();
    }

    // You can keep all your private methods, but some need minor changes
    private void initViews(@NonNull View view) {
        // All findViewById calls must use the view object from onViewCreated
        switchHalal = view.findViewById(R.id.switch_halal);
        switchKosher = view.findViewById(R.id.switch_kosher);
        switchVegan = view.findViewById(R.id.switch_vegan);
        switchVegetarian = view.findViewById(R.id.switch_vegetarian);
        doneButton = view.findViewById(R.id.done_button);

        LinearLayout optionsSection = view.findViewById(R.id.options_section);
        if (optionsSection != null && optionsSection.getChildCount() > 4) {
            addCustomCard = optionsSection.getChildAt(4);
        }

        // Use requireContext() to get a valid context
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void loadPreferences() {
        if (switchHalal != null) switchHalal.setChecked(sharedPreferences.getBoolean(KEY_HALAL, false));
        if (switchKosher != null) switchKosher.setChecked(sharedPreferences.getBoolean(KEY_KOSHER, false));
        if (switchVegan != null) switchVegan.setChecked(sharedPreferences.getBoolean(KEY_VEGAN, false));
        if (switchVegetarian != null) switchVegetarian.setChecked(sharedPreferences.getBoolean(KEY_VEGETARIAN, false));

        updateDoneButtonState();
    }

    private void setListeners() {
        // Switch listeners
        if (switchHalal != null) switchHalal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_HALAL, isChecked);
            updateDoneButtonState();
            updateSwitchColors(switchHalal, isChecked);
        });

        if (switchKosher != null) switchKosher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_KOSHER, isChecked);
            updateDoneButtonState();
            updateSwitchColors(switchKosher, isChecked);
        });

        if (switchVegan != null) switchVegan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_VEGAN, isChecked);
            updateDoneButtonState();
            updateSwitchColors(switchVegan, isChecked);
        });

        if (switchVegetarian != null) switchVegetarian.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_VEGETARIAN, isChecked);
            updateDoneButtonState();
            updateSwitchColors(switchVegetarian, isChecked);
        });

        // Done button listener
        if (doneButton != null) {
            doneButton.setOnClickListener(v -> {
                if (hasAnyPreferenceSelected()) {
                    Toast.makeText(requireContext(), "Preferences saved!", Toast.LENGTH_SHORT).show();
                    // In a fragment, you would navigate back or to another screen
                    // For example: Navigation.findNavController(v).popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Please select at least one preference", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Add Custom card click listener
        if (addCustomCard != null) {
            addCustomCard.setOnClickListener(v -> {
                // Here you would navigate to a new fragment, not start a new activity
                Toast.makeText(requireContext(), "Navigating to Custom Preferences", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireContext(), CustomPreferencesActivity.class);
                startActivity(intent);
            });
        }
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void updateSwitchColors(Switch switchView, boolean isChecked) {
        if (isChecked) {
            switchView.getThumbDrawable().setTint(requireContext().getResources().getColor(android.R.color.holo_blue_bright, null));
            switchView.getTrackDrawable().setTint(requireContext().getResources().getColor(android.R.color.holo_blue_light, null));
        } else {
            switchView.getThumbDrawable().setTint(requireContext().getResources().getColor(android.R.color.darker_gray, null));
            switchView.getTrackDrawable().setTint(requireContext().getResources().getColor(android.R.color.darker_gray, null));
        }
    }

    private boolean hasAnyPreferenceSelected() {
        return switchHalal.isChecked() || switchKosher.isChecked() ||
                switchVegan.isChecked() || switchVegetarian.isChecked();
    }

    private void updateDoneButtonState() {
        if (hasAnyPreferenceSelected()) {
            doneButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    requireContext().getResources().getColor(android.R.color.holo_blue_bright, null)));
            doneButton.setEnabled(true);
        } else {
            doneButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    requireContext().getResources().getColor(android.R.color.darker_gray, null)));
            doneButton.setEnabled(true);
        }
    }

    // onResume is still a valid lifecycle method for fragments
    @Override
    public void onResume() {
        super.onResume();
        loadPreferences();
    }
}