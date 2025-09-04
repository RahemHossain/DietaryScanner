package com.example.dietaryscanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {

    private RecyclerView recyclerViewSavedItems;
    private LinearLayout emptyStateLayout;
    private SavedItemsAdapter adapter;
    private List<SavedItem> savedItems;
    private SQLiteSavedItemsManager savedItemsManager;

    public SavedFragment() {
        // Required empty public constructor
    }

    public static SavedFragment newInstance() {
        return new SavedFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("SavedFragment", "onViewCreated called");

        initViews(view);
        initSavedItemsManager();
        loadSavedItems();
        setupRecyclerView();
        updateUI();
    }

    private void initViews(View view) {
        recyclerViewSavedItems = view.findViewById(R.id.recyclerViewSavedItems);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        Log.d("SavedFragment", "Views initialized");
    }

    private void initSavedItemsManager() {
        if (getContext() != null) {
            savedItemsManager = new SQLiteSavedItemsManager(getContext());
            Log.d("SavedFragment", "SavedItemsManager initialized");
        } else {
            Log.e("SavedFragment", "Context is null, cannot initialize SavedItemsManager");
        }
    }

    private void loadSavedItems() {
        if (savedItemsManager != null) {
            savedItems = savedItemsManager.getSavedItems();
            Log.d("SavedFragment", "Loaded " + savedItems.size() + " saved items");
        } else {
            savedItems = new ArrayList<>();
            Log.e("SavedFragment", "SavedItemsManager is null, using empty list");
        }

        if (savedItems == null) {
            savedItems = new ArrayList<>();
        }
    }

    private void setupRecyclerView() {
        if (getContext() == null) {
            Log.e("SavedFragment", "Context is null, cannot setup RecyclerView");
            return;
        }

        adapter = new SavedItemsAdapter(getContext(), savedItems);

        // Use LinearLayoutManager for a simple list view
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerViewSavedItems.setLayoutManager(gridLayoutManager);
        recyclerViewSavedItems.setAdapter(adapter);

        // Set click listeners
        adapter.setOnItemClickListener(item -> {
            Log.d("SavedFragment", "Item clicked: " + item.getName());
            // Launch item info screen to view details
            Intent intent = new Intent(getContext(), iteminfoscreen.class);
            intent.putExtra("barcode", item.getId());
            intent.putExtra("product_name", item.getName());
            intent.putExtra("ingredients", item.getIngredients());
            intent.putExtra("brands", item.getAllergyInfo()); // Using allergyInfo field for brands
            intent.putExtra("image_url", item.getImagePath());
            intent.putExtra("api_failure", false); // We have the data already
            startActivity(intent);
        });

        adapter.setOnItemRemoveListener((item, position) -> {
            Log.d("SavedFragment", "Remove clicked for: " + item.getName());
            removeSavedItem(item, position);
        });

        Log.d("SavedFragment", "RecyclerView setup complete");
    }

    private void updateUI() {
        if (savedItems == null || savedItems.isEmpty()) {
            recyclerViewSavedItems.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            Log.d("SavedFragment", "Showing empty state");
        } else {
            recyclerViewSavedItems.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            Log.d("SavedFragment", "Showing " + savedItems.size() + " saved items");
        }
    }

    private void removeSavedItem(SavedItem item, int position) {
        if (savedItemsManager == null) {
            Log.e("SavedFragment", "SavedItemsManager is null, cannot remove item");
            Toast.makeText(getContext(), "Error: Cannot remove item", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use the SQLite system to remove item
        boolean success = savedItemsManager.removeItem(item.getId());

        if (success) {
            // Update the local list and notify adapter
            if (position >= 0 && position < savedItems.size()) {
                savedItems.remove(position);
                adapter.notifyItemRemoved(position);
                updateUI();
                Toast.makeText(getContext(), item.getName() + " removed from saved items", Toast.LENGTH_SHORT).show();
                Log.d("SavedFragment", "Item removed successfully: " + item.getName());
            }
        } else {
            Toast.makeText(getContext(), "Failed to remove item", Toast.LENGTH_SHORT).show();
            Log.e("SavedFragment", "Failed to remove item: " + item.getName());
        }
    }

    public void refreshSavedItems() {
        Log.d("SavedFragment", "Refreshing saved items");
        loadSavedItems();
        if (adapter != null) {
            adapter.updateItems(savedItems);
        }
        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("SavedFragment", "onResume called - refreshing saved items");
        refreshSavedItems();
    }
}