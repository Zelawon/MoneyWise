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
import com.example.moneywise.Adapters.RecurringAdapter;
import com.example.moneywise.HomeActivity;
import com.example.moneywise.Login_Signup.LoginActivity;
import com.example.moneywise.Models.Recurring;
import com.example.moneywise.Models.Transaction;
import com.example.moneywise.R;
import com.example.moneywise.Transactions.AddTransactionActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RecurringPaymentsActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FloatingActionButton floatingButton;
    private ImageView menuIcon;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private RecurringAdapter adapter;
    private List<Recurring> recurringList;
    private String userId;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_payments);

        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        floatingButton = findViewById(R.id.floating_button);
        recyclerView = findViewById(R.id.recurringRecyclerView);

        // Initialize Firestore and the recurring list
        db = FirebaseFirestore.getInstance();
        recurringList = new ArrayList<>();

        // Initialize RecyclerView and adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecurringAdapter(this, recurringList);
        recyclerView.setAdapter(adapter);

        // Retrieve authenticated user ID
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : null;

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
                    startActivity(new Intent(RecurringPaymentsActivity.this, HomeActivity.class));
                    finishAffinity();
                } else if (itemId == R.id.setBudget_item) {
                    startActivity(new Intent(RecurringPaymentsActivity.this, SetBudgetsActivity.class));
                    finish();
                } else if (itemId == R.id.recurringPayments_item) {
                    drawerLayout.closeDrawer(GravityCompat.START);// Close the drawer
                } else if (itemId == R.id.plannedTransactions_item) {
                    startActivity(new Intent(RecurringPaymentsActivity.this, PlannedTransactionsActivity.class));
                    finish();
                } else if (itemId == R.id.about_item) {
                    startActivity(new Intent(RecurringPaymentsActivity.this, AboutActivity.class));
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
                                startActivity(new Intent(RecurringPaymentsActivity.this, LoginActivity.class));
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
                Intent intent = new Intent(RecurringPaymentsActivity.this, AddTransactionActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });

        // Set long press listener for the adapter
        adapter.setOnItemLongPressListener(new RecurringAdapter.OnItemLongPressListener() {
            @Override
            public void onItemLongPressed(Recurring recurring) {
                // Show confirmation dialog for deletion
                showDeleteConfirmationDialog(recurring);
                Log.e(TAG, "Recurring Transaction ID: " + recurring.getTransactionId());
            }
        });

        // Load data from Firestore
        loadDataFromFirestore();
    }
    private void deleteUserAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // User is not authenticated
            new MaterialAlertDialogBuilder(RecurringPaymentsActivity.this)
                    .setTitle("Reauthentication Required")
                    .setMessage("Your session has expired. Please log in again to delete your account.")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // Navigate to login screen
                        startActivity(new Intent(RecurringPaymentsActivity.this, LoginActivity.class));
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
                new MaterialAlertDialogBuilder(RecurringPaymentsActivity.this)
                        .setTitle("Reauthentication Required")
                        .setMessage("For security reasons, please log in again to delete your account.")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            // Navigate to login screen
                            startActivity(new Intent(RecurringPaymentsActivity.this, LoginActivity.class));
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
                                    Toast.makeText(RecurringPaymentsActivity.this, "User account deleted successfully", Toast.LENGTH_SHORT).show();

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
                                    startActivity(new Intent(RecurringPaymentsActivity.this, LoginActivity.class));
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
                                        startActivity(new Intent(RecurringPaymentsActivity.this, HomeActivity.class));
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
                                        startActivity(new Intent(RecurringPaymentsActivity.this, HomeActivity.class));
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
                                        startActivity(new Intent(RecurringPaymentsActivity.this, HomeActivity.class));
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
        Toast.makeText(RecurringPaymentsActivity.this, "User data deleted successfully", Toast.LENGTH_SHORT).show();
    }

    // Method to show delete confirmation dialog
    private void showDeleteConfirmationDialog(final Recurring recurring) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Warning: Confirm Deletion")
                .setMessage("Continuing will result in the PERMANENT DELETION of all transactions AFTER today's date. Are you absolutely certain you want to continue?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete the recurring payment
                        Log.e(TAG, "Recurring Transaction ID: " + recurring.getTransactionId());
                        deleteRecurringTransaction(recurring.getTransactionId(),recurring.getDate());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Method to delete recurring payment from Firestore
    private void deleteRecurringTransaction(String transactionId, String date) {
        db.collection("recurrings")
                .whereEqualTo("transactionId", transactionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete the document with the provided transaction ID
                            db.collection("recurrings").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.e(TAG, "-------------------deleted-------------------");
                                        // Transaction deleted successfully
                                        Toast.makeText(RecurringPaymentsActivity.this, "Recurring transaction deleted successfully", Toast.LENGTH_SHORT).show();
                                        //Reload recyclerview
                                        loadDataFromFirestore();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle errors
                                        Log.e(TAG, "Error deleting recurring transaction: " + e.getMessage(), e);
                                        Toast.makeText(RecurringPaymentsActivity.this, "Failed to delete recurring transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                        Log.e(TAG, "-------------------transactions-------------------");
                        // Get current date
                        Calendar calendar = Calendar.getInstance();
                        // Add one day
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        Date nextDay = calendar.getTime();
                        // Format dates
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        String nextDayStr = sdf.format(nextDay);
                        getTransactionsAndDelete(transactionId,nextDayStr);

                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(RecurringPaymentsActivity.this, "Error getting documents: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to delete the transaction from Firestore
    public void getTransactionsAndDelete(String transactionId, String date) {
        db.collection("transactions")
                .whereEqualTo("transactionId", transactionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Transaction> transactionList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Convert each document to a Transaction object
                            Transaction transaction = document.toObject(Transaction.class);
                            // Add the transaction to the list
                            transactionList.add(transaction);
                            Log.e(TAG, "item added to list " + transaction.getDate());
                        }
                        // Filter transactions by date
                        List<Transaction> filteredTransactions = filterTransactionsByDate(transactionList, date);
                        Log.e(TAG, "list filterd");
                        // Delete transactions from the database
                        deleteTransactions(filteredTransactions);

                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                        // Handle error
                    }
                });
    }

    // Method to filter transactions by date
    private List<Transaction> filterTransactionsByDate(List<Transaction> transactionList, String date) {
        List<Transaction> filteredTransactions = new ArrayList<>();

        // Parse input date string
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date inputDate = null;
        try {
            inputDate = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return filteredTransactions; // return an empty list if parsing fails
        }

        // Filter transactions
        for (Transaction transaction : transactionList) {
            Log.e(TAG, "filtering " + transaction.getDate() + " " + date);
            try {
                // Parse transaction date string
                Date transactionDate = sdf.parse(transaction.getDate());

                // Compare dates
                if (transactionDate != null && !transactionDate.before(inputDate)) {
                    filteredTransactions.add(transaction);
                    Log.e(TAG, "transaction with date found " + transaction.getDate() + " " + date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                // Handle parsing error for transaction date string
            }
        }
        return filteredTransactions;
    }


    private void deleteTransactions(List<Transaction> transactionsToDelete) {
        for (Transaction transaction : transactionsToDelete) {
            db.collection("transactions")
                    .whereEqualTo("transactionId", transaction.getTransactionId())
                    .whereEqualTo("date", transaction.getDate())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Delete the document
                                document.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Transaction deleted successfully
                                            Log.e(TAG, "Transaction deleted successfully");
                                            // Handle any additional logic here
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle errors
                                            Log.e(TAG, "Error deleting transaction: " + e.getMessage(), e);
                                            // Handle any additional error logic here
                                        });
                            }
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                            // Handle error
                        }
                    });
        }
    }


    private void loadDataFromFirestore() {
        if (userId != null) {
            db.collection("recurrings")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            recurringList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Recurring recurring = document.toObject(Recurring.class);
                                recurringList.add(recurring);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            // Handle possible errors
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Retrieve transactions again when the activity resumes
        loadDataFromFirestore();
    }
}