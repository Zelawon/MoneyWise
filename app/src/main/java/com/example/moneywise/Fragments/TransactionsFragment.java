package com.example.moneywise.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneywise.Adapters.TransactionAdapter;
import com.example.moneywise.HomeActivity;
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
import java.util.List;
import java.util.Locale;

public class TransactionsFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_transactions, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve current date from HomeActivity
        String currentDate = getCurrentDate();

        // Initialize RecyclerView and adapter
        recyclerView = rootView.findViewById(R.id.parentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(getContext(), transactionList);
        recyclerView.setAdapter(adapter);

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

        return rootView;
    }

    // Method to retrieve current date from HomeActivity
    private String getCurrentDate() {
        if (getActivity() instanceof HomeActivity) {
            return ((HomeActivity) getActivity()).getCurrentDate();
        } else {
            return ""; // Return default value or handle error
        }
    }

    // Retrieve Transactions
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

                            // Update the adapter with the new data
                            adapter.setTransactions(transactionList);
                        } else {
                            // Handle errors
                        }
                    }
                });
    }


}
