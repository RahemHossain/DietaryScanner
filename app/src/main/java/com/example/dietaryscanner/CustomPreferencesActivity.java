package com.example.dietaryscanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.ImageView;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomPreferencesActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PreferencesFile";
    private static final String KEY_CUSTOM_PREFERENCES = "custom_preferences";

    private EditText editCustomPreference;
    private Button btnAddPreference;
    private LinearLayout customPreferencesContainer;
    private SharedPreferences sharedPreferences;
    private Set<String> customPreferences;

    private Button btnSave;

    // A single-thread executor to handle background tasks
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_preferences);

        initViews();
        loadCustomPreferences();
        displayCustomPreferences();
        setListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shut down the executor service to prevent memory leaks
        executor.shutdown();
    }

    private void initViews() {
        editCustomPreference = findViewById(R.id.edit_custom_preference);
        btnAddPreference = findViewById(R.id.btn_add_preference);
        btnSave = findViewById(R.id.btn_save);
        customPreferencesContainer = findViewById(R.id.custom_preferences_container);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        customPreferences = new HashSet<>();
    }

    private void loadCustomPreferences() {
        // SharedPreferences.getStringSet is already safe to call on the main thread
        customPreferences = sharedPreferences.getStringSet(KEY_CUSTOM_PREFERENCES, new HashSet<>());
        if (customPreferences == null) {
            customPreferences = new HashSet<>();
        }
    }

    private void displayCustomPreferences() {
        customPreferencesContainer.removeAllViews();
        for (String preference : customPreferences) {
            addPreferenceCard(preference);
        }
    }

    private void addPreferenceCard(String preferenceText) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.custom_preference_item, customPreferencesContainer, false);

        TextView textPreference = cardView.findViewById(R.id.text_custom_preference);
        ImageView btnDelete = cardView.findViewById(R.id.btn_delete_preference);

        textPreference.setText(preferenceText);

        btnDelete.setOnClickListener(v -> {
            customPreferences.remove(preferenceText);
            customPreferencesContainer.removeView(cardView);
            saveCustomPreferences(); // ✅ Call to save on delete
        });

        customPreferencesContainer.addView(cardView);
    }

    private void setListeners() {
        btnAddPreference.setOnClickListener(v -> {
            String preferenceText = editCustomPreference.getText().toString().trim();
            if (TextUtils.isEmpty(preferenceText)) {
                Toast.makeText(this, "Please enter a preference", Toast.LENGTH_SHORT).show();
                return;
            }
            if (customPreferences.contains(preferenceText)) {
                Toast.makeText(this, "This preference already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            customPreferences.add(preferenceText);
            addPreferenceCard(preferenceText);
            editCustomPreference.setText("");
            saveCustomPreferences(); // ✅ Call to save on add

            Toast.makeText(this, "Preference added", Toast.LENGTH_SHORT).show();
        });

        // The "Save" button will save and return to the home screen
        btnSave.setOnClickListener(v -> {
            saveCustomPreferences();
            Toast.makeText(this, "Custom preferences saved!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CustomPreferencesActivity.this, homescreen.class);
            startActivity(intent);
            finish();
        });

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    /**
     * Saves preferences on a background thread to prevent ANR.
     */
    private void saveCustomPreferences() {
        executor.execute(() -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(KEY_CUSTOM_PREFERENCES, customPreferences);
            editor.apply();
        });
    }
}