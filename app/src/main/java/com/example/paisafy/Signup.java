package com.example.paisafy;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

public class Signup extends AppCompatActivity {

    private TextInputEditText signupName, signupEmail, signupPassword, signupConfirmPassword;
    private MaterialButton signupButton;
    private DbHelper dbHelper;
    private TextView goToLogin;
    MaterialCheckBox termsCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = new DbHelper(this);

        signupName = findViewById(R.id.signupName);
        signupEmail = findViewById(R.id.signupEmail);
        signupPassword = findViewById(R.id.signupPassword);
        signupConfirmPassword = findViewById(R.id.signupConfirmPassword);
        signupButton = findViewById(R.id.signupButton);
        goToLogin = findViewById(R.id.goToLogin);
        termsCheckbox = findViewById(R.id.termsCheckbox);

        signupButton.setOnClickListener(v -> handleSignup());
        goToLogin.setOnClickListener(v -> startActivity(new Intent(this, Login.class)));
    }

    private void handleSignup() {
        String name = signupName.getText().toString().trim();
        String email = signupEmail.getText().toString().trim();
        String password = signupPassword.getText().toString().trim();
        String confirmPassword = signupConfirmPassword.getText().toString().trim();

        // Reset errors
        signupName.setError(null);
        signupEmail.setError(null);
        signupPassword.setError(null);
        signupConfirmPassword.setError(null);

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            signupName.setError("Name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            signupEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupEmail.setError("Invalid email format");
            isValid = false;
        } else if (emailExists(email)) {
            signupEmail.setError("Email already registered");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            signupPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            signupPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            signupConfirmPassword.setError("Please confirm password");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            signupConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        if (!termsCheckbox.isChecked()) {
            Toast.makeText(this, "Please agree to the Terms to continue", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Save to DB
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        values.put("profile_pic", ""); // optional, blank for now

        long id = dbHelper.getWritableDatabase().insert("users", null, values);

        if (id > 0) {
            Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();

            // Pass email and password to LoginActivity
            Intent intent = new Intent(Signup.this, Login.class);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Signup failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean emailExists(String email) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT id FROM users WHERE email = ?",
                new String[]{email}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
}
