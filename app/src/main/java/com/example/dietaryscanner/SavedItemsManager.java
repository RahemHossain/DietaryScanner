package com.example.dietaryscanner;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SavedItemsManager {
    private static final String PREFS_NAME = "saved_products"; // Changed to match your existing system
    private static final String ITEMS_KEY = "saved_items_list"; // New key for the list format
    private static final String PRODUCTS_KEY = "products"; // Your existing key for barcodes set

    private Context context;
    private Gson gson;

    public SavedItemsManager(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    public void saveItem(String barcode, String name, String description, String imagePath, String ingredients, String brands) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Get existing saved items list
        List<SavedItem> savedItems = getSavedItems();

        // Check if item already exists (by barcode)
        boolean itemExists = false;
        for (SavedItem item : savedItems) {
            if (item.getId().equals(barcode)) {
                itemExists = true;
                break;
            }
        }

        if (!itemExists) {
            // Create new saved item
            SavedItem newItem = new SavedItem();
            newItem.setId(barcode); // Use barcode as ID for consistency
            newItem.setName(name != null ? name : "Unknown Product");
            newItem.setDescription(description != null ? description : "");
            newItem.setImagePath(imagePath != null ? imagePath : "");
            newItem.setIngredients(ingredients != null ? ingredients : "");
            newItem.setAllergyInfo(brands != null ? brands : "");
            newItem.setSavedDate(new Date());

            savedItems.add(0, newItem); // Add to beginning

            // Save the list
            saveItemsListToPreferences(savedItems);

            // Also maintain your existing barcode set for compatibility
            Set<String> savedProducts = new HashSet<>(prefs.getStringSet(PRODUCTS_KEY, new HashSet<>()));
            savedProducts.add(barcode);
            editor.putStringSet(PRODUCTS_KEY, savedProducts);

            // Save individual product data (your existing system)
            editor.putString("product_" + barcode + "_name", name);
            editor.putString("product_" + barcode + "_ingredients", ingredients);
            editor.putString("product_" + barcode + "_brands", brands);
            editor.putString("product_" + barcode + "_image", imagePath);

            editor.apply();
        }
    }

    public List<SavedItem> getSavedItems() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedItemsJson = prefs.getString(ITEMS_KEY, "[]");

        Type listType = new TypeToken<List<SavedItem>>(){}.getType();
        List<SavedItem> savedItems = gson.fromJson(savedItemsJson, listType);

        if (savedItems == null) {
            savedItems = new ArrayList<>();
        }

        // If the list is empty but we have old data, migrate it
        if (savedItems.isEmpty()) {
            migrateOldData(prefs, savedItems);
        }

        return savedItems;
    }

    private void migrateOldData(SharedPreferences prefs, List<SavedItem> savedItems) {
        // Get old barcode set
        Set<String> oldBarcodes = prefs.getStringSet(PRODUCTS_KEY, new HashSet<>());

        for (String barcode : oldBarcodes) {
            String name = prefs.getString("product_" + barcode + "_name", "Unknown Product");
            String ingredients = prefs.getString("product_" + barcode + "_ingredients", "");
            String brands = prefs.getString("product_" + barcode + "_brands", "");
            String imagePath = prefs.getString("product_" + barcode + "_image", "");

            SavedItem item = new SavedItem();
            item.setId(barcode);
            item.setName(name);
            item.setDescription(ingredients);
            item.setImagePath(imagePath);
            item.setIngredients(ingredients);
            item.setAllergyInfo(brands);
            item.setSavedDate(new Date());

            savedItems.add(item);
        }

        // Save the migrated data
        if (!savedItems.isEmpty()) {
            saveItemsListToPreferences(savedItems);
        }
    }

    public void removeItem(String barcode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Remove from list
        List<SavedItem> savedItems = getSavedItems();
        savedItems.removeIf(item -> item.getId().equals(barcode));
        saveItemsListToPreferences(savedItems);

        // Remove from old system too
        Set<String> savedProducts = new HashSet<>(prefs.getStringSet(PRODUCTS_KEY, new HashSet<>()));
        savedProducts.remove(barcode);
        editor.putStringSet(PRODUCTS_KEY, savedProducts);

        editor.remove("product_" + barcode + "_name");
        editor.remove("product_" + barcode + "_ingredients");
        editor.remove("product_" + barcode + "_brands");
        editor.remove("product_" + barcode + "_image");

        editor.apply();
    }

    public boolean isItemSaved(String barcode) {
        List<SavedItem> savedItems = getSavedItems();
        return savedItems.stream().anyMatch(item -> item.getId().equals(barcode));
    }

    private void saveItemsListToPreferences(List<SavedItem> savedItems) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String savedItemsJson = gson.toJson(savedItems);
        editor.putString(ITEMS_KEY, savedItemsJson);
        editor.apply();
    }

    public void clearAllItems() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}