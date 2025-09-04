package com.example.dietaryscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SavedItemsAdapter extends RecyclerView.Adapter<SavedItemsAdapter.ViewHolder> {

    private List<SavedItem> savedItems;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private OnItemRemoveListener onItemRemoveListener;

    // Interface for item click events
    public interface OnItemClickListener {
        void onItemClick(SavedItem item);
    }

    // Interface for item remove events
    public interface OnItemRemoveListener {
        void onItemRemove(SavedItem item, int position);
    }

    public SavedItemsAdapter(Context context, List<SavedItem> savedItems) {
        this.context = context;
        this.savedItems = savedItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saved_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedItem item = savedItems.get(position);

        holder.textViewFoodName.setText(item.getName());
        holder.textViewFoodDescription.setText(item.getDescription());

        // Format the saved date
        if (item.getSavedDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.textViewSavedDate.setText("Saved on: " + dateFormat.format(item.getSavedDate()));
        }

        // Load image if available
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            // Here you would load the actual image using Glide or Picasso
            // For now, we'll use a placeholder
            holder.imageViewFood.setImageResource(R.drawable.ic_launcher_background);
        } else {
            holder.imageViewFood.setImageResource(android.R.drawable.ic_menu_camera);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });

        holder.buttonRemove.setOnClickListener(v -> {
            if (onItemRemoveListener != null) {
                onItemRemoveListener.onItemRemove(item, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return savedItems.size();
    }

    public void updateItems(List<SavedItem> newItems) {
        this.savedItems = newItems;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < savedItems.size()) {
            savedItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemRemoveListener(OnItemRemoveListener listener) {
        this.onItemRemoveListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewFood;
        TextView textViewFoodName;
        TextView textViewFoodDescription;
        TextView textViewSavedDate;
        ImageButton buttonRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewFood = itemView.findViewById(R.id.imageViewFood);
            textViewFoodName = itemView.findViewById(R.id.textViewFoodName);
            textViewFoodDescription = itemView.findViewById(R.id.textViewFoodDescription);
            textViewSavedDate = itemView.findViewById(R.id.textViewSavedDate);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }
    }
}