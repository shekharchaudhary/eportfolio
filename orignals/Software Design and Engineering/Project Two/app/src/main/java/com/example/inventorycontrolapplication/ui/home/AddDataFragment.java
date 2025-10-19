package com.example.inventorycontrolapplication.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.inventorycontrolapplication.R;
import com.example.inventorycontrolapplication.data.Result;
import com.example.inventorycontrolapplication.data.repository.InventoryRepository;
import com.example.inventorycontrolapplication.utils.AppLogger;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment for adding new inventory items.
 * Enhanced with category input, validation, and better UX.
 */
public class AddDataFragment extends Fragment {

    private static final String TAG = "AddDataFragment";

    private TextInputEditText nameInput;
    private TextInputEditText categoryInput;
    private TextInputEditText typeInput;
    private TextInputEditText countInput;

    private TextInputLayout nameLayout;
    private TextInputLayout categoryLayout;
    private TextInputLayout typeLayout;
    private TextInputLayout countLayout;

    private MaterialButton saveButton;
    private MaterialButton cancelButton;
    private CircularProgressIndicator loadingIndicator;

    private InventoryRepository repository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppLogger.logMethodEntry(TAG, "onCreateView");

        View root = inflater.inflate(R.layout.fragment_add_data, container, false);

        // Initialize repository
        repository = new InventoryRepository(requireContext());

        // Initialize views
        initializeViews(root);

        // Set up button listeners
        setupListeners(root);

        AppLogger.logMethodExit(TAG, "onCreateView");
        return root;
    }

    /**
     * Initialize all views
     */
    private void initializeViews(View root) {
        // Input fields
        nameInput = root.findViewById(R.id.name);
        categoryInput = root.findViewById(R.id.category);
        typeInput = root.findViewById(R.id.type);
        countInput = root.findViewById(R.id.count);

        // Input layouts
        nameLayout = root.findViewById(R.id.name_layout);
        categoryLayout = root.findViewById(R.id.category_layout);
        typeLayout = root.findViewById(R.id.type_layout);
        countLayout = root.findViewById(R.id.count_layout);

        // Buttons
        saveButton = root.findViewById(R.id.save_button);
        cancelButton = root.findViewById(R.id.cancel_button);

        // Progress indicator
        loadingIndicator = root.findViewById(R.id.loading_indicator);
    }

    /**
     * Set up button click listeners
     */
    private void setupListeners(View root) {
        // Save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInventoryItem(root);
            }
        });

        // Cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppLogger.d(TAG, "Cancel button clicked");
                Navigation.findNavController(v).navigateUp();
            }
        });
    }

    /**
     * Validate and save inventory item
     */
    private void saveInventoryItem(View root) {
        AppLogger.logMethodEntry(TAG, "saveInventoryItem");

        // Clear previous errors
        clearErrors();

        // Get input values
        String name = getInputText(nameInput);
        String category = getInputText(categoryInput);
        String type = getInputText(typeInput);
        String count = getInputText(countInput);

        // Validate inputs
        if (!validateInputs(name, category, type, count)) {
            AppLogger.w(TAG, "Validation failed");
            return;
        }

        // Show loading indicator
        showLoading(true);

        // Use category as the type field (for backward compatibility)
        // If subcategory is provided, combine them
        String finalType = category;
        if (type != null && !type.isEmpty()) {
            finalType = category + " - " + type;
        }

        // Save to repository
        Result<Long> result = repository.addItem(name, finalType, count);

        // Hide loading
        showLoading(false);

        if (result instanceof Result.Success) {
            Long itemId = ((Result.Success<Long>) result).getData();
            AppLogger.i(TAG, "Successfully saved item with ID: " + itemId);

            // Show success message
            Toast.makeText(getContext(),
                    "Item added successfully!",
                    Toast.LENGTH_SHORT).show();

            // Navigate back
            Navigation.findNavController(root).navigateUp();

        } else if (result instanceof Result.Error) {
            Exception error = ((Result.Error) result).getError();
            AppLogger.e(TAG, "Failed to save item", error);

            // Show error message
            String errorMsg = error.getMessage() != null ?
                    error.getMessage() : "Failed to add item";
            Toast.makeText(getContext(),
                    errorMsg,
                    Toast.LENGTH_LONG).show();
        }

        AppLogger.logMethodExit(TAG, "saveInventoryItem");
    }

    /**
     * Validate all inputs
     */
    private boolean validateInputs(String name, String category, String type, String count) {
        boolean isValid = true;

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            nameLayout.setError("Product name is required");
            isValid = false;
        }

        // Validate category
        if (category == null || category.trim().isEmpty()) {
            categoryLayout.setError("Category is required");
            isValid = false;
        }

        // Type is optional, no validation needed

        // Validate count
        if (count == null || count.trim().isEmpty()) {
            countLayout.setError("Quantity is required");
            isValid = false;
        } else {
            try {
                int countValue = Integer.parseInt(count);
                if (countValue < 0) {
                    countLayout.setError("Quantity must be positive");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                countLayout.setError("Invalid quantity");
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * Clear all error messages
     */
    private void clearErrors() {
        if (nameLayout != null) nameLayout.setError(null);
        if (categoryLayout != null) categoryLayout.setError(null);
        if (typeLayout != null) typeLayout.setError(null);
        if (countLayout != null) countLayout.setError(null);
    }

    /**
     * Get text from input field
     */
    private String getInputText(TextInputEditText input) {
        if (input == null || input.getText() == null) {
            return null;
        }
        return input.getText().toString().trim();
    }

    /**
     * Show or hide loading indicator
     */
    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (saveButton != null) {
            saveButton.setEnabled(!show);
        }
        if (cancelButton != null) {
            cancelButton.setEnabled(!show);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AppLogger.d(TAG, "onDestroyView");

        // Clean up references
        nameInput = null;
        categoryInput = null;
        typeInput = null;
        countInput = null;
        nameLayout = null;
        categoryLayout = null;
        typeLayout = null;
        countLayout = null;
        saveButton = null;
        cancelButton = null;
        loadingIndicator = null;
        repository = null;
    }
}
