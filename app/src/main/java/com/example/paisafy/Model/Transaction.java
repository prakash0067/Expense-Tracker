package com.example.paisafy.Model;

public class Transaction {
    private int id,user_id;
    private String title,description;
    private String category;
    private double amount;
    private String date;
    private boolean isExpense;

    public Transaction(int id, String title, String category, double amount, String date, String description, int userId, boolean isExpense) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.user_id = userId;
        this.isExpense = isExpense;
    }

    public Transaction() {}

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public int getUserId() { return user_id; }

    public boolean isExpense() { return isExpense; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int user_id) { this.user_id = user_id; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(String date) { this.date = date; }
    public void setDescription(String description) { this.description = description; }
    public void setExpense(boolean isExpense) { this.isExpense = isExpense; }
}
