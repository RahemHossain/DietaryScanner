package com.example.dietaryscanner; // Or your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button; // Import Button
import androidx.appcompat.app.AppCompatActivity;

public class privacyscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacyscreen); // Links to privacyscreen.xml

        // 1. Find button2 by its ID
        //    Make sure your button in privacyscreen.xml has android:id="@+id/button2"
        Button homeButton = findViewById(R.id.button2);

        // 2. Set an OnClickListener for button2
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. Create an Intent to start HomeActivity
                Intent intent = new Intent(privacyscreen.this, allergyselectionscreen.class);

                // 4. Start HomeActivity
                startActivity(intent);

                // 5. (Optional) Finish privacyscreen if you don't want users to return to it
                //    by pressing the back button from HomeActivity.
                // finish();
            }
        });

        // Any other initialization for privacyscreen can go here
    }
}