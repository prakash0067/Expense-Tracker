package com.example.paisafy.Model;

public class ExpenseCategory {
    private String name;
    private int iconResId;

    public ExpenseCategory(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public String getName() { return name; }
    public int getIconResId() { return iconResId; }
}
