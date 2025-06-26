package com.example.paisafy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paisafy.DbHelper;
import com.example.paisafy.Model.Todo;
import com.example.paisafy.R;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;

import android.widget.TextView;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    Context context;
    List<Todo> todoList;
    DbHelper dbHelper;
    OnTodoChangeListener listener;

    public TodoAdapter(Context context, List<Todo> todoList, DbHelper dbHelper, OnTodoChangeListener listener) {
        this.context = context;
        this.todoList = todoList;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todoList.get(position);

        holder.taskTitle.setText(todo.getTask());
        holder.taskDate.setText(todo.getDate());
        holder.taskTime.setText(todo.getTime());

        // Set checkbox status
        holder.checkCircle.setChecked(todo.getStatus().equalsIgnoreCase("Completed"));

        // Check/uncheck status
        holder.checkCircle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatus = isChecked ? "Completed" : "Pending";
            dbHelper.updateTodoStatus(todo.getId(), newStatus);
            todo.setStatus(newStatus);
            Toast.makeText(context, "Marked as " + newStatus, Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onTodoChanged(); // notify activity to update summary
            }
        });

        // Delete task
        holder.optionsMenu.setOnClickListener(v -> {
            boolean deleted = dbHelper.deleteTodo(todo.getId());
            if (deleted) {
                todoList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Task Deleted", Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    listener.onTodoChanged(); // notify activity to update summary
                }
            } else {
                Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDate, taskTime;
        MaterialCheckBox checkCircle;
        ImageView optionsMenu;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDate = itemView.findViewById(R.id.taskDate);
            taskTime = itemView.findViewById(R.id.taskTime);
            checkCircle = itemView.findViewById(R.id.checkCircle);
            optionsMenu = itemView.findViewById(R.id.optionsMenu);
        }
    }
}