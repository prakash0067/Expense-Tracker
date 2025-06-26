package com.example.paisafy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisafy.Model.Todo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TodoList extends AppCompatActivity {

    FloatingActionButton addNewToDoBtn;
    RecyclerView recyclerView;
    TodoAdapter adapter;
    ArrayList<Todo> todoList;
    DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_todo_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Inflate custom toolbar layout
        View customView = getLayoutInflater().inflate(R.layout.toolbar_todo_custom, null);
        toolbar.addView(customView);

        // Set dynamic date
        TextView dateText = customView.findViewById(R.id.dateText);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM", Locale.getDefault());
        dateText.setText(sdf.format(new Date()));

        ImageView backBtn = customView.findViewById(R.id.backBtnIcon);
        backBtn.setOnClickListener(v -> onBackPressed());

        addNewToDoBtn = findViewById(R.id.addTaskButton);

        // ==== fab button action ====
        addNewToDoBtn.setOnClickListener(v -> {
            Intent intent = new Intent(TodoList.this,AddTodo.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.taskRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DbHelper(this);

        SharedPreferences preferences = getSharedPreferences("UserPrefsPaisafy", MODE_PRIVATE);
        int user_id = preferences.getInt("user_id", 0);

        todoList = dbHelper.getTodosByUser(user_id);
        adapter = new TodoAdapter(this, todoList, dbHelper, new OnTodoChangeListener() {
            @Override
            public void onTodoChanged() {
                updateSummaryCards(); // method to refresh top cards
            }
        });
        recyclerView.setAdapter(adapter);
        updateSummaryCards();
    }

    private void updateSummaryCards() {
        SharedPreferences preferences = getSharedPreferences("UserPrefsPaisafy", MODE_PRIVATE);
        int userId = preferences.getInt("user_id", 0);

        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        int totalToday = 0;
        int totalCompleted = 0;
        int totalPending = 0;
        int totalOverdue = 0;

        ArrayList<Todo> allTodos = dbHelper.getTodosByUser(userId);

        for (Todo todo : allTodos) {
            String status = todo.getStatus();
            String taskDate = todo.getDate();

            if ("Completed".equalsIgnoreCase(status)) {
                totalCompleted++;
                totalToday++;
            } else if ("Pending".equalsIgnoreCase(status)) {
                totalPending++;

                if (today.equals(taskDate)) {
                    totalToday++;
                } else {
                    try {
                        Date dueDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(taskDate);
                        if (dueDate != null && dueDate.before(new Date())) {
                            totalOverdue++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Update UI TextViews
        ((TextView) findViewById(R.id.todaysTasks)).setText(String.valueOf(totalToday));
        ((TextView) findViewById(R.id.pendingTasks)).setText(String.valueOf(totalPending));
        ((TextView) findViewById(R.id.completedTasks)).setText(String.valueOf(totalCompleted));
        ((TextView) findViewById(R.id.overdueTasks)).setText(String.valueOf(totalOverdue));
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = getSharedPreferences("UserPrefsPaisafy", MODE_PRIVATE);
        int user_id = preferences.getInt("user_id", 0);

        todoList.clear();
        todoList.addAll(dbHelper.getTodosByUser(user_id));
        adapter.notifyDataSetChanged();

        updateSummaryCards();
    }

}