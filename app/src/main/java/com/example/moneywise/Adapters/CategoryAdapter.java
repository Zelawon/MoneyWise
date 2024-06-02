package com.example.moneywise.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneywise.Models.Category;
import com.example.moneywise.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categoryList;
    private OnItemClickListener onItemClickListener;
    private Context context;
    private int[] circleColors; // Array of colors for circles

    // Constructor
    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
        this.circleColors = context.getResources().getIntArray(R.array.circle_colors); // Load colors from resources
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        TextView categoryColorTextView;
        RelativeLayout categoryColorCircle; // Relative layout representing the circle

        public ViewHolder(View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            categoryColorTextView = itemView.findViewById(R.id.categoryColorTextView);
            categoryColorCircle = itemView.findViewById(R.id.categoryColorCircle);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.categoryNameTextView.setText(category.getCategoryName());

        // Set the text of categoryColorTextView to the first letter of categoryNameTextView
        String categoryName = category.getCategoryName();
        if (!categoryName.isEmpty()) {
            holder.categoryColorTextView.setText(String.valueOf(categoryName.charAt(0)).toUpperCase()); // Set the first letter as uppercase
        }

        // Set the background color for categoryColorCircle
        int colorIndex = position % circleColors.length; // Calculate color index based on position
        holder.categoryColorCircle.setBackgroundTintList(ColorStateList.valueOf(circleColors[colorIndex]));

        // Set click listener for the parent item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // Method to set item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}
