package com.example.inventorycontrolapplication.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.inventorycontrolapplication.data.Result;
import com.example.inventorycontrolapplication.data.repository.IInventoryRepository;
import com.example.inventorycontrolapplication.utils.AppLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ViewModel for the Home screen displaying inventory items.
 * Follows SOLID principles:
 * - Dependency Inversion: Depends on IInventoryRepository interface, not concrete implementation
 * - Single Responsibility: Manages UI state for home screen
 * Implements structured logging and error handling.
 */
public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel";
    private final IInventoryRepository repository;
    private final MutableLiveData<List<HashMap>> inventoryItems = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    /**
     * Constructor with dependency injection
     * @param repository Inventory repository implementation
     */
    public HomeViewModel(@NonNull IInventoryRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }
        this.repository = repository;
        AppLogger.d(TAG, "HomeViewModel initialized with repository");
    }

    /**
     * Get LiveData for inventory items
     */
    public LiveData<List<HashMap>> getInventoryItems() {
        return inventoryItems;
    }

    /**
     * Get LiveData for error messages
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get LiveData for loading state
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Load all inventory records from repository
     */
    public void loadInventoryItems() {
        AppLogger.logMethodEntry(TAG, "loadInventoryItems");
        isLoading.setValue(true);

        try {
            Result<List<HashMap>> result = repository.getAllItems();

            if (result instanceof Result.Success) {
                List<HashMap> items = ((Result.Success<List<HashMap>>) result).getData();
                inventoryItems.setValue(items);
                errorMessage.setValue(null);
                AppLogger.i(TAG, "Successfully loaded " + items.size() + " inventory items");
            } else if (result instanceof Result.Error) {
                Exception error = ((Result.Error) result).getError();
                String message = "Failed to load inventory items";
                errorMessage.setValue(message);
                inventoryItems.setValue(new ArrayList<>());
                AppLogger.e(TAG, message, error);
            }

        } catch (Exception e) {
            AppLogger.e(TAG, "Unexpected error loading inventory items", e);
            errorMessage.setValue("An unexpected error occurred");
            inventoryItems.setValue(new ArrayList<>());
        } finally {
            isLoading.setValue(false);
            AppLogger.logMethodExit(TAG, "loadInventoryItems");
        }
    }

    /**
     * Legacy method for backward compatibility
     * @deprecated Use loadInventoryItems() and observe LiveData instead
     */
    @Deprecated
    public ArrayList<HashMap> GetRecords() throws Exception {
        AppLogger.w(TAG, "Using deprecated GetRecords method");
        Result<List<HashMap>> result = repository.getAllItems();

        if (result instanceof Result.Success) {
            List<HashMap> items = ((Result.Success<List<HashMap>>) result).getData();
            return items instanceof ArrayList ? (ArrayList<HashMap>) items : new ArrayList<>(items);
        } else if (result instanceof Result.Error) {
            Exception error = ((Result.Error) result).getError();
            AppLogger.e(TAG, "Error getting records", error);
            throw error;
        }

        throw new Exception("Unknown result type");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        AppLogger.d(TAG, "HomeViewModel cleared");
    }
}