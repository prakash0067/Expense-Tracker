package com.example.paisafy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.paisafy.Model.User;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    View navHome, navTransfer, navWallet, navProfile, navFabBtn;
    TextView userGreetingView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    ImageView profileImg;
    DbHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ========= side menu =============
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Handle Menu Icon Click
        findViewById(R.id.menuIcon).setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        // Set username in header dynamically if needed
        View headerView = navigationView.getHeaderView(0);
        TextView drawerUserName = headerView.findViewById(R.id.drawerUserName);
        profileImg = findViewById(R.id.profileImage);
        drawerUserName.setText("User's Name");

        // Handle drawer item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navHome) {
                loadFragment(new Home());
            } else if (id == R.id.navAddIncome) {
                loadFragment(new AddIncome());
            } else if (id == R.id.navAddExpense) {
                // Open Add Expense Fragment/Activity
                loadFragment(new AddExpense());
            } else if (id == R.id.navToDoList) {
                Intent intent = new Intent(MainActivity.this, TodoList.class);
                startActivity(intent);
            } else if (id == R.id.navAbout) {
                loadFragment(new About());
            } else if (id == R.id.navSettings) {
                // Open Profile fragment with settings tab selected
                Profile profileFragment = new Profile();
                Bundle args = new Bundle();
                args.putBoolean("open_settings_tab", true);
                profileFragment.setArguments(args);
                loadFragment(profileFragment);
            } else if (id == R.id.navLogout) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Clear shared preferences
                            SharedPreferences preferences = getSharedPreferences("UserPrefsPaisafy", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.clear(); // remove all data
                            editor.apply();

                            // Redirect to login screen
                            Intent intent = new Intent(MainActivity.this, Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // close current activity
                        })
                        .setNegativeButton("No", null)
                        .show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Initialize bottom nav views
        navHome = findViewById(R.id.navHome);
        navTransfer = findViewById(R.id.navTransfer);
        navWallet = findViewById(R.id.navWallet);
        navProfile = findViewById(R.id.navProfile);
        navFabBtn = findViewById(R.id.fabAdd);
        userGreetingView = findViewById(R.id.helloText);

        // Load default fragment
        loadFragment(new Home());

        // Set onClick listeners
        navHome.setOnClickListener(v -> loadFragment(new Home()));

        SharedPreferences preferences = getSharedPreferences("UserPrefsPaisafy", MODE_PRIVATE);
        int user_id = preferences.getInt("user_id", 0);

        dbHelper = new DbHelper(MainActivity.this);

        if (user_id != 0) {
            User currentUser = dbHelper.getUserById(user_id);
            if (currentUser != null) {
                drawerUserName.setText(currentUser.getName());
                userGreetingView.setText("Hey, " + currentUser.getName());
            }
        }

        navFabBtn.setOnClickListener(v ->
                loadFragment(new AddExpense())
        );

        navProfile.setOnClickListener(v -> loadFragment(new Profile()));
        profileImg.setOnClickListener(v -> loadFragment(new Profile()));
        navWallet.setOnClickListener(v -> loadFragment(new Wallet()));
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

}