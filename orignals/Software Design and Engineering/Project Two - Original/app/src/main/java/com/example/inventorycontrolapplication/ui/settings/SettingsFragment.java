package com.example.inventorycontrolapplication.ui.settings;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.inventorycontrolapplication.R;
import com.example.inventorycontrolapplication.data.NotificationTimer;
import com.google.android.material.snackbar.Snackbar;

import java.util.Map;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private NotificationTimer notificationService;
    // Activity Request
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // View Model
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        settingsViewModel.InitializeDataProvider(getContext());
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        // Generate Timer Service
        notificationService = new NotificationTimer(getContext());

        // Button/Switch Click Handlers
        Switch smsSwitch = root.findViewById(R.id.sms_int_switch);
        smsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Option is enabled
                if (isChecked) {
                    // Check for permissions
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) {
                        notificationService.StartThread();
                    } else {
                        // Hit the callback
                        requestPermissionLauncher.launch(new String[] {Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS});
                    }
                } else {
                    // Disable service if active
                    notificationService.StartThread();
                }
            }
        });

        Button deleteDataButton = root.findViewById(R.id.delete_all_data);
        deleteDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsViewModel.DeleteAllInventoryData();
                Snackbar.make(root, "Purge Successful", Snackbar.LENGTH_LONG)
                        .show();
            }
        });

        // Define application permission Callback
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            // INIT
            Boolean failedFlag = false;
            // Iterate over map
            for (Map.Entry<String, Boolean> item : permissions.entrySet())
            {
                // If permission is granted - continue
                if (item.getValue()) {
                    continue;
                }

                // Error State
                Snackbar.make(root, item.getKey() + " Denied", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.RED)
                        .show();

                // Zero out button
                failedFlag = true;
            }

            // Kill the button
            if (failedFlag)
                smsSwitch.setChecked(false);
        });

        // Return
        return root;
    }

}
