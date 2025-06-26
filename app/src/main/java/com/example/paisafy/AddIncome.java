package com.example.paisafy;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.paisafy.Model.ExpenseCategory;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.*;
import com.example.paisafy.Model.IncomeCategory;

public class AddIncome extends Fragment {

    private ImageView categoryIcon;
    private TextView selectedCategoryText;
    private TextView selectedDateText;
    private LinearLayout selectDateLayout;
    private String formattedIncomeDateForDB = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_income, container, false);

        categoryIcon = view.findViewById(R.id.categoryIcon);
        selectedCategoryText = view.findViewById(R.id.selectedCategoryText);
        View categorySelector = view.findViewById(R.id.selectCategoryLayout);

        TextInputEditText inputTitle = view.findViewById(R.id.inputTitle);
        TextInputEditText inputAmount = view.findViewById(R.id.inputIncomeAmount);
        TextInputEditText inputDescription = view.findViewById(R.id.inputIncomeDescription);
        Button btnAddIncome = view.findViewById(R.id.btnAddIncome);

        // Fetch user_id from shared preferences
        int user_id = requireActivity().getSharedPreferences("UserPrefsPaisafy", getContext().MODE_PRIVATE)
                .getInt("user_id", 0);

        // Categories (you can customize)
        List<IncomeCategory> categoryList = Arrays.asList(
                new IncomeCategory("Salary", R.drawable.ic_salary),
                new IncomeCategory("Freelance", R.drawable.ic_freelancer),
                new IncomeCategory("Gift", R.drawable.ic_gift),
                new IncomeCategory("Investment", R.drawable.ic_investment),
                new IncomeCategory("Other", R.drawable.ic_others)
        );

        // Setup category bottom sheet
        categorySelector.setOnClickListener(v -> {
            IncomeCategoryBottomSheet bottomSheet = new IncomeCategoryBottomSheet(categoryList, selectedCategory -> {
                selectedCategoryText.setText(selectedCategory.getName());
                categoryIcon.setImageResource(selectedCategory.getIconResId());
            });
            bottomSheet.show(getChildFragmentManager(), "IncomeCategorySheet");
        });

        // Date-Time Picker
        selectDateLayout = view.findViewById(R.id.selectIncomeDateLayout);
        selectedDateText = view.findViewById(R.id.selectedIncomeDateText);
        Calendar calendar = Calendar.getInstance();
        updateDateTimeText(selectedDateText, calendar);

        selectDateLayout.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Income Date")
                    .setSelection(calendar.getTimeInMillis())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                calendar.setTimeInMillis(selection);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePicker = new TimePickerDialog(requireContext(), (view1, hourOfDay, minute1) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute1);
                    updateDateTimeText(selectedDateText, calendar);
                }, hour, minute, false);

                timePicker.show();
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER_INCOME");
        });

        // Add Income Button
        btnAddIncome.setOnClickListener(v -> {
            inputAmount.setError(null);
            inputTitle.setError(null);

            String title = inputTitle.getText().toString().trim();
            String amountStr = inputAmount.getText().toString().trim();
            String description = inputDescription.getText().toString().trim();
            String category = selectedCategoryText.getText().toString().trim();
            String dateTime = selectedDateText.getText().toString().trim();

            boolean hasError = false;
            double amount = 0;

            if (amountStr.isEmpty()) {
                inputAmount.setError("Amount is required");
                hasError = true;
            } else {
                try {
                    amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        inputAmount.setError("Enter a valid amount");
                        hasError = true;
                    }
                } catch (NumberFormatException e) {
                    inputAmount.setError("Amount must be a number");
                    hasError = true;
                }
            }

            if (category.equalsIgnoreCase("Select Category")) {
                Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                hasError = true;
            }

            if (dateTime.isEmpty()) {
                Toast.makeText(getContext(), "Please select a date and time", Toast.LENGTH_SHORT).show();
                hasError = true;
            }

            if (hasError) return;

            if (title.isEmpty()) {
                title = category;
            }

            Log.d(" === Income Data ===", "User Id: " + user_id + "\nTitle: " + title + " \nAmount: " + amount+ " \nCategory: " +category+ " \nDescription: " +description+ " \nDate and Time: " +dateTime);

            DbHelper dbHelper = new DbHelper(requireContext());
            boolean success = dbHelper.insertIncome(user_id, title, amount, category, description, formattedIncomeDateForDB);

            if (success) {
                Toast.makeText(getContext(), "Income added successfully", Toast.LENGTH_SHORT).show();
                inputTitle.setText("");
                inputAmount.setText("");
                inputDescription.setText("");
                selectedCategoryText.setText("Select Category");
                categoryIcon.setImageResource(R.drawable.ic_category_2);
                updateDateTimeText(selectedDateText, Calendar.getInstance());
            } else {
                Toast.makeText(getContext(), "Failed to add income", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void updateDateTimeText(TextView textView, Calendar calendar) {
        // Display format for UI
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
        textView.setText(displayFormat.format(calendar.getTime()));

        // SQLite-friendly format for DB
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        formattedIncomeDateForDB = dbFormat.format(calendar.getTime());
    }
}
