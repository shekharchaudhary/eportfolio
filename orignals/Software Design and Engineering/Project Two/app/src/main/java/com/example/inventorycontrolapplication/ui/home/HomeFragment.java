package com.example.inventorycontrolapplication.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorycontrolapplication.R;
import com.example.inventorycontrolapplication.data.RecycleDataAdapter;
import com.example.inventorycontrolapplication.utils.AppLogger;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

/**
 * Fragment displaying the inventory list with empty state handling.
 * Enhanced with modern UI, empty state view, and better user experience.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private RecycleDataAdapter adapter;
    private LinearLayout emptyStateView;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AppLogger.logMethodEntry(TAG, "onCreateView");

        // Initialize ViewModel with dependency injection
        com.example.inventorycontrolapplication.data.repository.InventoryRepository repository =
                new com.example.inventorycontrolapplication.data.repository.InventoryRepository(getContext());
        HomeViewModelFactory factory = new HomeViewModelFactory(repository);
        homeViewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        recyclerView = root.findViewById(R.id.data);
        emptyStateView = root.findViewById(R.id.empty_state);
        FloatingActionButton fab = root.findViewById(R.id.floating_add_data);
        MaterialButton emptyStateButton = root.findViewById(R.id.empty_state_add_button);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // FAB click listener
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppLogger.d(TAG, "FAB clicked - navigating to add data");
                Navigation.findNavController(view).navigate(R.id.add_data);
            }
        });

        // Empty state button click listener
        if (emptyStateButton != null) {
            emptyStateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppLogger.d(TAG, "Empty state button clicked - navigating to add data");
                    Navigation.findNavController(view).navigate(R.id.add_data);
                }
            });
        }

        // Load inventory data
        loadInventoryData(root);

        AppLogger.logMethodExit(TAG, "onCreateView");
        return root;
    }

    /**
     * Load inventory data and update UI accordingly
     */
    private void loadInventoryData(View root) {
        AppLogger.logMethodEntry(TAG, "loadInventoryData");

        try {
            // Use deprecated method for backward compatibility
            ArrayList items = homeViewModel.GetRecords();

            if (items == null || items.isEmpty()) {
                // Show empty state
                showEmptyState();
                AppLogger.i(TAG, "No inventory items found - showing empty state");
            } else {
                // Show data
                showInventoryList(items);
                AppLogger.i(TAG, "Loaded " + items.size() + " inventory items");
            }

        } catch (Exception e) {
            AppLogger.e(TAG, "Error loading inventory data", e);
            Snackbar.make(root, "Failed to load inventory data: " + e.getMessage(), Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED)
                    .show();
            // Show empty state as fallback
            showEmptyState();
        }

        AppLogger.logMethodExit(TAG, "loadInventoryData");
    }

    /**
     * Show the empty state view
     */
    private void showEmptyState() {
        AppLogger.d(TAG, "Displaying empty state view");
        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.VISIBLE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * Show the inventory list with data
     */
    private void showInventoryList(ArrayList items) {
        AppLogger.d(TAG, "Displaying inventory list with " + items.size() + " items");

        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }

        // Set adapter with data
        adapter = new RecycleDataAdapter(getContext(), items);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        AppLogger.d(TAG, "onResume - reloading inventory data");

        // Reload data when fragment resumes (e.g., after adding new item)
        View root = getView();
        if (root != null) {
            loadInventoryData(root);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AppLogger.d(TAG, "onDestroyView");
        recyclerView = null;
        emptyStateView = null;
        adapter = null;
    }
}
