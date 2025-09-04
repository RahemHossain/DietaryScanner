package com.example.dietaryscanner;

import android.content.Context;
import android.util.Log;
import java.util.List;

public class SQLiteSavedItemsManager {
    private DatabaseHelper dbHelper;
    private Context context;

    public SQLiteSavedItemsManager(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    public boolean saveItem(String barcode, String name, String description, String imagePath, String ingredients, String brands) {
        if (barcode == null || barcode.isEmpty()) {
            Log.e("SavedItemsManager", "Cannot save item with empty barcode");
            return false;
        }

        long result = dbHelper.addSavedItem(barcode, name, description, imagePath, ingredients, brands);
        boolean success = result != -1;

        Log.d("SavedItemsManager", "Save item result: " + success + " for barcode: " + barcode);
        return success;
    }

    public List<SavedItem> getSavedItems() {
        List<SavedItem> items = dbHelper.getAllSavedItems();
        Log.d("SavedItemsManager", "Retrieved " + items.size() + " saved items");
        return items;
    }

    public boolean isItemSaved(String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            return false;
        }
        boolean saved = dbHelper.isItemSaved(barcode);
        Log.d("SavedItemsManager", "Item saved check for " + barcode + ": " + saved);
        return saved;
    }

    public boolean removeItem(String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            Log.e("SavedItemsManager", "Cannot remove item with empty barcode");
            return false;
        }

        int result = dbHelper.deleteSavedItem(barcode);
        boolean success = result > 0;

        Log.d("SavedItemsManager", "Remove item result: " + success + " for barcode: " + barcode);
        return success;
    }

    public int getSavedItemsCount() {
        return dbHelper.getSavedItemsCount();
    }

    public void clearAllItems() {
        dbHelper.clearAllSavedItems();
        Log.d("SavedItemsManager", "All items cleared");
    }
}