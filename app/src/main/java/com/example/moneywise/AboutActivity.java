package com.example.moneywise;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.moneywise.Login_Signup.LoginActivity;
import com.example.moneywise.Operations.PlannedTransactionsActivity;
import com.example.moneywise.Operations.RecurringPaymentsActivity;
import com.example.moneywise.Operations.SetBudgetsActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AboutActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageView menuIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

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
        userEmailTextView.setText(email);        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.home_item) {
                    startActivity(new Intent(AboutActivity.this, HomeActivity.class));
                    finishAffinity();
                } else if (itemId == R.id.setBudget_item) {
                    startActivity(new Intent(AboutActivity.this, SetBudgetsActivity.class));
                    finish();
                } else if (itemId == R.id.recurringPayments_item) {
                    startActivity(new Intent(AboutActivity.this, RecurringPaymentsActivity.class));
                    finish();
                } else if (itemId == R.id.plannedTransactions_item) {
                    startActivity(new Intent(AboutActivity.this, PlannedTransactionsActivity.class));
                    finish();
                } else if (itemId == R.id.about_item) {
                    drawerLayout.closeDrawer(GravityCompat.START);// Close the drawer
                } else if (itemId == R.id.logout_item) {
                    Context context = drawerLayout.getContext();
                    showLogoutPrompt(context);
                }

                // Close the drawer after selecting an item
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
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
                                startActivity(new Intent(AboutActivity.this, LoginActivity.class));
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }


        });
    }
}