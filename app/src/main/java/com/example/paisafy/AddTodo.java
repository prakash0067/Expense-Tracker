package com.example.paisafy;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class AddTodo extends AppCompatActivity {

    TextInputEditText dateEditText, timeEditText;
    TextInputEditText titleEditText;
    MaterialButton saveButton;
    DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_todo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);

        // Show Date Picker
        dateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                dateEditText.setText(date);
            }, year, month, day);

            datePickerDialog.show();
        });

        // Show Time Picker
        timeEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(AddTodo.this, (view, hourOfDay, minute1) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                timeEditText.setText(time);
            }, hour, minute, true);

            timePickerDialog.show();
        });

        SharedPreferences preferences = getSharedPreferences("UserPrefsPaisafy", MODE_PRIVATE);
        int user_id = preferences.getInt("user_id", 0);

        titleEditText = findViewById(R.id.taskTitleEditText);
        saveButton = findViewById(R.id.saveTodoBtn);
        dbHelper = new DbHelper(this);

        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String date = dateEditText.getText().toString().trim();
            String time = timeEditText.getText().toString().trim();

            if (title.isEmpty()) {
                titleEditText.setError("Title is required");
                titleEditText.requestFocus();
                return;
            }

            if (date.isEmpty()) {
                dateEditText.setError("Date is required");
                dateEditText.requestFocus();
                return;
            }

            if (time.isEmpty()) {
                timeEditText.setError("Time is required");
                timeEditText.requestFocus();
                return;
            }

            boolean success = dbHelper.insertTodo(user_id, title, date, time);

            if (success) {
                Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
                finish(); // close the screen
            } else {
                Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show();
            }
        });

    }
}