package com.example.dietaryscanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DietaryScanner.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_SAVED_ITEMS = "saved_items";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_BARCODE = "barcode";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_IMAGE_PATH = "image_path";
    private static final String COLUMN_INGREDIENTS = "ingredients";
    private static final String COLUMN_BRANDS = "brands";
    private static final String COLUMN_SAVED_DATE = "saved_date";

    // Date format for database storage
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SAVED_ITEMS_TABLE = "CREATE TABLE " + TABLE_SAVED_ITEMS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BARCODE + " TEXT UNIQUE,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_IMAGE_PATH + " TEXT,"
                + COLUMN_INGREDIENTS + " TEXT,"
                + COLUMN_BRANDS + " TEXT,"
                + COLUMN_SAVED_DATE + " TEXT"
                + ")";

        db.execSQL(CREATE_SAVED_ITEMS_TABLE);
        Log.d("DatabaseHelper", "Table created: " + TABLE_SAVED_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_ITEMS);
        onCreate(db);
    }

    // Add a new saved item
    public long addSavedItem(String barcode, String name, String description, String imagePath, String ingredients, String brands) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_BARCODE, barcode);
        values.put(COLUMN_NAME, name != null ? name : "Unknown Product");
        values.put(COLUMN_DESCRIPTION, description != null ? description : "");
        values.put(COLUMN_IMAGE_PATH, imagePath != null ? imagePath : "");
        values.put(COLUMN_INGREDIENTS, ingredients != null ? ingredients : "");
        values.put(COLUMN_BRANDS, brands != null ? brands : "");
        values.put(COLUMN_SAVED_DATE, dateFormat.format(new Date()));

        long result = db.insertWithOnConflict(TABLE_SAVED_ITEMS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();

        Log.d("DatabaseHelper", "Item saved with result: " + result + " for barcode: " + barcode);
        return result;
    }

    // Get all saved items
    public List<SavedItem> getAllSavedItems() {
        List<SavedItem> savedItemList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SAVED_ITEMS + " ORDER BY " + COLUMN_SAVED_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SavedItem savedItem = new SavedItem();
                savedItem.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BARCODE)));
                savedItem.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                savedItem.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                savedItem.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));
                savedItem.setIngredients(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS)));
                savedItem.setAllergyInfo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRANDS)));

                // Parse saved date
                String dateString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SAVED_DATE));
                try {
                    savedItem.setSavedDate(dateFormat.parse(dateString));
                } catch (ParseException e) {
                    savedItem.setSavedDate(new Date()); // Default to current date if parse fails
                    Log.e("DatabaseHelper", "Error parsing date: " + dateString, e);
                }

                savedItemList.add(savedItem);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        Log.d("DatabaseHelper", "Retrieved " + savedItemList.size() + " saved items");
        return savedItemList;
    }

    // Check if item is saved
    public boolean isItemSaved(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT 1 FROM " + TABLE_SAVED_ITEMS + " WHERE " + COLUMN_BARCODE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{barcode});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();

        Log.d("DatabaseHelper", "Item exists check for barcode " + barcode + ": " + exists);
        return exists;
    }

    // Delete a saved item
    public int deleteSavedItem(String barcode) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_SAVED_ITEMS, COLUMN_BARCODE + " = ?", new String[]{barcode});
        db.close();

        Log.d("DatabaseHelper", "Deleted item with barcode " + barcode + ", result: " + result);
        return result;
    }

    // Get saved items count
    public int getSavedItemsCount() {
        String countQuery = "SELECT * FROM " + TABLE_SAVED_ITEMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    // Clear all saved items
    public void clearAllSavedItems() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SAVED_ITEMS, null, null);
        db.close();
        Log.d("DatabaseHelper", "All saved items cleared");
    }
}