package com.example.dietaryscanner;

import java.util.Date;

public class SavedItem {
    private String id;
    private String name;
    private String description;
    private String imagePath;
    private Date savedDate;
    private String ingredients;
    private String allergyInfo;

    public SavedItem() {
        // Required empty constructor for database operations
    }

    public SavedItem(String id, String name, String description, String imagePath, Date savedDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.savedDate = savedDate;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Date getSavedDate() {
        return savedDate;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getAllergyInfo() {
        return allergyInfo;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setSavedDate(Date savedDate) {
        this.savedDate = savedDate;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public void setAllergyInfo(String allergyInfo) {
        this.allergyInfo = allergyInfo;
    }
}