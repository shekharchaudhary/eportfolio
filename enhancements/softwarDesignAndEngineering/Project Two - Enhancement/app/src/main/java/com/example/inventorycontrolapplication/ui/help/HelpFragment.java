package com.example.inventorycontrolapplication.ui.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.inventorycontrolapplication.R;

import ir.drax.constraintaccordionlist.AccordionItem;
import ir.drax.constraintaccordionlist.AccordionList;

public class HelpFragment extends Fragment {

    private HelpViewModel helpViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Create the view model
        helpViewModel = new ViewModelProvider(this).get(HelpViewModel.class);
        View root = inflater.inflate(R.layout.fragment_help, container, false);

        // Create the help accordians
        AccordionList accordionList = root.findViewById(R.id.accordion);
        accordionList
                .setARROW_ICON(R.drawable.left_chevron)
                .push(new AccordionItem("How to add items to the list?", "Add items by hitting the floating button on the home screen and inputting data. Hitting submit will add a record."))
                .push(new AccordionItem("How to alter the product count?", "To change the product count start by clicking on the count on the row you needing to be edit. A form will appear asking to input the new count."))
                .push(new AccordionItem("How to delete a product from the list?", "To delete a row tap the blue delete button on the right-hand side of the screen for the product you no longer need."))
                .push(new AccordionItem("Can a registered username be reused?", "A username can only be registered once."))
                .build();
        return root;
    }
}
