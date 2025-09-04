package com.example.dietaryscanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductViewHolder> {

    private List<MockProductDatabase.MockProduct> products;
    private Context context;
    private OnProductClickListener onProductClickListener;

    public interface OnProductClickListener {
        void onProductClick(MockProductDatabase.MockProduct product);
    }

    public ProductsAdapter(Context context, List<MockProductDatabase.MockProduct> products) {
        this.context = context;
        this.products = products;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        MockProductDatabase.MockProduct product = products.get(position);

        holder.productName.setText(product.getName());
        holder.productBrand.setText(product.getBrand());

        // Get user preferences to show compatibility
        SharedPreferences prefs = context.getSharedPreferences("dietary_preferences", Context.MODE_PRIVATE);
        Set<String> preferences = prefs.getStringSet("selected_preferences", new HashSet<>());

        // Show compatibility status
        if (!preferences.isEmpty()) {
            boolean isCompatible = true;
            StringBuilder incompatibleWith = new StringBuilder();

            for (String preference : preferences) {
                if (!product.isCompatibleWith(preference)) {
                    isCompatible = false;
                    if (incompatibleWith.length() > 0) {
                        incompatibleWith.append(", ");
                    }
                    incompatibleWith.append(preference);
                }
            }

            if (isCompatible) {
                holder.compatibilityIndicator.setVisibility(View.VISIBLE);
                holder.compatibilityIndicator.setText("✓");
                holder.compatibilityIndicator.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
            } else {
                holder.compatibilityIndicator.setVisibility(View.VISIBLE);
                holder.compatibilityIndicator.setText("⚠");
                holder.compatibilityIndicator.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.background_light));
            }
        } else {
            holder.compatibilityIndicator.setVisibility(View.GONE);
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
        }

        // Set default product image based on product type
        setProductImage(holder.productImage, product.getName());

        holder.itemView.setOnClickListener(v -> {
            if (onProductClickListener != null) {
                onProductClickListener.onProductClick(product);
            }
        });
    }

    private void setProductImage(ImageView imageView, String productName) {
        // Set default images based on product name (you can replace with actual images)
        String lowerName = productName.toLowerCase();

        if (lowerName.contains("coca-cola") || lowerName.contains("coke")) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery); // Replace with actual image
        } else if (lowerName.contains("pepsi")) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery); // Replace with actual image
        } else if (lowerName.contains("kit kat")) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery); // Replace with actual image
        } else if (lowerName.contains("oreo")) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery); // Replace with actual image
        } else if (lowerName.contains("lay's")) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery); // Replace with actual image
        } else if (lowerName.contains("snickers")) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery); // Replace with actual image
        } else if (lowerName.contains("sprite")) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery); // Replace with actual image
        } else if (lowerName.contains("cheerios")) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery); // Replace with actual image
        } else if (lowerName.contains("doritos")) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery); // Replace with actual image
        } else if (lowerName.contains("red bull")) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery); // Replace with actual image
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_camera); // Default image
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<MockProductDatabase.MockProduct> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.onProductClickListener = listener;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView productImage;
        TextView productName;
        TextView productBrand;
        TextView compatibilityIndicator;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productBrand = itemView.findViewById(R.id.productBrand);
            compatibilityIndicator = itemView.findViewById(R.id.compatibilityIndicator);
        }
    }
}