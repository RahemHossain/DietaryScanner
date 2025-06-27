package com.example.dietaryscanner;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class introscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Link this Activity to its layout file
        setContentView(R.layout.introscreen);

        // Find the "Get Started" button by its ID
        Button introButton = findViewById(R.id.introbutton);

        // Set a click listener on the button
        introButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to open the PrivacyScreen Activity
                Intent intent = new Intent(introscreen.this, privacyscreen.class);

                // Start the new Activity
                startActivity(intent);
            }
        });
    }
}
