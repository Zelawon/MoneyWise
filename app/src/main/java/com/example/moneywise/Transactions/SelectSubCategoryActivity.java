package com.example.moneywise.Transactions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneywise.Adapters.SubCategoryAdapter;
import com.example.moneywise.Models.Item;
import com.example.moneywise.R;

import java.util.ArrayList;
import java.util.List;

public class SelectSubCategoryActivity extends AppCompatActivity {

    private RecyclerView subCategoryRecyclerView;
    private ImageView backIcon;
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sub_category);

        // Receive the selected category from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("selected_category")) {
            selectedCategory = intent.getStringExtra("selected_category");
        }

        // Initialize views
        backIcon = findViewById(R.id.back_icon);
        subCategoryRecyclerView = findViewById(R.id.parentSubCatRecyclerView);
        subCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create sample subcategory data based on the selected category
        List<Item> subCategories = createSampleSubCategories(selectedCategory);

        // Set up the adapter for subcategory RecyclerView
        SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(this, subCategories);
        subCategoryRecyclerView.setAdapter(subCategoryAdapter);

        // Set OnClickListener for the back icon
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set item click listener for the adapter
        subCategoryAdapter.setOnItemClickListener(new SubCategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Item item) {
                // Show the clicked subcategory in a toast
                //Toast.makeText(SelectSubCategoryActivity.this, item.getItemName(), Toast.LENGTH_SHORT).show();

                // Pass the selected subcategory back to the AddTransactionActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_sub_category", item.getItemName());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    // Method to create sample subcategory data based on the selected category
    private List<Item> createSampleSubCategories(String selectedCategory) {
        List<Item> items = new ArrayList<>();

        // Add subcategories based on the selected category
        switch (selectedCategory) {
            case "Food":
                items.add(new Item("Food General"));
                items.add(new Item("Supermarket"));
                items.add(new Item("Restaurant"));
                items.add(new Item("Delivery"));
                items.add(new Item("Everyday"));
                break;
            case "Entertainment":
                items.add(new Item("Entertainment General"));
                items.add(new Item("Hobbies"));
                items.add(new Item("Cinema and Theatre"));
                items.add(new Item("Streaming Services"));
                items.add(new Item("Disco"));
                items.add(new Item("Bar"));
                items.add(new Item("Vacation"));
                items.add(new Item("Games"));
                items.add(new Item("Concert"));
                items.add(new Item("Book"));
                items.add(new Item("Indoor Activities"));
                items.add(new Item("Outdoor Activities"));
                break;
            case "Transportation":
                items.add(new Item("Transportation General"));
                items.add(new Item("Monthly Pass"));
                items.add(new Item("Bus Fare"));
                items.add(new Item("Subway Fare"));
                items.add(new Item("Taxi Fare"));
                items.add(new Item("Train Ticket"));
                items.add(new Item("Ferry Ticket"));
                items.add(new Item("Ride-Sharing"));
                items.add(new Item("Bicycle Rental"));
                items.add(new Item("Scooter Rental"));
                items.add(new Item("Car Rental"));
                break;
            case "Home":
                items.add(new Item("Home General"));
                items.add(new Item("Electricity"));
                items.add(new Item("Water"));
                items.add(new Item("Gas"));
                items.add(new Item("Heating"));
                items.add(new Item("Rent"));
                items.add(new Item("Credit"));
                items.add(new Item("Insurance"));
                items.add(new Item("Internet"));
                items.add(new Item("TV"));
                items.add(new Item("Telephone"));
                items.add(new Item("Furniture"));
                items.add(new Item("Appliances"));
                items.add(new Item("Repairs"));
                items.add(new Item("Maintenance"));
                items.add(new Item("Cleaning"));
                items.add(new Item("Property Taxes"));
                items.add(new Item("Landscaping"));
                break;
            case "Clothing":
                items.add(new Item("Clothing General"));
                items.add(new Item("Trousers"));
                items.add(new Item("Shoes"));
                items.add(new Item("Sweaters"));
                items.add(new Item("Shirts"));
                items.add(new Item("Jackets"));
                items.add(new Item("T-shirts"));
                items.add(new Item("Shorts"));
                items.add(new Item("Dresses"));
                items.add(new Item("Skirts"));
                items.add(new Item("Sportswear"));
                items.add(new Item("Accessories"));
                items.add(new Item("Jewellery"));
                items.add(new Item("Underwear"));
                items.add(new Item("Sleepwear"));
                items.add(new Item("Swimwear"));
                break;
            case "Car":
                items.add(new Item("Car General"));
                items.add(new Item("Fuel"));
                items.add(new Item("Parking Fees"));
                items.add(new Item("Tolls"));
                items.add(new Item("Amortization"));
                items.add(new Item("Insurance"));
                items.add(new Item("Repairs"));
                items.add(new Item("Maintenance"));
                items.add(new Item("Car Wash"));
                items.add(new Item("Traffic Tickets"));
                items.add(new Item("Accessories"));
                break;
            case "Electronics":
                items.add(new Item("Electronics General"));
                items.add(new Item("Computer"));
                items.add(new Item("Tablet"));
                items.add(new Item("Telephone"));
                items.add(new Item("TV"));
                items.add(new Item("Printer"));
                items.add(new Item("Camera"));
                items.add(new Item("Gaming Console"));
                items.add(new Item("Accessories"));
                break;
            case "Health and Beauty":
                items.add(new Item("Health & Beauty General"));
                items.add(new Item("Cosmetics"));
                items.add(new Item("Perfume"));
                items.add(new Item("Health Insurance"));
                items.add(new Item("Medicament"));
                items.add(new Item("Nutrients"));
                items.add(new Item("GYM"));
                items.add(new Item("Hairdresser"));
                items.add(new Item("Beautician"));
                items.add(new Item("Solarium"));
                break;
            case "Education":
                items.add(new Item("Education General"));
                items.add(new Item("Tuition Fees"));
                items.add(new Item("Textbooks"));
                items.add(new Item("Stationery"));
                items.add(new Item("School Supplies"));
                items.add(new Item("Uniforms"));
                items.add(new Item("School Events"));
                items.add(new Item("School Meals"));
                items.add(new Item("Student Loans"));
                items.add(new Item("Scholarships"));
                items.add(new Item("Exam Fees"));
                items.add(new Item("Educational Software"));
                items.add(new Item("Lab Fees"));
                break;
            case "Children":
                items.add(new Item("Children General"));
                items.add(new Item("Toys"));
                items.add(new Item("Clothing"));
                items.add(new Item("Healthcare"));
                items.add(new Item("School"));
                items.add(new Item("Childcare"));
                items.add(new Item("Allowance"));
                items.add(new Item("Entertainment"));
                break;
            case "Work":
                items.add(new Item("Work General"));
                items.add(new Item("Salary"));
                items.add(new Item("Bonus"));
                items.add(new Item("Professional Memberships"));
                items.add(new Item("Business Expenses"));
                items.add(new Item("Freelance Income"));
                items.add(new Item("Pension"));
                items.add(new Item("Other"));
                break;
            case "Bureaucracy":
                items.add(new Item("Bureaucracy General"));
                items.add(new Item("Government Fees"));
                items.add(new Item("Licenses"));
                items.add(new Item("Permits"));
                items.add(new Item("Passport Renewal"));
                items.add(new Item("Visa Fees"));
                items.add(new Item("Legal Documents"));
                items.add(new Item("Notary Services"));
                items.add(new Item("Court Fees"));
                items.add(new Item("Fines"));
                items.add(new Item("Immigration Services"));
                items.add(new Item("Tax Preparation"));
                items.add(new Item("Accounting Services"));
                items.add(new Item("Consulting Fees"));
                items.add(new Item("Registration Fees"));
                break;
            case "Gifts":
                items.add(new Item("Gifts General"));
                items.add(new Item("Birthday Gifts"));
                items.add(new Item("Christmas Gifts"));
                items.add(new Item("Anniversary Gifts"));
                items.add(new Item("Wedding Gifts"));
                items.add(new Item("Graduation Gifts"));
                items.add(new Item("Baby Shower Gifts"));
                items.add(new Item("Housewarming Gifts"));
                items.add(new Item("Valentine's Day Gifts"));
                items.add(new Item("Mother's Day Gifts"));
                items.add(new Item("Father's Day Gifts"));
                items.add(new Item("Thank You Gifts"));
                items.add(new Item("Sympathy Gifts"));
                items.add(new Item("Corporate Gifts"));
                items.add(new Item("Gift Cards"));
                break;
            case "Bank":
                items.add(new Item("Bank General"));
                items.add(new Item("ATM Withdrawals"));
                items.add(new Item("Online Banking Fees"));
                items.add(new Item("Account Maintenance Fees"));
                items.add(new Item("Overdraft Fees"));
                items.add(new Item("Credit Card Fees"));
                items.add(new Item("Investment Fees"));
                items.add(new Item("Transaction Fees"));
                items.add(new Item("Late Payment Fees"));
                break;
            default:
                // Handle default case if necessary
                break;
        }

        return items;
    }
}
