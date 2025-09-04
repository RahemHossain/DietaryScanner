package com.example.dietaryscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutionException;

public class IngredientsCameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private LinearLayout cameraContainer;
    private LinearLayout resultContainer;

    private Button captureButton;
    private Button retakeButton;
    private Button analyzeButton;
    private TextView instructionsText;
    private EditText ingredientsEditText;
    private ProgressBar progressBar;

    private ImageCapture imageCapture;
    private TextRecognizer textRecognizer;

    private String barcode;
    private String productName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients_camera);

        // Get data from intent
        barcode = getIntent().getStringExtra("barcode");
        productName = getIntent().getStringExtra("product_name");

        initViews();
        initTextRecognizer();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1001);
        }
    }

    private void initViews() {
        previewView = findViewById(R.id.preview_view);
        cameraContainer = findViewById(R.id.camera_container);
        resultContainer = findViewById(R.id.result_container);

        captureButton = findViewById(R.id.capture_button);
        retakeButton = findViewById(R.id.retake_button);
        analyzeButton = findViewById(R.id.analyze_button);
        instructionsText = findViewById(R.id.instructions_text);
        ingredientsEditText = findViewById(R.id.ingredients_edit_text);
        progressBar = findViewById(R.id.progress_bar);

        // Set up instructions
        instructionsText.setText("📸 Point camera at ingredients list\n\nCapture photo to scan ingredients automatically");

        // Set up buttons
        captureButton.setOnClickListener(v -> capturePhoto());
        retakeButton.setOnClickListener(v -> retakePhoto());
        analyzeButton.setOnClickListener(v -> analyzeIngredients());

        // Show camera mode initially
        showCameraMode();
    }

    private void initTextRecognizer() {
        // Use the newer, more accurate text recognizer with better options
        TextRecognizerOptions options = new TextRecognizerOptions.Builder()
                .setExecutor(ContextCompat.getMainExecutor(this))
                .build();
        textRecognizer = TextRecognition.getClient(options);
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required to scan ingredients", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void showCameraMode() {
        cameraContainer.setVisibility(View.VISIBLE);
        resultContainer.setVisibility(View.GONE);
        instructionsText.setText("📸 Point camera at ingredients list\n\nCapture photo to scan ingredients automatically");
    }

    private void showResultMode(String extractedText) {
        cameraContainer.setVisibility(View.GONE);
        resultContainer.setVisibility(View.VISIBLE);

        if (extractedText.isEmpty()) {
            ingredientsEditText.setHint("No text detected. Type ingredients manually...");
        } else {
            ingredientsEditText.setText(extractedText);
        }

        instructionsText.setText("✅ Ingredients detected!\n\nReview and edit if needed, then tap Analyze");
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        showProcessing(true);

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        processImageForText(image);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Photo capture failed: " + exception.getMessage(), exception);
                        showProcessing(false);
                        Toast.makeText(IngredientsCameraActivity.this, "Failed to capture photo. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void processImageForText(ImageProxy imageProxy) {
        @SuppressWarnings("ConstantConditions")
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            // Process with better error handling and multiple attempts
            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        try {
                            Log.d("OCR", "=== RAW OCR RESULTS ===");
                            Log.d("OCR", "Full text: " + visionText.getText());

                            // Log all text blocks for debugging
                            for (Text.TextBlock block : visionText.getTextBlocks()) {
                                Log.d("OCR", "Block: '" + block.getText() + "'");
                                for (Text.Line line : block.getLines()) {
                                    Log.d("OCR", "  Line: '" + line.getText() + "'");
                                }
                            }

                            extractTextFromVisionResult(visionText);
                        } catch (Exception e) {
                            Log.e("OCR", "Error processing vision result", e);
                            runOnUiThread(() -> {
                                showProcessing(false);
                                showResultMode(""); // Show empty for manual entry
                                Toast.makeText(this, "Error processing text. Please type manually.", Toast.LENGTH_SHORT).show();
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("OCR", "Text recognition failed", e);
                        runOnUiThread(() -> {
                            showProcessing(false);
                            showResultMode(""); // Show empty result for manual entry
                            Toast.makeText(this, "Failed to read text. You can type manually.", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnCompleteListener(task -> {
                        try {
                            imageProxy.close();
                        } catch (Exception e) {
                            Log.e("OCR", "Error closing image proxy", e);
                        }
                    });
        } else {
            imageProxy.close();
            runOnUiThread(() -> {
                showProcessing(false);
                Toast.makeText(this, "Failed to capture image. Please try again.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void extractTextFromVisionResult(Text visionText) {
        StringBuilder extractedIngredients = new StringBuilder();
        String countryOfOrigin = "";

        // Get ALL detected text with positions for debugging
        Log.d("OCR", "=== ALL DETECTED TEXT BLOCKS ===");
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            Log.d("OCR", "Block: '" + block.getText() + "' | Bounds: " + block.getBoundingBox());
        }

        // STRATEGY 1: Collect ALL text and try to find the best ingredients section
        StringBuilder allText = new StringBuilder();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            allText.append(block.getText()).append(" ");
        }

        String fullText = allText.toString();
        Log.d("OCR", "Full combined text: " + fullText);

        // STRATEGY 2: Look for ingredients keyword and get nearby text
        String[] allBlocks = new String[visionText.getTextBlocks().size()];
        int ingredientsBlockIndex = -1;

        for (int i = 0; i < visionText.getTextBlocks().size(); i++) {
            Text.TextBlock block = visionText.getTextBlocks().get(i);
            allBlocks[i] = block.getText();
            String blockLower = block.getText().toLowerCase();

            if (blockLower.contains("ingredient") || blockLower.contains("ngredient")) {
                ingredientsBlockIndex = i;
                Log.d("OCR", "Found ingredients keyword at block " + i + ": " + block.getText());
            }
        }

        // If we found ingredients label, get surrounding blocks
        if (ingredientsBlockIndex >= 0) {
            StringBuilder ingredientsText = new StringBuilder();

            // Include the ingredients block itself
            ingredientsText.append(allBlocks[ingredientsBlockIndex]).append(" ");

            // Include blocks after the ingredients label (likely the actual list)
            for (int i = ingredientsBlockIndex + 1; i < allBlocks.length && i < ingredientsBlockIndex + 3; i++) {
                if (allBlocks[i].length() > 5) { // Skip very short blocks
                    ingredientsText.append(allBlocks[i]).append(" ");
                }
            }

            extractedIngredients.append(ingredientsText.toString());
            Log.d("OCR", "Extracted from ingredients section: " + ingredientsText.toString());
        }

        // STRATEGY 3: If no explicit ingredients found, look for the longest text block
        if (extractedIngredients.length() < 20) {
            Log.d("OCR", "No good ingredients section found, looking for longest block");

            String longestBlock = "";
            for (Text.TextBlock block : visionText.getTextBlocks()) {
                String blockText = block.getText();
                if (blockText.length() > longestBlock.length() && blockText.length() > 15) {
                    String lowerText = blockText.toLowerCase();

                    // Skip obviously non-ingredient blocks
                    if (!lowerText.contains("nutrition") && !lowerText.contains("calories") &&
                            !lowerText.contains("serving") && !lowerText.contains("distributed")) {
                        longestBlock = blockText;
                    }
                }
            }

            if (!longestBlock.isEmpty()) {
                extractedIngredients.append(longestBlock);
                Log.d("OCR", "Using longest block: " + longestBlock);
            }
        }

        // STRATEGY 4: If still nothing good, just use all text
        if (extractedIngredients.length() < 10) {
            extractedIngredients.append(fullText);
            Log.d("OCR", "Using all text as fallback");
        }

        // Look for country of origin
        countryOfOrigin = findCountryOfOrigin(visionText);

        String finalText = cleanupOcrText(extractedIngredients.toString().trim());

        // Add country info if found
        if (!countryOfOrigin.isEmpty()) {
            finalText += "\n\n[Country of Origin: " + countryOfOrigin.toUpperCase() + "]";
        }

        Log.d("OCR", "Final extracted text: " + finalText);

        final String resultText = finalText;

        runOnUiThread(() -> {
            showProcessing(false);
            showResultMode(resultText);
        });
    }

    // Score a text block based on how likely it is to contain ingredients
    private int scoreTextBlock(String text) {
        if (text == null || text.trim().isEmpty()) return 0;

        String lowerText = text.toLowerCase();
        int score = 0;

        // High value ingredients
        String[] highValueIngredients = {
                "flour", "sugar", "salt", "oil", "water", "milk", "egg", "butter", "wheat", "corn", "rice", "soy",
                "pork", "beef", "chicken", "bacon", "gelatin", "alcohol", "dairy", "nuts", "peanuts"
        };

        for (String ingredient : highValueIngredients) {
            if (lowerText.contains(ingredient)) {
                score += 10;
                Log.d("OCR", "Found high-value ingredient: " + ingredient);
            }
        }

        // Comma-separated lists (very typical for ingredients)
        if (text.contains(",")) {
            int commas = text.length() - text.replace(",", "").length();
            score += commas * 3;
        }

        // Parenthetical content (common in ingredients)
        if (text.contains("(") && text.contains(")")) {
            score += 5;
        }

        // Longer text blocks are more likely to be ingredients lists
        if (text.length() > 50) score += 3;
        if (text.length() > 100) score += 3;
        if (text.length() > 200) score += 4;

        // Penalize nutrition facts, company info, etc.
        if (lowerText.contains("calories") || lowerText.contains("nutrition") ||
                lowerText.contains("serving") || lowerText.contains("distributed") ||
                lowerText.contains("manufactured") || lowerText.contains("net wt")) {
            score -= 15;
        }

        // Bonus for containing the word "ingredients"
        if (lowerText.contains("ingredients")) {
            score += 8;
        }

        return score;
    }

    private Text.TextBlock findContainsBlock(Text visionText) {
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            String blockText = block.getText().toLowerCase();
            if (blockText.contains("contains") &&
                    (blockText.contains("wheat") || blockText.contains("soy") || blockText.contains("nut") ||
                            blockText.contains("milk") || blockText.contains("egg"))) {
                return block;
            }
        }
        return null;
    }

    private Text.TextBlock findIngredientsLabel(Text visionText) {
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            String blockText = block.getText().toLowerCase();
            if (blockText.contains("ingredients") || blockText.contains("ingredient")) {
                return block;
            }
        }
        return null;
    }

    private Text.TextBlock findTextAboveBlock(Text visionText, Text.TextBlock referenceBlock) {
        if (referenceBlock.getBoundingBox() == null) return null;

        int referenceTop = referenceBlock.getBoundingBox().top;
        Text.TextBlock closestBlock = null;
        int closestDistance = Integer.MAX_VALUE;

        for (Text.TextBlock block : visionText.getTextBlocks()) {
            if (block.getBoundingBox() != null && !block.equals(referenceBlock)) {
                int blockBottom = block.getBoundingBox().bottom;

                if (blockBottom < referenceTop) {
                    int distance = referenceTop - blockBottom;
                    if (distance < closestDistance && scoreTextBlock(block.getText()) > 5) {
                        closestDistance = distance;
                        closestBlock = block;
                    }
                }
            }
        }

        return closestBlock;
    }

    private Text.TextBlock findTextBelowBlock(Text visionText, Text.TextBlock referenceBlock) {
        if (referenceBlock.getBoundingBox() == null) return null;

        int referenceBottom = referenceBlock.getBoundingBox().bottom;
        Text.TextBlock closestBlock = null;
        int closestDistance = Integer.MAX_VALUE;

        for (Text.TextBlock block : visionText.getTextBlocks()) {
            if (block.getBoundingBox() != null && !block.equals(referenceBlock)) {
                int blockTop = block.getBoundingBox().top;

                if (blockTop > referenceBottom) {
                    int distance = blockTop - referenceBottom;
                    if (distance < closestDistance && scoreTextBlock(block.getText()) > 5) {
                        closestDistance = distance;
                        closestBlock = block;
                    }
                }
            }
        }

        return closestBlock;
    }

    private Text.TextBlock findLongestIngredientsBlock(Text visionText) {
        Text.TextBlock longestBlock = null;
        int maxLength = 0;

        for (Text.TextBlock block : visionText.getTextBlocks()) {
            String blockText = block.getText();
            if (blockText.length() > maxLength && blockText.length() > 20) {
                String lowerText = blockText.toLowerCase();

                // Skip obvious non-ingredient blocks
                if (!lowerText.contains("nutrition") && !lowerText.contains("serving") &&
                        !lowerText.contains("calories") && !lowerText.contains("distributed") &&
                        !lowerText.contains("manufactured") && !lowerText.contains("net wt")) {

                    maxLength = blockText.length();
                    longestBlock = block;
                }
            }
        }

        return longestBlock;
    }

    private String findCountryOfOrigin(Text visionText) {
        String[] muslimCountries = {
                "turkey", "malaysia", "indonesia", "bangladesh", "pakistan", "egypt",
                "morocco", "tunisia", "jordan", "lebanon", "uae", "saudi arabia",
                "qatar", "kuwait", "oman", "bahrain"
        };

        for (Text.TextBlock block : visionText.getTextBlocks()) {
            String blockText = block.getText().toLowerCase();
            if (blockText.contains("product of") || blockText.contains("made in") ||
                    blockText.contains("manufactured in") || blockText.contains("distributed") ||
                    blockText.contains("imported from")) {

                for (String country : muslimCountries) {
                    if (blockText.contains(country)) {
                        Log.d("OCR", "Found Muslim country of origin: " + country);
                        return country;
                    }
                }
            }
        }
        return "";
    }

    private boolean containsIngredientPatterns(String text) {
        // Enhanced ingredient detection with critical dietary restriction words
        String[] strongIndicators = {
                "flour", "sugar", "salt", "oil", "water", "milk", "egg", "butter",
                "wheat", "corn", "rice", "soy", "palm", "canola", "sunflower",
                "vanilla", "chocolate", "cocoa", "yeast", "sodium", "acid",
                "powder", "syrup", "starch", "extract", "natural flavor",
                "artificial flavor", "preservative", "emulsifier", "stabilizer"
        };

        // CRITICAL dietary restriction ingredients that MUST be detected
        String[] criticalIngredients = {
                // Meat and animal products
                "pork", "beef", "chicken", "bacon", "ham", "lard", "gelatin", "gelatine",
                "turkey", "lamb", "duck", "fish", "seafood", "shellfish", "anchovy", "tuna", "salmon",

                // Alcohol
                "alcohol", "ethanol", "wine", "beer", "vodka", "rum", "whiskey", "brandy", "sake", "mirin",

                // Dairy
                "dairy", "lactose", "casein", "whey", "cream", "yogurt", "yoghurt",

                // Nuts and allergens
                "nuts", "peanuts", "almonds", "walnuts", "cashews", "pistachios", "hazelnuts", "pecans",
                "sesame", "tree nuts"
        };

        String lowerText = text.toLowerCase();
        int strongMatches = 0;
        int criticalMatches = 0;

        // Count regular ingredient indicators
        for (String indicator : strongIndicators) {
            if (lowerText.contains(indicator)) {
                strongMatches++;
            }
        }

        // Count critical dietary restriction ingredients (these are weighted more heavily)
        for (String critical : criticalIngredients) {
            if (lowerText.contains(critical)) {
                criticalMatches++;
                Log.d("OCR", "Found critical ingredient: " + critical);
            }
        }

        // Also look for comma-separated lists (typical ingredient format)
        boolean hasCommaList = text.contains(",") && text.split(",").length >= 3;

        // Check for parenthetical ingredients like "flour (wheat flour, rice flour)"
        boolean hasParenthetical = text.contains("(") && text.contains(")");

        // If we find ANY critical dietary ingredients, this is likely an ingredients list
        // Otherwise use the original logic
        return criticalMatches >= 1 || strongMatches >= 2 || hasCommaList || (strongMatches >= 1 && hasParenthetical);
    }

    private String cleanupOcrText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String cleaned = text
                // Fix spacing issues
                .replaceAll("\\s+", " ") // Multiple spaces to single space
                .replaceAll("([a-z])([A-Z])", "$1 $2") // Add space between lowercase and uppercase

                // Fix common OCR word errors
                .replaceAll("\\b(ing redients?|ngredients?|lngredients?)\\b", "ingredients")
                .replaceAll("\\b(contalns|conlains|contains?)\\b", "contains")
                .replaceAll("\\b(soybean|soy bean)\\b", "soybean")
                .replaceAll("\\b(seasame|seseme|sesames?)\\b", "sesame")
                .replaceAll("\\b(tre nuts?|tree nuls?|treenuts?)\\b", "tree nuts")

                // CRITICAL: Fix meat and animal product OCR errors (case insensitive)
                .replaceAll("(?i)\\b(pork|p0rk|porK|p0rK)\\b", "pork")
                .replaceAll("(?i)\\b(beef|beaf|beeF|b33f)\\b", "beef")
                .replaceAll("(?i)\\b(chicken|chickan|chiken|chicKen)\\b", "chicken")
                .replaceAll("(?i)\\b(bacon|bac0n|bacen|bacOn)\\b", "bacon")
                .replaceAll("(?i)\\b(ham|harn|hamm|h4m)\\b", "ham")
                .replaceAll("(?i)\\b(lard|Iard|1ard|larD)\\b", "lard")
                .replaceAll("(?i)\\b(gelatin|gelatln|gelafin|gelallne?)\\b", "gelatin")
                .replaceAll("(?i)\\b(gelatine|gelatme|gelatLne)\\b", "gelatine")
                .replaceAll("(?i)\\b(turkey|furkey|turKey|turk3y)\\b", "turkey")
                .replaceAll("(?i)\\b(lamb|lamB|1amb|Iamb)\\b", "lamb")
                .replaceAll("(?i)\\b(duck|ducK|duCk|d0ck)\\b", "duck")
                .replaceAll("(?i)\\b(fish|flsh|fi5h|f1sh)\\b", "fish")
                .replaceAll("(?i)\\b(seafood|seafo0d|sea food)\\b", "seafood")
                .replaceAll("(?i)\\b(shellfish|shell fish|shellflsh)\\b", "shellfish")
                .replaceAll("(?i)\\b(anchovy|anch0vy|anchOvy)\\b", "anchovy")
                .replaceAll("(?i)\\b(tuna|funa|tun4|tunA)\\b", "tuna")
                .replaceAll("(?i)\\b(salmon|sa1mon|salm0n|saImon)\\b", "salmon")

                // CRITICAL: Fix alcohol OCR errors
                .replaceAll("(?i)\\b(alcohol|alc0hol|alcoh0l|aIcohol)\\b", "alcohol")
                .replaceAll("(?i)\\b(ethanol|ethan0l|ethano1|3thanol)\\b", "ethanol")
                .replaceAll("(?i)\\b(wine|wlne|w1ne|winE)\\b", "wine")
                .replaceAll("(?i)\\b(beer|be3r|beeR|b33r)\\b", "beer")
                .replaceAll("(?i)\\b(vodka|v0dka|vodKa|vodka)\\b", "vodka")
                .replaceAll("(?i)\\b(rum|rurn|ruM|r0m)\\b", "rum")
                .replaceAll("(?i)\\b(whiskey|whisky|whlskey|wh1skey)\\b", "whiskey")
                .replaceAll("(?i)\\b(brandy|branDy|brandt|brand?y)\\b", "brandy")
                .replaceAll("(?i)\\b(sake|sakE|s4ke|saKe)\\b", "sake")
                .replaceAll("(?i)\\b(mirin|mlrin|mir1n|miRin)\\b", "mirin")
                .replaceAll("(?i)\\b(vanilla extract|vanila extract|van1lla extract)\\b", "vanilla extract")

                // CRITICAL: Fix common allergen OCR errors
                .replaceAll("(?i)\\b(nuts?|nuls?|nu7s?|n0ts?)\\b", "nuts")
                .replaceAll("(?i)\\b(peanuts?|peanuls?|pean0ts?|p3anuts?)\\b", "peanuts")
                .replaceAll("(?i)\\b(almonds?|alm0nds?|aimonds?|aImonds?)\\b", "almonds")
                .replaceAll("(?i)\\b(walnuts?|walnuls?|waInuts?|waln0ts?)\\b", "walnuts")
                .replaceAll("(?i)\\b(cashews?|cash3ws?|cashEws?|cashaws?)\\b", "cashews")
                .replaceAll("(?i)\\b(pistachios?|plstachios?|pistachi0s?)\\b", "pistachios")
                .replaceAll("(?i)\\b(hazelnuts?|hazelnuls?|haz3lnuts?)\\b", "hazelnuts")
                .replaceAll("(?i)\\b(pecans?|p3cans?|peCans?|p0cans?)\\b", "pecans")

                // CRITICAL: Fix dairy OCR errors
                .replaceAll("(?i)\\b(dairy|dalry|da1ry|dairY)\\b", "dairy")
                .replaceAll("(?i)\\b(lactose|lact0se|lactOse|1actose)\\b", "lactose")
                .replaceAll("(?i)\\b(casein|cas3in|casEin|case1n)\\b", "casein")
                .replaceAll("(?i)\\b(whey|wh3y|wheY|wh0y)\\b", "whey")
                .replaceAll("(?i)\\b(cream|cr3am|creAm|cr0am)\\b", "cream")
                .replaceAll("(?i)\\b(yogurt|y0gurt|yogurf|y0g0rt)\\b", "yogurt")
                .replaceAll("(?i)\\b(yoghurt|y0ghurt|yoghurT|y0gh0rt)\\b", "yoghurt")

                // Fix common vegetable/ingredient OCR errors (especially potato)
                .replaceAll("(?i)\\b(potato|potata|p0tato|potaf0|pofato|polato)\\b", "potato")
                .replaceAll("(?i)\\b(sal|sall|sait|sa1t)\\b", "salt") // salt corrections
                .replaceAll("(?i)\\b(sugat|sugaf|sugarn?|s0gar)\\b", "sugar") // sugar corrections
                .replaceAll("(?i)\\b(flout|floun|flowr?|fl0ur)\\b", "flour") // flour corrections
                .replaceAll("(?i)\\b(waler|wafer|watem?|wat3r)\\b", "water") // water corrections
                .replaceAll("(?i)\\b(oiI|oil|0il|o1l)\\b", "oil") // oil corrections (I vs l vs 0)
                .replaceAll("(?i)\\b(mllk|milK|miIk|m1lk)\\b", "milk") // milk corrections
                .replaceAll("(?i)\\b(eqq|egq|eg9|3gg)\\b", "egg") // egg corrections
                .replaceAll("(?i)\\b(bufter|buter|butfer|butt3r)\\b", "butter") // butter corrections
                .replaceAll("(?i)\\b(cheess|chese|chees|ch33se)\\b", "cheese") // cheese corrections
                .replaceAll("(?i)\\b(wheal|whaat|wheaf|wh3at)\\b", "wheat") // wheat corrections
                .replaceAll("(?i)\\b(com|c0rn|corn)\\b", "corn") // corn corrections
                .replaceAll("(?i)\\b(ricE|riee|r1ce|riCe)\\b", "rice") // rice corrections
                .replaceAll("(?i)\\b(s0y|soy|50y|s3y)\\b", "soy") // soy corrections (0 vs o)
                .replaceAll("(?i)\\b(palrn|paim|palm|pa1m)\\b", "palm") // palm corrections
                .replaceAll("(?i)\\b(canola|can0la|canoIa|can01a)\\b", "canola") // canola corrections
                .replaceAll("(?i)\\b(vanilla|vaniIla|vanila|van1lla)\\b", "vanilla") // vanilla corrections
                .replaceAll("(?i)\\b(chocolate|choc0late|chocolafe|ch0colate)\\b", "chocolate") // chocolate corrections
                .replaceAll("(?i)\\b(baking|baklng|bakina|bak1ng)\\b", "baking") // baking corrections
                .replaceAll("(?i)\\b(sodium|s0dium|sodlum|sod1um)\\b", "sodium") // sodium corrections
                .replaceAll("(?i)\\b(powder|powdef|powden|p0wder)\\b", "powder") // powder corrections
                .replaceAll("(?i)\\b(extract|exfract|extracf|3xtract)\\b", "extract") // extract corrections
                .replaceAll("(?i)\\b(yeast|yeasf|yeasl|y3ast)\\b", "yeast") // yeast corrections
                .replaceAll("(?i)\\b(onion|0nion|onl0n|on1on)\\b", "onion") // onion corrections
                .replaceAll("(?i)\\b(spices|spieES|spicos|sp1ces)\\b", "spices") // spices corrections
                .replaceAll("(?i)\\b(garlic|garIic|garlLc|garl1c)\\b", "garlic") // garlic corrections
                .replaceAll("(?i)\\b(pepper|peppet|peppen|p3pper)\\b", "pepper") // pepper corrections
                .replaceAll("(?i)\\b(paprika|paprLka|paprlka|papr1ka)\\b", "paprika") // paprika corrections
                .replaceAll("(?i)\\b(tomato|t0mato|lomato|tom4to)\\b", "tomato") // tomato corrections
                .replaceAll("(?i)\\b(capsicum|capslcum|capsicom|caps1cum)\\b", "capsicum") // capsicum corrections

                // Fix numeric/letter confusion in common ingredients
                .replaceAll("\\b0il\\b", "oil") // 0 to o in oil
                .replaceAll("\\b0nion\\b", "onion") // 0 to o in onion
                .replaceAll("\\bmiIk\\b", "milk") // I to l in milk
                .replaceAll("\\boiI\\b", "oil") // I to l in oil

                // Remove standalone labels but keep if they're part of a sentence
                .replaceAll("^Ingredients?:?\\s*", "") // Remove "Ingredients:" at start
                .replaceAll("\\bIngredients?:?\\s*", "Ingredients: ") // Fix in middle of text

                // Clean up punctuation
                .replaceAll("\\s*,\\s*", ", ") // Fix comma spacing
                .replaceAll("\\s*\\.\\s*", ". ") // Fix period spacing
                .replaceAll("\\s*;\\s*", "; ") // Fix semicolon spacing
                .replaceAll("\\s*\\(\\s*", " (") // Fix parentheses spacing
                .replaceAll("\\s*\\)\\s*", ") ") // Fix closing parentheses spacing

                // Remove extra newlines but keep paragraph breaks
                .replaceAll("\\n+", " ")

                // Fix word boundaries
                .replaceAll("([a-z])([A-Z])", "$1 $2") // Add space between words that got merged

                .trim();

        Log.d("OCR", "Text after cleanup: " + cleaned);
        return cleaned;
    }

    private void showProcessing(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        captureButton.setEnabled(!show);
        if (show) {
            instructionsText.setText("🔍 Scanning ingredients...");
        }
    }

    private void retakePhoto() {
        showCameraMode();
    }

    private void analyzeIngredients() {
        String ingredients = ingredientsEditText.getText().toString().trim();

        Log.d("OCR", "Analyze button clicked. Ingredients: " + ingredients);

        if (ingredients.isEmpty()) {
            Toast.makeText(this, "Please enter ingredients or retake photo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Return to iteminfoscreen with extracted ingredients
        Intent resultIntent = new Intent();
        resultIntent.putExtra("extracted_ingredients", ingredients);
        setResult(RESULT_OK, resultIntent);

        Log.d("OCR", "Returning to iteminfoscreen with ingredients: " + ingredients);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textRecognizer != null) {
            textRecognizer.close();
        }
    }
}