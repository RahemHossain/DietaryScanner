package com.example.dietaryscanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Skip UI, just do routing

        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                decideNextScreen();
            }
        }, 1000);
    }

    private void decideNextScreen() {
        SharedPreferences prefs = getSharedPreferences("dietary_preferences", MODE_PRIVATE);

        boolean privacyAccepted = prefs.getBoolean("privacy_accepted", false);

        // Provide an empty Set as the default value to prevent NullPointerException
        Set<String> selectedPreferences = prefs.getStringSet("selected_preferences", new HashSet<String>());

        // Now check if the set is empty
        boolean hasPreferences = !selectedPreferences.isEmpty();

        Intent intent;

        if (!privacyAccepted) {
            // Go to Privacy screen
            intent = new Intent(this, privacyscreen.class);

        } else if (!hasPreferences) {
            // Go to Preferences setup screen
            intent = new Intent(this, allergyselectionscreen.class);

        } else {
            // Go straight to home (MainActivity with HomeFragment)
            intent = new Intent(this, homescreen.class);
        }

        startActivity(intent);
        finish();
    }
}
