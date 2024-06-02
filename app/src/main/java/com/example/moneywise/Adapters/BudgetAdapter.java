package com.example.moneywise.Adapters;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneywise.Models.Budget;
import com.example.moneywise.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    private List<Budget> budgetList;
    private Context context;
    private FirebaseFirestore db;



    public BudgetAdapter(Context context, List<Budget> budgetList) {
        this.context = context;
        this.budgetList = budgetList;
        db = FirebaseFirestore.getInstance();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = budgetList.get(position);
        holder.bind(budget);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getStoredValue(budget.getCategory(), new OnValueReceivedListener() {
                    @Override
                    public void onValueReceived(float value) {
                        // Do something with the received value
                        String title = "";
                        if (value == 0) {
                            title = "Budget Not Set";
                        } else if (budget.getAmount() > value) {
                            title = "You Are Under Budget";
                            Log.e("TAG","---------------------------");
                            Log.e("amont form bd: ", String.valueOf(value));
                            Log.e("amont from month: ", String.valueOf(budget.getAmount()));
                            Log.e("TAG","---------------------------");
                        } else if (budget.getAmount() < value) {
                            title = "You Are Over Budget!";
                            Log.e("TAG","---------------------------");
                            Log.e("amont form bd: ", String.valueOf(value));
                            Log.e("amont from month: ", String.valueOf(budget.getAmount()));
                            Log.e("TAG","---------------------------");
                        }
                        new MaterialAlertDialogBuilder(context)
                                .setTitle(title)
                                .setMessage(""+budget.getCategory()+" budget is: " + value + "€")
                                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();

                    }
                });
                return true;
            }
        });
    }

    interface OnValueReceivedListener {
        void onValueReceived(float value);
    }

    private void getStoredValue(String category, OnValueReceivedListener listener) {
        // Retrieve authenticated user ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("budgets").document(userId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Check if the category exists in the document
                    if (document.contains(category)) {
                        // Retrieve the value of the category
                        Object value = document.get(category);
                        if (value instanceof String) {
                            try {
                                float floatValue = Float.parseFloat((String) value);
                                // Notify the listener with the retrieved value
                                listener.onValueReceived(floatValue);
                                return; // Exit the method once the value is received
                            } catch (NumberFormatException e) {
                                // Handle parsing error
                                Log.e(TAG, "Error parsing float value", e);
                            }
                        }
                    }
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
            // If category is not found or an error occurs, notify listener with default value
            listener.onValueReceived(0.0f);
        });
    }



    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTextView;
        TextView amountTextView;
        TextView itemColorTextView;
        View itemColorCircle;
        private Context context;
        private Map<String, Integer> categoryColorMap;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            itemColorTextView = itemView.findViewById(R.id.itemColorTextView);
            itemColorCircle = itemView.findViewById(R.id.itemColorCircle);
            context = itemView.getContext();
            initializeCategoryColorMap();
        }

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

        public void bind(Budget budget) {
            categoryTextView.setText(budget.getCategory());

            // Check if amount is positive or negative and set prefix accordingly
            String amountPrefix;
            if (budget.getAmount() >= 0) {
                amountPrefix = "+";
                amountTextView.setTextColor(ContextCompat.getColor(context, R.color.green));
            } else {
                amountPrefix = "-";
                amountTextView.setTextColor(ContextCompat.getColor(context, R.color.red));
            }

            // Set the first letter of the category name to itemColorTextView
            String categoryName = budget.getCategory();
            if (!categoryName.isEmpty()) {
                char firstLetter = Character.toUpperCase(categoryName.charAt(0));
                itemColorTextView.setText(String.valueOf(firstLetter));
            } else {
                // Set a default character if category name is empty
                itemColorTextView.setText("?");
            }

            // Set the background color of itemColorCircle based on the category
            Integer colorResId = categoryColorMap.getOrDefault(categoryName, R.color.black);
            itemColorCircle.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, colorResId)));

            // Set the amount text with prefix and suffix
            amountTextView.setText(amountPrefix + Math.abs(budget.getAmount()) + " €");
        }
    }
}
