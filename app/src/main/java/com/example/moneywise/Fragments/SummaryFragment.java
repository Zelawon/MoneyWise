package com.example.moneywise.Fragments;

import static android.graphics.Typeface.BOLD;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneywise.Adapters.SummaryAdapter;
import com.example.moneywise.Adapters.TransactionAdapter;
import com.example.moneywise.HomeActivity;
import com.example.moneywise.Models.Summary;
import com.example.moneywise.Models.Transaction;
import com.example.moneywise.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
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

public class SummaryFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private SummaryAdapter summaryAdapter;
    private List<Transaction> transactionList;

    public SummaryFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_summary, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView and adapter
        recyclerView = rootView.findViewById(R.id.parentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        summaryAdapter = new SummaryAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(summaryAdapter);

        // Retrieve authenticated user ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;

        // Retrieve transactions
        retrieveTransactions(userId);

        return rootView;
    }

    private void retrieveTransactions(String userId) {
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Transaction> transactions = new ArrayList<>();
                            // Get today's date
                            Calendar todayCalendar = Calendar.getInstance();
                            todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
                            todayCalendar.set(Calendar.MINUTE, 0);
                            todayCalendar.set(Calendar.SECOND, 0);
                            todayCalendar.set(Calendar.MILLISECOND, 0);
                            Date today = todayCalendar.getTime();

                            for (DocumentSnapshot document : task.getResult()) {
                                Transaction transaction = document.toObject(Transaction.class);
                                // Parse transaction date
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                                Date transactionDate;
                                try {
                                    transactionDate = dateFormat.parse(transaction.getDate());
                                } catch (ParseException e) {
                                    Log.e("SummaryFragment", "Error parsing transaction date: ", e);
                                    continue;
                                }
                                // Compare transaction date with today's date
                                if (transactionDate.after(today)) {
                                    // Skip transactions occurring after today's date
                                    continue;
                                }
                                transactions.add(transaction);
                            }
                            // Group transactions by month-year
                            Map<String, List<Transaction>> transactionsByMonthYear = organizeTransactionsByMonthYear(transactions);
                            // Generate summaries for each month-year group
                            List<Summary> summaries = generateSummaries(transactionsByMonthYear);
                            // Display summaries in RecyclerView
                            summaryAdapter.setSummaryList(summaries);
                            summaryAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("SummaryFragment", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    // Group transactions by month-year
    private Map<String, List<Transaction>> organizeTransactionsByMonthYear(List<Transaction> transactions) {
        Map<String, List<Transaction>> transactionsByMonthYear = new HashMap<>();
        for (Transaction transaction : transactions) {
            String[] dateParts = transaction.getDate().split("-");
            String monthYear = dateParts[1] + "-" + dateParts[2]; // Month-Year format
            if (!transactionsByMonthYear.containsKey(monthYear)) {
                transactionsByMonthYear.put(monthYear, new ArrayList<>());
            }
            transactionsByMonthYear.get(monthYear).add(transaction);
        }
        return transactionsByMonthYear;
    }

    // Generate summaries for each month-year group
    private List<Summary> generateSummaries(Map<String, List<Transaction>> transactionsByMonthYear) {
        List<Summary> summaries = new ArrayList<>();
        for (Map.Entry<String, List<Transaction>> entry : transactionsByMonthYear.entrySet()) {
            String monthYear = entry.getKey();
            List<Transaction> transactions = entry.getValue();

            double totalIncome = 0;
            double totalExpense = 0;
            int numTransactions = transactions.size();
            Map<String, Double> categorySpending = new HashMap<>();

            for (Transaction transaction : transactions) {
                double amount = Double.parseDouble(transaction.getAmount());
                if ("Income".equals(transaction.getType())) {
                    totalIncome += amount;
                } else if ("Expense".equals(transaction.getType())) {
                    totalExpense += amount;
                    String category = transaction.getCategory();
                    categorySpending.put(category, categorySpending.getOrDefault(category, 0.0) + amount);
                }
            }

            double sum = totalIncome - totalExpense;
            String mostSpendingCategory = findMostSpendingCategory(categorySpending);

            Summary summary = new Summary();
            summary.setDate(monthYear); // Month-Year format
            summary.setIncome(totalIncome);
            summary.setExpense(totalExpense);
            summary.setSum(sum);
            summary.setNumTransactions(numTransactions);
            summary.setMostSpendingCategory(mostSpendingCategory);

            summaries.add(summary);
        }

        // Sort summaries by date in descending order
        Collections.sort(summaries, new Comparator<Summary>() {
            @Override
            public int compare(Summary summary1, Summary summary2) {
                // Parse dates for comparison
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM-yyyy");
                try {
                    Date date1 = dateFormat.parse(summary1.getDate());
                    Date date2 = dateFormat.parse(summary2.getDate());
                    // Compare dates in descending order
                    return date2.compareTo(date1);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        return summaries;
    }

    // Utility method to find the category with the highest spending
    private String findMostSpendingCategory(Map<String, Double> categorySpending) {
        String mostSpendingCategory = "";
        double maxSpending = 0;
        for (Map.Entry<String, Double> entry : categorySpending.entrySet()) {
            if (entry.getValue() > maxSpending) {
                mostSpendingCategory = entry.getKey();
                maxSpending = entry.getValue();
            }
        }
        return mostSpendingCategory;
    }
}


