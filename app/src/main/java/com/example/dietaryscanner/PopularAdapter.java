package com.example.dietaryscanner;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.ViewHolder> {

    private final List<MockProductDatabase.MockProduct> products;
    private final Context context;
    private final Map<String, Integer> imageMap;

    public PopularAdapter(Context context, List<MockProductDatabase.MockProduct> products, Map<String, Integer> imageMap) {
        this.context = context;
        this.products = products;
        this.imageMap = imageMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_popular, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MockProductDatabase.MockProduct product = products.get(position);

        holder.name.setText(product.getName());

        // Show product image if available
        if (imageMap.containsKey(product.getBarcode())) {
            holder.image.setImageResource(imageMap.get(product.getBarcode()));
        } else {
            holder.image.setImageResource(R.drawable.chips_default); // fallback
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, iteminfoscreen.class);
            intent.putExtra("barcode", product.getBarcode());
            intent.putExtra("product_name", product.getName());
            intent.putExtra("ingredients", product.getIngredients());
            intent.putExtra("brands", product.getBrand());
            intent.putExtra("api_failure", false);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.popularImage);
            name = itemView.findViewById(R.id.popularName);
        }
    }
}
