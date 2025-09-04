package com.example.dietaryscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.HashSet;
import java.util.Set;

public class iteminfoscreen extends AppCompatActivity {

    // Using the EXACT IDs from your original layout document
    private TextView statusText;        // textView
    private TextView productNameText;   // textView2
    private TextView ingredientsText;   // textView3
    private ImageView productImage;     // imageView
    private Button favoriteButton;      // button
    private ProgressBar progressBar;    // progressBar

    // Navigation views
    private ImageView navSaved, navHome, navBarcode, navMenu;


    private String barcode;
    private String productName;
    private String ingredients;
    private String brands;
    private String imageUrl;


    private SQLiteSavedItemsManager savedItemsManager;

    // Enhanced dietary restriction keywords with verification needed categories

    // DEFINITELY NOT HALAL - Red flag
    private final String[] NON_HALAL_KEYWORDS = {
            "pork", "ham", "bacon", "lard", "wine", "beer", "rum", "whiskey",
            "vodka", "champagne", "cognac", "ethanol", "alcohol"
    };

    // NEEDS VERIFICATION - Yellow flag for halal
    private final String[] HALAL_VERIFICATION_NEEDED = {
            "gelatin", "enzymes", "natural flavors", "artificial flavors", "mono and diglycerides",
            "monoglycerides", "diglycerides", "emulsifiers", "beef", "chicken", "duck", "lamb",
            "mutton", "turkey", "meat", "whey", "casein", "lactose", "milk powder", "cheese powder",
            "vanilla extract", "lecithin", "glycerin", "glycerol", "stearic acid", "oleic acid",
            "palmitic acid", "cysteine", "rennet", "pepsin", "lipase", "protease", "amylase"
    };

    // DEFINITELY NOT KOSHER - Red flag
    private final String[] NON_KOSHER_KEYWORDS = {
            "pork", "ham", "bacon", "shellfish", "crab", "lobster", "shrimp", "gelatin"
    };

    // NEEDS VERIFICATION - Yellow flag for kosher
    private final String[] KOSHER_VERIFICATION_NEEDED = {
            "milk", "cheese", "butter", "cream", "beef", "chicken", "meat", "enzymes",
            "natural flavors", "artificial flavors", "wine", "grape juice", "rennet"
    };

    // DEFINITELY NOT VEGAN - Red flag
    private final String[] NON_VEGAN_KEYWORDS = {
            "milk", "cheese", "butter", "cream", "egg", "honey", "gelatin", "whey",
            "casein", "lactose", "meat", "chicken", "beef", "pork", "fish", "lard"
    };

    // NEEDS VERIFICATION - Yellow flag for vegan
    private final String[] VEGAN_VERIFICATION_NEEDED = {
            "natural flavors", "artificial flavors", "sugar", "mono and diglycerides",
            "monoglycerides", "diglycerides", "lecithin", "vitamin d3", "omega-3",
            "glycerin", "glycerol", "cysteine"
    };

    // DEFINITELY NOT VEGETARIAN - Red flag
    private final String[] NON_VEGETARIAN_KEYWORDS = {
            "meat", "chicken", "beef", "pork", "fish", "gelatin", "lard", "chicken fat", "beef fat"
    };

    // NEEDS VERIFICATION - Yellow flag for vegetarian
    private final String[] VEGETARIAN_VERIFICATION_NEEDED = {
            "enzymes", "rennet", "pepsin", "lipase", "natural flavors", "cheese",
            "vitamin d3", "omega-3", "glycerin", "glycerol"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iteminfoscreen);

        initViews();


        savedItemsManager = new SQLiteSavedItemsManager(this);
        setupNavigationListeners();
        getIntentData();
        showLoadingState();
        displayProductInfo();
        loadProductImage();
        analyzeDietaryCompatibility();
        setupFavoriteButton();
    }

    private void initViews() {
        statusText = findViewById(R.id.textView);
        productNameText = findViewById(R.id.textView2);
        ingredientsText = findViewById(R.id.textView3);
        productImage = findViewById(R.id.imageView);
        favoriteButton = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);

        // Initialize navigation views
        navSaved = findViewById(R.id.nav_saved);
        navHome = findViewById(R.id.nav_home);
        navBarcode = findViewById(R.id.nav_barcode);
        navMenu = findViewById(R.id.nav_menu);
    }







    private void setupNavigationListeners() {
        if (navSaved != null) {
            navSaved.setOnClickListener(v -> {
                Intent intent = new Intent(iteminfoscreen.this, homescreen.class);
                intent.putExtra("fragment", "saved");
                startActivity(intent);
            });
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(iteminfoscreen.this, homescreen.class);
                intent.putExtra("fragment", "home");
                startActivity(intent);
            });
        }

        if (navBarcode != null) {
            navBarcode.setOnClickListener(v -> {
                Intent intent = new Intent(iteminfoscreen.this, cameraActivity.class);
                startActivity(intent);
            });
        }

        if (navMenu != null) {
            navMenu.setOnClickListener(v -> {
                Intent intent = new Intent(iteminfoscreen.this, homescreen.class);
                intent.putExtra("fragment", "settings");
                startActivity(intent);
            });
        }

        updateNavigationTints(null);
    }

    private void updateNavigationTints(ImageView selectedItem) {
        if (navSaved != null) navSaved.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));
        if (navHome != null) navHome.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));
        if (navBarcode != null) navBarcode.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));
        if (navMenu != null) navMenu.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));

        if (selectedItem != null) {
            selectedItem.setColorFilter(Color.parseColor("#007AFF"));
        }
    }

    private void getIntentData() {
        barcode = getIntent().getStringExtra("barcode");
        productName = getIntent().getStringExtra("product_name");
        ingredients = getIntent().getStringExtra("ingredients");
        brands = getIntent().getStringExtra("brands");
        imageUrl = getIntent().getStringExtra("image_url");

        // Check if we came here due to API failure
        boolean apiFailure = getIntent().getBooleanExtra("api_failure", false);

        Log.d("ItemInfo", "Image URL received: " + imageUrl);
        Log.d("ItemInfo", "Barcode: " + barcode);
        Log.d("ItemInfo", "Ingredients: " + ingredients);
        Log.d("ItemInfo", "API Failure flag: " + apiFailure);

        // Check if we have valid product data from API
        boolean hasValidApiData = !apiFailure &&
                (ingredients != null && !ingredients.isEmpty() &&
                        !ingredients.equals("null") && !ingredients.equals("Not available in database")) &&
                (productName != null && !productName.isEmpty() &&
                        !productName.equals("null") && !productName.equals("Unknown Product"));

        Log.d("ItemInfo", "Has valid API data: " + hasValidApiData);

        if (!hasValidApiData) {
            Log.d("ItemInfo", "No valid API data found, will prompt for ingredient photo");
        }
    }

    private void showLoadingState() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        statusText.setText("Analyzing Product...");
        ingredientsText.setText("Loading ingredients...");
    }

    private void displayProductInfo() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        productNameText.setText(productName != null ? productName : "Unknown Product");

        if (ingredients != null && !ingredients.isEmpty()) {
            ingredientsText.setText("Ingredients: " + ingredients);
        } else {
            ingredientsText.setText("Ingredients: Not available in database");
        }
    }

    private void loadProductImage() {
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
            Log.d("ItemInfo", "Attempting to load image from URL: " + imageUrl);

            Glide.with(this)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.chips_default)
                            .error(R.drawable.chips_default)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop())
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            Log.d("ItemInfo", "Image failed to load, creating barcode image");
                            createAndSetBarcodeImage();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            Log.d("ItemInfo", "Image loaded successfully from URL");
                            return false;
                        }
                    })
                    .into(productImage);
        } else {
            Log.d("ItemInfo", "No image URL available, creating barcode image");
            createAndSetBarcodeImage();
        }
    }

    private void createAndSetBarcodeImage() {
        if (barcode != null && !barcode.isEmpty()) {
            Bitmap barcodeImage = createBarcodeImage(barcode);
            productImage.setImageBitmap(barcodeImage);
        } else {
            productImage.setImageResource(R.drawable.chips_default);
        }
    }

    private Bitmap createBarcodeImage(String barcodeText) {
        int width = 400;
        int height = 200;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);

        for (int i = 0; i < barcodeText.length() && i < 20; i++) {
            char digit = barcodeText.charAt(i);
            int digitValue = Character.getNumericValue(digit);

            for (int j = 0; j < digitValue % 5 + 1; j++) {
                float x = 20 + (i * 18) + (j * 3);
                canvas.drawLine(x, 20, x, height - 50, paint);
            }
        }

        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);

        Rect textBounds = new Rect();
        paint.getTextBounds(barcodeText, 0, barcodeText.length(), textBounds);

        float x = width / 2f;
        float y = height - 15;
        canvas.drawText(barcodeText, x, y, paint);

        paint.setTextSize(16);
        paint.setColor(Color.GRAY);
        canvas.drawText("BARCODE", width / 2f, 15, paint);

        return bitmap;
    }

    private void analyzeDietaryCompatibility() {
        SharedPreferences prefs = getSharedPreferences("dietary_preferences", MODE_PRIVATE);
        Set<String> preferences = prefs.getStringSet("selected_preferences", new HashSet<>());

        Log.d("DietaryAnalysis", "Retrieved preferences: " + preferences.toString());

        // Check if we have valid ingredient data
        boolean apiFailure = getIntent().getBooleanExtra("api_failure", false);
        boolean hasValidIngredients = ingredients != null && !ingredients.isEmpty() &&
                !ingredients.equals("null") &&
                !ingredients.equals("Not available in database") &&
                !ingredients.equals("Unknown Product");

        Log.d("DietaryAnalysis", "API Failure: " + apiFailure);
        Log.d("DietaryAnalysis", "Has valid ingredients: " + hasValidIngredients);
        Log.d("DietaryAnalysis", "Ingredients value: '" + ingredients + "'");

        if (apiFailure || !hasValidIngredients) {
            // No API data available, prompt user to take photo of ingredients
            Log.d("DietaryAnalysis", "Triggering camera fallback");
            promptForIngredientsPhoto(preferences);
            return;
        }

        // Continue with normal analysis if we have ingredients
        performDietaryAnalysis(preferences);
    }

    private void promptForIngredientsPhoto(Set<String> preferences) {
        setPermissibilityStatus("Photo Needed", "#FFA726", "Take a photo of ingredients to analyze", preferences);

        // Update the ingredients text to show photo prompt
        ingredientsText.setText("📸 API data not available.\n\nTo analyze this product's dietary compatibility, please take a clear photo of the ingredients list on the packaging.");

        // Add photo capture button immediately (not using post)
        addPhotoCaptureButton(preferences);
    }

    private void addPhotoCaptureButton(Set<String> preferences) {
        Log.d("ItemInfo", "addPhotoCaptureButton called - using crash-proof implementation");

        // NUCLEAR OPTION: Don't add any buttons to ScrollView
        // Just launch camera after a short delay to ensure UI is ready

        runOnUiThread(() -> {
            new android.os.Handler().postDelayed(() -> {
                Toast.makeText(this, "Opening ingredient scanner...", Toast.LENGTH_SHORT).show();
                launchIngredientsCameraCapture();
            }, 500); // Small delay to ensure activity is fully loaded
        });

        Log.d("ItemInfo", "Scheduled camera launch");
    }

    private Button createLearnMoreButton(Set<String> preferences) {
        Button learnMoreButton = new Button(this);
        learnMoreButton.setText("🔍 See Symbol Images");
        learnMoreButton.setBackgroundColor(Color.parseColor("#007AFF"));
        learnMoreButton.setTextColor(Color.WHITE);
        learnMoreButton.setAllCaps(false);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(32, 20, 32, 20);
        learnMoreButton.setLayoutParams(buttonParams);

        learnMoreButton.setOnClickListener(v -> showSymbolGuide(preferences));

        return learnMoreButton;
    }

    private void launchIngredientsCameraCapture() {
        Intent cameraIntent = new Intent(this, IngredientsCameraActivity.class);
        cameraIntent.putExtra("barcode", barcode);
        cameraIntent.putExtra("product_name", productName != null ? productName : "Unknown Product");

        // CRITICAL: Pass all current data so we can recreate the activity properly
        cameraIntent.putExtra("ingredients", ingredients);
        cameraIntent.putExtra("brands", brands);
        cameraIntent.putExtra("image_url", imageUrl);

        startActivityForResult(cameraIntent, 1001);

        // DON'T finish() here - we want to return to this activity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("ItemInfo", "onActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // Got OCR results back from camera activity
            String extractedIngredients = data.getStringExtra("extracted_ingredients");
            Log.d("ItemInfo", "Received OCR ingredients: " + extractedIngredients);

            if (extractedIngredients != null && !extractedIngredients.isEmpty()) {
                ingredients = extractedIngredients;

                // Update display and analyze
                displayProductInfo();

                SharedPreferences prefs = getSharedPreferences("dietary_preferences", MODE_PRIVATE);
                Set<String> preferences = prefs.getStringSet("selected_preferences", new HashSet<>());
                performDietaryAnalysis(preferences);
            } else {
                Toast.makeText(this, "Could not extract ingredients from photo. Please try again.", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d("ItemInfo", "Activity result not handled - requestCode: " + requestCode + ", resultCode: " + resultCode);
        }
    }

    private void performDietaryAnalysis(Set<String> preferences) {
        String lowerIngredients = ingredients.toLowerCase();
        Log.d("DietaryAnalysis", "Ingredients to analyze: " + lowerIngredients);

        if (preferences.isEmpty()) {
            Log.d("DietaryAnalysis", "No preferences found - showing 'No Preferences Set'");
            setPermissibilityStatus("No Preferences Set", "#9E9E9E", "Set your dietary preferences to get analysis", preferences);
            return;
        }

        boolean hasHalalCertification = checkForHalalCertification();

        String overallStatus = "Permissible To Consume";
        String overallColor = "#5AB46E";
        String detailMessage = "All ingredients appear to meet your dietary requirements";
        Set<String> problematicIngredients = new HashSet<>();
        Set<String> verificationNeededIngredients = new HashSet<>();

        for (String preference : preferences) {
            String cleanPreference = preference.trim().toLowerCase();
            Log.d("DietaryAnalysis", "Checking preference: '" + cleanPreference + "'");

            DietaryResult result = analyzeSinglePreference(cleanPreference, lowerIngredients, hasHalalCertification);

            problematicIngredients.addAll(result.problematicIngredients);
            verificationNeededIngredients.addAll(result.verificationNeededIngredients);

            if (result.status == DietaryStatus.NOT_PERMISSIBLE) {
                overallStatus = "Not Permissible";
                overallColor = "#F44336";
                detailMessage = result.message;
                break;
            } else if (result.status == DietaryStatus.NEEDS_VERIFICATION) {
                overallStatus = "Verification Needed";
                overallColor = "#FFA726";
                detailMessage = result.message;
            }
        }

        updateIngredientsTextWithHighlighting(problematicIngredients, verificationNeededIngredients);
        setPermissibilityStatus(overallStatus, overallColor, detailMessage, preferences);
    }

    private boolean checkForHalalCertification() {
        String allText = ((productName != null ? productName : "") + " " +
                (brands != null ? brands : "")).toLowerCase();

        return allText.contains("halal certified") ||
                allText.contains("halal approval") ||
                allText.contains("halal authority") ||
                allText.contains("halal logo") ||
                allText.contains("certified halal");
    }

    private DietaryResult analyzeSinglePreference(String preference, String ingredients, boolean hasHalalCertification) {
        switch (preference) {
            case "halal":
                return analyzeHalal(ingredients, hasHalalCertification);
            case "kosher":
                return analyzeKosher(ingredients);
            case "vegan":
                return analyzeVegan(ingredients);
            case "vegetarian":
                return analyzeVegetarian(ingredients);
            default:
                return new DietaryResult(DietaryStatus.PERMISSIBLE, "Unknown preference: " + preference, new HashSet<>(), new HashSet<>());
        }
    }

    private DietaryResult analyzeHalal(String ingredients, boolean hasHalalCertification) {
        Set<String> problematic = new HashSet<>();
        Set<String> needsVerification = new HashSet<>();

        // If product has halal certification, it's automatically green
        if (hasHalalCertification) {
            return new DietaryResult(DietaryStatus.PERMISSIBLE, "Product has halal certification", problematic, needsVerification);
        }

        // Check for definitely haram ingredients
        for (String haram : NON_HALAL_KEYWORDS) {
            if (ingredients.contains(haram)) {
                problematic.add(haram);
            }
        }

        if (!problematic.isEmpty()) {
            return new DietaryResult(DietaryStatus.NOT_PERMISSIBLE,
                    "Contains " + String.join(", ", problematic) + " which is not halal", problematic, needsVerification);
        }

        // Check if product is vegan/vegetarian (likely halal if no alcohol/pork)
        boolean isVeganFriendly = true;
        boolean hasAnimalProducts = false;

        // Check for animal products that might not be halal
        for (String animalProduct : NON_VEGAN_KEYWORDS) {
            if (ingredients.contains(animalProduct)) {
                hasAnimalProducts = true;
                break;
            }
        }

        // If no animal products and no alcohol/pork, likely halal
        if (!hasAnimalProducts) {
            return new DietaryResult(DietaryStatus.PERMISSIBLE, "Product appears to be plant-based/vegan - likely halal", problematic, needsVerification);
        }

        // Check for ingredients that need verification
        for (String questionable : HALAL_VERIFICATION_NEEDED) {
            if (ingredients.contains(questionable)) {
                needsVerification.add(questionable);
            }
        }

        if (!needsVerification.isEmpty()) {
            return new DietaryResult(DietaryStatus.NEEDS_VERIFICATION,
                    "Contains " + String.join(", ", needsVerification) + " - verify halal source/processing", problematic, needsVerification);
        }

        return new DietaryResult(DietaryStatus.PERMISSIBLE, "No obvious non-halal ingredients found", problematic, needsVerification);
    }

    private DietaryResult analyzeKosher(String ingredients) {
        Set<String> problematic = new HashSet<>();
        Set<String> needsVerification = new HashSet<>();

        for (String nonKosher : NON_KOSHER_KEYWORDS) {
            if (ingredients.contains(nonKosher)) {
                problematic.add(nonKosher);
            }
        }

        if (!problematic.isEmpty()) {
            return new DietaryResult(DietaryStatus.NOT_PERMISSIBLE,
                    "Contains " + String.join(", ", problematic) + " which is not kosher", problematic, needsVerification);
        }

        for (String questionable : KOSHER_VERIFICATION_NEEDED) {
            if (ingredients.contains(questionable)) {
                needsVerification.add(questionable);
            }
        }

        if (!needsVerification.isEmpty()) {
            return new DietaryResult(DietaryStatus.NEEDS_VERIFICATION,
                    "Contains " + String.join(", ", needsVerification) + " - verify kosher certification", problematic, needsVerification);
        }

        return new DietaryResult(DietaryStatus.PERMISSIBLE, "No obvious non-kosher ingredients found", problematic, needsVerification);
    }

    private DietaryResult analyzeVegan(String ingredients) {
        Set<String> problematic = new HashSet<>();
        Set<String> needsVerification = new HashSet<>();

        for (String nonVegan : NON_VEGAN_KEYWORDS) {
            if (ingredients.contains(nonVegan)) {
                problematic.add(nonVegan);
            }
        }

        if (!problematic.isEmpty()) {
            return new DietaryResult(DietaryStatus.NOT_PERMISSIBLE,
                    "Contains " + String.join(", ", problematic) + " which is not vegan", problematic, needsVerification);
        }

        for (String questionable : VEGAN_VERIFICATION_NEEDED) {
            if (ingredients.contains(questionable)) {
                needsVerification.add(questionable);
            }
        }

        if (!needsVerification.isEmpty()) {
            return new DietaryResult(DietaryStatus.NEEDS_VERIFICATION,
                    "Contains " + String.join(", ", needsVerification) + " - verify vegan source", problematic, needsVerification);
        }

        return new DietaryResult(DietaryStatus.PERMISSIBLE, "No obvious non-vegan ingredients found", problematic, needsVerification);
    }

    private DietaryResult analyzeVegetarian(String ingredients) {
        Set<String> problematic = new HashSet<>();
        Set<String> needsVerification = new HashSet<>();

        for (String nonVegetarian : NON_VEGETARIAN_KEYWORDS) {
            if (ingredients.contains(nonVegetarian)) {
                problematic.add(nonVegetarian);
            }
        }

        if (!problematic.isEmpty()) {
            return new DietaryResult(DietaryStatus.NOT_PERMISSIBLE,
                    "Contains " + String.join(", ", problematic) + " which is not vegetarian", problematic, needsVerification);
        }

        for (String questionable : VEGETARIAN_VERIFICATION_NEEDED) {
            if (ingredients.contains(questionable)) {
                needsVerification.add(questionable);
            }
        }

        if (!needsVerification.isEmpty()) {
            return new DietaryResult(DietaryStatus.NEEDS_VERIFICATION,
                    "Contains " + String.join(", ", needsVerification) + " - verify vegetarian source", problematic, needsVerification);
        }

        return new DietaryResult(DietaryStatus.PERMISSIBLE, "No obvious non-vegetarian ingredients found", problematic, needsVerification);
    }

    private void updateIngredientsTextWithHighlighting(Set<String> problematicIngredients, Set<String> verificationNeededIngredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }

        SpannableString spannableIngredients = new SpannableString("Ingredients: " + ingredients);
        String lowerIngredients = ingredients.toLowerCase();
        int offset = "Ingredients: ".length();

        for (String problematic : problematicIngredients) {
            int index = lowerIngredients.indexOf(problematic);
            while (index != -1) {
                int start = index + offset;
                int end = start + problematic.length();
                spannableIngredients.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableIngredients.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = lowerIngredients.indexOf(problematic, index + 1);
            }
        }

        for (String verification : verificationNeededIngredients) {
            int index = lowerIngredients.indexOf(verification);
            while (index != -1) {
                int start = index + offset;
                int end = start + verification.length();
                spannableIngredients.setSpan(new ForegroundColorSpan(Color.parseColor("#FF8C00")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableIngredients.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = lowerIngredients.indexOf(verification, index + 1);
            }
        }

        ingredientsText.setText(spannableIngredients);
    }

    private void setPermissibilityStatus(String status, String colorHex, String detailMessage, Set<String> preferences) {
        statusText.setText(status);
        Log.d("DietaryAnalysis", "Setting status to: " + status + " - " + detailMessage);

        View mainLayout = findViewById(R.id.main_layout);
        if (mainLayout != null) {
            mainLayout.setBackgroundColor(Color.parseColor(colorHex));
            Log.d("DietaryAnalysis", "Background color changed to: " + colorHex);
        }

        // For photo needed case, don't append analysis text yet
        if (status.equals("Photo Needed")) {
            // Don't modify ingredientsText here, let promptForIngredientsPhoto handle it
            return;
        }

        String currentIngredientsText = ingredientsText.getText().toString();
        if (!currentIngredientsText.contains("Analysis:")) {
            String tipsSection = generateTipsSection(preferences, status);

            // Create SpannableStringBuilder to preserve highlighting
            android.text.SpannableStringBuilder fullText = new android.text.SpannableStringBuilder(ingredientsText.getText());
            fullText.append("\n\nAnalysis: ").append(detailMessage);
            if (!tipsSection.isEmpty()) {
                fullText.append("\n\n").append(tipsSection);
            }

            // Set the text with highlighting preserved
            ingredientsText.setText(fullText);

            // Always add the learn more button if there are preferences
            if (!preferences.isEmpty()) {
                addLearnMoreButton(preferences);
            }
        }
    }

    private void addLearnMoreButton(Set<String> preferences) {
        Log.d("ItemInfo", "addLearnMoreButton called - using crash-proof implementation");

        // NUCLEAR OPTION: Don't touch ScrollView AT ALL
        // Instead, modify the existing favorite button to show symbol guide

        if (favoriteButton != null) {
            // Change the favorite button text to indicate dual functionality
            String currentText = favoriteButton.getText().toString();
            if (!currentText.contains("Hold for")) {
                if (currentText.contains("Add")) {
                    favoriteButton.setText("Add to Favorites (Hold for Symbols)");
                } else {
                    favoriteButton.setText("Remove from Favorites (Hold for Symbols)");
                }
            }

            // Add long-press listener for symbol guide
            favoriteButton.setOnLongClickListener(v -> {
                showSymbolGuide(preferences);
                return true;
            });

            Log.d("ItemInfo", "Modified favorite button to show symbols on long press");
        } else {
            // If no favorite button, show toast with instructions
            Toast.makeText(this, "Dietary analysis complete. Check the menu for symbol guides.", Toast.LENGTH_LONG).show();
            Log.d("ItemInfo", "No favorite button found, showed toast instead");
        }
    }

    private String generateTipsSection(Set<String> preferences, String status) {
        if (preferences.isEmpty()) {
            return "";
        }

        StringBuilder tips = new StringBuilder();

        // Add color-coding explanation if ingredients were highlighted
        if (status.equals("Verification Needed") || status.equals("Not Permissible")) {
            tips.append("🎨 INGREDIENT COLORS:\n");
            tips.append("🔴 Red = Forbidden - Do NOT eat\n");
            tips.append("🟠 Orange = Check first - Verify before eating\n\n");
        }

        return tips.toString();
    }

    private void showSymbolGuide(Set<String> preferences) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(createSymbolGuideLayout(preferences, dialog));
        dialog.show();
    }

    private View createSymbolGuideLayout(Set<String> preferences, Dialog dialog) {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);
        mainLayout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("📖 Certification Symbols Guide");
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        title.setPadding(0, 0, 0, 30);
        mainLayout.addView(title);

        for (String preference : preferences) {
            String cleanPref = preference.trim().toLowerCase();
            addSymbolSection(mainLayout, cleanPref);
        }

        Button closeButton = new Button(this);
        closeButton.setText("Got it!");
        closeButton.setOnClickListener(v -> dialog.dismiss());
        mainLayout.addView(closeButton);

        return mainLayout;
    }

    private void addSymbolSection(LinearLayout parent, String preference) {
        TextView header = new TextView(this);
        header.setTextSize(16);
        header.setTextColor(Color.BLACK);
        header.setPadding(0, 20, 0, 15);

        // Create a horizontally scrollable container for symbols
        ScrollView horizontalScroll = new ScrollView(this);
        horizontalScroll.setHorizontalScrollBarEnabled(true);

        LinearLayout symbolContainer = new LinearLayout(this);
        symbolContainer.setOrientation(LinearLayout.HORIZONTAL);
        symbolContainer.setPadding(0, 0, 0, 20);

        horizontalScroll.addView(symbolContainer);

        switch (preference) {
            case "halal":
                header.setText("🕌 HALAL SYMBOLS:");
                addSymbolImage(symbolContainer, R.drawable.halal_italia, "Halal Italia");
                addSymbolImage(symbolContainer, R.drawable.ifanca_m, "IFANCA (M Symbol)");
                addSymbolImage(symbolContainer, R.drawable.iswa_halal, "ISWA Certification");
                addSymbolImage(symbolContainer, R.drawable.niht_halal, "NIHT Trust");
                addSymbolImage(symbolContainer, R.drawable.halal_crescent, "Halal Crescent");
                addSymbolImage(symbolContainer, R.drawable.halal_text_simple, "Halal Text");
                break;
            case "kosher":
                header.setText("✡️ KOSHER SYMBOLS:");
                addSymbolImage(symbolContainer, R.drawable.kosher_ou, "OU Orthodox Union");
                addSymbolImage(symbolContainer, R.drawable.kosher_ok, "OK Kosher");
                addSymbolImage(symbolContainer, R.drawable.kosher_star_k, "Star-K");
                break;
            case "vegan":
                header.setText("🌱 VEGAN SYMBOLS:");
                addSymbolImage(symbolContainer, R.drawable.vegan_certified, "Certified Vegan");
                addSymbolImage(symbolContainer, R.drawable.vegan_leaf, "Vegan Leaf");
                addSymbolImage(symbolContainer, R.drawable.vegan_v, "Vegan V");
                break;
            case "vegetarian":
                header.setText("🥕 VEGETARIAN SYMBOLS:");
                addSymbolImage(symbolContainer, R.drawable.vegetarian_v, "Vegetarian V");
                addSymbolImage(symbolContainer, R.drawable.vegetarian_green_dot, "Green Dot (India)");
                addSymbolImage(symbolContainer, R.drawable.vegetarian_leaf, "Vegetarian Leaf");
                break;
        }

        parent.addView(header);
        parent.addView(horizontalScroll);

        if (preference.equals("halal")) {
            addHalalAuthorityInfo(parent);
        }
    }

    private void addSymbolImage(LinearLayout container, int drawableId, String description) {
        LinearLayout symbolLayout = new LinearLayout(this);
        symbolLayout.setOrientation(LinearLayout.VERTICAL);
        symbolLayout.setPadding(10, 10, 10, 10);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        try {
            imageView.setImageResource(drawableId);
        } catch (Exception e) {
            imageView.setImageResource(android.R.drawable.ic_dialog_info);
        }

        TextView descText = new TextView(this);
        descText.setText(description);
        descText.setTextSize(10);
        descText.setTextColor(Color.BLACK);
        descText.setGravity(Gravity.CENTER);

        symbolLayout.addView(imageView);
        symbolLayout.addView(descText);
        container.addView(symbolLayout);
    }

    private void addHalalAuthorityInfo(LinearLayout parent) {
        TextView infoHeader = new TextView(this);
        infoHeader.setText("ℹ️ TRUSTED HALAL AUTHORITIES:");
        infoHeader.setTextSize(14);
        infoHeader.setTextColor(Color.BLACK);
        infoHeader.setPadding(0, 20, 0, 10);
        parent.addView(infoHeader);

        TextView authorityInfo = new TextView(this);
        authorityInfo.setText(
                "✅ IFANCA (M Symbol) - Global leader, 50+ countries\n" +
                        "✅ ISWA - USA Halal Chamber of Commerce\n" +
                        "✅ Halal Italia - Italian food certification\n" +
                        "✅ NIHT - High standards, Sharia Law compliant\n\n" +
                        "🔍 Look for these symbols on packaging for guaranteed halal products!"
        );
        authorityInfo.setTextSize(12);
        authorityInfo.setTextColor(Color.DKGRAY);
        authorityInfo.setPadding(10, 0, 10, 0);
        parent.addView(authorityInfo);
    }

    // Helper classes
    private enum DietaryStatus {
        PERMISSIBLE,
        NEEDS_VERIFICATION,
        NOT_PERMISSIBLE
    }

    private static class DietaryResult {
        DietaryStatus status;
        String message;
        Set<String> problematicIngredients;
        Set<String> verificationNeededIngredients;

        DietaryResult(DietaryStatus status, String message, Set<String> problematicIngredients, Set<String> verificationNeededIngredients) {
            this.status = status;
            this.message = message;
            this.problematicIngredients = problematicIngredients;
            this.verificationNeededIngredients = verificationNeededIngredients;
        }
    }

    private void setupFavoriteButton() {
        updateFavoriteButtonState();

        favoriteButton.setOnClickListener(v -> {
            if (isProductInFavorites()) {
                removeFromFavorites();
            } else {
                addToFavorites();
            }
        });
    }

    private boolean isProductInFavorites() {
        if (savedItemsManager == null || barcode == null || barcode.isEmpty()) {
            Log.e("ItemInfo", "SavedItemsManager is null or barcode is invalid");
            return false;
        }
        boolean isSaved = savedItemsManager.isItemSaved(barcode);
        Log.d("ItemInfo", "Item saved check for " + barcode + ": " + isSaved);
        return isSaved;
    }

    private void addToFavorites() {
        if (savedItemsManager == null) {
            Toast.makeText(this, "Error: Cannot save item", Toast.LENGTH_SHORT).show();
            Log.e("ItemInfo", "SavedItemsManager is null");
            return;
        }

        if (barcode == null || barcode.isEmpty()) {
            Toast.makeText(this, "Error: Invalid product data", Toast.LENGTH_SHORT).show();
            Log.e("ItemInfo", "Barcode is null or empty");
            return;
        }

        Log.d("ItemInfo", "Attempting to save - Barcode: " + barcode + ", Name: " + productName);

        boolean success = savedItemsManager.saveItem(
                barcode,
                productName,
                ingredients,
                imageUrl,
                ingredients,
                brands
        );

        if (success) {
            Toast.makeText(this, "Added to favorites!", Toast.LENGTH_SHORT).show();
            Log.d("ItemInfo", "Item saved successfully");
        } else {
            Toast.makeText(this, "Failed to save item", Toast.LENGTH_SHORT).show();
            Log.e("ItemInfo", "Failed to save item");
        }

        updateFavoriteButtonState();
    }

    private void removeFromFavorites() {
        if (savedItemsManager == null) {
            Toast.makeText(this, "Error: Cannot remove item", Toast.LENGTH_SHORT).show();
            Log.e("ItemInfo", "SavedItemsManager is null");
            return;
        }

        if (barcode == null || barcode.isEmpty()) {
            Toast.makeText(this, "Error: Invalid product data", Toast.LENGTH_SHORT).show();
            Log.e("ItemInfo", "Barcode is null or empty");
            return;
        }

        boolean success = savedItemsManager.removeItem(barcode);

        if (success) {
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
            Log.d("ItemInfo", "Item removed successfully");
        } else {
            Toast.makeText(this, "Failed to remove item", Toast.LENGTH_SHORT).show();
            Log.e("ItemInfo", "Failed to remove item");
        }

        updateFavoriteButtonState();
    }

    private void updateFavoriteButtonState() {
        if (isProductInFavorites()) {
            favoriteButton.setText("Remove From Favorites");
        } else {
            favoriteButton.setText("Add To Favorites");
        }
    }
}