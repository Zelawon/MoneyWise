package com.example.moneywise.Transactions;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moneywise.Models.Transaction;
import com.example.moneywise.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ModifyTransactionActivity extends AppCompatActivity {

    // Declare view variables
    private TextView amountTextView, categoryTextView, subCategoryTextView, titleTextView, dateTextView, notesTextView,
            frequencyTextView, endDateTextView,dialogTitle;
    private LinearLayout subCategoryLayout, frequencyLayout, endDateLayout;

    private RadioGroup radioGroup;
    private RadioButton radioExpense, radioIncome;
    private MaterialSwitch recurringSwitch;
    private ImageView backIcon,deleteIcon;
    Button okTransactionButton;

    private FirebaseFirestore db;
    private static final String TAG = "ModifyTransactionActivity";
    private static final int CATEGORY_REQUEST_CODE = 10;
    private static final int SUB_CATEGORY_REQUEST_CODE = 20;
    private String type = "Expense";
    private String userId ="", dateOnOpen="";
    private MaterialDatePicker<Long> materialDatePicker;
    private boolean firstKeypress = true;
    private boolean commaAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_transaction);

        // Initialize views
        amountTextView = findViewById(R.id.amount);
        categoryTextView = findViewById(R.id.category_text_view);
        subCategoryTextView = findViewById(R.id.sub_category_text_view);
        subCategoryLayout = findViewById(R.id.sub_category_layout);
        radioGroup = findViewById(R.id.radioGroup);
        radioExpense = findViewById(R.id.radio_expense);
        radioIncome = findViewById(R.id.radio_income);
        titleTextView = findViewById(R.id.transaction_title_text_view);
        dateTextView = findViewById(R.id.date_picker_text_view);
        notesTextView = findViewById(R.id.notes_text_view);
        recurringSwitch = findViewById(R.id.recurring_switch);
        frequencyTextView = findViewById(R.id.frequency_text_view);
        endDateTextView = findViewById(R.id.end_date_text_view);
        frequencyLayout = findViewById(R.id.frequency_layout);
        endDateLayout = findViewById(R.id.end_date_layout);
        backIcon = findViewById(R.id.back_icon);
        deleteIcon = findViewById(R.id.deleteTran);
        okTransactionButton = findViewById(R.id.ok_transacion_button);

        db = FirebaseFirestore.getInstance();

        // Get the transactionDate from the intent
        String transactionDate = getIntent().getStringExtra("transactionDate");
        Log.e(TAG, "Transaction Date: " + transactionDate);

        // Get the transactionId from the intent
        String transactionId = getIntent().getStringExtra("transactionId");

        if (transactionId != null) {
            loadTransaction(transactionId,transactionDate);
        } else {
            Toast.makeText(this, "No transaction ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }


        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMaterialPicker(dateTextView, "Select transaction date");
            }
        });
        endDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMaterialPicker(endDateTextView, "Select Recurring payment end date");
            }
        });

        frequencyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFrequencySelectionDialog();
            }
        });

        // Click listener for category TextView
        categoryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the new activity to select category
                Intent intent = new Intent(ModifyTransactionActivity.this, SelectCategoryActivity.class);
                startActivityForResult(intent, CATEGORY_REQUEST_CODE);
            }
        });

        // Click listener for sub-category TextView
        subCategoryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the new activity to select sub-category
                Intent intent = new Intent(ModifyTransactionActivity.this, SelectSubCategoryActivity.class);

                // Pass the selected category as an extra with the intent
                String selectedCategory = categoryTextView.getText().toString();
                intent.putExtra("selected_category", selectedCategory);

                startActivityForResult(intent, SUB_CATEGORY_REQUEST_CODE);
            }
        });

        // Set a listener to the RadioGroup
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Check which radio button is selected
                if (checkedId == R.id.radio_expense) {
                    // Expense radio button is selected
                    type = "Expense";
                    //Toast.makeText(AddTransactionActivity.this, "Expense selected", Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.radio_income) {
                    // Income radio button is selected
                    type = "Income";
                    //Toast.makeText(AddTransactionActivity.this, "Income selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up listener for Recurring switch
        recurringSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Show or hide frequency and end date layouts based on switch state
                if (isChecked) {
                    frequencyLayout.setVisibility(View.VISIBLE);
                    endDateLayout.setVisibility(View.VISIBLE);
                } else {
                    frequencyLayout.setVisibility(View.GONE);
                    endDateLayout.setVisibility(View.GONE);
                    // Reset values
                    frequencyTextView.setText("");
                    endDateTextView.setText("");
                }
            }
        });

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog("Insert title", titleTextView);
            }
        });

        notesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog("Insert note", notesTextView);
            }
        });

        amountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the custom dialog layout
                View dialogView = LayoutInflater.from(ModifyTransactionActivity.this).inflate(R.layout.dialog_number_keyboard, null);

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
                AlertDialog.Builder builder = new AlertDialog.Builder(ModifyTransactionActivity.this);
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
                                if (newText.endsWith(".")) {
                                    commaAdded = true;
                                } else {
                                    commaAdded = false;
                                }
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
                        // If the amount is "0.", "0.0", or "0.0" followed by any number of zeros, set it to "0"
                        if (amount.equals("0.") || amount.equals("0.0") || amount.matches("0.0+")) {
                            amount = "0";
                        }
                        // Remove leading zero if present
                        if (amount.startsWith("0") && !amount.startsWith("0.")) {
                            amount = amount.substring(1);
                        }
                        // Set the amount in the TextView
                        amountTextView.setText(amount);
                        dialog.dismiss();
                    }
                });

                // Show the dialog
                dialog.show();
            }
        });

        // OK transaction Button
        okTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = amountTextView.getText().toString().trim();
                String category = categoryTextView.getText().toString().isEmpty() ? "Uncategorized" : categoryTextView.getText().toString();
                if (amount.isEmpty()) {
                    // Show error dialog if amount is empty
                    showErrorDialog("Error Occurred", "Amount cannot be empty");
                } else if (amount.equals("0")) {
                    // Show error dialog if amount is zero
                    showErrorDialog("Error Occurred", "Amount cannot be equal to 0");
                } else if (recurringSwitch.isChecked() && frequencyTextView.getText().toString().isEmpty()) {
                    // Show error dialog if Recurring payment is active and frequency is not selected
                    showErrorDialog("Error Occurred", "Please select the frequency for Recurring payment");
                } else if (recurringSwitch.isChecked() && endDateTextView.getText().toString().isEmpty()) {
                    // Show error dialog if Recurring payment is active and end date is not selected
                    showErrorDialog("Error Occurred", "Please select the end date for Recurring payment");
                } else if (!category.equals("Uncategorized") && subCategoryTextView.getText().toString().isEmpty()) {
                    // Show error dialog if category is selected and it's not "Uncategorized" but subcategory is not selected
                    showErrorDialog("Error Occurred", "Please select a subcategory");
                } else {
                    // Construct a Transaction object with the necessary fields
                    Transaction transaction = new Transaction();
                    transaction.setUserId(userId); // Set the user's UID
                    transaction.setTransactionId(transactionId);
                    transaction.setAmount(amountTextView.getText().toString());
                    transaction.setCategory(categoryTextView.getText().toString());
                    transaction.setSubcategory(subCategoryTextView.getText().toString());
                    transaction.setType(type);
                    transaction.setTitle(titleTextView.getText().toString());
                    transaction.setDate(dateTextView.getText().toString());
                    transaction.setNote(notesTextView.getText().toString());
                    transaction.setRecurring(recurringSwitch.isChecked());
                    transaction.setFrequency(frequencyTextView.getText().toString());
                    transaction.setEndDateFrequency(endDateTextView.getText().toString());

                    //Delete the old transaction and Add the new transaction to the database
                    DeleteThenAdd(transactionId,dateOnOpen,transaction);
                }
            }
        });

        // Delete transaction Button
        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(ModifyTransactionActivity.this)
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this transaction?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User confirmed deletion, proceed to delete the transaction
                                Log.e(TAG, "DATE ON OPEN"+dateOnOpen);
                                getTransactionsAndDelete(transactionId,dateOnOpen);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

    }

    // Method to delete the old transaction then add the new one from Firestore
    public void DeleteThenAdd(String transactionId, String date, Transaction tran) {
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
                        update(filteredTransactions, tran);

                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                        // Handle error
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
        for (Transaction transaction : transactionList) {
            Log.e(TAG, "filtering "+transaction.getDate()+" "+date);
            if (transaction.getDate().equals(date)) {
                filteredTransactions.add(transaction);
                Log.e(TAG, "transaction with date found "+transaction.getDate()+" "+date);
            }
        }
        return filteredTransactions;
    }

    // Method to delete transactions from the database based on ID and date
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
                                            finish();
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

    // Method to delete transactions from the database based on ID and date
    private void update(List<Transaction> transactionsToDelete, Transaction tran) {
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
                                            finish();
                                            // Handle any additional logic here
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle errors
                                            Log.e(TAG, "Error deleting transaction: " + e.getMessage(), e);
                                            // Handle any additional error logic here
                                        });
                            }
                            db.collection("transactions")
                            .add(tran)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    // Transaction added successfully
                                    Log.d(TAG, "Transaction Modified Successfully");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle errors
                                    Log.e(TAG, "Failed to Modify " + e.getMessage(), e);
                                }
                            });
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                            // Handle error
                        }
                    });
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CATEGORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Update category_text_view with the selected category
            String selectedCategory = data.getStringExtra("selected_category");
            categoryTextView.setText(selectedCategory);
            subCategoryTextView.setText("");
            // Check if the selected category is "Uncategorized"
            if (!selectedCategory.equals("Uncategorized")) {
                // Show the subcategory layout
                subCategoryLayout.setVisibility(View.VISIBLE);
            } else {
                // If the category is "Uncategorized", hide the subcategory layout
                subCategoryLayout.setVisibility(View.GONE);
                // Reset the subcategory TextView text
                subCategoryTextView.setText("");
            }
        } else if (requestCode == SUB_CATEGORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Update sub_category_text_view with the selected sub-category
            String selectedSubCategory = data.getStringExtra("selected_sub_category");
            subCategoryTextView.setText(selectedSubCategory);
        }
    }

    private void loadTransaction(String transactionId, String transactionDate) {
        // Query Firestore to fetch the transaction details based on ID
        db.collection("transactions")
                .whereEqualTo("transactionId", transactionId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Filter transactions by date locally
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    String docDate = document.getString("date");
                                    if (docDate != null && docDate.equals(transactionDate)) {
                                        // Transaction found, parse data
                                        Transaction transaction = document.toObject(Transaction.class);
                                        userId=transaction.getUserId();
                                        // Populate views with transaction details
                                        amountTextView.setText(transaction.getAmount());
                                        categoryTextView.setText(transaction.getCategory());
                                        // Show subcategory only if it's not empty
                                        if (transaction.getSubcategory() != null && !transaction.getSubcategory().isEmpty()) {
                                            subCategoryTextView.setText(transaction.getSubcategory());
                                            subCategoryLayout.setVisibility(View.VISIBLE);
                                        } else {
                                            subCategoryLayout.setVisibility(View.GONE);
                                        }
                                        if ("Expense".equals(transaction.getType())) {
                                            radioExpense.setChecked(true);
                                            type = "Expense";
                                        } else if ("Income".equals(transaction.getType())) {
                                            radioIncome.setChecked(true);
                                            type = "Income";
                                        }
                                        titleTextView.setText(transaction.getTitle());
                                        dateTextView.setText(transaction.getDate());
                                        dateOnOpen = transaction.getDate();
                                        notesTextView.setText(transaction.getNote());
                                        recurringSwitch.setChecked(transaction.isRecurring());
                                        frequencyTextView.setText(transaction.getFrequency());
                                        endDateTextView.setText(transaction.getEndDateFrequency());
                                        // Show or hide frequency and end date layouts based on Recurring state
                                        if (transaction.isRecurring()) {
                                            frequencyLayout.setVisibility(View.VISIBLE);
                                            endDateLayout.setVisibility(View.VISIBLE);
                                        } else {
                                            frequencyLayout.setVisibility(View.GONE);
                                            endDateLayout.setVisibility(View.GONE);
                                        }
                                        // Exit the loop after finding the transaction
                                        return;
                                    }
                                }
                                // If the loop completes without finding the transaction for the specific date
                                Log.d(TAG, "Transaction with ID " + transactionId + " on Date " + transactionDate + " does not exist.");
                                Toast.makeText(ModifyTransactionActivity.this, "Transaction not found", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                // Transaction with the specified ID not found
                                Log.d(TAG, "Transaction with ID " + transactionId + " does not exist.");
                                Toast.makeText(ModifyTransactionActivity.this, "Transaction not found", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            // Handle database error
                            Log.e(TAG, "Firestore error: ", task.getException());
                            Toast.makeText(ModifyTransactionActivity.this, "Firestore error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    private void showMaterialPicker(final TextView targetTextView, String pickerTitle) {
        // Check if the MaterialDatePicker is already added
        if (getSupportFragmentManager().findFragmentByTag("MATERIAL_DATE_PICKER") == null) {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText(pickerTitle);

            // Set today's date as the default selection
            builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

            materialDatePicker = builder.build();
            materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                @Override
                public void onPositiveButtonClick(Long selection) {
                    // Convert the selected date to a readable format
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String dateString = sdf.format(new Date(selection));

                    // Set the selected date to the TextView
                    targetTextView.setText(dateString);
                }
            });
            materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        }
    }

    private boolean isFrequencyDialogShowing = false;
    private AlertDialog frequencyDialog;
    private void showFrequencySelectionDialog() {
        if (!isFrequencyDialogShowing) {
            // Mark that the dialog is currently showing
            isFrequencyDialogShowing = true;

            // Inflate the custom dialog layout
            View dialogView = LayoutInflater.from(ModifyTransactionActivity.this).inflate(R.layout.dialog_frequency_selector, null);

            // Initialize views inside the dialog
            EditText dialogInput = dialogView.findViewById(R.id.dialog_input);
            Spinner dialogUnitSpinner = dialogView.findViewById(R.id.dialog_unit_spinner);
            Button dialogCancel = dialogView.findViewById(R.id.dialog_cancel);
            Button dialogOk = dialogView.findViewById(R.id.dialog_ok);

            // Create custom adapter for the spinner
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.frequency_units_array, R.layout.item_spinner_layout);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dialogUnitSpinner.setAdapter(adapter);

            // Create the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(ModifyTransactionActivity.this);
            builder.setView(dialogView);

            // Handle button clicks inside the dialog
            frequencyDialog = builder.create();

            frequencyDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    // Reset the flag when the dialog is dismissed
                    isFrequencyDialogShowing = false;
                }
            });

            dialogCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Dismiss the dialog and reset the flag
                    frequencyDialog.dismiss();
                }
            });

            dialogOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the selected frequency number and unit
                    String frequencyNumber = dialogInput.getText().toString().trim();
                    String frequencyUnit = dialogUnitSpinner.getSelectedItem().toString();

                    // Construct the frequency text ("5 days") if input is not empty
                    String frequencyText = !frequencyNumber.isEmpty() ? frequencyNumber + " " + frequencyUnit : "";

                    // Set the selected frequency to the TextView
                    frequencyTextView.setText(frequencyText);

                    // Dismiss the dialog
                    frequencyDialog.dismiss();
                }
            });

            // Show the dialog
            frequencyDialog.show();
        }
    }

    private void showCustomDialog(String title, final TextView textView) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_text_input);

        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        final EditText dialogInput = dialog.findViewById(R.id.dialog_input);
        TextView dialogOk = dialog.findViewById(R.id.dialog_ok);
        TextView dialogCancel = dialog.findViewById(R.id.dialog_cancel);

        dialogTitle.setText(title);

        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = dialogInput.getText().toString().trim();
                textView.setText(inputText);
                dialog.dismiss();
            }
        });

        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // Set the width of the dialog to match parent
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.show();
    }

    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
