package com.example.inventorycontrolapplication;

import android.content.Context;

import com.example.inventorycontrolapplication.data.Result;
import com.example.inventorycontrolapplication.data.repository.InventoryRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for InventoryRepository.
 * Tests CRUD operations, error handling, and data validation.
 * Uses Robolectric for Android context dependencies.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class InventoryRepositoryTest {

    private InventoryRepository repository;
    private Context context;

    public void setUp() {
        context = RuntimeEnvironment.application;
        context = RuntimeEnvironment.application;
        repository = new InventoryRepository(context);

        // Clean up any existing test data
        repository.deleteAllItems();
    }

    @Test
    public void testConstructor_WithNullContext_ThrowsException() {
        try {
            new InventoryRepository(null);
            fail("Should throw IllegalArgumentException for null context");
        } catch (IllegalArgumentException e) {
            assertEquals("Context cannot be null", e.getMessage());
        }
    }

    @Test
    public void testGetAllItems_EmptyDatabase_ReturnsEmptyList() {
        Result<List<HashMap>> result = repository.getAllItems();

        assertTrue("Result should be Success", result instanceof Result.Success);
        List<HashMap> items = ((Result.Success<List<HashMap>>) result).getData();
        assertNotNull("Items list should not be null", items);
        assertEquals("Items list should be empty", 0, items.size());
    }

    @Test
    public void testAddItem_ValidData_ReturnsSuccessWithId() {
        Result<Long> result = repository.addItem("Test Item", "Electronics", "10");

        assertTrue("Result should be Success", result instanceof Result.Success);
        Long itemId = ((Result.Success<Long>) result).getData();
        assertNotNull("Item ID should not be null", itemId);
        assertTrue("Item ID should be positive", itemId > 0);
    }

    @Test
    public void testAddItem_NullName_ReturnsError() {
        Result<Long> result = repository.addItem(null, "Electronics", "10");

        assertTrue("Result should be Error", result instanceof Result.Error);
        Exception error = ((Result.Error) result).getError();
        assertTrue("Error should be IllegalArgumentException",
                error instanceof IllegalArgumentException);
    }

    @Test
    public void testAddItem_EmptyName_ReturnsError() {
        Result<Long> result = repository.addItem("", "Electronics", "10");

        assertTrue("Result should be Error", result instanceof Result.Error);
    }

    @Test
    public void testAddItem_NullType_ReturnsError() {
        Result<Long> result = repository.addItem("Test Item", null, "10");

        assertTrue("Result should be Error", result instanceof Result.Error);
    }

    @Test
    public void testAddItem_NullCount_ReturnsError() {
        Result<Long> result = repository.addItem("Test Item", "Electronics", null);

        assertTrue("Result should be Error", result instanceof Result.Error);
    }

    @Test
    public void testAddItem_WhitespaceOnlyName_ReturnsError() {
        Result<Long> result = repository.addItem("   ", "Electronics", "10");

        assertTrue("Result should be Error", result instanceof Result.Error);
    }

    @Test
    public void testGetAllItems_AfterAddingItem_ReturnsItem() {
        // Add an item
        repository.addItem("Test Item", "Electronics", "10");

        // Retrieve all items
        Result<List<HashMap>> result = repository.getAllItems();

        assertTrue("Result should be Success", result instanceof Result.Success);
        List<HashMap> items = ((Result.Success<List<HashMap>>) result).getData();
        assertEquals("Should have 1 item", 1, items.size());

        HashMap item = items.get(0);
        assertEquals("Item name should match", "Test Item", item.get("name"));
        assertEquals("Item type should match", "Electronics", item.get("type"));
        assertEquals("Item count should match", "10", item.get("count"));
    }

    @Test
    public void testGetItemById_ExistingItem_ReturnsItem() {
        // Add an item
        Result<Long> addResult = repository.addItem("Test Item", "Electronics", "10");
        Long itemId = ((Result.Success<Long>) addResult).getData();

        // Retrieve the item
        Result<HashMap> result = repository.getItemById(String.valueOf(itemId));

        assertTrue("Result should be Success", result instanceof Result.Success);
        HashMap item = ((Result.Success<HashMap>) result).getData();
        assertNotNull("Item should not be null", item);
        assertEquals("Item name should match", "Test Item", item.get("name"));
    }

    @Test
    public void testGetItemById_NonExistingItem_ReturnsError() {
        Result<HashMap> result = repository.getItemById("99999");

        assertTrue("Result should be Error", result instanceof Result.Error);
    }

    @Test
    public void testGetItemById_NullId_ReturnsError() {
        Result<HashMap> result = repository.getItemById(null);

        assertTrue("Result should be Error", result instanceof Result.Error);
        Exception error = ((Result.Error) result).getError();
        assertTrue("Error should be IllegalArgumentException",
                error instanceof IllegalArgumentException);
    }

    @Test
    public void testGetItemById_EmptyId_ReturnsError() {
        Result<HashMap> result = repository.getItemById("");

        assertTrue("Result should be Error", result instanceof Result.Error);
    }

    @Test
    public void testUpdateItemCount_ValidData_ReturnsSuccess() {
        // Add an item
        Result<Long> addResult = repository.addItem("Test Item", "Electronics", "10");
        Long itemId = ((Result.Success<Long>) addResult).getData();

        // Update the count
        Result<Boolean> result = repository.updateItemCount(String.valueOf(itemId), "20");

        assertTrue("Result should be Success", result instanceof Result.Success);
        Boolean success = ((Result.Success<Boolean>) result).getData();
        assertTrue("Update should be successful", success);

        // Verify the update
        Result<HashMap> getResult = repository.getItemById(String.valueOf(itemId));
        HashMap item = ((Result.Success<HashMap>) getResult).getData();
        assertEquals("Count should be updated", "20", item.get("count"));
    }

    @Test
    public void testUpdateItemCount_NonExistingItem_ReturnsFalse() {
        Result<Boolean> result = repository.updateItemCount("99999", "20");

        assertTrue("Result should be Success", result instanceof Result.Success);
        Boolean success = ((Result.Success<Boolean>) result).getData();
        assertFalse("Update should fail for non-existing item", success);
    }

    @Test
    public void testUpdateItemCount_NullId_ReturnsError() {
        Result<Boolean> result = repository.updateItemCount(null, "20");

        assertTrue("Result should be Error", result instanceof Result.Error);
    }

    @Test
    public void testUpdateItemCount_NullCount_ReturnsError() {
        Result<Boolean> result = repository.updateItemCount("1", null);

        assertTrue("Result should be Error", result instanceof Result.Error);
    }

    @Test
    public void testDeleteItem_ExistingItem_ReturnsSuccess() {
        // Add an item
        Result<Long> addResult = repository.addItem("Test Item", "Electronics", "10");
        Long itemId = ((Result.Success<Long>) addResult).getData();

        // Delete the item
        Result<Boolean> result = repository.deleteItem(String.valueOf(itemId));

        assertTrue("Result should be Success", result instanceof Result.Success);
        Boolean success = ((Result.Success<Boolean>) result).getData();
        assertTrue("Delete should be successful", success);

        // Verify deletion
        Result<HashMap> getResult = repository.getItemById(String.valueOf(itemId));
        assertTrue("Item should not exist", getResult instanceof Result.Error);
    }

    @Test
    public void testDeleteItem_NonExistingItem_ReturnsFalse() {
        Result<Boolean> result = repository.deleteItem("99999");

        assertTrue("Result should be Success", result instanceof Result.Success);
        Boolean success = ((Result.Success<Boolean>) result).getData();
        assertFalse("Delete should fail for non-existing item", success);
    }

    @Test
    public void testDeleteItem_NullId_ReturnsError() {
        Result<Boolean> result = repository.deleteItem(null);

        assertTrue("Result should be Error", result instanceof Result.Error);
    }

    @Test
    public void testDeleteAllItems_MultipleItems_DeletesAll() {
        // Add multiple items
        repository.addItem("Item 1", "Type A", "5");
        repository.addItem("Item 2", "Type B", "10");
        repository.addItem("Item 3", "Type C", "15");

        // Delete all
        Result<Boolean> result = repository.deleteAllItems();

        assertTrue("Result should be Success", result instanceof Result.Success);

        // Verify all deleted
        Result<List<HashMap>> getResult = repository.getAllItems();
        List<HashMap> items = ((Result.Success<List<HashMap>>) getResult).getData();
        assertEquals("All items should be deleted", 0, items.size());
    }

    @Test
    public void testDeleteAllItems_EmptyDatabase_ReturnsSuccess() {
        Result<Boolean> result = repository.deleteAllItems();

        assertTrue("Result should be Success even for empty database",
                result instanceof Result.Success);
    }

    @Test
    public void testAddMultipleItems_AllRetrieved() {
        // Add multiple items
        repository.addItem("Item 1", "Type A", "5");
        repository.addItem("Item 2", "Type B", "10");
        repository.addItem("Item 3", "Type C", "15");

        // Retrieve all
        Result<List<HashMap>> result = repository.getAllItems();
        List<HashMap> items = ((Result.Success<List<HashMap>>) result).getData();

        assertEquals("Should have 3 items", 3, items.size());
    }

    @Test
    public void testAddItem_TrimsWhitespace() {
        Result<Long> result = repository.addItem("  Test Item  ", "  Electronics  ", "  10  ");

        assertTrue("Result should be Success", result instanceof Result.Success);
        Long itemId = ((Result.Success<Long>) result).getData();

        Result<HashMap> getResult = repository.getItemById(String.valueOf(itemId));
        HashMap item = ((Result.Success<HashMap>) getResult).getData();

        // Note: Trimming happens before insertion, so stored values should be trimmed
        assertEquals("Name should be trimmed", "Test Item", item.get("name"));
    }
}
