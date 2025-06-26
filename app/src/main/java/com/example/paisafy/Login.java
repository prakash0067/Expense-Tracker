package com.example.paisafy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.paisafy.Model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class Login extends AppCompatActivity {

    private TextInputEditText emailIdView, passwordView;
    private MaterialButton loginButtonView;
    private TextView forgotPasswordView, signUpView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailIdView = findViewById(R.id.emailEditText);
        passwordView = findViewById(R.id.passwordEditText);
        loginButtonView = findViewById(R.id.signInBtn);
        forgotPasswordView = findViewById(R.id.forgotPassword);
        signUpView = findViewById(R.id.signUpText);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("email") && intent.hasExtra("password")) {
            String email = intent.getStringExtra("email");
            String password = intent.getStringExtra("password");

            if (!TextUtils.isEmpty(email)) {
                emailIdView.setText(email);
            }

            if (!TextUtils.isEmpty(password)) {
                passwordView.setText(password);
            }
        }

        // Set click listener for login button
        loginButtonView.setOnClickListener(view -> loginUser());

        // (Optional) Navigate to Sign Up screen if needed
        signUpView.setOnClickListener(v -> {
             startActivity(new Intent(Login.this, Signup.class));
        });

        forgotPasswordView.setOnClickListener(v -> {
            Toast.makeText(Login.this, "Forgot password clicked!", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(Login.this, ForgotPasswordActivity.class));
        });
    }

    private void loginUser() {
        String email = emailIdView.getText() != null ? emailIdView.getText().toString().trim() : "";
        String password = passwordView.getText() != null ? passwordView.getText().toString().trim() : "";

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailIdView.setError("Email is required");
            emailIdView.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailIdView.setError("Please enter a valid email");
            emailIdView.requestFocus();
            return;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordView.setError("Password is required");
            passwordView.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordView.setError("Password must be at least 6 characters");
            passwordView.requestFocus();
            return;
        }

        DbHelper dbHelper = new DbHelper(this);
        User user = dbHelper.authenticateUser(email, password);

        if (user != null) {
            setSharedPref(user);
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void setSharedPref(User user) {
        SharedPreferences preferences = getSharedPreferences("UserPrefsPaisafy", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("user_id", user.getId());
        editor.putString("user_name", user.getName());
        editor.putString("user_email", user.getEmail());
        editor.putString("join_date", user.getJoinDate());
        editor.putBoolean("isLoggedIn",true);

        if (user.getProfilePic() != null) {
            editor.putString("profile_pic", user.getProfilePic());
        }

        editor.apply();
    }

}