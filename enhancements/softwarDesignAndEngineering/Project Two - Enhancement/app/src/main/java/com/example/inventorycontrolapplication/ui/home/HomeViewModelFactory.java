package com.example.inventorycontrolapplication.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.inventorycontrolapplication.data.repository.IInventoryRepository;

/**
 * Factory for creating HomeViewModel with dependencies.
 * Follows Dependency Inversion Principle - allows injection of repository implementation.
 * Ensures proper dependency injection for ViewModel construction.
 */
public class HomeViewModelFactory implements ViewModelProvider.Factory {

    private final IInventoryRepository repository;

    /**
     * Constructor accepting repository dependency
     * @param repository Implementation of IInventoryRepository
     */
    public HomeViewModelFactory(@NonNull IInventoryRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
