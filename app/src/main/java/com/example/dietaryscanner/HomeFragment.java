package com.example.dietaryscanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment {

    private EditText searchEditText;
    private TextView preferencesText;
    private GridLayout popularItemsGrid;
    private RecyclerView recycler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViewsRecursively(view);
        updatePreferencesDisplay();
        setupSearchFunctionality();
        setupPopularItems(view);
        hideFakeSavedItems(view);
    }

    private void findViewsRecursively(View parent) {
        if (parent instanceof EditText) {
            searchEditText = (EditText) parent;
        } else if (parent instanceof TextView && preferencesText == null) {
            TextView tv = (TextView) parent;
            if (tv.getText().toString().contains("Preference")) {
                preferencesText = tv;
            }
        } else if (parent instanceof GridLayout) {
            popularItemsGrid = (GridLayout) parent;
        } else if (parent instanceof RecyclerView) {
            recycler = (RecyclerView) parent;
        }

        // Recursively search children
        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            for (int i = 0; i < group.getChildCount(); i++) {
                findViewsRecursively(group.getChildAt(i));
            }
        }
    }

    private void updatePreferencesDisplay() {
        if (preferencesText == null || getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences("dietary_preferences", Context.MODE_PRIVATE);
        Set<String> preferences = prefs.getStringSet("selected_preferences", new HashSet<>());

        if (preferences.isEmpty()) {
            preferencesText.setText("Your Preference: Not set - Configure in Settings");
        } else {
            StringBuilder text = new StringBuilder("Your Preference: ");
            boolean first = true;
            for (String pref : preferences) {
                if (!first) text.append(", ");
                text.append(pref.substring(0, 1).toUpperCase()).append(pref.substring(1));
                first = false;
            }
            preferencesText.setText(text.toString());
        }
    }

    private void setupSearchFunctionality() {
        if (searchEditText == null) return;

        searchEditText.setHint("Search products or barcodes");
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            }
            return true;
        });
    }

    private void performSearch(String query) {
        // Check direct barcode match first
        MockProductDatabase.MockProduct product = MockProductDatabase.getProduct(query);
        if (product != null) {
            openProduct(product);
            searchEditText.setText("");
            return;
        }

        // Search by name
        List<MockProductDatabase.MockProduct> results = MockProductDatabase.searchProducts(query);
        if (!results.isEmpty()) {
            Toast.makeText(getContext(), "Found: " + results.get(0).getName(), Toast.LENGTH_SHORT).show();
            openProduct(results.get(0));
            searchEditText.setText("");
        } else {
            Toast.makeText(getContext(), "No products found for: " + query, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPopularItems(View view) {
        if (recycler == null) {
            recycler = view.findViewById(R.id.popularRecycler);
        }

        // Map ALL barcodes to images - you need to add mappings for ALL products
        Map<String, Integer> imageMap = new HashMap<>();
        imageMap.put("049000042566", R.drawable.coke);        // Coca-Cola Classic
        imageMap.put("012000814174", R.drawable.pepsi);        // Pepsi Cola (using coke as placeholder)
        imageMap.put("034000002016", R.drawable.kitkat);        // Kit Kat Wafer Bar (using coke as placeholder)
        imageMap.put("044000032227", R.drawable.oreo);        // Oreo Original Cookies
        imageMap.put("028400064316", R.drawable.lays);        // Lay's Classic Potato Chips
        imageMap.put("040000000242", R.drawable.snickers);    // Snickers Chocolate Bar
        imageMap.put("049000028991", R.drawable.sprite);      // Sprite Lemon-Lime Soda
        imageMap.put("016000275300", R.drawable.cheerios);    // Cheerios Cereal
        imageMap.put("028400014939", R.drawable.doritos);     // Doritos Nacho Cheese
        imageMap.put("902832000004", R.drawable.coke);        // Red Bull Energy Drink (using coke as placeholder)

        List<MockProductDatabase.MockProduct> products = MockProductDatabase.getAllProducts();
        PopularAdapter adapter = new PopularAdapter(getContext(), products, imageMap);

        recycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recycler.setAdapter(adapter);
    }

    private void hideFakeSavedItems(View parent) {
        // Hide the fake Takis items
        View savedItem1 = parent.findViewById(R.id.savedItem1);
        View savedItem2 = parent.findViewById(R.id.savedItem2);

        if (savedItem1 != null) {
            savedItem1.setVisibility(View.GONE);
        }
        if (savedItem2 != null) {
            savedItem2.setVisibility(View.GONE);
        }
    }

    private void openProduct(MockProductDatabase.MockProduct product) {
        if (getContext() == null) return;

        Intent intent = new Intent(getContext(), iteminfoscreen.class);
        intent.putExtra("barcode", product.getBarcode());
        intent.putExtra("product_name", product.getName());
        intent.putExtra("ingredients", product.getIngredients());
        intent.putExtra("brands", product.getBrand());
        intent.putExtra("api_failure", false);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferencesDisplay();
    }
}