package com.example.dietaryscanner; // Or your package

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class homescreen extends AppCompatActivity { // Must extend Activity or a subclass
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home); // Assuming your layout is home.xml
        // ... your activity code ...
    }
}