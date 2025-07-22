// alergyselectionscreen.java (or AllergySelectionActivity.java)
package com.example.dietaryscanner; // Your package name

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class allergyselectionscreen extends AppCompatActivity { // Or public class AllergySelectionActivity ...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This line links the Activity to allergyselection.xml
        setContentView(R.layout.allergyselection); // Ensure R.layout.allergyselection is correct
    }
}