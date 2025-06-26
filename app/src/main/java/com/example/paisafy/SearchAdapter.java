package com.example.paisafy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisafy.Model.Transaction;
import com.example.paisafy.Model.ExpenseCategory;
import com.example.paisafy.Model.IncomeCategory;

import java.util.*;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private final List<Transaction> resultList;
    private final Context context;
    private final List<ExpenseCategory> expenseCategories;
    private final List<IncomeCategory> incomeCategories;
    private final String currencySymbol;

    public SearchAdapter(Context context, List<Transaction> resultList,
                         List<ExpenseCategory> expenseCategories,
                         List<IncomeCategory> incomeCategories) {
        this.context = context;
        this.resultList = resultList;
        this.expenseCategories = expenseCategories;
        this.incomeCategories = incomeCategories;

        // Inside constructor
        SharedPreferences prefs = context.getSharedPreferences("UserPrefsPaisafy", Context.MODE_PRIVATE);
        String currencyFull = prefs.getString("currency", "INR ₹");

        // Extract only the symbol (last part after space)
        if (currencyFull.contains(" ")) {
            this.currencySymbol = currencyFull.substring(currencyFull.lastIndexOf(" ") + 1);
        } else {
            this.currencySymbol = currencyFull; // fallback if no space
        }

    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Transaction transaction = resultList.get(position);

        holder.title.setText(transaction.getTitle());
        holder.date.setText(transaction.getDate());

        // Format amount with currency symbol
        String formattedAmount = (transaction.isExpense() ? "-" : "+") + currencySymbol + transaction.getAmount();
        holder.amount.setText(formattedAmount);
        holder.amount.setTextColor(transaction.isExpense() ? Color.RED : Color.GREEN);

        // Get proper icon
        int iconRes = getIconForCategory(transaction.getCategory(), transaction.isExpense());
        holder.icon.setImageResource(iconRes);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TransactionDetail.class);
            intent.putExtra("transaction_id", transaction.getId());
            intent.putExtra("transaction_type", transaction.isExpense() ? "expenses" : "income");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, amount;
        ImageView icon;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.transactionTitle);
            date = itemView.findViewById(R.id.transactionDate);
            amount = itemView.findViewById(R.id.transactionAmount);
            icon = itemView.findViewById(R.id.transactionIcon);
        }
    }

    private int getIconForCategory(String category, boolean isExpense) {
        if (isExpense) {
            for (ExpenseCategory c : expenseCategories) {
                if (c.getName().equalsIgnoreCase(category)) {
                    return c.getIconResId();
                }
            }
        } else {
            for (IncomeCategory c : incomeCategories) {
                if (c.getName().equalsIgnoreCase(category)) {
                    return c.getIconResId();
                }
            }
        }
        return R.drawable.ic_category; // Default icon
    }
}