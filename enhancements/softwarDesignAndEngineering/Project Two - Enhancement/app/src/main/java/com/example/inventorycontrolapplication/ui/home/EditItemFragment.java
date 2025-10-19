package com.example.inventorycontrolapplication.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.inventorycontrolapplication.R;
import com.example.inventorycontrolapplication.data.InventoryDataSource;
import com.example.inventorycontrolapplication.utils.AppLogger;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment for editing full item details (name, category, quantity)
 */
public class EditItemFragment extends Fragment {

    private static final String TAG = "EditItemFragment";
    private InventoryDataSource dataSource;

    // UI Components
    private TextInputEditText nameInput, categoryInput, typeInput, countInput;
    private TextInputLayout nameLayout, categoryLayout, typeLayout, countLayout;
    private MaterialButton saveButton, cancelButton;

    // Item data
    private String itemId, itemName, itemType, itemCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        AppLogger.logMethodEntry(TAG, "onCreateView");

        // Initialize data source
        dataSource = new InventoryDataSource(getContext());

        // Inflate layout
        View root = inflater.inflate(R.layout.fragment_edit_item, container, false);

        // Get arguments
        if (getArguments() != null) {
            itemId = EditItemFragmentArgs.fromBundle(getArguments()).getItemId();
            itemName = EditItemFragmentArgs.fromBundle(getArguments()).getItemName();
            itemType = EditItemFragmentArgs.fromBundle(getArguments()).getItemType();
            itemCount = EditItemFragmentArgs.fromBundle(getArguments()).getItemCount();
        }

        // Initialize views
        initializeViews(root);

        // Populate fields with current data
        populateFields();

        // Set up button listeners
        setupButtonListeners(root);

        AppLogger.logMethodExit(TAG, "onCreateView");
        return root;
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews(View root) {
        nameInput = root.findViewById(R.id.name);
        categoryInput = root.findViewById(R.id.category);
        typeInput = root.findViewById(R.id.type);
        countInput = root.findViewById(R.id.count);

        nameLayout = root.findViewById(R.id.name_layout);
        categoryLayout = root.findViewById(R.id.category_layout);
        typeLayout = root.findViewById(R.id.type_layout);
        countLayout = root.findViewById(R.id.count_layout);

        saveButton = root.findViewById(R.id.save_button);
        cancelButton = root.findViewById(R.id.cancel_button);
    }

    /**
     * Populate form fields with existing item data
     */
    private void populateFields() {
        AppLogger.d(TAG, "Populating fields with existing data");

        nameInput.setText(itemName);
        countInput.setText(itemCount);

        // Parse category and subcategory from itemType
        // Format is "Category - Subcategory" or just "Category"
        if (itemType != null && itemType.contains(" - ")) {
            String[] parts = itemType.split(" - ", 2);
            categoryInput.setText(parts[0]);
            typeInput.setText(parts[1]);
        } else {
            categoryInput.setText(itemType);
        }
    }

    /**
     * Setup button click listeners
     */
    private void setupButtonListeners(View root) {
        // Cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppLogger.d(TAG, "Cancel button clicked");
                getActivity().onBackPressed();
            }
        });

        // Save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem(root);
            }
        });
    }

    /**
     * Validate and save the edited item
     */
    private void saveItem(View root) {
        AppLogger.logMethodEntry(TAG, "saveItem");

        // Get input values
        String name = getInputText(nameInput);
        String category = getInputText(categoryInput);
        String type = getInputText(typeInput);
        String count = getInputText(countInput);

        // Validate inputs
        if (!validateInputs(name, category, count)) {
            return;
        }

        // Combine category and subcategory
        String finalType = category;
        if (type != null && !type.trim().isEmpty()) {
            finalType = category + " - " + type;
        }

        try {
            // Update item in database
            dataSource.updateInventoryItem(itemId, name, finalType, count);

            AppLogger.i(TAG, "Item updated successfully - ID: " + itemId);

            // Show success message
            Snackbar.make(root, R.string.success_item_updated, Snackbar.LENGTH_LONG).show();

            // Navigate back
            getActivity().onBackPressed();

        } catch (Exception e) {
            AppLogger.e(TAG, "Error updating item", e);
            Snackbar.make(root, R.string.error_update_item, Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED)
                    .show();
        }

        AppLogger.logMethodExit(TAG, "saveItem");
    }

    /**
     * Validate form inputs
     */
    private boolean validateInputs(String name, String category, String count) {
        boolean isValid = true;

        // Clear previous errors
        nameLayout.setError(null);
        categoryLayout.setError(null);
        countLayout.setError(null);

        // Validate product name
        if (name == null || name.trim().isEmpty()) {
            nameLayout.setError(getString(R.string.error_product_name));
            isValid = false;
        } else if (name.trim().length() < 4) {
            nameLayout.setError(getString(R.string.invalid_productName));
            isValid = false;
        }

        // Validate category
        if (category == null || category.trim().isEmpty()) {
            categoryLayout.setError(getString(R.string.error_category));
            isValid = false;
        } else if (category.trim().length() < 4) {
            categoryLayout.setError(getString(R.string.invalid_productType));
            isValid = false;
        }

        // Validate quantity
        if (count == null || count.trim().isEmpty()) {
            countLayout.setError(getString(R.string.error_quantity));
            isValid = false;
        } else {
            try {
                int quantity = Integer.parseInt(count);
                if (quantity <= 0) {
                    countLayout.setError(getString(R.string.error_quantity_positive));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                countLayout.setError(getString(R.string.invalid_productCount));
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * Helper to safely get text from EditText
     */
    private String getInputText(TextInputEditText input) {
        if (input != null && input.getText() != null) {
            return input.getText().toString().trim();
        }
        return "";
    }
}
