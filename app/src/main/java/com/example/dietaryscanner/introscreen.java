package com.example.dietaryscanner; // Or your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class introscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introscreen); // Links to introscreen.xml

        // 1. Find the button in introscreen.xml
        //    Make sure your button in introscreen.xml has the ID "introbutton"
        Button introButton = findViewById(R.id.introbutton);

        // 2. Set a click listener on the button
        introButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. Create an Intent to open the privacyscreen Activity
                //    This assumes your privacyscreen activity class is named "privacyscreen"
                Intent intent = new Intent(introscreen.this, privacyscreen.class);

                // 4. Start the new Activity
                startActivity(intent);
            }
        });
    }
}