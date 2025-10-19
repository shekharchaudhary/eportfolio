package com.example.inventorycontrolapplication.data;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorycontrolapplication.MainActivity;
import com.example.inventorycontrolapplication.R;
import com.example.inventorycontrolapplication.ui.home.EditDataFragment;
import com.example.inventorycontrolapplication.ui.home.EditDataFragmentArgs;
import com.example.inventorycontrolapplication.ui.home.HomeFragment;
import com.example.inventorycontrolapplication.ui.home.HomeFragmentDirections;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RecycleDataAdapter extends RecyclerView.Adapter<RecycleDataAdapter.ViewHolder> {
    private List<HashMap> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private InventoryDataSource dataSource;

    /* FLAGS for Recycle View */
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    // data is passed into the constructor
    public RecycleDataAdapter(Context context, List<HashMap> data) {
        this.mInflater = LayoutInflater.from(context);
        this.dataSource = new InventoryDataSource(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_warehouse, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the row
        HashMap row = mData.get(position);
        // Traverse HashMap
        Iterator it = row.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            switch (entry.getKey().toString()) {
                case "_id":
                    holder.itemId.setText(entry.getValue().toString());
                    break;
                case "name":
                    holder.itemName.setText(entry.getValue().toString());
                    break;
                case "type":
                    holder.itemType.setText(entry.getValue().toString());
                    break;
                case "count":
                    holder.itemCount.setText(entry.getValue().toString());
                    break;
                default:
                    break;
            }
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView itemId, itemName, itemType, itemCount;
        Button deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            itemId = itemView.findViewById(R.id.item_id);
            itemName = itemView.findViewById(R.id.item_name);
            itemType = itemView.findViewById(R.id.item_type);

            // Set a listener to open the edit pane
            itemCount = itemView.findViewById(R.id.item_count);
            itemCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UpdateCountOnClick(view, itemId.getText().toString(),
                            itemCount.getText().toString());
                }
            });

            // Set the button to peg this to space
            deleteButton = itemView.findViewById(R.id.delete_data);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DeleteButtonOnClick(view, itemId.getText().toString());
                }
            });

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    /*
        Button Click Functions
     */

    // Helper to calculate the position of the record in the grid
    private int CalculatePosition(String id) {
        for (int counter = 0; counter < mData.size(); counter++) {
            HashMap currentRow = mData.get(counter);
            // Check to see if the strings are equal, then send the position back
            if (currentRow.get("_id").toString().equals(id)) {
                return counter;
            }
        }
        return -1;
    }

    private void UpdateCountOnClick(View view, String id, String count)
    {
        HomeFragmentDirections.ActionHomeToEdit action = HomeFragmentDirections.actionHomeToEdit(id, count);
        Navigation.findNavController(view).navigate(action);
    }

    private void DeleteButtonOnClick(View view, String id)
    {
        // Delete Entry from Database
        dataSource.deleteInventoryDatabase(id);
        // Remove Item from Items & notifychanged
        int position = CalculatePosition(id);
        mData.remove(position);
        notifyItemChanged(position);
        // Alert Record Deleted
        Snackbar.make(view, "Record Deleted!", Snackbar.LENGTH_LONG)
                .show();
    }

}
