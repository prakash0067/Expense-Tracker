package com.example.paisafy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

public class Wallet extends Fragment {

    private TextView totalBalanceText, incomeAmountText, expenseAmountText, monthComparison;
    private DbHelper dbHelper;
    private String currencySymbol = "₹";
    private int userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        // Init DB
        dbHelper = new DbHelper(getContext());

        // Fetch user ID and currency from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefsPaisafy", Context.MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        String fullCurrency = prefs.getString("currency", "INR ₹");
        if (fullCurrency.contains(" ")) {
            currencySymbol = fullCurrency.substring(fullCurrency.lastIndexOf(" ") + 1);
        } else {
            currencySymbol = fullCurrency;
        }

        // Bind Views
        totalBalanceText = view.findViewById(R.id.totalBalanceText);
        incomeAmountText = view.findViewById(R.id.incomeAmountText);
        expenseAmountText = view.findViewById(R.id.expenseAmountText);
        monthComparison = view.findViewById(R.id.monthComparison);

        // Load data
        loadWalletSummary();

        return view;
    }

    private void loadWalletSummary() {
        // Current month totals
        double incomeTotal = dbHelper.getTotalAmountByUser("income", userId);
        double expenseTotal = dbHelper.getTotalAmountByUser("expenses", userId);
        double balance = incomeTotal - expenseTotal;

        // Previous month totals
        double prevIncome = dbHelper.getPreviousMonthTotalByUser("income", userId);
        double prevExpense = dbHelper.getPreviousMonthTotalByUser("expenses", userId);
        double prevBalance = prevIncome - prevExpense;

        // Set values to views
        totalBalanceText.setText(formatCurrency(balance));
        incomeAmountText.setText(formatCurrency(incomeTotal));
        expenseAmountText.setText(formatCurrency(expenseTotal));

        updateComparisonText(balance, prevBalance);
    }

    private void updateComparisonText(double current, double previous) {
        if (previous == 0) {
            monthComparison.setText("No data for last month");
            monthComparison.setTextColor(Color.GRAY);
            return;
        }

        double percent = ((current - previous) / previous) * 100;
        String formatted = String.format(Locale.getDefault(), "%.1f", Math.abs(percent));

        if (percent > 0) {
            monthComparison.setText("↑ " + formatted + "% Increase");
            monthComparison.setTextColor(Color.parseColor("#388E3C")); // Green
        } else if (percent < 0) {
            monthComparison.setText("↓ " + formatted + "% Decrease");
            monthComparison.setTextColor(Color.parseColor("#C62828")); // Red
        } else {
            monthComparison.setText("No Change from Last Month");
            monthComparison.setTextColor(Color.GRAY);
        }
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "%s%,.0f", currencySymbol, amount);
    }
}
