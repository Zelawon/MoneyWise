package com.example.moneywise.Fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.moneywise.HomeActivity;
import com.example.moneywise.Models.DailyBalance;
import com.example.moneywise.Models.DailyTotals;
import com.example.moneywise.Models.Transaction;
import com.example.moneywise.R;
import com.example.moneywise.Reports.ReportExpandData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsFragment extends Fragment {
    private LineChart transactionsLineChart,balanceLineChart;
    private PieChart incomesPieChart,expensesPieChart;
    private TextView expenseReport, incomesReport;
    private FirebaseFirestore db;
    private List<Transaction> transactionList ;
    private List<DailyTotals> dailyTotalsList = new ArrayList<>();
    private Map<String, Integer> categoryColorMap;
    private List<DailyBalance> dailyBalances;
    private Context context;
    private String totalAmountText="", currentDate;

    public ReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionsLineChart = view.findViewById(R.id.transactions_lineChart);
        balanceLineChart = view.findViewById(R.id.balance_lineChart);
        incomesPieChart = view.findViewById(R.id.incomesPieChart);
        expensesPieChart = view.findViewById(R.id.expensesPieChart);
        expenseReport = view.findViewById(R.id.expenseReport_textview);
        incomesReport = view.findViewById(R.id.incomesReport_textview);

        // Initialize context
        context = getContext();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve current date from HomeActivity
        currentDate = getCurrentDate();
        Log.d("Debug", "Current Date: " + currentDate); // Debug line


        // Retrieve authenticated user ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;
        Log.d("Debug", "User ID: " + userId); // Debug line


        // Call retrieveTran with the current date and user ID
        if (userId != null) {
            // Extracting currentMonth
            String currentMonth = currentDate.substring(0, 2);
            Log.d("Debug", "Current Month: " + currentMonth); // Debug line

            // Extracting currentYear
            String currentYear = currentDate.substring(3);
            Log.d("Debug", "Current Year: " + currentYear); // Debug line

            // Call retrieveTransactions first
            retrieveTransactions(currentMonth, currentYear, userId);

            // Then call retrieveBlalance
            retrieveBlalance(currentMonth, currentYear, userId);
        } else {
            // Handle the case where the user is not authenticated
            Log.d("Debug", "User ID is null");
        }

        setupLineChartTransactions();
        setupLineChartBalance();
    }


    // Inside your retrieveTransactions method
    private void retrieveBlalance(String currentMonth, String currentYear, String userId) {
                // Call calculateTotalAmount with a callback
        calculateTotalAmount(currentMonth, currentYear, userId, new TotalAmountCallback() {
            @Override
            public void onTotalAmountCalculated(double totalAmount) {
                double dailyBalance = totalAmount;
                db.collection("transactions")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                List<Transaction> transactions = new ArrayList<>();
                                QuerySnapshot querySnapshot = task.getResult();
                                if (querySnapshot != null) {
                                    // Initialize daily balance with the total starting balance

                                    for (QueryDocumentSnapshot document : querySnapshot) {
                                        Transaction transaction = document.toObject(Transaction.class);
                                        // Assuming date format is dd-MM-yyyy
                                        String[] dateParts = transaction.getDate().split("-");
                                        if (dateParts.length == 3) {
                                            String day = dateParts[0];
                                            String month = dateParts[1];
                                            String year = dateParts[2];

                                            if (month.equals(currentMonth) && year.equals(currentYear)) {
                                                transactions.add(transaction);
                                            }
                                        }
                                    }
                                }
                                // Process the transactions list as needed
                                // Calculate daily balances
                                List<DailyBalance> dailyBalances = calculateDailyBalances(transactions, dailyBalance, currentMonth, currentYear);
                                // Process the daily balances as needed
                                processDailyBalances(dailyBalances);
                                //Do the Incomes & Expenses PieChart
                                processPieCharts(transactions);
                            } else {
                                // Handle the error
                                System.out.println("Error getting documents: " + task.getException());
                            }
                        });

            }

            private void processPieCharts(List<Transaction> transactions) {
                if (transactions == null) {
                    Log.e(TAG, "Transactions list is null");
                    return;
                }

                List<Transaction> expenseTransactions = new ArrayList<>();
                List<Transaction> incomeTransactions = new ArrayList<>();

                // Map categories to colors
                Map<String, Integer> categoryColors = new HashMap<>();
                categoryColors.put("Uncategorized",Color.parseColor("#114232"));
                categoryColors.put("Food", Color.parseColor("#FFC0CB"));
                categoryColors.put("Entertainment", Color.parseColor("#008000"));
                categoryColors.put("Transportation", Color.parseColor("#0000FF"));
                categoryColors.put("Home", Color.parseColor("#A52A2A"));
                categoryColors.put("Clothing", Color.parseColor("#800080"));
                categoryColors.put("Car", Color.parseColor("#000080"));
                categoryColors.put("Electronics", Color.parseColor("#FFA500"));
                categoryColors.put("Health and Beauty", Color.parseColor("#FF0000"));
                categoryColors.put("Education", Color.parseColor("#00FFFF"));
                categoryColors.put("Children", Color.parseColor("#FFFF00"));
                categoryColors.put("Bureaucracy", Color.parseColor("#800000"));
                categoryColors.put("Gifts", Color.parseColor("#FFCC33"));
                categoryColors.put("Bank", Color.parseColor("#000000"));
                categoryColors.put("Work", Color.parseColor("#9E9E9E"));

                // Separate transactions based on their type
                for (Transaction transaction : transactions) {
                    if ("Expense".equals(transaction.getType())) {
                        expenseTransactions.add(transaction);
                    } else if ("Income".equals(transaction.getType())) {
                        incomeTransactions.add(transaction);
                    }
                }
                //  two separate lists: expenseTransactions and incomeTransactions

                // Calculate total amount for each expense category
                Map<String, Double> categoryTotalExpensesMap = new HashMap<>();
                for (Transaction expenseTransaction : expenseTransactions) {
                    String category = expenseTransaction.getCategory();
                    double amount = Double.parseDouble(expenseTransaction.getAmount());
                    // Add amount to the total for this category
                    categoryTotalExpensesMap.put(category, categoryTotalExpensesMap.getOrDefault(category, 0.0) + amount);
                }
                // Calculate total amount for each income category
                Map<String, Double> categoryTotalIncomeMap = new HashMap<>();
                for (Transaction incomeTransaction : incomeTransactions) {
                    String category = incomeTransaction.getCategory();
                    double amount = Double.parseDouble(incomeTransaction.getAmount());
                    // Add amount to the total for this category
                    categoryTotalIncomeMap.put(category, categoryTotalIncomeMap.getOrDefault(category, 0.0) + amount);
                }

                // categoryTotalExpensesMap contains the total amount for each expense category
                ArrayList<PieEntry> entriesExpense = new ArrayList<>();
                ArrayList<Integer> colorsExpense = new ArrayList<>();
                for (Map.Entry<String, Double> entry : categoryTotalExpensesMap.entrySet()) {
                    entriesExpense.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
                    Integer color = categoryColors.get(entry.getKey());
                    if (color != null) {
                        colorsExpense.add(color);
                    } else {
                        // Provide a default color if the category color is not found
                        colorsExpense.add(Color.parseColor("#7FFF00"));
                    }
                    Log.d(TAG, "Expense Category: " + entry.getKey() + ", Total Amount: " + entry.getValue());
                }
                // categoryTotalIncomeMap contains the total amount for each income category
                ArrayList<PieEntry> entriesIncome = new ArrayList<>();
                ArrayList<Integer> colorsIncome = new ArrayList<>();
                for (Map.Entry<String, Double> entry : categoryTotalIncomeMap.entrySet()) {
                    entriesIncome.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
                    Integer color = categoryColors.get(entry.getKey());
                    if (color != null) {
                        colorsIncome.add(color);
                    } else {
                        // Provide a default color if the category color is not found
                        colorsIncome.add(Color.parseColor("#7FFF00"));
                    }
                    Log.d(TAG, "Income Category: " + entry.getKey() + ", Total Amount: " + entry.getValue());
                }
                setupPieChart(expensesPieChart, entriesExpense, colorsExpense);
                setupPieChart(incomesPieChart, entriesIncome, colorsIncome);

                // Set OnClickListener for expenseReport_textview
                expenseReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Start new activity with categoryTotalExpensesMap for expenses
                        startNewActivity(categoryTotalExpensesMap,currentDate);
                    }
                });

                // Set OnClickListener for incomesReport_textview
                incomesReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Start new activity with categoryTotalIncomeMap for incomes
                        startNewActivity(categoryTotalIncomeMap, currentDate);
                    }
                });
            }
            private void startNewActivity(Map<String, Double> categoryTotalMap, String currentDate) {
                // Check if the categoryTotalMap and currentDate are not null
                if (categoryTotalMap != null && !categoryTotalMap.isEmpty() && currentDate != null && !currentDate.isEmpty()) {
                    // Log the contents of categoryTotalMap
                    for (Map.Entry<String, Double> entry : categoryTotalMap.entrySet()) {
                        Log.d(TAG, "Category: " + entry.getKey() + ", Total: " + entry.getValue());
                    }

                    // Create the intent
                    Intent intent = new Intent(getActivity(), ReportExpandData.class);
                    intent.putExtra("categoryTotalMap", (Serializable) categoryTotalMap);
                    intent.putExtra("currentDate", currentDate);
                    startActivity(intent);
                } else {
                    // Handle the case where data is invalid
                    Toast.makeText(getActivity(), "No data to show.", Toast.LENGTH_SHORT).show();
                }
            }

            private void setupPieChart(final PieChart pieChart, ArrayList<PieEntry> entries, ArrayList<Integer> colors) {
                if (entries == null || entries.isEmpty() || colors == null || colors.isEmpty()) {
                    pieChart.clear(); // Clear any existing data
                    pieChart.setNoDataText("No data available"); // Set message for no data
                    pieChart.invalidate(); // Refresh the chart
                    return;
                }

                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setDrawValues(false); // disable value labels

                // Retrieve colors & Null check for colors
                if (colors != null && !colors.isEmpty()) {
                    dataSet.setColors(colors);  // Retrieve colors
                } else {
                    // Provide a default color set if colors is null or empty
                    dataSet.setColors(Color.parseColor("#7FFF00"));
                }

                dataSet.setSliceSpace(3f); // Space between slices
                dataSet.setSelectionShift(5f); // Shift distance for selected slice

                PieData data = new PieData(dataSet); // Create PieData
                pieChart.setData(data); // Set data to PieChart
                pieChart.getDescription().setEnabled(false); // Remove description label
                pieChart.getLegend().setEnabled(false); // Remove legend
                pieChart.setDrawEntryLabels(false); // Disable the display of category names (labels)
                pieChart.setRotationEnabled(false);
                pieChart.invalidate(); // Refresh the chart

                pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(Entry e, Highlight h) {
                        if (e == null)
                            return;
                        PieEntry entry = (PieEntry) e;
                        float value = entry.getValue();
                        String label = entry.getLabel();
                        pieChart.setCenterText(label + ": " + value + "€"); // Display label and value in the center of the chart
                        pieChart.invalidate(); // Refresh the chart
                    }

                    @Override
                    public void onNothingSelected() {
                        pieChart.setCenterText(""); // Display nothing in the center of the chart
                    }
                });
            }

            private List<DailyBalance> calculateDailyBalances(List<Transaction> transactions, double oldBalance, String currentMonth, String currentYear) {
                Map<String, List<Transaction>> transactionsByDay = new HashMap<>();

                for (Transaction transaction : transactions) {
                    String day = transaction.getDate().split("-")[0]; // Extract the day part
                    transactionsByDay
                            .computeIfAbsent(day, k -> new ArrayList<>())
                            .add(transaction);
                }

                List<DailyBalance> dailyBalances = new ArrayList<>();
                double balance = oldBalance;

                for (int i = 1; i <= 31; i++) {
                    String day = String.format("%02d", i);
                    List<Transaction> dayTransactions = transactionsByDay.getOrDefault(day, new ArrayList<>());

                    for (Transaction transaction : dayTransactions) {
                        double amount = Double.parseDouble(transaction.getAmount());
                        if ("Income".equalsIgnoreCase(transaction.getType())) {
                            balance += amount;
                        } else if ("Expense".equalsIgnoreCase(transaction.getType())) {
                            balance -= amount;
                        }
                    }

                    dailyBalances.add(new DailyBalance(day + "-" + currentMonth + "-" + currentYear, balance));
                }

                return dailyBalances;
            }

            private void processDailyBalances(List<DailyBalance> dailyBalances) {
                // Implement this method to process the list of daily balances
                setLineChartBalance(dailyBalances);

                for (DailyBalance dailyBalance : dailyBalances) {
                    Log.d(TAG, "Day: " + dailyBalance.getDay() + " - Balance: " + dailyBalance.getBalance());
                }
            }


        });
    }

    // Method to start a new activity with the categoryTotalIncomeMap
    private void calculateTotalAmount(String currentMonth, String currentYear, String userId, TotalAmountCallback callback) {
        Log.d("Debug", "Calculating total amount..."); // Debug line
        Log.d("Debug", "Current month: " + currentMonth); // Debug line
        Log.d("Debug", "Current year: " + currentYear); // Debug line
        Log.d("Debug", "User ID: " + userId); // Debug line

        // Get current date
        Calendar calendar = Calendar.getInstance();
        int currentMonthInt = Integer.parseInt(currentMonth); // Assuming currentMonth is in the format "MM"
        int currentYearInt = Integer.parseInt(currentYear); // Assuming currentYear is in the format "YYYY"
        calendar.set(Calendar.MONTH, currentMonthInt - 1); // Subtracting 1 because Calendar.MONTH is zero-based
        calendar.set(Calendar.YEAR, currentYearInt);
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set day to 1st of the month

        // Calculate the first day of the current month
        Date firstDayOfMonth = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String firstDayOfMonthString = dateFormat.format(firstDayOfMonth);

        Log.d("Debug", "First day of month: " + firstDayOfMonthString); // Debug line

        // Execute Firestore query to calculate total amount
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        float totalExpense = 0.0f;
                        float totalIncome = 0.0f;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String amountStr = document.getString("amount"); // Assuming "amount" field is a string
                                String type = document.getString("type"); // Assuming "type" field indicates Expense or Income
                                String dateString = document.getString("date"); // Assuming "date" field stores the transaction date

                                // Parse the transaction date
                                Date transactionDate = dateFormat.parse(dateString);

                                if (amountStr != null && type != null && dateString != null && transactionDate != null) {
                                    // Compare transaction date with the first day of the current month
                                    if (transactionDate.before(firstDayOfMonth) && !dateString.startsWith("01-" + currentMonth + "-" + currentYear)) {
                                        float amount = Float.parseFloat(amountStr);
                                        if (type.equals("Expense")) {
                                            totalExpense -= amount;
                                        } else if (type.equals("Income")) {
                                            totalIncome += amount;
                                        }
                                    }
                                }
                            } catch (NumberFormatException | ParseException e) {
                                Log.e("Error", "Error parsing amount: " + e.getMessage()); // Debug line
                                // Handle the case where parsing fails or the amount is not a valid number
                            }
                        }
                        float totalAmount = totalIncome + totalExpense;

                        Log.d("TAG", "Balance before current month: "+totalAmount);
                        // Once total amount is calculated, invoke the callback
                        callback.onTotalAmountCalculated(Double.parseDouble(String.valueOf(totalAmount)));
                    } else {
                        // Handle failure
                    }
                });
    }
    interface TotalAmountCallback {
        void onTotalAmountCalculated(double totalAmount);
    }



    private void retrieveTransactions(String currentMonth, String currentYear, String userId) {
        // Get today's date at the start of the day
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
                            // Initialize a map to store totals for each day
                            Map<String, DailyTotals> dailyTotalsMap = new HashMap<>();

                            // Iterate over each document in the query result
                            for (DocumentSnapshot document : task.getResult()) {
                                // Convert the document into a Transaction object
                                Transaction transaction = document.toObject(Transaction.class);
                                // Get the date of the transaction
                                String transactionDate = transaction.getDate();

                                // Parse the transaction date
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                try {
                                    Date date = dateFormat.parse(transactionDate);
                                    // Compare transaction date with today's date
                                    if (!date.after(todayDate)) { // Ignore transactions after today
                                        // Get the month and year of the transaction
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(date);
                                        String transactionMonth = String.format("%02d", cal.get(Calendar.MONTH) + 1); // Adding 1 because Calendar.MONTH is zero-based
                                        String transactionYear = String.valueOf(cal.get(Calendar.YEAR));

                                        // Check if the transaction's month and year match the current month and year
                                        if (transactionMonth.equals(currentMonth) && transactionYear.equals(currentYear)) {
                                            // If the transaction matches, process it for daily totals
                                            String day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
                                            String key = day + "-" + currentMonth + "-" + currentYear;

                                            // Initialize the daily total if not already present
                                            dailyTotalsMap.putIfAbsent(key, new DailyTotals());
                                            DailyTotals totals = dailyTotalsMap.get(key);

                                            // Set the date field
                                            totals.setDate(key);

                                            // Update the daily totals based on the transaction type
                                            if ("Income".equals(transaction.getType())) {
                                                totals.setIncome(totals.getIncome() + Double.parseDouble(transaction.getAmount()));
                                            } else if ("Expense".equals(transaction.getType())) {
                                                totals.setExpense(totals.getExpense() + Double.parseDouble(transaction.getAmount()));
                                            }
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            // Update the class-level dailyTotalsList with the new data
                            dailyTotalsList = new ArrayList<>(dailyTotalsMap.values());

                            // Log the daily totals
                            for (DailyTotals totals : dailyTotalsList) {
                                Log.d("DailyTotals", "Date: " + totals.getDate() + ", Income: " + totals.getIncome() + ", Expense: " + totals.getExpense());
                            }

                            // Call setLineChartTransactionsData after dailyTotalsList is populated
                            setLineChartTransactionsData(dailyTotalsList);
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

    private void setupLineChartTransactions() {
        // Disable touch gestures (e.g., tap and swipe)
        transactionsLineChart.setTouchEnabled(false);

        // Hide the background grid
        transactionsLineChart.setDrawGridBackground(true);

        // Set borders
        transactionsLineChart.setDrawBorders(true); // Enable drawing borders
        transactionsLineChart.setBorderColor(R.color.darkest_gray); // Set border color

        // Customize X axis
        XAxis xAxis = transactionsLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Set position of X axis
        xAxis.setTextColor(Color.BLACK); // Set color of X axis labels
        xAxis.setTextSize(12f); // Set size of X axis labels text

        // Customize left Y axis
        YAxis leftAxis = transactionsLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK); // Set color of left Y axis labels
        leftAxis.setTextSize(12f); // Set size of left Y axis labels text
        leftAxis.setValueFormatter(new EuroValueFormatter());

        // Customize right Y axis
        YAxis rightAxis = transactionsLineChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y axis

        // Customize chart legend
        Legend legend = transactionsLineChart.getLegend();
        legend.setTextColor(Color.BLACK); // Set color of legend labels
        legend.setTextSize(12f); // Set size of legend labels text

        transactionsLineChart.setDescription(null);
    }

    private void setLineChartTransactionsData(List<DailyTotals> dailyTotals) {
        if (!isAdded()) {
            // Fragment is not attached to the activity, cannot proceed
            return;
        }
        List<Entry> expenseEntries = new ArrayList<>();
        List<Entry> incomeEntries = new ArrayList<>();

        // Determine the number of days in the month based on the DailyTotals
        int numDaysInMonth = 0;
        if (!dailyTotals.isEmpty()) {
            // Parse the date from the first DailyTotals to get the month
            String firstDate = dailyTotals.get(0).getDate();
            try {
                // Parse the date string to get the month
                Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(firstDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                // Get the actual maximum number of days in the month
                numDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Create entries for expenses and incomes
        for (int i = 1; i <= numDaysInMonth; i++) {
            // Initialize expense and income to 0 for the current day
            float expense = 0;
            float income = 0;

            // Find corresponding DailyTotals for the current day, if available
            for (DailyTotals dailyTotal : dailyTotals) {
                try {
                    // Parse the date to get the day of the month
                    Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dailyTotal.getDate());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

                    // If the DailyTotals corresponds to the current day, update expense and income
                    if (dayOfMonth == i) {
                        expense = (float) dailyTotal.getExpense();
                        income = (float) dailyTotal.getIncome();
                        break;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            // Add entries for expenses and incomes
            expenseEntries.add(new Entry(i, expense));
            incomeEntries.add(new Entry(i, income));
        }

        // Check if both expenseEntries and incomeEntries are empty
        if (expenseEntries.isEmpty() && incomeEntries.isEmpty()) {
            // Clear any existing data
            transactionsLineChart.clear();
            // Customize the message when there is no data
//            transactionsLineChart.setNoDataText("No data available for this month");
            transactionsLineChart.invalidate(); // Refresh the chart
            return;
        }

        LineDataSet expenseDataSet = new LineDataSet(expenseEntries, "Expenses");
        expenseDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.light_red));
        expenseDataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.maroon));

        LineDataSet incomeDataSet = new LineDataSet(incomeEntries, "Incomes");
        incomeDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.green5));
        incomeDataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.green1));

        // Customize circle appearance
        expenseDataSet.setDrawCircles(true);
        expenseDataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.light_red));
        expenseDataSet.setCircleRadius(2f);
        expenseDataSet.setDrawCircleHole(false);

        incomeDataSet.setDrawCircles(true);
        incomeDataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.green5));
        incomeDataSet.setCircleRadius(2f);
        incomeDataSet.setDrawCircleHole(false);

        // Customize line thickness
        expenseDataSet.setLineWidth(1.5f);
        incomeDataSet.setLineWidth(1.5f);

        LineData lineData = new LineData(expenseDataSet, incomeDataSet);
        transactionsLineChart.setData(lineData);

        // Ensure the no data message is cleared if there is data
        transactionsLineChart.setNoDataText("");
        transactionsLineChart.invalidate(); // Refresh the chart
    }

    private void setupLineChartBalance() {
        // Disable touch gestures (e.g., tap and swipe)
        balanceLineChart.setTouchEnabled(false);

        // Hide the background grid
        balanceLineChart.setDrawGridBackground(true);

        // Set borders
        balanceLineChart.setDrawBorders(true); // Enable drawing borders
        balanceLineChart.setBorderColor(R.color.darkest_gray); // Set border color

        // Customize X axis
        XAxis xAxis = balanceLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Set position of X axis
        xAxis.setTextColor(Color.BLACK); // Set color of X axis labels
        xAxis.setTextSize(12f); // Set size of X axis labels text

        // Customize left Y axis
        YAxis leftAxis = balanceLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK); // Set color of left Y axis labels
        leftAxis.setTextSize(12f); // Set size of left Y axis labels text
        leftAxis.setValueFormatter(new EuroValueFormatter());

        // Customize right Y axis
        YAxis rightAxis = balanceLineChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y axis

        // Customize chart legend
        Legend legend = balanceLineChart.getLegend();
        legend.setTextColor(Color.BLACK); // Set color of legend labels
        legend.setTextSize(12f); // Set size of legend labels text

        balanceLineChart.setDescription(null);
    }

    private void setLineChartBalance(List<DailyBalance> dailyBalances)  {
        if (!isAdded()) {
            // Fragment is not attached to the activity, cannot proceed
            return;
        }
        if (dailyBalances == null) {
            // If dailyBalances is null, log an error and return
            Log.e(TAG, "Daily balances list is null");
            return;
        }
        List<Entry> balanceEntries = new ArrayList<>();

        // Determine the number of days in the month based on the DailyTotals
        int numDaysInMonth = 0;
        if (!dailyBalances.isEmpty()) {
            // Parse the date from the first DailyBalance to get the month
            String firstDate = dailyBalances.get(0).getDay();
            try {
                // Parse the date string to get the month
                Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(firstDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                // Get the actual maximum number of days in the month
                numDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Create entries for balance
        for (int i = 1; i <= numDaysInMonth; i++) {
            // Initialize balance amount to 0 for the current day
            float balanceAmount = 0;

            // Find corresponding DailyBalance for the current day, if available
            for (DailyBalance dailyBalance : dailyBalances) {
                try {
                    // Parse the date to get the day of the month
                    Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dailyBalance.getDay());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

                    // If the DailyBalance corresponds to the current day, update balance amount
                    if (dayOfMonth == i) {
                        balanceAmount = (float) dailyBalance.getBalance();
                        break;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            // Add entry for balance
            balanceEntries.add(new Entry(i, balanceAmount));
        }

        // Check if balanceEntries is empty
        if (balanceEntries.isEmpty()) {
            // Clear any existing data
            balanceLineChart.clear();
            // Customize the message when there is no data
//        balanceLineChart.setNoDataText("No data available for this month");
            balanceLineChart.invalidate(); // Refresh the chart
            return;
        }

        LineDataSet balanceDataSet = new LineDataSet(balanceEntries, "Balance");
        balanceDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.yellow2));
        balanceDataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.yellow1));

        // Customize circle appearance
        balanceDataSet.setDrawCircles(true);
        balanceDataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.yellow2));
        balanceDataSet.setCircleRadius(2f);
        balanceDataSet.setDrawCircleHole(false);

        // Customize line thickness
        balanceDataSet.setLineWidth(1.5f);

        LineData lineData = new LineData(balanceDataSet);
        balanceLineChart.setData(lineData);

        // Ensure the no data message is cleared if there is data
        balanceLineChart.setNoDataText("");
        balanceLineChart.invalidate(); // Refresh the chart
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    public class EuroValueFormatter extends ValueFormatter {

        private final DecimalFormat mFormat;

        public EuroValueFormatter() {
            mFormat = new DecimalFormat("#€");
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return mFormat.format(value);
        }
    }
}
