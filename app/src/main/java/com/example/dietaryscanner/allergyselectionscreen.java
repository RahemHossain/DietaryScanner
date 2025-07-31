package com.example.dietaryscanner;

import android.content.Intent; // IMPORTANT: Add this import
import android.os.Bundle;
import android.widget.Button; // IMPORTANT: Add this import
import androidx.appcompat.app.AppCompatActivity;

public class allergyselectionscreen extends AppCompatActivity {

    // 1. Declare a private Button variable
    private Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allergyselection); // This line links the Activity to allergyselection.xml

        // 2. Initialize the doneButton using findViewById()
        //    Make sure R.id.done_button matches the ID of your Button in allergyselection.xml
        doneButton = findViewById(R.id.done_button); // Assuming the button's ID is 'done_button'

        // 3. Set an OnClickListener on the doneButton
        if (doneButton != null) { // Always good practice to check if the view was found
            doneButton.setOnClickListener(v -> {
                // This code will execute when the "Done" button is clicked

                // Create an Intent to start CustomPreferencesActivity
                Intent intent = new Intent(allergyselectionscreen.this, CustomPreferencesActivity.class);

                // Start the new Activity
                startActivity(intent);

                // Optional: If you want to close allergyselectionscreen so the user
                // doesn't return to it when pressing the back button from CustomPreferencesActivity.
                // finish();
            });
        } else {
            // Log an error if the button isn't found, which helps in debugging
            android.util.Log.e("AllergySelection", "Done button with ID 'done_button' not found in allergyselection.xml!");
        }

        // ... any other setup code for your allergyselectionscreen goes here ...
    }
}