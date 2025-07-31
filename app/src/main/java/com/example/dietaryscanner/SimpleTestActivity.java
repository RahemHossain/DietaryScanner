package com.example.dietaryscanner;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class SimpleTestActivity extends AppCompatActivity {

    private static final String TAG = "SimpleTest";
    private TextView resultText;

    // Simple API interface
    interface TestAPI {
        @GET("api/v2/product/{barcode}.json")
        Call<TestResponse> getProduct(@Path("barcode") String barcode);
    }

    // Simple response class
    public static class TestResponse {
        public TestProduct product;
        public int status;
        public String status_verbose;
    }

    public static class TestProduct {
        public String product_name;
        public String ingredients_text;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_test);

        resultText = findViewById(R.id.resultText);
        Button testButton = findViewById(R.id.testButton);

        testButton.setOnClickListener(v -> testAPI());
    }

    private void testAPI() {
        Log.d(TAG, "Starting API test...");
        Toast.makeText(this, "Testing API...", Toast.LENGTH_SHORT).show();

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://world.openfoodfacts.org/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            TestAPI api = retrofit.create(TestAPI.class);
            Call<TestResponse> call = api.getProduct("5449000000996");

            Log.d(TAG, "API URL: " + call.request().url().toString());

            call.enqueue(new retrofit2.Callback<TestResponse>() {
                @Override
                public void onResponse(Call<TestResponse> call, Response<TestResponse> response) {
                    Log.d(TAG, "Response received. Code: " + response.code());

                    runOnUiThread(() -> {
                        if (response.isSuccessful() && response.body() != null) {
                            TestResponse testResponse = response.body();

                            String result = "✅ SUCCESS!\n\n";
                            result += "Status: " + testResponse.status + "\n";
                            result += "Status Verbose: " + testResponse.status_verbose + "\n\n";

                            if (testResponse.product != null) {
                                result += "Product Name: " + testResponse.product.product_name + "\n\n";
                                result += "Ingredients: " + testResponse.product.ingredients_text;
                            }

                            resultText.setText(result);
                            Toast.makeText(SimpleTestActivity.this, "API SUCCESS!", Toast.LENGTH_LONG).show();

                        } else {
                            String error = "❌ FAILED\nResponse code: " + response.code();
                            resultText.setText(error);
                            Toast.makeText(SimpleTestActivity.this, "API Failed: " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onFailure(Call<TestResponse> call, Throwable t) {
                    Log.e(TAG, "API call failed", t);
                    runOnUiThread(() -> {
                        String error = "❌ NETWORK ERROR\n" + t.getMessage();
                        resultText.setText(error);
                        Toast.makeText(SimpleTestActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception in testAPI", e);
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}