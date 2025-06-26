package com.example.paisafy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Loading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Delay for splash (optional, 1 second)
        new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginStatus, 2000);
    }

    private void checkLoginStatus() {
        SharedPreferences preferences = getSharedPreferences("UserPrefsPaisafy", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Redirect to main activity
            startActivity(new Intent(Loading.this, MainActivity.class));
        } else {
            // Redirect to login screen
            startActivity(new Intent(Loading.this, Login.class));
        }
        finish(); // Close Loading screen
    }
}
