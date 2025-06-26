package com.example.paisafy;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.paisafy.Model.ExpenseCategory;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class CategoryBottomSheet extends BottomSheetDialogFragment {

    public interface CategorySelectListener {
        void onCategorySelected(ExpenseCategory category);
    }

    private CategorySelectListener listener;
    private List<ExpenseCategory> categoryList;

    public CategoryBottomSheet(List<ExpenseCategory> categoryList, CategorySelectListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_bottom_sheet, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ExpenseCategoryAdapter adapter = new ExpenseCategoryAdapter(categoryList, category -> {
            listener.onCategorySelected(category);
            dismiss();
        });
        recyclerView.setAdapter(adapter);

        return view;
    }
}
