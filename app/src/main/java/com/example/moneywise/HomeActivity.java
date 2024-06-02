package com.example.moneywise;

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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.moneywise.Fragments.ReportsFragment;
import com.example.moneywise.Fragments.ReviewFragment;
import com.example.moneywise.Fragments.SummaryFragment;
import com.example.moneywise.Fragments.TransactionsFragment;
import com.example.moneywise.Login_Signup.LoginActivity;
import com.example.moneywise.Operations.PlannedTransactionsActivity;
import com.example.moneywise.Operations.RecurringPaymentsActivity;
import com.example.moneywise.Operations.SetBudgetsActivity;
import com.example.moneywise.Transactions.AddTransactionActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    // UI Elements
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private BottomNavigationView bottomNavigationView;
    private TextView datePlaceholderText,leftPlaceholderText,rightPlaceholderText;
    private ImageView rightButton,leftButton,menuIcon;
    private Fragment defaultFragment;
    private FloatingActionButton floatingButton;
    private boolean isFragmentTransactionPending = false;
    private long lastClickTime = 0;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String userID,displayedFragment;

    // Date and Time
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    // Other
    private float totalAmount = 0.0f; // Variable to store total amount


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if user is authenticated
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You are not logged in. Redirecting to login page...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Get user ID and Firestore instance
        userID = firebaseAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // Calculate user financial details
        calculateTotalAmount();
        calculateFutureTransactionsAmount();

        // Initialize UI components
        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        leftPlaceholderText = findViewById(R.id.left_placeholder_text);
        rightPlaceholderText = findViewById(R.id.right_placeholder_text);
        datePlaceholderText = findViewById(R.id.placeholder_text);
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);
        floatingButton = findViewById(R.id.floating_button);

        // Set up date format and calendar
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MM-yyyy", Locale.getDefault());

        // Update date display and manage button visibility
        updateDateDisplay();

        // Set up date navigation buttons
        leftButton.setOnClickListener(v -> {
            changeDate(-1);
            loadCurrentFragment();
        });

        rightButton.setOnClickListener(v -> {
            changeDate(1);
            loadCurrentFragment();
        });

        // Set default fragment to TransactionsFragment
        bottomNavigationView.setSelectedItemId(R.id.menu_transactions);
        replaceFragment(new TransactionsFragment());

        // Set up navigation drawer toggle
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Set up menu icon click listener
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(findViewById(R.id.nav_view)));

        // Set up navigation view listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView userEmailTextView = headerView.findViewById(R.id.textView_email);
        userEmailTextView.setText(firebaseAuth.getCurrentUser().getEmail());
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Set up bottom navigation view listener with debounce
        bottomNavigationView.setOnItemSelectedListener(item -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 500) {
                return false; // Prevent double-click within 500ms
            }
            lastClickTime = currentTime;

            return onBottomNavigationItemSelected(item);
        });

        // Set floating button click listener
        floatingButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddTransactionActivity.class);
            intent.putExtra("USER_ID", userID);
            startActivity(intent);
        });
    }

    // Update the date display and manage right button visibility
    private void updateDateDisplay() {
        String currentDate = dateFormat.format(calendar.getTime());
        datePlaceholderText.setText(currentDate);
        Calendar today = Calendar.getInstance();
        if (dateFormat.format(today.getTime()).equals(currentDate)) {
            rightButton.setVisibility(View.GONE);
            rightButton.setEnabled(false);
        } else {
            rightButton.setVisibility(View.VISIBLE);
            rightButton.setEnabled(true);
        }
    }

    // Change the displayed date by a specified number of months
    private void changeDate(int months) {
        calendar.add(Calendar.MONTH, months);
        updateDateDisplay();
    }

    // Load the currently displayed fragment
    private void loadCurrentFragment() {
        if (displayedFragment == null) {
            // Handle the case where displayedFragment is null
            replaceFragment(new TransactionsFragment());
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.executePendingTransactions(); // Ensure all pending transactions are complete

        switch (displayedFragment) {
            case "transactions":
                replaceFragment(new TransactionsFragment());
                break;
            case "review":
                replaceFragment(new ReviewFragment());
                break;
            case "summary":
                replaceFragment(new SummaryFragment());
                break;
            case "reports":
                replaceFragment(new ReportsFragment());
                break;
            default:
                replaceFragment(new TransactionsFragment());
                break;
        }
    }

    // Replace the current fragment
    private synchronized void replaceFragment(Fragment fragment) {
        if (isFragmentTransactionPending) {
            return; // Prevent overlapping transactions
        }
        isFragmentTransactionPending = true;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commitAllowingStateLoss(); // Allow state loss to prevent crashes in some cases

        fragmentManager.executePendingTransactions(); // Ensure transaction is complete
        isFragmentTransactionPending = false;
    }

    // Handle bottom navigation item selection
    private boolean onBottomNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.menu_transactions) {
            fragment = new TransactionsFragment();
            setVisibility(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE);
            displayedFragment = "transactions";
        } else if (itemId == R.id.menu_review) {
            fragment = new ReviewFragment();
            setVisibility(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.INVISIBLE);
            displayedFragment = "review";
        } else if (itemId == R.id.menu_summary) {
            fragment = new SummaryFragment();
            setVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
            displayedFragment = "summary";
        } else if (itemId == R.id.menu_reports) {
            fragment = new ReportsFragment();
            setVisibility(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.INVISIBLE);
            displayedFragment = "reports";
        }

        if (fragment != null) {
            calendar = Calendar.getInstance();
            updateDateDisplay(); // Update date display to current date
            replaceFragment(fragment);
            return true;
        }
        return false;
    }

    // Set visibility of date-related views and floating button
    private void setVisibility(int date, int left, int right, int floating) {
        datePlaceholderText.setVisibility(date);
        leftButton.setVisibility(left);
        rightButton.setVisibility(right);
        floatingButton.setVisibility(floating);
    }

    // Handle navigation item selection
    private boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if (itemId == R.id.home_item) {
            bottomNavigationView.setSelectedItemId(R.id.menu_transactions);
        } else if (itemId == R.id.setBudget_item) {
            startActivity(new Intent(HomeActivity.this, SetBudgetsActivity.class));
        } else if (itemId == R.id.recurringPayments_item) {
            startActivity(new Intent(HomeActivity.this, RecurringPaymentsActivity.class));
        } else if (itemId == R.id.plannedTransactions_item) {
            startActivity(new Intent(HomeActivity.this, PlannedTransactionsActivity.class));
        } else if (itemId == R.id.about_item) {
            startActivity(new Intent(HomeActivity.this, AboutActivity.class));
        } else if (itemId == R.id.logout_item) {
            showLogoutPrompt();
        } else if (itemId == R.id.deleteAccount_item) {
            showDeleteAccountPrompt();
        } else if (itemId == R.id.clearDatabase_item) {
            showDeleteDatabasePrompt();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Show logout prompt
    private void showLogoutPrompt() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    finishAffinity();
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Show delete account prompt
    private void showDeleteAccountPrompt() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Deletion of Account")
                .setMessage("Continuing will result in the PERMANENT DELETION of the user and all of its data. Are you absolutely certain you want to continue?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUserAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Show delete database prompt
    private void showDeleteDatabasePrompt() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Deletion of User Data")
                .setMessage("Continuing will result in the PERMANENT DELETION of all the user data. Are you absolutely certain you want to continue?")
                .setPositiveButton("Clear", (dialog, which) -> deleteUserData())
                .setNegativeButton("Cancel", null)
                .show();
    }



    private void calculateTotalAmount() {
        // Get today's date
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String todayString = dateFormat.format(today);

        db.collection("transactions")
                .whereEqualTo("userId", userID)
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
                                    // Compare the transaction date with today's date
                                    if (transactionDate.before(today) || dateString.equals(todayString)) {
                                        float amount = Float.parseFloat(amountStr);
                                        if (type.equals("Expense")) {
                                            totalExpense -= amount;
                                        } else if (type.equals("Income")) {
                                            totalIncome += amount;
                                        }
                                    }
                                }
                            } catch (NumberFormatException | ParseException e) {
                                // Handle the case where parsing fails or the amount is not a valid number
                                Toast.makeText(HomeActivity.this, "Invalid transaction data", Toast.LENGTH_SHORT).show();
                            }
                        }
                        totalAmount = totalIncome + totalExpense;

                        // Format the total amount string to display only three numbers after the decimal point
                        String totalAmountText = String.format(Locale.getDefault(), "Balance: %.2f€", totalAmount);
                        leftPlaceholderText.setText(totalAmountText);
                    } else {
                        Toast.makeText(HomeActivity.this, "Failed to retrieve transactions", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void calculateFutureTransactionsAmount() {
        // Get the current day
        Date currentDay = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String currentDayString = dateFormat.format(currentDay);

        // Get the current month and year
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        // Query the "transactions" collection in the database for transactions with the specified userId
        db.collection("transactions")
                .whereEqualTo("userId", userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        float futureExpense = 0.0f;
                        float futureIncome = 0.0f;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String amountStr = document.getString("amount"); // Assuming "amount" field is a string
                                String type = document.getString("type"); // Assuming "type" field indicates Expense or Income
                                String dateString = document.getString("date"); // Assuming "date" field stores the transaction date

                                // Parse the transaction date
                                Date transactionDate = dateFormat.parse(dateString);

                                // Compare transaction date with the current day and check if it is in the current month and year
                                if (amountStr != null && type != null && dateString != null && transactionDate != null) {
                                    Calendar transactionCalendar = Calendar.getInstance();
                                    transactionCalendar.setTime(transactionDate);

                                    int transactionMonth = transactionCalendar.get(Calendar.MONTH);
                                    int transactionYear = transactionCalendar.get(Calendar.YEAR);

                                    if (transactionDate.after(currentDay) && transactionMonth == currentMonth && transactionYear == currentYear) {
                                        float amount = Float.parseFloat(amountStr);
                                        if (type.equals("Expense")) {
                                            futureExpense -= amount;
                                        } else if (type.equals("Income")) {
                                            futureIncome += amount;
                                        }
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                                Toast.makeText(HomeActivity.this, "Invalid future transaction data", Toast.LENGTH_SHORT).show();
                            }
                        }
                        float futureAmount = futureIncome + futureExpense;

                        // Format the total amount string to display only two numbers after the decimal point
                        String totalFutureText = String.format(Locale.getDefault(), "Total: %.2f€", futureAmount);
                        rightPlaceholderText.setText(totalFutureText);
                    } else {
                        // Handle errors
                        Toast.makeText(HomeActivity.this, "Failed to fetch transactions", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set the default fragment to TransactionsFragment
        defaultFragment = new TransactionsFragment();
        replaceFragment(defaultFragment);
        // Set the selected item in the bottom navigation view to Transactions
        bottomNavigationView.setSelectedItemId(R.id.menu_transactions);

        // Recalculate amounts in case of any changes while the activity was paused
        calculateTotalAmount();
        calculateFutureTransactionsAmount();
    }

    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit the app? You will also be logged out.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Log out from the Firebase authentication account
                    FirebaseAuth.getInstance().signOut();
                    // Close all activities
                    finishAffinity();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public String getCurrentDate() {
        return datePlaceholderText.getText().toString(); // Assuming placeholderText is a TextView
    }

    private void deleteUserAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // User is not authenticated
            new MaterialAlertDialogBuilder(HomeActivity.this)
                    .setTitle("Reauthentication Required")
                    .setMessage("Your session has expired. Please log in again to delete your account.")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // Navigate to login screen
                        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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
                new MaterialAlertDialogBuilder(HomeActivity.this)
                        .setTitle("Reauthentication Required")
                        .setMessage("For security reasons, please log in again to delete your account.")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            // Navigate to login screen
                            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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
                                    Toast.makeText(HomeActivity.this, "User account deleted successfully", Toast.LENGTH_SHORT).show();

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
                                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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
                .whereEqualTo("userId", userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete each recurring payment document
                            db.collection("recurrings").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Recurring payment document deleted successfully");
                                        // Handle success
                                        replaceFragment(defaultFragment);
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
                .whereEqualTo("userId", userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete each transaction document
                            db.collection("transactions").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Transaction document deleted successfully");
                                        replaceFragment(defaultFragment);
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
                .whereEqualTo("userId", userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete each budget document
                            db.collection("budgets").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Budget document deleted successfully");
                                        replaceFragment(defaultFragment);
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
        Toast.makeText(HomeActivity.this, "User data deleted successfully", Toast.LENGTH_SHORT).show();
    }
}
