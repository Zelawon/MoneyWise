package com.example.moneywise.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneywise.Models.Transaction;
import com.example.moneywise.R;
import com.example.moneywise.Transactions.ModifyTransactionActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactionList;
    private Map<String, Integer> categoryColorMap;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = new ArrayList<>(); // Initialize an empty list
        initializeCategoryColorMap(); // Initialize the category-color mapping
    }

    // Method to set the transaction list
    public void setTransactions(List<Transaction> transactions) {
        this.transactionList = transactions;
        notifyDataSetChanged(); // Notify RecyclerView that the data set has changed
    }

    // Initialize the category-color mapping
    private void initializeCategoryColorMap() {
        categoryColorMap = new HashMap<>();
        categoryColorMap.put("Uncategorized", R.color.green1);
        categoryColorMap.put("Food", R.color.pink);
        categoryColorMap.put("Entertainment", R.color.green);
        categoryColorMap.put("Transportation", R.color.blue);
        categoryColorMap.put("Home", R.color.brown);
        categoryColorMap.put("Clothing", R.color.purple);
        categoryColorMap.put("Car", R.color.navy);
        categoryColorMap.put("Electronics", R.color.orange);
        categoryColorMap.put("Health and Beauty", R.color.red);
        categoryColorMap.put("Education", R.color.cyan);
        categoryColorMap.put("Children", R.color.yellow);
        categoryColorMap.put("Work", R.color.gray);
        categoryColorMap.put("Bureaucracy", R.color.maroon);
        categoryColorMap.put("Gifts", R.color.deep_gold);
        categoryColorMap.put("Bank", R.color.black);
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder {

        private TextView categoryTextView;
        private TextView titleTextView;
        private TextView amountTextView;
        private TextView dateTextView;
        private TextView itemColorTextView;
        private View itemColorCircle;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            itemColorTextView = itemView.findViewById(R.id.itemColorTextView);
            itemColorCircle = itemView.findViewById(R.id.itemColorCircle);

            // Set long press listener
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Retrieve transaction
                    Transaction transaction = transactionList.get(getAdapterPosition());

                    // Show toast with transaction ID
                    //Toast.makeText(context, "Transaction ID: " + transaction.getTransactionId(), Toast.LENGTH_SHORT).show();

                    // Start ModifyTransactionActivity with transaction ID
                    Intent intent = new Intent(context, ModifyTransactionActivity.class);
                    intent.putExtra("transactionId", transaction.getTransactionId());
                    intent.putExtra("transactionDate", transaction.getDate());
                    context.startActivity(intent);

                    return true; // Consume the long click event
                }
            });
        }

        public void bind(Transaction transaction) {
            String subCategory = transaction.getSubcategory();
            if (subCategory == null || subCategory.isEmpty()) {
                subCategory = transaction.getCategory();
            }
            categoryTextView.setText(subCategory);
            titleTextView.setText(transaction.getTitle());

            // Set the first letter of the category to itemColorTextView
            String category = transaction.getCategory();
            if (!category.isEmpty()) {
                char firstLetter = category.charAt(0);
                itemColorTextView.setText(String.valueOf(firstLetter).toUpperCase()); // Convert to uppercase if needed
            } else {
                itemColorTextView.setText("U"); // Default to "U" if category is empty
            }

            // Set the background color of itemColorCircle based on the category
            Integer colorResId = categoryColorMap.getOrDefault(category, R.color.black);
            itemColorCircle.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, colorResId)));

            // Set the amount text
            String amount = transaction.getAmount();
            if (transaction.getType().equals("Expense")) {
                amountTextView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                amount = "-" + amount; // Prefix "-" for expenses
            } else if (transaction.getType().equals("Income")) {
                amountTextView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                amount = "+" + amount; // Prefix "+" for income
            }
            amountTextView.setText(amount + "€"); // Append € symbol after amount

            dateTextView.setText(transaction.getDate());
        }
    }
}
