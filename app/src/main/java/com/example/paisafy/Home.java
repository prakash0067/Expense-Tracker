package com.example.paisafy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.paisafy.Model.ExpenseCategory;
import com.example.paisafy.Model.IncomeCategory;
import com.example.paisafy.Model.Transaction;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class Home extends Fragment {

    ImageView filterTransaction;
    RecyclerView recentTransactions;
    private String currentType = "expenses"; // default
    private LottieAnimationView loaderAnimation;
    DbHelper dbHelper;
    EditText searchField;
    RecyclerView searchRecycler;
    FrameLayout searchOverlay;
    TextView expenseCurrencySymbol, incomeCurrencySymbol;
    String currencySymbols;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tabDaily = view.findViewById(R.id.tabDaily);
        TextView tabMonthly = view.findViewById(R.id.tabMonthly);
        TextView tabYearly = view.findViewById(R.id.tabYearly);
        filterTransaction = view.findViewById(R.id.filterIcon);
        recentTransactions = view.findViewById(R.id.recyclerTransactions);
        recentTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        loaderAnimation = view.findViewById(R.id.loaderAnimation);
        searchField = view.findViewById(R.id.searchField);
        searchRecycler = view.findViewById(R.id.searchRecycler);
        searchOverlay = view.findViewById(R.id.searchOverlay);
        searchRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseCurrencySymbol = view.findViewById(R.id.expenseCurrencySymbol);
        incomeCurrencySymbol = view.findViewById(R.id.incomeCurrencySymbol);

        // Fetch and display the currency symbol in the income/expense cards
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefsPaisafy", Context.MODE_PRIVATE);
        String fullCurrency = prefs.getString("currency", "INR ₹");
        String currencySymbol = fullCurrency.contains(" ") ?
                fullCurrency.substring(fullCurrency.lastIndexOf(" ") + 1) : fullCurrency;

        currencySymbols = currencySymbol;
        expenseCurrencySymbol.setText(currencySymbol);
        incomeCurrencySymbol.setText(currencySymbol);

        // getting user id from shared pref
        int user_id = requireActivity().getSharedPreferences("UserPrefsPaisafy", getContext().MODE_PRIVATE)
                .getInt("user_id", 0);

        filterTransaction.setOnClickListener(v -> {
            currentType = currentType.equals("expenses") ? "income" : "expenses";
            loadTransactions(user_id);
        });

        View.OnClickListener toggleClickListener = v -> {
            // Reset all tabs
            tabDaily.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabDaily.setTextColor(Color.parseColor("#888888"));

            tabMonthly.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabMonthly.setTextColor(Color.parseColor("#888888"));

            tabYearly.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabYearly.setTextColor(Color.parseColor("#888888"));

            // Highlight selected tab
            ((TextView) v).setBackgroundResource(R.drawable.bg_tab_selected);
            ((TextView) v).setTextColor(Color.parseColor("#FFFFFF"));

            String selected = "daily"; // default
            if (v.getId() == R.id.tabMonthly) {
                selected = "monthly";
            } else if (v.getId() == R.id.tabYearly) {
                selected = "yearly";
            }

            // Save to SharedPreferences
            requireActivity().getSharedPreferences("UserPrefsPaisafy", getContext().MODE_PRIVATE)
                    .edit()
                    .putString("selected_period", selected)
                    .apply();

            // Update chart + income/expense cards
            renderPieChart(view, selected, user_id, () -> loadTransactions(user_id));
            updateIncomeExpenseSummary(selected, user_id);
        };

        tabDaily.setOnClickListener(toggleClickListener);
        tabMonthly.setOnClickListener(toggleClickListener);
        tabYearly.setOnClickListener(toggleClickListener);

        // Restore tab selection
        String saved = requireActivity().getSharedPreferences("UserPrefsPaisafy", getContext().MODE_PRIVATE)
                .getString("selected_period", "monthly"); // default to "monthly"

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            switch (saved) {
                case "daily":
                    tabDaily.performClick();
                    break;
                case "yearly":
                    tabYearly.performClick();
                    break;
                default:
                    tabMonthly.performClick();
                    break;
            }
        }, 300);


        searchField.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    searchOverlay.setVisibility(View.VISIBLE);
                    performSearch(user_id, query);
                } else {
                    searchOverlay.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        return view;
    }

    private void loadTransactions(int userId) {
        loaderAnimation.setVisibility(View.VISIBLE); // Show loader

        new Thread(() -> {
            dbHelper = new DbHelper(getContext());
            List<Transaction> transactions = dbHelper.getRecentTransactions(userId, currentType, 10);

            requireActivity().runOnUiThread(() -> {
                TransactionAdapter adapter = new TransactionAdapter(getContext(),transactions, currentType);
                recentTransactions.setAdapter(adapter);
                loaderAnimation.setVisibility(View.GONE); // Hide loader
            });
        }).start();
    }

    private void renderPieChart(View view, String period, int userId, Runnable onComplete) {
        loaderAnimation.setVisibility(View.VISIBLE);

        new Thread(() -> {
            dbHelper = new DbHelper(getContext());
            List<Pair<String, Double>> data = dbHelper.getExpenseByCategory(period, userId);

            requireActivity().runOnUiThread(() -> {
                PieChart pieChart = view.findViewById(R.id.pieChart);

                if (data == null || data.isEmpty()) {
                    pieChart.clear();
                    pieChart.setNoDataText("No expense data available");
                    pieChart.setNoDataTextColor(Color.GRAY);
                    pieChart.invalidate();
                    loaderAnimation.setVisibility(View.GONE);
                    if (onComplete != null) onComplete.run(); // <- callback
                    return;
                }

                List<PieEntry> entries = new ArrayList<>();
                for (Pair<String, Double> item : data) {
                    entries.add(new PieEntry(item.second.floatValue(), item.first));
                }

                PieDataSet dataSet = new PieDataSet(entries, "Expense by Category");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                dataSet.setValueTextSize(12f);
                dataSet.setValueTextColor(Color.BLACK);

                PieData pieData = new PieData(dataSet);
                pieData.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.format(Locale.getDefault(), "%s%.0f", currencySymbols, value);
                    }
                });

                pieChart.setData(pieData);
                pieChart.getLegend().setEnabled(false);
                pieChart.setUsePercentValues(false); // Show actual amounts instead of percentages
                pieChart.setDrawHoleEnabled(true);
                pieChart.setEntryLabelColor(Color.BLACK);
                pieChart.setCenterText(period.substring(0, 1).toUpperCase() + period.substring(1));
                pieChart.setCenterTextSize(16f);
                pieChart.getDescription().setEnabled(false);
                pieChart.invalidate();

                loaderAnimation.setVisibility(View.GONE);
                if (onComplete != null) onComplete.run(); // <- callback
            });
        }).start();
    }

    private void updateIncomeExpenseSummary(String period, int userId) {
        loaderAnimation.setVisibility(View.VISIBLE);

        new Thread(() -> {
            dbHelper = new DbHelper(getContext());
            double totalIncome = dbHelper.getTotalIncome(period, userId);
            double totalExpense = dbHelper.getTotalExpense(period, userId);

            requireActivity().runOnUiThread(() -> {
                TextView incomeText = getView().findViewById(R.id.incomeAmount);
                TextView expenseText = getView().findViewById(R.id.expenseAmount);

                Log.d("===== Expenses =====",String.format(Locale.getDefault(), "%,.0f", totalExpense));

                incomeText.setText(String.format(Locale.getDefault(), "%,.0f", totalIncome));
                expenseText.setText(String.format(Locale.getDefault(), "%,.0f", totalExpense));

                loaderAnimation.setVisibility(View.GONE);
            });
        }).start();
    }

    private void performSearch(int userId, String query) {
        new Thread(() -> {
            dbHelper = new DbHelper(getContext());
            List<Transaction> expenseResults = dbHelper.searchTransactions(userId, query, "expenses");
            List<Transaction> incomeResults = dbHelper.searchTransactions(userId, query, "income");

            List<Transaction> allResults = new ArrayList<>();
            allResults.addAll(expenseResults);
            allResults.addAll(incomeResults);

            requireActivity().runOnUiThread(() -> {
                SearchAdapter adapter = new SearchAdapter(
                        requireContext(),
                        allResults,
                        getExpenseCategoryList(),
                        getIncomeCategoryList()
                );
                searchRecycler.setAdapter(adapter);
            });
        }).start();
    }

    private List<ExpenseCategory> getExpenseCategoryList() {
        List<ExpenseCategory> list = new ArrayList<>();
        list.add(new ExpenseCategory("Food", R.drawable.ic_food));
        list.add(new ExpenseCategory("Transport", R.drawable.ic_transport));
        list.add(new ExpenseCategory("Shopping", R.drawable.ic_shopping));
        list.add(new ExpenseCategory("Health", R.drawable.ic_health));
        list.add(new ExpenseCategory("Entertainment", R.drawable.ic_entertainment));
        list.add(new ExpenseCategory("Bills", R.drawable.ic_bills));
        list.add(new ExpenseCategory("Travel", R.drawable.ic_travel));
        list.add(new ExpenseCategory("Groceries", R.drawable.ic_grocery));
        list.add(new ExpenseCategory("Education", R.drawable.ic_education));
        list.add(new ExpenseCategory("Others", R.drawable.ic_others));
        return list;
    }

    private List<IncomeCategory> getIncomeCategoryList() {
        return Arrays.asList(
                new IncomeCategory("Salary", R.drawable.ic_salary),
                new IncomeCategory("Freelance", R.drawable.ic_freelancer),
                new IncomeCategory("Gift", R.drawable.ic_gift),
                new IncomeCategory("Investment", R.drawable.ic_investment),
                new IncomeCategory("Other", R.drawable.ic_others)
        );
    }

}