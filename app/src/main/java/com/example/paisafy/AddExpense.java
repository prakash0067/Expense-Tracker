package com.example.paisafy;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paisafy.Model.ExpenseCategory;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpense extends Fragment {

    private ImageView categoryIcon;
    private TextView selectedCategoryText;
    private List<ExpenseCategory> categoryList;
    LinearLayout selectDateLayout;
    TextView selectedDateText;
    // At class level
    private String formattedDateForDB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        // Bind views
        categoryIcon = view.findViewById(R.id.categoryIcon);
        selectedCategoryText = view.findViewById(R.id.selectedCategoryText);
        View categorySelector = view.findViewById(R.id.selectCategoryLayout);

        TextInputEditText inputTitle = view.findViewById(R.id.inputTitle);
        TextInputEditText inputAmount = view.findViewById(R.id.inputAmount);
        TextInputEditText inputDescription = view.findViewById(R.id.inputDescription);
        Button btnAddExpense = view.findViewById(R.id.btnAddExpense);


        // fetching user id from shared pref
        int user_id = requireActivity().getSharedPreferences("UserPrefsPaisafy", getContext().MODE_PRIVATE)
                .getInt("user_id", 0);

        // Setup category list
        categoryList = new ArrayList<>();
        categoryList.add(new ExpenseCategory("Food", R.drawable.ic_food));
        categoryList.add(new ExpenseCategory("Transport", R.drawable.ic_transport));
        categoryList.add(new ExpenseCategory("Shopping", R.drawable.ic_shopping));
        categoryList.add(new ExpenseCategory("Health", R.drawable.ic_health));
        categoryList.add(new ExpenseCategory("Entertainment", R.drawable.ic_entertainment));
        categoryList.add(new ExpenseCategory("Bills", R.drawable.ic_bills));
        categoryList.add(new ExpenseCategory("Travel", R.drawable.ic_travel));
        categoryList.add(new ExpenseCategory("Groceries", R.drawable.ic_grocery));
        categoryList.add(new ExpenseCategory("Education", R.drawable.ic_education));
        categoryList.add(new ExpenseCategory("Others", R.drawable.ic_others));

        // Show category bottom sheet on click
        categorySelector.setOnClickListener(v -> {
            CategoryBottomSheet bottomSheet = new CategoryBottomSheet(categoryList, selectedCategory -> {
                selectedCategoryText.setText(selectedCategory.getName());
                categoryIcon.setImageResource(selectedCategory.getIconResId());
            });
            bottomSheet.show(getChildFragmentManager(), "CategoryBottomSheet");
        });

        // ===== Date and Time Picker
        selectDateLayout = view.findViewById(R.id.selectDateLayout);
        selectedDateText = view.findViewById(R.id.selectedDateText);

        // Default: current date-time
        Calendar calendar = Calendar.getInstance();
        updateDateTimeText(selectedDateText, calendar);

        // On click: open MaterialDatePicker
        selectDateLayout.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Expense Date")
                    .setSelection(calendar.getTimeInMillis())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                calendar.setTimeInMillis(selection);

                // After selecting date, open TimePickerDialog
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePicker = new TimePickerDialog(requireContext(), (view1, hourOfDay, minute1) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute1);

                    updateDateTimeText(selectedDateText, calendar);

                }, hour, minute, false);

                timePicker.show();
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        btnAddExpense.setOnClickListener(v -> {
            // Reset previous errors
            inputAmount.setError(null);
            inputTitle.setError(null);

            String title = inputTitle.getText().toString().trim();
            String amountStr = inputAmount.getText().toString().trim();
            String description = inputDescription.getText().toString().trim();
            String category = selectedCategoryText.getText().toString().trim();
            String dateTime = selectedDateText.getText().toString().trim();

            boolean hasError = false;

            // Validate amount
            if (amountStr.isEmpty()) {
                inputAmount.setError("Amount is required");
                hasError = true;
            }

            double amount = 0;
            if (!amountStr.isEmpty()) {
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

            // Validate category (just in case)
            if (category.equalsIgnoreCase("Select Category")) {
                Toast.makeText(getContext(), "Please select a valid category", Toast.LENGTH_SHORT).show();
                hasError = true;
            }

            // Validate date
            if (dateTime.isEmpty()) {
                Toast.makeText(getContext(), "Please select a date and time", Toast.LENGTH_SHORT).show();
                hasError = true;
            }

            if (hasError) return;

            // Set title same as category if left empty
            if (title.isEmpty()) {
                title = category;
            }

            // Insert into database
            Log.d(" === Expense Data ===", "User Id: " + user_id + "\nTitle: " + title + " \nAmount: " + amount+ " \nCategory: " +category+ " \nDescription: " +description+ " \nDate and Time: " +dateTime);

            DbHelper dbHelper = new DbHelper(requireContext());
            boolean success = dbHelper.insertExpense(user_id, amount, category, title, description, formattedDateForDB);

            if (success) {
                Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();

                // Clear inputs
                inputTitle.setText("");
                inputAmount.setText("");
                inputDescription.setText("");
                selectedCategoryText.setText("Select Category");
                categoryIcon.setImageResource(R.drawable.ic_category_2);
                Calendar now = Calendar.getInstance();
                updateDateTimeText(selectedDateText, now);
            } else {
                Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    private void updateDateTimeText(TextView textView, Calendar calendar) {
        // For display
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
        textView.setText(displayFormat.format(calendar.getTime()));

        // For database (correct format for SQLite)
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        formattedDateForDB = dbFormat.format(calendar.getTime());
    }
}