package com.example.moneywise.Transactions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneywise.Adapters.CategoryAdapter;
import com.example.moneywise.Models.Category;
import com.example.moneywise.R;

import java.util.ArrayList;
import java.util.List;

public class SelectCategoryActivity extends AppCompatActivity {

    private RecyclerView parentRecyclerView;
    private List<Category> categories;
    private int[] circleColors; // Array of colors for circles
    private ImageView backIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        backIcon = findViewById(R.id.back_icon);

        // Initialize the parent RecyclerView
        parentRecyclerView = findViewById(R.id.parentRecyclerView);
        parentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load colors from resources
        circleColors = getResources().getIntArray(R.array.circle_colors);

        // Create sample data
        categories = createSampleData(); // Assign the value to categories

        // Set up the parent RecyclerView adapter
        CategoryAdapter categoryAdapter = new CategoryAdapter(this, categories);
        parentRecyclerView.setAdapter(categoryAdapter);

        // Set OnClickListener for the back icon
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the activity when the back icon is clicked
                finish();
            }
        });

        // Handle item click listener for the parent RecyclerView
        categoryAdapter.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Get the clicked category
                Category clickedCategory = categories.get(position);

                // Set the selected category and finish the activity
                String selectedCategory = clickedCategory.getCategoryName(); // Replace with actual selected category
                Intent resultIntent = new Intent(); // Create a new intent to hold the result
                resultIntent.putExtra("selected_category", selectedCategory);
                setResult(Activity.RESULT_OK, resultIntent); // Set the result with a success code and the result intent
                finish(); // Finish the activity
            }
        });
    }

    // Method to create sample data
    private List<Category> createSampleData() {
        List<Category> categories = new ArrayList<>();

        // Create categories with items
        categories.add(new Category("Uncategorized"));
        categories.add(new Category("Food"));
        categories.add(new Category("Entertainment"));
        categories.add(new Category("Transportation"));
        categories.add(new Category("Home"));
        categories.add(new Category("Clothing"));
        categories.add(new Category("Car"));
        categories.add(new Category("Electronics"));
        categories.add(new Category("Health and Beauty"));
        categories.add(new Category("Education"));
        categories.add(new Category("Children"));
        categories.add(new Category("Work"));
        categories.add(new Category("Bureaucracy"));
        categories.add(new Category("Gifts"));
        categories.add(new Category("Bank"));

        return categories;
    }
}
