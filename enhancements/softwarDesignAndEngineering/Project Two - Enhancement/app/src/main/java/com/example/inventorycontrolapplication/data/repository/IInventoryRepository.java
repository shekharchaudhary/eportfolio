package com.example.inventorycontrolapplication.data.repository;

import com.example.inventorycontrolapplication.data.Result;

import java.util.HashMap;
import java.util.List;

/**
 * Interface for inventory repository operations.
 * Follows Interface Segregation Principle - focused on inventory-specific operations.
 * Follows Dependency Inversion Principle - abstracts data access implementation.
 */
public interface IInventoryRepository {

    /**
     * Get all inventory items
     * @return Result containing list of inventory items or Error on failure
     */
    Result<List<HashMap>> getAllItems();

    /**
     * Get a specific inventory item by ID
     * @param itemId The item ID
     * @return Result containing item data or Error on failure
     */
    Result<HashMap> getItemById(String itemId);

    /**
     * Add new inventory item
     * @param productName Name of the product
     * @param productType Type/category of the product
     * @param productCount Initial count
     * @return Result containing item ID on success or Error on failure
     */
    Result<Long> addItem(String productName, String productType, String productCount);

    /**
     * Update inventory item count
     * @param itemId ID of item to update
     * @param newCount New count value
     * @return Result containing success status or Error on failure
     */
    Result<Boolean> updateItemCount(String itemId, String newCount);

    /**
     * Delete inventory item
     * @param itemId ID of item to delete
     * @return Result containing success status or Error on failure
     */
    Result<Boolean> deleteItem(String itemId);

    /**
     * Delete all inventory items
     * @return Result containing success status or Error on failure
     */
    Result<Boolean> deleteAllItems();
}
