package com.example.paisafy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import com.example.paisafy.Model.Todo;
import com.example.paisafy.Model.Transaction;
import com.example.paisafy.Model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseTracker.db";
    private static final int DATABASE_VERSION = 6;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_INCOME = "income";
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_TODO = "todo";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // User table
        String createUserTable = "CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "profile_pic TEXT, " +
                "join_date TEXT DEFAULT CURRENT_TIMESTAMP" +
                ");";

        // Income table
        String createIncomeTable = "CREATE TABLE " + TABLE_INCOME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "title TEXT, " +
                "amount REAL NOT NULL, " +
                "category TEXT, " +
                "description TEXT, " +
                "date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id)" +
                ");";

        // Expenses table
        String createExpensesTable = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "title TEXT, " +
                "amount REAL NOT NULL, " +
                "category TEXT, " +
                "description TEXT, " +
                "date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id)" +
                ");";

        // To-do table
        String createTodoTable = "CREATE TABLE " + TABLE_TODO + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "task TEXT NOT NULL, " +
                "status TEXT DEFAULT 'Pending', " +
                "date TEXT, " +        // Format: YYYY-MM-DD
                "time TEXT, " +        // Format: HH:mm (24-hr)
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id)" +
                ");";

        // Execute queries
        db.execSQL(createUserTable);
        db.execSQL(createIncomeTable);
        db.execSQL(createExpensesTable);
        db.execSQL(createTodoTable);

        // Add a default user
        String insertDefaultUser = "INSERT INTO " + TABLE_USERS + " (name, email, password, profile_pic) " +
                "VALUES ('Prakash Sirvi', 'prakash@gmail.com', '12345678', NULL);";

        db.execSQL(insertDefaultUser);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCOME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Recreate tables
        onCreate(db);
    }

    // function to authenticate user
    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE email = ? AND password = ?";
        String[] selectionArgs = { email, password };

        try (Cursor cursor = db.rawQuery(query, selectionArgs)) {
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String userEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String userPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                String profilePic = cursor.getString(cursor.getColumnIndexOrThrow("profile_pic"));
                String joinDate = cursor.getString(cursor.getColumnIndexOrThrow("join_date"));

                return new User(id, name, userEmail, userPassword, profilePic, joinDate);
            }
        }

        return null;
    }

    // Returns total income for a user based on type: "daily", "monthly", "yearly"
    public double getTotalIncome(int userId, String type) {
        return getTotalAmount(TABLE_INCOME, userId, type);
    }

    // Returns total expenses for a user based on type: "daily", "monthly", "yearly"
    public double getTotalExpense(int userId, String type) {
        return getTotalAmount(TABLE_EXPENSES, userId, type);
    }

    // Private reusable function for summing amounts based on type and table
    private double getTotalAmount(String table, int userId, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;

        String whereClause = "user_id = ? AND ";
        String[] whereArgs;

        switch (type.toLowerCase()) {
            case "daily":
                whereClause += "date(date) = date('now')";
                whereArgs = new String[]{String.valueOf(userId)};
                break;
            case "monthly":
                whereClause += "strftime('%Y-%m', date) = strftime('%Y-%m', 'now')";
                whereArgs = new String[]{String.valueOf(userId)};
                break;
            case "yearly":
                whereClause += "strftime('%Y', date) = strftime('%Y', 'now')";
                whereArgs = new String[]{String.valueOf(userId)};
                break;
            default:
                return 0;
        }

        Cursor cursor = db.query(
                table,
                new String[]{"SUM(amount) AS total"},
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            cursor.close();
        }

        return total;
    }

    public List<Pair<String, Double>> getExpenseByCategory(String period, int userId) {
        List<Pair<String, Double>> data = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String dateCondition = "";
        switch (period.toLowerCase()) {
            case "daily":
                dateCondition = "date(date) = date('now')";
                break;
            case "monthly":
                dateCondition = "strftime('%Y-%m', date) = strftime('%Y-%m', 'now')";
                break;
            case "yearly":
                dateCondition = "strftime('%Y', date) = strftime('%Y', 'now')";
                break;
            default:
                dateCondition = "1=1"; // no filter
        }

        String query = "SELECT category, SUM(amount) as total FROM " + TABLE_EXPENSES +
                " WHERE user_id = ? AND " + dateCondition +
                " GROUP BY category";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                data.add(new Pair<>(category, total));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return data;
    }

    // Get user by ID
    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE id = ?";
        String[] selectionArgs = { String.valueOf(userId) };

        try (Cursor cursor = db.rawQuery(query, selectionArgs)) {
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                String profilePic = cursor.getString(cursor.getColumnIndexOrThrow("profile_pic"));
                String joinDate = cursor.getString(cursor.getColumnIndexOrThrow("join_date"));

                return new User(id, name, email, password, profilePic, joinDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // If user not found
    }

    public boolean insertExpense(int userId, double amount, String category, String title, String description, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("amount", amount);
        values.put("title", title);
        values.put("category", category);
        values.put("description", description);

        // Always use this line unless a custom date is passed
        if (date == null) {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        }

        values.put("date", date);

        long result = db.insert(TABLE_EXPENSES, null, values);
        return result != -1;
    }

    public List<Transaction> getRecentTransactions(int userId, String type, int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Transaction> transactions = new ArrayList<>();

        String table = type.equals("income") ? TABLE_INCOME : TABLE_EXPENSES;
        boolean isExpense = table.equals(TABLE_EXPENSES);

        String query = "SELECT * FROM " + table + " WHERE user_id = ? ORDER BY date DESC LIMIT ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(limit)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                int user_id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));

                transactions.add(new Transaction(id, title, category, amount, date, description, user_id, true));
            } while (cursor.moveToNext());

            cursor.close();
        }

        return transactions;
    }

    public boolean updateUserInfo(int userId, String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);

        int rowsAffected = db.update(
                "users",
                values,
                "id = ?",
                new String[]{String.valueOf(userId)}
        );

        return rowsAffected > 0;
    }

    public boolean updateUserProfilePicPath(int userId, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("profile_pic", imagePath);
        return db.update("users", values, "id=?", new String[]{String.valueOf(userId)}) > 0;
    }


    // to do list
    public boolean insertTodo(int userId, String task, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("task", task);
        values.put("date", date);
        values.put("time", time);
        values.put("status", "Pending");

        long result = db.insert(TABLE_TODO, null, values);
        return result != -1;
    }

    // Update status
    public boolean updateTodoStatus(int todoId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", newStatus);
        int result = db.update(TABLE_TODO, values, "id = ?", new String[]{String.valueOf(todoId)});
        return result > 0;
    }

    // Delete task
    public boolean deleteTodo(int todoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_TODO, "id = ?", new String[]{String.valueOf(todoId)});
        return result > 0;
    }

    public ArrayList<Todo> getTodosByUser(int userId) {
        ArrayList<Todo> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TODO + " WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String task = cursor.getString(cursor.getColumnIndexOrThrow("task"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                list.add(new Todo(id, userId, task, date, time, status));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // For Income Total
    public double getTotalIncome(String period, int userId) {
        String query = "SELECT SUM(amount) FROM " + TABLE_INCOME + " WHERE user_id = ?";

        if ("daily".equalsIgnoreCase(period)) {
            query += " AND date(date) = date('now')";
        } else if ("monthly".equalsIgnoreCase(period)) {
            query += " AND strftime('%m', date) = strftime('%m', 'now') AND strftime('%Y', date) = strftime('%Y', 'now')";
        } else if ("yearly".equalsIgnoreCase(period)) {
            query += " AND strftime('%Y', date) = strftime('%Y', 'now')";
        }

        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(userId)});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    // For Expense Total
    public double getTotalExpense(String period, int userId) {
        String query = "SELECT SUM(amount) FROM " + TABLE_EXPENSES + " WHERE user_id = ?";

        if ("daily".equalsIgnoreCase(period)) {
            query += " AND date(date) = date('now')";
        } else if ("monthly".equalsIgnoreCase(period)) {
            query += " AND strftime('%m', date) = strftime('%m', 'now') AND strftime('%Y', date) = strftime('%Y', 'now')";
        } else if ("yearly".equalsIgnoreCase(period)) {
            query += " AND strftime('%Y', date) = strftime('%Y', 'now')";
        }

        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(userId)});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public boolean insertIncome(int userId, String title, double amount, String category, String description, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", title);
        values.put("amount", amount);
        values.put("category", category);
        values.put("description", description);
        values.put("date", date);

        long result = db.insert(TABLE_INCOME, null, values);
        return result != -1;
    }

    public List<Transaction> searchTransactions(int userId, String query, String type) {
        List<Transaction> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + type +
                        " WHERE user_id = ? AND (title LIKE ? OR category LIKE ? OR description LIKE ?) ORDER BY date DESC",
                new String[]{String.valueOf(userId), "%" + query + "%", "%" + query + "%", "%" + query + "%"});

        if (cursor.moveToFirst()) {
            do {
                Transaction t = new Transaction();
                t.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                t.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                t.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                t.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
                t.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                t.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                t.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                t.setExpense(type.equals("expenses")); // true for expenses, false for income
                results.add(t);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return results;
    }

    public boolean deleteTransaction(int id, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        String table = type.equals("expenses") ? "expenses" : "income";
        int rows = db.delete(table, "id = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public Transaction getTransactionById(int id, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        String table = type.equals("expenses") ? "expenses" : "income";

        Cursor cursor = db.query(
                table,
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Transaction transaction = null;

        if (cursor != null && cursor.moveToFirst()) {
            transaction = new Transaction();
            transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            transaction.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
            transaction.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
            transaction.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
            transaction.setExpense(type.equals("expenses"));
        }

        if (cursor != null) cursor.close();
        return transaction;
    }

    public double getTotalAmountByUser(String type, int userId) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(amount) FROM " + type + " WHERE user_id = ? AND strftime('%Y-%m', date) = strftime('%Y-%m', 'now')";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public double getPreviousMonthTotalByUser(String type, int userId) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(amount) FROM " + type + " WHERE user_id = ? AND strftime('%Y-%m', date) = strftime('%Y-%m', 'now', '-1 month')";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

}
