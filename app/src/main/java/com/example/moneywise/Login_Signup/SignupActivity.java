package com.example.moneywise.Login_Signup;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moneywise.Helpers.SnackbarHelper;
import com.example.moneywise.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLogIn;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogIn = findViewById(R.id.textViewLogIn);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                    SnackbarHelper.showSnackbar(v, "Please fill in all fields");
                } else if (!isValidEmail(email)) {
                    SnackbarHelper.showSnackbar(v, "Please enter a valid email address");
                } else if (password.length() < 6) {
                    SnackbarHelper.showSnackbar(v, "Password should be at least 6 characters long");
                } else if (!password.equals(confirmPassword)) {
                    SnackbarHelper.showSnackbar(v, "Passwords do not match");
                } else {
                    registerUser(email, password);
                }
            }
        });

        // Set OnClickListener for login navigation
        textViewLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to login activity and Close current activity
                //startActivity(new Intent(Signup.this, Login.class));
                finish();
            }
        });
    }

    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    private void registerUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success
                            Toast.makeText(SignupActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                            // Log out all accounts
                            FirebaseAuth.getInstance().signOut();
                            // Navigate to login activity
                            //startActivity(new Intent(Signup.this, Login.class));
                            finish(); // Close current activity
                        } else {
                            // If sign up fails, display a message to the user.
                            SnackbarHelper.showSnackbar(findViewById(android.R.id.content), "Registration failed: " + task.getException().getMessage());
                        }
                    }
                });
    }


}
