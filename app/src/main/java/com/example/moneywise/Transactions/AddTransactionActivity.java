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
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moneywise.Models.Recurring;
import com.example.moneywise.Models.Transaction;
import com.example.moneywise.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AddTransactionActivity extends AppCompatActivity {
    private boolean firstKeypress = true;
    private boolean commaAdded = false;
    private String type = "Expense";
    private TextView amountTextView,dialogTitle,notesTextView,transactionTitleTextView,
            datePickerTextView,endDateTextView,frequencyTextView, categoryTextView,subCategoryTextView;
    private MaterialSwitch recurringSwitch;
    private LinearLayout frequencyLayout,endDateLayout,subCategoryLayout;
    private ImageView backIcon;
    private RadioGroup radioGroup;
    Button okTransactionButton;
    private MaterialDatePicker<Long> materialDatePicker;
    private static final int CATEGORY_REQUEST_CODE = 1;
    private static final int SUB_CATEGORY_REQUEST_CODE = 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        String userId = getIntent().getStringExtra("USER_ID");

        backIcon = findViewById(R.id.back_icon);
        amountTextView = findViewById(R.id.amount);
        transactionTitleTextView = findViewById(R.id.transaction_title_text_view);
        notesTextView = findViewById(R.id.notes_text_view);
        datePickerTextView = findViewById(R.id.date_picker_text_view);
        endDateTextView = findViewById(R.id.end_date_text_view);
        frequencyTextView =findViewById(R.id.frequency_text_view);
        okTransactionButton = findViewById(R.id.ok_transacion_button);
        categoryTextView = findViewById(R.id.category_text_view);
        subCategoryTextView = findViewById(R.id.sub_category_text_view);
        radioGroup = findViewById(R.id.radioGroup);


        //Initialize the subcategory layout
        subCategoryLayout = findViewById(R.id.sub_category_layout);
        subCategoryLayout.setVisibility(View.GONE);

        //Initialize the Firestore database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        frequencyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFrequencySelectionDialog();
            }
        });

        // Set today's date as default
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        datePickerTextView.setText(currentDate);
        datePickerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMaterialPicker(datePickerTextView, "Select transaction date");
            }
        });


        endDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMaterialPicker(endDateTextView, "Select Recurring payment end date");
            }
        });

        // Click listener for category TextView
        categoryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the new activity to select category
                Intent intent = new Intent(AddTransactionActivity.this, SelectCategoryActivity.class);
                startActivityForResult(intent, CATEGORY_REQUEST_CODE);
            }
        });
        // Click listener for sub-category TextView
        subCategoryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the new activity to select sub-category
                Intent intent = new Intent(AddTransactionActivity.this, SelectSubCategoryActivity.class);

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

        //find the switch and define the layouts to show/hide:
        recurringSwitch = findViewById(R.id.recurring_switch);
        frequencyLayout = findViewById(R.id.frequency_layout);
        endDateLayout = findViewById(R.id.end_date_layout);

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
                }
            }
        });

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        transactionTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog("Insert title", transactionTitleTextView);
            }
        });

        notesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog("Insert note", notesTextView);
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
                    if (recurringSwitch.isChecked()) {
                        String frequency = frequencyTextView.getText().toString();
                        String endDate = endDateTextView.getText().toString();
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date transactionDate = sdf.parse(datePickerTextView.getText().toString());
                            Date endRecurringDate = sdf.parse(endDate);

                            // Check if the transaction date is after the end recurring date
                            if (transactionDate.after(endRecurringDate)) {
                                // Show error dialog if the transaction date is after the end date
                                showErrorDialog("Error Occurred", "Recurring transaction end date cannot be before the start date");
                                return; // Exit the onClick method
                            }

                            // Generate a single transaction ID for all Recurring transactions
                            String transactionId = generateTransactionId();

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(transactionDate);

                            // Loop to add Recurring transactions until reaching or exceeding the end date
                            while (calendar.getTime().before(endRecurringDate) || calendar.getTime().equals(endRecurringDate)) {
                                // Construct a Transaction object with the necessary fields
                                Transaction transaction = new Transaction();
                                transaction.setUserId(userId); // Set the user's UID
                                transaction.setTransactionId(transactionId); // Use the same transaction ID for all Recurring transactions
                                transaction.setAmount(amountTextView.getText().toString());
                                transaction.setCategory(category);
                                transaction.setSubcategory(subCategoryTextView.getText().toString());
                                transaction.setType(type);
                                transaction.setTitle(transactionTitleTextView.getText().toString());
                                transaction.setDate(sdf.format(calendar.getTime()));
                                transaction.setNote(notesTextView.getText().toString());
                                transaction.setRecurring(false);
                                transaction.setFrequency("");
                                transaction.setEndDateFrequency("");

                                // Add transaction to Firestore database
                                db.collection("transactions")
                                        .add(transaction)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                // Transaction added successfully
                                                Log.d("AddTransactionActivity", "Transaction added successfully");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle errors
                                                Log.e("AddTransactionActivity", "Failed to add transaction: " + e.getMessage(), e);
                                            }
                                        });

                                // Increment transaction date based on frequency
                                if (frequency.toLowerCase().contains("day")) {
                                    calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(frequency.split(" ")[0]));
                                } else if (frequency.toLowerCase().contains("week")) {
                                    calendar.add(Calendar.WEEK_OF_MONTH, Integer.parseInt(frequency.split(" ")[0]));
                                } else if (frequency.toLowerCase().contains("month")) {
                                    calendar.add(Calendar.MONTH, Integer.parseInt(frequency.split(" ")[0]));
                                } else if (frequency.toLowerCase().contains("year")) {
                                    calendar.add(Calendar.YEAR, Integer.parseInt(frequency.split(" ")[0]));
                                }
                            }

                            // Add transaction to recurring database
                            Recurring recurring = new Recurring();
                            recurring.setUserId(userId); // Set the user's UID
                            recurring.setTransactionId(transactionId); // Use the same transaction ID for all Recurring transactions
                            recurring.setAmount(amountTextView.getText().toString());
                            // Check if subcategory is empty and set to "Uncategorized" if true
                            String subcategory = subCategoryTextView.getText().toString().trim();
                            if (subcategory.isEmpty()) {
                                subcategory = "Uncategorized";
                            }
                            recurring.setSubcategory(subcategory);
                            recurring.setDate(datePickerTextView.getText().toString());
                            recurring.setEndDateFrequency(endDate);
                            recurring.setType(type);


                            // Add recurring to Firestore database
                            db.collection("recurrings")
                                    .add(recurring)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            // Transaction added successfully
                                            Log.d("AddTransactionActivity", "Recurring added successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Handle errors
                                            Log.e("AddTransactionActivity", "Failed to add recurring: " + e.getMessage(), e);
                                        }
                                    });

                            // Finish activity after adding all Recurring transactions
                            Toast.makeText(AddTransactionActivity.this, "Recurring transactions added successfully", Toast.LENGTH_SHORT).show();
                            finish();

                        } catch (ParseException e) {
                            e.printStackTrace();
                            // Handle parsing error
                            showErrorDialog("Error Occurred", "Failed to parse date: " + e.getMessage());
                        }
                    } else {
                        // If not Recurring, add the transaction normally
                        // Construct a Transaction object as before
                        Transaction transaction = new Transaction();
                        transaction.setUserId(userId); // Set the user's UID
                        transaction.setTransactionId(generateTransactionId()); // Generate transaction ID
                        transaction.setAmount(amountTextView.getText().toString());
                        transaction.setCategory(category);
                        transaction.setSubcategory(subCategoryTextView.getText().toString());
                        transaction.setType(type);
                        transaction.setTitle(transactionTitleTextView.getText().toString());
                        transaction.setDate(datePickerTextView.getText().toString());
                        transaction.setNote(notesTextView.getText().toString());
                        transaction.setRecurring(false);

                        // Add transaction to Firestore database
                        db.collection("transactions")
                                .add(transaction)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        // Transaction added successfully
                                        Toast.makeText(AddTransactionActivity.this, "Transaction added successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle errors
                                        Log.e("AddTransactionActivity", "Failed to add transaction: " + e.getMessage(), e);
                                        Toast.makeText(AddTransactionActivity.this, "Failed to add transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }
            // Method to generate a unique transaction ID
            private String generateTransactionId() {
                // Generate a unique transaction ID using a combination of timestamp and a random number
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
                String timestamp = sdf.format(new Date());
                Random random = new Random();
                int randomNumber = random.nextInt(10000); // Generate a random number between 0 and 9999
                return timestamp + randomNumber;
            }
        });

        amountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the custom dialog layout
                View dialogView = LayoutInflater.from(AddTransactionActivity.this).inflate(R.layout.dialog_number_keyboard, null);

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
                AlertDialog.Builder builder = new AlertDialog.Builder(AddTransactionActivity.this);
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

    }

    @Override
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
            View dialogView = LayoutInflater.from(AddTransactionActivity.this).inflate(R.layout.dialog_frequency_selector, null);

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
            AlertDialog.Builder builder = new AlertDialog.Builder(AddTransactionActivity.this);
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




    @Override
    public void onBackPressed() {
        finish();
    }
}
