package com.example.dietaryscanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Solely to test pages, delete when necessary.
public class TestActivity extends AppCompatActivity {

    private EditText barcodeInput;
    private EditText productNameInput;
    private EditText ingredientsInput;
    private Button testButton;
    private Button quickTestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        barcodeInput = findViewById(R.id.editTextBarcode);
        productNameInput = findViewById(R.id.editTextProductName);
        ingredientsInput = findViewById(R.id.editTextIngredients);
        testButton = findViewById(R.id.buttonTest);
        quickTestButton = findViewById(R.id.buttonQuickTest);
    }

    private void setupClickListeners() {
        testButton.setOnClickListener(v -> {
            String barcode = barcodeInput.getText().toString().trim();
            String productName = productNameInput.getText().toString().trim();
            String ingredients = ingredientsInput.getText().toString().trim();

            if (barcode.isEmpty()) {
                Toast.makeText(this, "Please enter a barcode!", Toast.LENGTH_SHORT).show();
                return;
            }

            openItemInfoScreen(barcode, productName, ingredients, true);
        });

        quickTestButton.setOnClickListener(v -> {
            // Test with Nutella - this should definitely work
            Toast.makeText(this, "Testing with Nutella barcode", Toast.LENGTH_SHORT).show();
            openItemInfoScreen("3017620422003", "", "", true);
        });
    }

    private void openItemInfoScreen(String barcode, String productName, String ingredients, boolean isPermissible) {
        Intent intent = new Intent(this, iteminfoscreen.class);
        intent.putExtra("barcode", barcode);
        intent.putExtra("productName", productName);
        intent.putExtra("ingredients", ingredients);
        intent.putExtra("isPermissible", isPermissible);
        startActivity(intent);
    }
}