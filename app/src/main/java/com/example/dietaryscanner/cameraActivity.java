package com.example.dietaryscanner;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

@OptIn(markerClass = ExperimentalGetImage.class)
public class cameraActivity extends AppCompatActivity {

    private static final String TAG = "cameraActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 1001;

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private boolean isScanning = true;
    private long lastScanTime = 0;
    private static final long SCAN_COOLDOWN = 3000; // 3 seconds between scans

    // API interface
    interface ProductAPI {
        @GET("api/v2/product/{barcode}.json")
        Call<ProductResponse> getProduct(@Path("barcode") String barcode);
    }

    public static class ProductResponse {
        public Product product;
        public int status;
        public String status_verbose;
    }

    public static class Product {
        public String product_name;
        public String ingredients_text;
        public String image_url;
        public String brands;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera); // Use your existing camera.xml layout

        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        setupBackButton();

        // Check camera permission first
        if (checkCameraPermission()) {
            Log.d(TAG, "Camera permission already granted, starting camera");
            initializeCamera();
        } else {
            Log.d(TAG, "Camera permission not granted, requesting permission");
            requestCameraPermission();
        }
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Camera permission is needed to scan barcodes", Toast.LENGTH_LONG).show();
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "Permission result received: requestCode=" + requestCode);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted by user");
                Toast.makeText(this, "Camera ready - point at a barcode!", Toast.LENGTH_SHORT).show();
                initializeCamera();
            } else {
                Log.e(TAG, "Camera permission denied by user");
                Toast.makeText(this, "Camera permission is required for barcode scanning",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initializeCamera() {
        Log.d(TAG, "Initializing camera...");

        // Initialize barcode scanner
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39)
                .build();

        barcodeScanner = BarcodeScanning.getClient(options);

        // Start camera
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindCameraUseCases(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Error starting camera", e);
                    Toast.makeText(cameraActivity.this, "Error starting camera: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        // Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image analysis for real-time barcode scanning
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(new Size(640, 480)) // Lower resolution for better performance
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new BarcodeAnalyzer());

        // Camera selector - flexible for different devices
        CameraSelector cameraSelector;

        try {
            // Try back camera first (preferred for barcode scanning)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

            // Check if back camera is available
            if (!cameraProvider.hasCamera(cameraSelector)) {
                Log.w(TAG, "Back camera not available, trying front camera");
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                if (!cameraProvider.hasCamera(cameraSelector)) {
                    Log.w(TAG, "Front camera not available, using any available camera");
                    cameraSelector = new CameraSelector.Builder().build();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error selecting camera, using default", e);
            cameraSelector = new CameraSelector.Builder().build();
        }

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll();

            // Bind use cases to camera
            Camera camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis);

            Log.d(TAG, "Camera started successfully");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(cameraActivity.this, "Camera ready - point at a barcode!", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception exc) {
            Log.e(TAG, "Use case binding failed", exc);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(cameraActivity.this, "Failed to start camera: " + exc.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            if (!isScanning) {
                imageProxy.close();
                return;
            }

            // Rate limiting - don't scan too frequently
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastScanTime < 1000) { // Limit to once per second
                imageProxy.close();
                return;
            }

            InputImage image = InputImage.fromMediaImage(
                    imageProxy.getImage(),
                    imageProxy.getImageInfo().getRotationDegrees()
            );

            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String barcodeValue = barcode.getDisplayValue();
                            if (barcodeValue != null && !barcodeValue.isEmpty()) {

                                // Check cooldown period
                                long currentTime2 = System.currentTimeMillis();
                                if (currentTime2 - lastScanTime < SCAN_COOLDOWN) {
                                    break; // Skip if too soon after last scan
                                }

                                lastScanTime = currentTime2;
                                isScanning = false; // Stop scanning temporarily

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(cameraActivity.this,
                                                "Barcode detected: " + barcodeValue,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Log.d(TAG, "Barcode detected: " + barcodeValue);
                                fetchProductInfo(barcodeValue);
                                break;
                            }
                        }
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Barcode scanning failed", e);
                        imageProxy.close();
                    });
        }
    }

    private void fetchProductInfo(String barcode) {
        Log.d(TAG, "Fetching product info for barcode: " + barcode);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://world.openfoodfacts.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ProductAPI api = retrofit.create(ProductAPI.class);
        Call<ProductResponse> call = api.getProduct(barcode);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful() && response.body() != null) {
                            ProductResponse productResponse = response.body();

                            if (productResponse.status == 1 && productResponse.product != null) {
                                // Check if we have meaningful ingredient data
                                boolean hasIngredients = productResponse.product.ingredients_text != null &&
                                        !productResponse.product.ingredients_text.trim().isEmpty();
                                boolean hasProductName = productResponse.product.product_name != null &&
                                        !productResponse.product.product_name.trim().isEmpty();

                                if (hasIngredients || hasProductName) {
                                    // Product found with some useful data - navigate normally
                                    navigateToProductDetail(barcode, productResponse.product, false);
                                } else {
                                    // Product exists but no useful data - trigger camera fallback
                                    Log.d(TAG, "Product found but no ingredients/name available");
                                    navigateToProductDetail(barcode, productResponse.product, true);
                                }
                            } else {
                                // Product not found in database - trigger camera fallback
                                Log.d(TAG, "Product not found in database, triggering camera fallback");
                                Product emptyProduct = new Product();
                                emptyProduct.product_name = "Unknown Product";
                                navigateToProductDetail(barcode, emptyProduct, true);
                            }
                        } else {
                            // API returned error response - trigger camera fallback
                            Log.d(TAG, "API error response: " + response.code() + ", triggering camera fallback");
                            Product emptyProduct = new Product();
                            emptyProduct.product_name = "Unknown Product";
                            navigateToProductDetail(barcode, emptyProduct, true);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage(), t);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Network error - trigger camera fallback
                        Log.d(TAG, "Network error, triggering camera fallback");
                        Product emptyProduct = new Product();
                        emptyProduct.product_name = "Unknown Product";
                        navigateToProductDetail(barcode, emptyProduct, true);
                    }
                });
            }
        });
    }

    private void resumeScanning() {
        // Resume scanning after a delay
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isScanning = true;
            }
        }, 2000);
    }

    private void navigateToProductDetail(String barcode, Product product, boolean apiFailure) {
        Intent intent = new Intent(this, iteminfoscreen.class);
        intent.putExtra("barcode", barcode);
        intent.putExtra("product_name", product.product_name);
        intent.putExtra("ingredients", product.ingredients_text);
        intent.putExtra("brands", product.brands);
        intent.putExtra("image_url", product.image_url);
        intent.putExtra("api_failure", apiFailure); // KEY: This flag triggers camera fallback

        Log.d(TAG, "Navigating to product detail - API failure: " + apiFailure);
        Log.d(TAG, "Product name: " + product.product_name);
        Log.d(TAG, "Ingredients: " + product.ingredients_text);

        startActivity(intent);
        finish(); // Close camera activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isScanning = false;
    }
}