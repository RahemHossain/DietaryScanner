package com.example.dietaryscanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockProductDatabase {

    private static final Map<String, MockProduct> products = new HashMap<>();

    static {
        // Coca-Cola Classic
        products.put("049000042566", new MockProduct(
                "049000042566",
                "Coca-Cola Classic",
                "Coca-Cola Company",
                "carbonated water, high fructose corn syrup, caramel color, phosphoric acid, natural flavors, caffeine",
                R.drawable.coke, // <- Use R.drawable resource ID,
                true, // halal
                false, // not kosher (no certification)
                false, // not vegan (contains natural flavors that may be animal-derived)
                true  // vegetarian
        ));

        // Pepsi
        products.put("012000814174", new MockProduct(
                "012000814174",
                "Pepsi Cola",
                "PepsiCo",
                "carbonated water, high fructose corn syrup, caramel color, sugar, phosphoric acid, caffeine, citric acid, natural flavor",
                R.drawable.pepsi,
                true, // halal
                false, // not kosher
                false, // not vegan
                true  // vegetarian
        ));

        // Kit Kat
        products.put("034000002016", new MockProduct(
                "034000002016",
                "Kit Kat Wafer Bar",
                "Hershey Company",
                "milk chocolate, wheat flour, sugar, cocoa butter, nonfat milk, chocolate, refined palm kernel oil, lactose, milk fat, contains 2% or less of: soy lecithin, PGPR, yeast, artificial flavor, salt, sodium bicarbonate",
                R.drawable.kitkat,
                false, // not halal (contains milk, unclear sourcing)
                false, // not kosher (no certification)
                false, // not vegan (contains milk)
                true   // vegetarian
        ));

        // Oreo Cookies
        products.put("044000032227", new MockProduct(
                "044000032227",
                "Oreo Original Cookies",
                "Mondelez International",
                "unbleached enriched flour, sugar, palm and/or canola oil, cocoa powder, high fructose corn syrup, leavening, salt, soy lecithin, chocolate, artificial flavor",
                R.drawable.oreo,
                true, // halal (no animal products)
                false, // not kosher
                true, // vegan (reformulated without milk)
                true  // vegetarian
        ));

        // Lay's Classic Potato Chips
        products.put("028400064316", new MockProduct(
                "028400064316",
                "Lay's Classic Potato Chips",
                "Frito-Lay",
                "potatoes, vegetable oil, salt",
                R.drawable.lays,
                true, // halal
                true, // kosher
                true, // vegan
                true  // vegetarian
        ));

        // Snickers Bar
        products.put("040000000242", new MockProduct(
                "040000000242",
                "Snickers Chocolate Bar",
                "Mars, Inc.",
                "milk chocolate, peanuts, corn syrup, sugar, palm oil, skim milk, lactose, salt, egg whites, artificial flavor",
                R.drawable.snickers,
                false, // not halal (contains milk, egg)
                false, // not kosher
                false, // not vegan (contains milk, egg)
                true   // vegetarian
        ));

        // Sprite
        products.put("049000028991", new MockProduct(
                "049000028991",
                "Sprite Lemon-Lime Soda",
                "Coca-Cola Company",
                "carbonated water, high fructose corn syrup, citric acid, natural flavors, sodium citrate, sodium benzoate",
                R.drawable.sprite,
                true, // halal
                true, // kosher
                true, // vegan
                true  // vegetarian
        ));

        // Cheerios
        products.put("016000275300", new MockProduct(
                "016000275300",
                "Cheerios Cereal",
                "General Mills",
                "whole grain oats, corn starch, sugar, salt, tripotassium phosphate, vitamin E, vitamin C, vitamin A, vitamin B6, vitamin B1, vitamin B2, folic acid, vitamin B12, vitamin D3",
                R.drawable.cheerios,
                true, // halal
                true, // kosher certified
                false, // not vegan (contains vitamin D3 which may be animal-derived)
                true   // vegetarian
        ));

        // Doritos Nacho Cheese
        products.put("028400014939", new MockProduct(
                "028400014939",
                "Doritos Nacho Cheese",
                "Frito-Lay",
                "corn, vegetable oil, salt, cheddar cheese, whey, monosodium glutamate, buttermilk, romano cheese, whey protein concentrate, onion powder, corn flour, natural and artificial flavor",
                R.drawable.doritos,
                false, // not halal (contains cheese, whey)
                false, // not kosher (no certification, contains cheese)
                false, // not vegan (contains cheese, whey, buttermilk)
                false   //
        ));

        // Red Bull Energy Drink
        products.put("902832000004", new MockProduct(
                "902832000004",
                "Red Bull Energy Drink",
                "Red Bull GmbH",
                "caffeine, taurine, b-complex vitamins, sucrose, glucose, alpine water",
                R.drawable.redbull,
                true, // halal (synthetic taurine)
                true, // kosher
                true, // vegan (synthetic ingredients)
                true  // vegetarian
        ));
    }

    public static MockProduct getProduct(String barcode) {
        return products.get(barcode);
    }

    public static List<MockProduct> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public static List<MockProduct> searchProducts(String query) {
        List<MockProduct> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (MockProduct product : products.values()) {
            if (product.getName().toLowerCase().contains(lowerQuery) ||
                    product.getBrand().toLowerCase().contains(lowerQuery) ||
                    product.getBarcode().contains(query)) {
                results.add(product);
            }
        }

        return results;
    }

    public static class MockProduct {
        private String barcode;
        private String name;
        private String brand;
        private String ingredients;
        private int imageResId; // This stores the drawable resource ID
        private boolean halal;
        private boolean kosher;
        private boolean vegan;
        private boolean vegetarian;

        public MockProduct(String barcode, String name, String brand, String ingredients,
                           int imageResId, // This is the drawable resource ID
                           boolean halal, boolean kosher, boolean vegan, boolean vegetarian) {
            this.barcode = barcode;
            this.name = name;
            this.brand = brand;
            this.ingredients = ingredients;
            this.imageResId = imageResId;
            this.halal = halal;
            this.kosher = kosher;
            this.vegan = vegan;
            this.vegetarian = vegetarian;
        }

        // Getters
        public String getBarcode() { return barcode; }
        public String getName() { return name; }
        public String getBrand() { return brand; }
        public String getIngredients() { return ingredients; }
        public int getImageResId() { return imageResId; } // This returns the drawable resource ID
        public boolean isHalal() { return halal; }
        public boolean isKosher() { return kosher; }
        public boolean isVegan() { return vegan; }
        public boolean isVegetarian() { return vegetarian; }

        public boolean isCompatibleWith(String preference) {
            switch (preference.toLowerCase()) {
                case "halal":
                    return halal;
                case "kosher":
                    return kosher;
                case "vegan":
                    return vegan;
                case "vegetarian":
                    return vegetarian;
                default:
                    return true;
            }
        }
    }
}