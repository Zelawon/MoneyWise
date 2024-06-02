package com.example.moneywise.Operations;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneywise.AboutActivity;
import com.example.moneywise.Adapters.TransactionAdapter;
import com.example.moneywise.HomeActivity;
import com.example.moneywise.Login_Signup.LoginActivity;
import com.example.moneywise.Models.Transaction;
import com.example.moneywise.R;
import com.example.moneywise.Transactions.AddTransactionActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
import java.util.concurrent.TimeUnit;

public class PlannedTransactionsActivity extends AppCompatActivity {

    private FloatingActionButton floatingButton;
    private TextView amountTextView, monthYearTextView;
    private ImageView menuIcon, leftArrowImageView, rightArrowImageView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
    private String userId;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planned_transactions);

        // Initialize views
        floatingButton = findViewById(R.id.floating_button);
        amountTextView = findViewById(R.id.amount_textview);
        monthYearTextView = findViewById(R.id.month_year_textview);
        menuIcon = findViewById(R.id.menu_icon);
        leftArrowImageView = findViewById(R.id.leftArrow_image_view);
        rightArrowImageView = findViewById(R.id.rightArrow_image_view);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.plannedTransactionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this, transactionList);
        recyclerView.setAdapter(adapter);

        // Retrieve authenticated user ID
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : null;

        // Get the current date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
        String formattedDate = dateFormatter.format(calendar.getTime());

        //Set the current date textview
        monthYearTextView.setText(formattedDate);

        // Hide leftArrowImageView if the monthYearTextView is the same as the current month and year
        if (formattedDate.equals(dateFormatter.format(new Date()))) {
            leftArrowImageView.setVisibility(View.GONE);
        }

        retrieveTransactions(formattedDate, userId);

        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);

        // Toggle button for drawer
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Setup click listener for menu icon
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(findViewById(R.id.nav_view));
            }
        });

        // Navigation View listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView userEmailTextView = headerView.findViewById(R.id.textView_email);
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        userEmailTextView.setText(email);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.home_item) {
                    startActivity(new Intent(PlannedTransactionsActivity.this, HomeActivity.class));
                    finishAffinity();
                } else if (itemId == R.id.setBudget_item) {
                    startActivity(new Intent(PlannedTransactionsActivity.this, SetBudgetsActivity.class));
                    finish();
                } else if (itemId == R.id.recurringPayments_item) {
                    startActivity(new Intent(PlannedTransactionsActivity.this, RecurringPaymentsActivity.class));
                    finish();
                } else if (itemId == R.id.plannedTransactions_item) {
                    drawerLayout.closeDrawer(GravityCompat.START);// Close the drawer
                } else if (itemId == R.id.about_item) {
                    startActivity(new Intent(PlannedTransactionsActivity.this, AboutActivity.class));
                    finish();
                } else if (itemId == R.id.logout_item) {
                    Context context = drawerLayout.getContext();
                    showLogoutPrompt(context);
                }else if (itemId == R.id.deleteAccount_item) {
                    Context context = drawerLayout.getContext();
                    showDeleteAccountPrompt(context);
                }else if (itemId == R.id.clearDatabase_item) {
                    Context context = drawerLayout.getContext();
                    showDeleteDatabasePrompt(context);
                }

                // Close the drawer after selecting an item
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            private void showDeleteAccountPrompt(Context context) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Deletion of Account")
                        .setMessage("Continuing will result in the PERMANENT DELETION of the user and all of its data. Are you absolutely certain you want to continue?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteUserAccount();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            private void showDeleteDatabasePrompt(Context context) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Deletion of User Data")
                        .setMessage("Continuing will result in the PERMANENT DELETION of all the user data. Are you absolutely certain you want to continue?")
                        .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteUserData();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            private void showLogoutPrompt(Context context) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Log out from the Firebase authentication account
                                FirebaseAuth.getInstance().signOut();
                                finishAffinity();
                                startActivity(new Intent(PlannedTransactionsActivity.this, LoginActivity.class));
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });


        // Set click listener for the floating action button
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the AddTransactionActivity and pass the user ID
                Intent intent = new Intent(PlannedTransactionsActivity.this, AddTransactionActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });

        // Set click listener for left arrow
        leftArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current month and year from monthYearTextView
                String currentMonthYear = monthYearTextView.getText().toString();
                // Parse the current month and year
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
                Calendar currentCalendar = Calendar.getInstance();
                try {
                    currentCalendar.setTime(dateFormat.parse(currentMonthYear));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // Subtract one month
                currentCalendar.add(Calendar.MONTH, -1);
                // Update monthYearTextView
                monthYearTextView.setText(dateFormat.format(currentCalendar.getTime()));

                // Retrieve transactions for the updated month and year
                retrieveTransactions(dateFormat.format(currentCalendar.getTime()), userId);

                // Hide leftArrowImageView if the monthYearTextView is the same as the current month and year
                if (dateFormat.format(currentCalendar.getTime()).equals(dateFormatter.format(new Date()))) {
                    leftArrowImageView.setVisibility(View.GONE);
                }
            }
        });

        // Set click listener for right arrow
        rightArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current month and year from monthYearTextView
                String currentMonthYear = monthYearTextView.getText().toString();
                // Parse the current month and year
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
                Calendar currentCalendar = Calendar.getInstance();
                try {
                    currentCalendar.setTime(dateFormat.parse(currentMonthYear));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // Add one month
                currentCalendar.add(Calendar.MONTH, 1);
                // Update monthYearTextView
                monthYearTextView.setText(dateFormat.format(currentCalendar.getTime()));

                // Retrieve transactions for the updated month and year
                retrieveTransactions(dateFormat.format(currentCalendar.getTime()), userId);

                // Show leftArrowImageView when navigating away from the current month and year
                leftArrowImageView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void deleteUserAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // User is not authenticated
            new MaterialAlertDialogBuilder(PlannedTransactionsActivity.this)
                    .setTitle("Reauthentication Required")
                    .setMessage("Your session has expired. Please log in again to delete your account.")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // Navigate to login screen
                        startActivity(new Intent(PlannedTransactionsActivity.this, LoginActivity.class));
                        finish();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;
        }

        // Check if the user's authentication token is recent enough
        if (user.getMetadata() != null && user.getMetadata().getLastSignInTimestamp() != 0) {
            long lastSignInTimestamp = user.getMetadata().getLastSignInTimestamp();
            long currentTime = System.currentTimeMillis();
            long signInTimeDifference = currentTime - lastSignInTimestamp;
            long signInTimeThreshold = TimeUnit.MINUTES.toMillis(5); // Adjusted to 5 minutes

            // Ensure getLastSignInTimestamp() is not zero before performing the comparison
            if (signInTimeDifference >= 0 && signInTimeDifference >= signInTimeThreshold) {
                // User needs to reauthenticate because the authentication is not recent enough
                new MaterialAlertDialogBuilder(PlannedTransactionsActivity.this)
                        .setTitle("Reauthentication Required")
                        .setMessage("For security reasons, please log in again to delete your account.")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            // Navigate to login screen
                            startActivity(new Intent(PlannedTransactionsActivity.this, LoginActivity.class));
                            finish();
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return;
            }
        }

        // Force token refresh
        user.getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Token refreshed successfully, proceed with account deletion
                        user.delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Account deleted successfully
                                    Log.d(TAG, "User account deleted successfully");
                                    Toast.makeText(PlannedTransactionsActivity.this, "User account deleted successfully", Toast.LENGTH_SHORT).show();

                                    // Your Firestore cleanup code goes here
                                    String userId = user.getUid();

                                    // Delete user data from Firestore collections...
                                    db.collection("recurrings")
                                            .whereEqualTo("userId", userId)
                                            .get()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task1.getResult()) {
                                                        db.collection("recurrings").document(document.getId()).delete();
                                                    }
                                                } else {
                                                    Log.e(TAG, "Error getting recurring payment documents: ", task1.getException());
                                                    // Handle error
                                                }
                                            });

                                    db.collection("transactions")
                                            .whereEqualTo("userId", userId)
                                            .get()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task1.getResult()) {
                                                        db.collection("transactions").document(document.getId()).delete();
                                                    }
                                                } else {
                                                    Log.e(TAG, "Error getting transaction documents: ", task1.getException());
                                                    // Handle error
                                                }
                                            });

                                    db.collection("budgets")
                                            .whereEqualTo("userId", userId)
                                            .get()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task1.getResult()) {
                                                        db.collection("budgets").document(document.getId()).delete();
                                                    }
                                                } else {
                                                    Log.e(TAG, "Error getting budget documents: ", task1.getException());
                                                }
                                            });

                                    // Navigate to login screen after account deletion
                                    finishAffinity();
                                    startActivity(new Intent(PlannedTransactionsActivity.this, LoginActivity.class));
                                })
                                .addOnFailureListener(e -> {
                                    // Failed to delete user account
                                    Log.e(TAG, "Error deleting user account: " + e.getMessage(), e);
                                    // Handle failure
                                });
                    } else {
                        // Token refresh failed
                        Exception exception = task.getException();
                        Log.e(TAG, "Token refresh failed: " + exception.getMessage(), exception);
                        // Handle failure
                    }
                });
    }

    private void deleteUserData() {
        // Delete user data from 'recurrings' collection
        db.collection("recurrings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete each recurring payment document
                            db.collection("recurrings").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Recurring payment document deleted successfully");
                                        finishAffinity();
                                        startActivity(new Intent(PlannedTransactionsActivity.this, HomeActivity.class));
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error deleting recurring payment document: " + e.getMessage(), e);
                                        // Handle failure
                                    });
                        }
                    } else {
                        Log.e(TAG, "Error getting recurring payment documents: ", task.getException());
                        // Handle error
                    }
                });

        // Delete user data from 'transactions' collection
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete each transaction document
                            db.collection("transactions").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Transaction document deleted successfully");
                                        finishAffinity();
                                        startActivity(new Intent(PlannedTransactionsActivity.this, HomeActivity.class));
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error deleting transaction document: " + e.getMessage(), e);
                                        // Handle failure
                                    });
                        }
                    } else {
                        Log.e(TAG, "Error getting transaction documents: ", task.getException());
                        // Handle error
                    }
                });

        // Delete user data from 'budgets' collection
        db.collection("budgets")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete each budget document
                            db.collection("budgets").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Budget document deleted successfully");
                                        finishAffinity();
                                        startActivity(new Intent(PlannedTransactionsActivity.this, HomeActivity.class));
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error deleting budget document: " + e.getMessage(), e);
                                    });
                        }
                    } else {
                        Log.e(TAG, "Error getting budget documents: ", task.getException());
                    }
                });
        // Notify user and navigate to HomeActivity after deletion
        Toast.makeText(PlannedTransactionsActivity.this, "User data deleted successfully", Toast.LENGTH_SHORT).show();
    }

    private void retrieveTransactions(String currentMonthYear, String userId) {
        // Get today's date
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        Date todayDate = todayCal.getTime();

        // Parse the current month and year
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
        Calendar currentCalendar = Calendar.getInstance();
        try {
            currentCalendar.setTime(dateFormat.parse(currentMonthYear));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Get the real-life month and year
        int realMonth = todayCal.get(Calendar.MONTH) + 1; // Adding 1 because Calendar.MONTH is zero-based
        int realYear = todayCal.get(Calendar.YEAR);

        // Check if the current month and year is equal to today's real-life month and year
        boolean isCurrentMonthYearCurrentMonth = currentCalendar.get(Calendar.MONTH) + 1 == realMonth &&
                currentCalendar.get(Calendar.YEAR) == realYear;

        // Query the "transactions" collection in the database for transactions with the specified userId
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Initialize total income and total expense variables
                            float totalIncome = 0.0f;
                            float totalExpense = 0.0f;
                            // If the query is successful, initialize a list to store transactions
                            transactionList = new ArrayList<>();
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
                                    if (isCurrentMonthYearCurrentMonth) {
                                        // If it's the current month and year
                                        // Display transactions occurring after today's date until the end of the current real-life month
                                        if (date.after(todayDate) && date.getMonth() + 1 == realMonth && date.getYear() + 1900 == realYear) {
                                            transactionList.add(transaction);
                                            // Update total amount based on transaction type
                                            if (transaction.getType().equals("Income")) {
                                                totalIncome += Float.parseFloat(transaction.getAmount());
                                            } else if (transaction.getType().equals("Expense")) {
                                                totalExpense += Float.parseFloat(transaction.getAmount());
                                            }
                                        }
                                    } else {
                                        // If it's a future month, don't display transactions
                                        // If it's not the current month and year, display transactions for that specific month and year
                                        if (date.getMonth() + 1 == currentCalendar.get(Calendar.MONTH) + 1 && date.getYear() + 1900 == currentCalendar.get(Calendar.YEAR)) {
                                            transactionList.add(transaction);
                                            // Update total amount based on transaction type
                                            if (transaction.getType().equals("Income")) {
                                                totalIncome += Float.parseFloat(transaction.getAmount());
                                            } else if (transaction.getType().equals("Expense")) {
                                                totalExpense += Float.parseFloat(transaction.getAmount());
                                            }
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

                            // Calculate the net amount
                            float netAmount = totalIncome - totalExpense;

                            /// Format the net amount based on whether it's positive or negative
                            String formattedAmount;
                            if (netAmount >= 0) {
                                formattedAmount = String.format(Locale.getDefault(), "+%.2f€", netAmount);
                            } else {
                                formattedAmount = String.format(Locale.getDefault(), "%.2f€", netAmount);
                            }

                            // Set the formatted net amount to amountTextView
                            amountTextView.setText(formattedAmount);

                        } else {
                            // Handle errors
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Retrieve transactions again when the activity resumes
        retrieveTransactions(monthYearTextView.getText().toString(), userId);
    }
}
