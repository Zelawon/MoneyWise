package com.example.moneywise.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneywise.Adapters.BudgetAdapter;
import com.example.moneywise.HomeActivity;
import com.example.moneywise.Models.Budget;
import com.example.moneywise.Models.Transaction;
import com.example.moneywise.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReviewFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private BudgetAdapter adapter;
    private List<Transaction> transactionList;
    private Context context;

    public ReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize context
        context = getContext();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView and adapter
        recyclerView = view.findViewById(R.id.parentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BudgetAdapter(context, new ArrayList<>()); // Initialize adapter with an empty list
        recyclerView.setAdapter(adapter);

        // Retrieve current date from HomeActivity
        String currentDate = getCurrentDate();

        // Retrieve authenticated user ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;

        // Call retrieveTransactions with the current date and user ID
        if (userId != null) {
            // Extracting currentMonth
            String currentMonth = currentDate.substring(0, 2);

            // Extracting currentYear
            String currentYear = currentDate.substring(3);

            retrieveTransactions(currentMonth, currentYear, userId);
        } else {
            // Handle the case where the user is not authenticated
        }
    }

    private void retrieveTransactions(String currentMonth, String currentYear, String userId) {
        // Get today's date
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        Date todayDate = todayCal.getTime();

        // Query the "transactions" collection in the database for transactions with the specified userId
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // If the query is successful, initialize a list to store transactions
                            transactionList = new ArrayList<>();
                            // Iterate over each document in the query result
                            for (DocumentSnapshot document : task.getResult()) {
                                // Convert the document into a Transaction object
                                Transaction transaction = document.toObject(Transaction.class);
                                // Get the date of the transaction
                                String transactionDate = transaction.getDate();

                                // Parse the transaction date to compare with today's date
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                try {
                                    Date date = dateFormat.parse(transactionDate);
                                    // Compare transaction date with today's date
                                    if (!date.after(todayDate)) { // Don't add transactions after today
                                        // Get the month and year of the transaction
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(date);
                                        String transactionMonth = String.format("%02d", cal.get(Calendar.MONTH) + 1); // Adding 1 because Calendar.MONTH is zero-based
                                        String transactionYear = String.valueOf(cal.get(Calendar.YEAR));

                                        // Check if the transaction's month and year match the current month and year
                                        if (transactionMonth.equals(currentMonth) && transactionYear.equals(currentYear)) {
                                            // If the transaction matches, add it to the list
                                            transactionList.add(transaction);
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            // Sort the transactionList by date in reverse order
                            Collections.sort(transactionList, new Comparator<Transaction>() {
                                @Override
                                public int compare(Transaction t1, Transaction t2) {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                    try {
                                        Date date1 = dateFormat.parse(t1.getDate());
                                        Date date2 = dateFormat.parse(t2.getDate());
                                        // Compare dates in reverse order
                                        return date2.compareTo(date1);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        return 0;
                                    }
                                }
                            });

                            // Initialize a map to store total amounts for each category
                            Map<String, Double> categoryTotalMap = new HashMap<>();

                            // Iterate through the transactionList
                            for (Transaction transaction : transactionList) {
                                // Get the category of the transaction
                                String category = transaction.getCategory();

                                // Get the amount of the transaction as a double
                                double amount = Double.parseDouble(transaction.getAmount());

                                // Check if it's an income or expense transaction
                                if (transaction.getType().equals("Income")) {
                                    // If it's an income transaction, add the amount to the category total
                                    categoryTotalMap.put(category, categoryTotalMap.getOrDefault(category, 0.0) + amount);
                                } else if(transaction.getType().equals("Expense")) {
                                    // If it's an expense transaction, subtract the amount from the category total
                                    categoryTotalMap.put(category, categoryTotalMap.getOrDefault(category, 0.0) - amount);
                                }
                            }

                            // Map containing total amounts for each category
                            List<Budget> budgetList = new ArrayList<>();
                            for (Map.Entry<String, Double> entry : categoryTotalMap.entrySet()) {
                                String category = entry.getKey();
                                double totalAmount = entry.getValue();
                                Log.e("CategoryTotal", "Category: " + category + ", Total Amount: " + totalAmount);

                                // Create a Budget object and set its properties
                                Budget budget = new Budget();
                                budget.setUserId(userId);
                                budget.setCategory(category);
                                budget.setAmount((float) totalAmount);
                                // Add the Budget object to the list
                                budgetList.add(budget);
                            }

                            // Update adapter with new data
                            adapter = new BudgetAdapter(context, budgetList);
                            recyclerView.setAdapter(adapter);
                        } else {
                            // Handle errors
                        }
                    }
                });
    }

    private String getCurrentDate() {
        if (getActivity() instanceof HomeActivity) {
            return ((HomeActivity) getActivity()).getCurrentDate();
        } else {
            return ""; // Return default value or handle error
        }
    }
}
