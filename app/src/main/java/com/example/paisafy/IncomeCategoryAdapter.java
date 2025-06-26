package com.example.paisafy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paisafy.Model.IncomeCategory;

import java.util.List;

public class IncomeCategoryAdapter extends RecyclerView.Adapter<IncomeCategoryAdapter.CategoryViewHolder> {
    private List<IncomeCategory> categoryList;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(IncomeCategory category);
    }

    public IncomeCategoryAdapter(List<IncomeCategory> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false); // Reusing the same layout
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        IncomeCategory category = categoryList.get(position);
        holder.txtName.setText(category.getName());
        holder.imgIcon.setImageResource(category.getIconResId());

        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageView imgIcon;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtCategoryName);
            imgIcon = itemView.findViewById(R.id.imgCategoryIcon);
        }
    }
}
