package com.example.inventorycontrolapplication.ui.home;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.inventorycontrolapplication.R;
import com.example.inventorycontrolapplication.data.InventoryDataSource;
import com.example.inventorycontrolapplication.data.Result;
import com.example.inventorycontrolapplication.data.helpers.SqlDbHelper;
import com.example.inventorycontrolapplication.data.model.LoggedInUser;
import com.example.inventorycontrolapplication.data.model.SqlDbContract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class AddDataViewModel extends ViewModel {

    private MutableLiveData<AddDataFormState> addDataFormState = new MutableLiveData<>();
    private InventoryDataSource dataSource;

    public AddDataViewModel() {
    }

    LiveData<AddDataFormState> getAddDataFormState() {
        return addDataFormState;
    }

    // Initialize the Data Source  Provider
    public void InitializeDataProvider(Context context) {
        dataSource = new InventoryDataSource(context);
    }

    // For Computing the form errors
    public void addDataDataChanged(String productName, String productType, String productCount) {
        if (!isStringValid(productName)) {
            addDataFormState.setValue(new AddDataFormState(R.string.invalid_productName, null, null));
        } else if (!isStringValid(productType)) {
            addDataFormState.setValue(new AddDataFormState(null, R.string.invalid_productType, null));
        } else if (!isValidNumber(productCount)) {
            addDataFormState.setValue(new AddDataFormState(null, null, R.string.invalid_productCount));
        } else {
            addDataFormState.setValue(new AddDataFormState(true));
        }
    }

    // Submit the data to the database
    public void AddRecord(String name, String type, String count) throws IOException {
        try {
            // Insert a record
            dataSource.insertInventoryDatabase(name, type, count);
        } catch (Exception e) {
            throw new IOException("Error inserting record", e);
        }
    }

    /*
        Private Helpers
     */

    // A string validation for over 3 chars
    private boolean isStringValid(String sys) {
        return sys != null && sys.trim().length() > 3;
    }

    // checks to see if the entered number is really a number
    private boolean isValidNumber(String sys) {
        try {
            int i = Integer.parseInt(sys);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
