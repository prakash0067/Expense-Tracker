package com.example.paisafy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisafy.Model.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<Transaction> transactionList;
    private final String type; // "income" or "expenses"
    private final Context context;
    private final int defaultIconResId = R.drawable.ic_category_2;
    private final Map<String, Integer> categoryIconMap = new HashMap<>();
    private final String currencySymbol;

    public TransactionAdapter(Context context, List<Transaction> list, String type) {
        this.transactionList = list;
        this.type = type;
        this.context = context;
        this.currencySymbol = "₹";

        categoryIconMap.put("Food", R.drawable.ic_food);
        categoryIconMap.put("Transport", R.drawable.ic_transport);
        categoryIconMap.put("Shopping", R.drawable.ic_shopping);
        categoryIconMap.put("Health", R.drawable.ic_health);
        categoryIconMap.put("Entertainment", R.drawable.ic_entertainment);
        categoryIconMap.put("Bills", R.drawable.ic_bills);
        categoryIconMap.put("Travel", R.drawable.ic_travel);
        categoryIconMap.put("Groceries", R.drawable.ic_grocery);
        categoryIconMap.put("Education", R.drawable.ic_education);
        categoryIconMap.put("Others", R.drawable.ic_others);
        categoryIconMap.put("Salary", R.drawable.ic_salary);
        categoryIconMap.put("Freelance", R.drawable.ic_freelancer);
        categoryIconMap.put("Gift", R.drawable.ic_gift);
        categoryIconMap.put("Investment", R.drawable.ic_investment);
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.title.setText(transaction.getTitle());
        holder.date.setText(formatDate(transaction.getDate()));

        String prefix = type.equals("income") ? "+" : "-";
        int color = type.equals("income") ? 0xFF4CAF50 : 0xFFF44336;

        // Set amount with currency symbol
        holder.amount.setText(prefix + holder.currencySymbol + transaction.getAmount());
        holder.amount.setTextColor(color);

        int iconResId = categoryIconMap.getOrDefault(transaction.getCategory(), defaultIconResId);
        holder.icon.setImageResource(iconResId);

        // Open TransactionDetail activity on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TransactionDetail.class);
            intent.putExtra("transaction_id", transaction.getId());
            intent.putExtra("transaction_type", type); // "income" or "expenses"
            context.startActivity(intent);
        });
    }

    private String formatDate(String dateString) {
        return dateString; // You can format this if needed
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, amount;
        ImageView icon;
        String currencySymbol = "₹";

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.transactionTitle);
            date = itemView.findViewById(R.id.transactionDate);
            amount = itemView.findViewById(R.id.transactionAmount);
            icon = itemView.findViewById(R.id.transactionIcon);

            SharedPreferences prefs = itemView.getContext()
                    .getSharedPreferences("UserPrefsPaisafy", Context.MODE_PRIVATE);
            String fullCurrency = prefs.getString("currency", "INR ₹");

            if (fullCurrency.contains(" ")) {
                currencySymbol = fullCurrency.substring(fullCurrency.lastIndexOf(" ") + 1);
            } else {
                currencySymbol = fullCurrency;
            }
        }
    }
}