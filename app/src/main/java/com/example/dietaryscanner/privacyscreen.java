package com.example.dietaryscanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;

public class privacyscreen extends AppCompatActivity {

    private static final String PREFS_NAME = "dietary_preferences";
    private static final String PRIVACY_KEY = "privacy_accepted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacyscreen);

        Button homeButton = findViewById(R.id.button2);

        homeButton.setOnClickListener(v -> {
            // ✅ Save acceptance so user doesn’t see this again
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(PRIVACY_KEY, true).apply();

            // ✅ Go to preference setup next
            Intent intent = new Intent(privacyscreen.this, allergyselectionscreen.class);
            startActivity(intent);

            // ✅ Prevent back button from returning here
            finish();
        });
    }
}
