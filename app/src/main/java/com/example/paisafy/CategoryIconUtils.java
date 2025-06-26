package com.example.paisafy;

import com.example.paisafy.R;

public class CategoryIconUtils {

    public static int getCategoryIcon(String category, boolean isExpense) {
        if (isExpense) {
            switch (category) {
                case "Food":
                    return R.drawable.ic_food;
                case "Transport":
                    return R.drawable.ic_transport;
                case "Shopping":
                    return R.drawable.ic_shopping;
                case "Health":
                    return R.drawable.ic_health;
                case "Entertainment":
                    return R.drawable.ic_entertainment;
                case "Bills":
                    return R.drawable.ic_bills;
                case "Travel":
                    return R.drawable.ic_travel;
                case "Groceries":
                    return R.drawable.ic_grocery;
                case "Education":
                    return R.drawable.ic_education;
                default:
                    return R.drawable.ic_others;
            }
        } else {
            switch (category) {
                case "Salary":
                    return R.drawable.ic_salary;
                case "Freelance":
                    return R.drawable.ic_freelancer;
                case "Gift":
                    return R.drawable.ic_gift;
                case "Investment":
                    return R.drawable.ic_investment;
                default:
                    return R.drawable.ic_others;
            }
        }
    }
}
