package com.example.paisafy;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.paisafy.Model.Transaction;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionDetail extends AppCompatActivity {

    private TextView detailTitle, detailDesc, detailDate, detailTime, detailCategory, detailType, detailAmount;
    private ImageView detailCategoryIcon;
    private DbHelper dbHelper;
    private int transactionId;
    private String transactionType; // "income" or "expenses"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DbHelper(this);

        // Get transaction ID and type
        transactionId = getIntent().getIntExtra("transaction_id", -1);
        transactionType = getIntent().getStringExtra("transaction_type");

        // Initialize views
        detailTitle = findViewById(R.id.detailTitle);
        detailDesc = findViewById(R.id.detailDesc);
        detailDate = findViewById(R.id.detailDate);
        detailTime = findViewById(R.id.detailTime);
        detailCategory = findViewById(R.id.detailCategory);
        detailType = findViewById(R.id.detailType);
        detailAmount = findViewById(R.id.detailAmount);
        detailCategoryIcon = findViewById(R.id.detailCategoryIcon);

        // Load and display transaction data
        loadTransactionDetails();

        // Handle toolbar back button
        findViewById(R.id.topAppBar).setOnClickListener(v -> onBackPressed());

        // Handle delete button
        findViewById(R.id.deleteTransaction).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        boolean success = dbHelper.deleteTransaction(transactionId, transactionType);
                        if (success) {
                            Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                            finish(); // go back
                        } else {
                            Toast.makeText(this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void loadTransactionDetails() {
        Transaction transaction = dbHelper.getTransactionById(transactionId, transactionType);
        if (transaction == null) {
            Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        detailTitle.setText(transaction.getTitle());
        detailDesc.setText(transaction.getDescription());

        // Split the stored datetime (e.g., "2024-06-23 17:40:12")
        String rawDateTime = transaction.getDate(); // full string with date + time
        String datePart = "", timePart = "";

        if (rawDateTime != null && rawDateTime.contains(" ")) {
            String[] parts = rawDateTime.split(" ");
            datePart = formatDate(parts[0]); // "2024-06-23" -> "23 Jun 2024"
            timePart = formatTime(parts[1]); // "17:40:12" -> "5:40 PM"
        }

        detailDate.setText(datePart);
        detailTime.setText(timePart);

        detailCategory.setText(transaction.getCategory());
        detailType.setText(transaction.isExpense() ? "Expense" : "Income");

        // Get currency from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefsPaisafy", MODE_PRIVATE);
        String currency = prefs.getString("currency", "INR ₹");
        String currencySymbol = currency.contains(" ") ? currency.substring(currency.lastIndexOf(" ") + 1) : currency;

        // Format amount
        String formattedAmount = String.format("%s%,.0f", currencySymbol, transaction.getAmount());
        detailAmount.setText(formattedAmount);

        // Color based on type
        if (transaction.isExpense()) {
            detailAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            detailAmount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        // Set category icon
        int iconResId = CategoryIconUtils.getCategoryIcon(transaction.getCategory(), transaction.isExpense());
        detailCategoryIcon.setImageResource(iconResId);
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return output.format(input.parse(dateStr));
        } catch (Exception e) {
            e.printStackTrace();
            return dateStr; // fallback
        }
    }

    private String formatTime(String timeStr) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return output.format(input.parse(timeStr));
        } catch (Exception e) {
            e.printStackTrace();
            return timeStr; // fallback
        }
    }

}
