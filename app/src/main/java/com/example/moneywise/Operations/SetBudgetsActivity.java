package com.example.moneywise.Operations;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.moneywise.AboutActivity;
import com.example.moneywise.HomeActivity;
import com.example.moneywise.Login_Signup.LoginActivity;
import com.example.moneywise.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SetBudgetsActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageView menuIcon;
    private TextView dialogTitle;
    private boolean firstKeypress = true;
    private boolean commaAdded = false;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budgets);

        db = FirebaseFirestore.getInstance();
        // Retrieve authenticated user ID
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : null;

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
                    startActivity(new Intent(SetBudgetsActivity.this, HomeActivity.class));
                    finishAffinity();
                } else if (itemId == R.id.setBudget_item) {
                    drawerLayout.closeDrawer(GravityCompat.START);// Close the drawer
                } else if (itemId == R.id.recurringPayments_item) {
                    startActivity(new Intent(SetBudgetsActivity.this, RecurringPaymentsActivity.class));
                    finish();
                } else if (itemId == R.id.plannedTransactions_item) {
                    startActivity(new Intent(SetBudgetsActivity.this, PlannedTransactionsActivity.class));
                    finish();
                } else if (itemId == R.id.about_item) {
                    startActivity(new Intent(SetBudgetsActivity.this, AboutActivity.class));
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
                                startActivity(new Intent(SetBudgetsActivity.this, LoginActivity.class));
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        // Array of all EditText IDs and corresponding category names
        int[] editTextIds = {
                R.id.budget_amount_food,
                R.id.budget_amount_entertainment,
                R.id.budget_amount_transportation,
                R.id.budget_amount_home,
                R.id.budget_amount_clothing,
                R.id.budget_amount_car,
                R.id.budget_amount_electronics,
                R.id.budget_amount_health_and_beauty,
                R.id.budget_amount_education,
                R.id.budget_amount_children,
                R.id.budget_amount_work,
                R.id.budget_amount_bureaucracy,
                R.id.budget_amount_gifts,
                R.id.budget_amount_bank
        };

        String[] categoryNames = {
                "Food",
                "Entertainment",
                "Transportation",
                "Home",
                "Clothing",
                "Car",
                "Electronics",
                "Health and Beauty",
                "Education",
                "Children",
                "Work",
                "Bureaucracy",
                "Gifts",
                "Bank"
        };

        // Retrieve and set stored values for each EditText
        for (int i = 0; i < editTextIds.length; i++) {
            EditText editText = findViewById(editTextIds[i]);
            String categoryName = categoryNames[i];

            // Method to retrieve stored values and set them to the EditText
            getStoredValue(categoryName, editText);

            // Set onClickListener for each EditText
            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showInputDialog((EditText) v);
                }
            });
        }
    }

    // Method to retrieve stored values or return 0 if empty
    private void getStoredValue(String categoryName, EditText editText) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("budgets").document(userId);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document exists, get the value for the specific category
                        String value = document.getString(categoryName);
                        if (value != null) {
                            editText.setText(value);
                        } else {
                            editText.setText("0.0");
                        }
                    } else {
                        // Document does not exist, set default value
                        editText.setText("0.0");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    // Handle the error, set default value
                    editText.setText("0.0");
                }
            }
        });
    }


    // Method to save entered values
    private void saveEnteredValue(String categoryName, float value) {
        // Create a reference to the Firestore collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("budgets").document(userId);

        // Get the current document from Firestore
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document exists, update only the specific category value
                        docRef.update(categoryName, String.valueOf(value))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Category value successfully updated!");
                                        // You can show a success message if needed
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Error updating category value: " + e.getMessage());
                                        // Handle the error
                                    }
                                });
                    } else {
                        // Document does not exist, create it with initial values
                        Map<String, Object> budgetData = new HashMap<>();
                        budgetData.put("userId", userId);
                        budgetData.put("Food", "0.0");
                        budgetData.put("Entertainment", "0.0");
                        budgetData.put("Transportation", "0.0");
                        budgetData.put("Home", "0.0");
                        budgetData.put("Clothing", "0.0");
                        budgetData.put("Car", "0.0");
                        budgetData.put("Electronics", "0.0");
                        budgetData.put("Health and Beauty", "0.0");
                        budgetData.put("Education", "0.0");
                        budgetData.put("Children", "0.0");
                        budgetData.put("Work", "0.0");
                        budgetData.put("Bureaucracy", "0.0");
                        budgetData.put("Gifts", "0.0");
                        budgetData.put("Bank", "0.0");

                        // Set the value for the specific category
                        budgetData.put(categoryName, String.valueOf(value));

                        // Create the document with the initial values
                        docRef.set(budgetData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Document successfully created with initial values!");
                                        // You can show a success message if needed
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Error creating document: " + e.getMessage());
                                        // Handle the error
                                    }
                                });
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    // Handle the error
                }
            }
        });
    }

    private void showInputDialog(final EditText editText) {
        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(SetBudgetsActivity.this).inflate(R.layout.dialog_number_keyboard, null);

        // Initialize views inside the dialog
        dialogTitle = dialogView.findViewById(R.id.dialog_amount_title);
        Button button7 = dialogView.findViewById(R.id.button_7);
        Button button8 = dialogView.findViewById(R.id.button_8);
        Button button9 = dialogView.findViewById(R.id.button_9);
        Button button4 = dialogView.findViewById(R.id.button_4);
        Button button5 = dialogView.findViewById(R.id.button_5);
        Button button6 = dialogView.findViewById(R.id.button_6);
        Button button1 = dialogView.findViewById(R.id.button_1);
        Button button2 = dialogView.findViewById(R.id.button_2);
        Button button3 = dialogView.findViewById(R.id.button_3);
        Button buttonComma = dialogView.findViewById(R.id.button_commma);
        Button button0 = dialogView.findViewById(R.id.button_0);
        Button buttonDelete = dialogView.findViewById(R.id.button_delete);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);
        Button buttonOK = dialogView.findViewById(R.id.button_ok);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(SetBudgetsActivity.this);
        builder.setView(dialogView);

        // Handle button clicks inside the dialog
        AlertDialog dialog = builder.create();

        // Reset firstKeypress and comma flag when dialog is shown
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                firstKeypress = true;
                commaAdded = false;
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = ((Button) v).getText().toString();
                if (firstKeypress) {
                    dialogTitle.setText(""); // Clear the amount on first key press
                    firstKeypress = false;
                }
                if (buttonText.equals(".") && commaAdded) {
                    return; // If comma is already added, do nothing
                }
                if (buttonText.equals(".")) {
                    commaAdded = true; // Mark comma as added
                }
                dialogTitle.append(buttonText);
            }
        };

        button7.setOnClickListener(onClickListener);
        button8.setOnClickListener(onClickListener);
        button9.setOnClickListener(onClickListener);
        button4.setOnClickListener(onClickListener);
        button5.setOnClickListener(onClickListener);
        button6.setOnClickListener(onClickListener);
        button1.setOnClickListener(onClickListener);
        button2.setOnClickListener(onClickListener);
        button3.setOnClickListener(onClickListener);
        button0.setOnClickListener(onClickListener);
        buttonComma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstKeypress) {
                    dialogTitle.setText("0"); // Add "0" before comma if it's the first key press
                    firstKeypress = false;
                }
                if (!commaAdded) {
                    dialogTitle.append(".");
                    commaAdded = true;
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogTitle != null) {
                    String currentText = dialogTitle.getText().toString();
                    if (!currentText.isEmpty()) {
                        String newText = currentText.substring(0, currentText.length() - 1);
                        dialogTitle.setText(newText);
                        commaAdded = newText.contains(".");
                    }
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Handle OK button click
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = dialogTitle.getText().toString().trim();
                if (amount.isEmpty() || amount.equals("0.") || amount.equals("0.0") || amount.matches("0.0+")) {
                    amount = "0";
                }
                if (amount.startsWith("0") && !amount.startsWith("0.")) {
                    amount = amount.substring(1);
                }
                try {
                    float amountValue = Float.parseFloat(amount);
                    editText.setText(String.valueOf(amountValue));

                    // Save the amount in SharedPreferences
                    String categoryName = getCategoryName(editText.getId());
                    saveEnteredValue(categoryName, amountValue);
                } catch (NumberFormatException e) {
                    editText.setText("0");
                }
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }

    private String getCategoryName(int editTextId) {
        if (editTextId == R.id.budget_amount_food) {
            return "Food";
        } else if (editTextId == R.id.budget_amount_entertainment) {
            return "Entertainment";
        } else if (editTextId == R.id.budget_amount_transportation) {
            return "Transportation";
        } else if (editTextId == R.id.budget_amount_home) {
            return "Home";
        } else if (editTextId == R.id.budget_amount_clothing) {
            return "Clothing";
        } else if (editTextId == R.id.budget_amount_car) {
            return "Car";
        } else if (editTextId == R.id.budget_amount_electronics) {
            return "Electronics";
        } else if (editTextId == R.id.budget_amount_health_and_beauty) {
            return "Health and Beauty";
        } else if (editTextId == R.id.budget_amount_education) {
            return "Education";
        } else if (editTextId == R.id.budget_amount_children) {
            return "Children";
        } else if (editTextId == R.id.budget_amount_work) {
            return "Work";
        } else if (editTextId == R.id.budget_amount_bureaucracy) {
            return "Bureaucracy";
        } else if (editTextId == R.id.budget_amount_gifts) {
            return "Gifts";
        } else if (editTextId == R.id.budget_amount_bank) {
            return "Bank";
        } else {
            return "";
        }
    }

    private void deleteUserAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // User is not authenticated
            new MaterialAlertDialogBuilder(SetBudgetsActivity.this)
                    .setTitle("Reauthentication Required")
                    .setMessage("Your session has expired. Please log in again to delete your account.")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // Navigate to login screen
                        startActivity(new Intent(SetBudgetsActivity.this, LoginActivity.class));
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
                new MaterialAlertDialogBuilder(SetBudgetsActivity.this)
                        .setTitle("Reauthentication Required")
                        .setMessage("For security reasons, please log in again to delete your account.")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            // Navigate to login screen
                            startActivity(new Intent(SetBudgetsActivity.this, LoginActivity.class));
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
                                    Toast.makeText(SetBudgetsActivity.this, "User account deleted successfully", Toast.LENGTH_SHORT).show();

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
                                    startActivity(new Intent(SetBudgetsActivity.this, LoginActivity.class));
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
                                        startActivity(new Intent(SetBudgetsActivity.this, HomeActivity.class));
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
                                        startActivity(new Intent(SetBudgetsActivity.this, HomeActivity.class));
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
                                        startActivity(new Intent(SetBudgetsActivity.this, HomeActivity.class));
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
        Toast.makeText(SetBudgetsActivity.this, "User data deleted successfully", Toast.LENGTH_SHORT).show();
    }

}
