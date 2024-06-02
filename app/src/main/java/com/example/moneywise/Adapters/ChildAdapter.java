// ChildAdapter.java
package com.example.moneywise.Adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneywise.Models.Item;
import com.example.moneywise.R;

import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ViewHolder> {
    private List<Item> itemList;
    private int parentColor; // Color of the parent item

    // Constructor
    public ChildAdapter(List<Item> itemList, int parentColor) {
        this.itemList = itemList;
        this.parentColor = parentColor;
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        TextView itemColorTextView;
        RelativeLayout itemColorCircle;

        public ViewHolder(View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            itemColorTextView = itemView.findViewById(R.id.itemColorTextView);
            itemColorCircle = itemView.findViewById(R.id.itemColorCircle);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.itemNameTextView.setText(item.getItemName());

        // Set the text of itemColorTextView to the first letter of itemNameTextView
        String itemName = item.getItemName();
        if (!itemName.isEmpty()) {
            holder.itemColorTextView.setText(String.valueOf(itemName.charAt(0)).toUpperCase()); // Set the first letter as uppercase
        }

        // Set the background tint of the item view to the color of the parent item
        holder.itemColorCircle.setBackgroundTintList(ColorStateList.valueOf(parentColor));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
