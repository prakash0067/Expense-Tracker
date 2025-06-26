# 💸 Paisafy - Smart Personal Expense Tracker (Offline, SQLite Based)

**Paisafy** is a modern and lightweight Android app for personal expense tracking. It empowers users to manage income, expenses, and budgets in a simplified, offline-first environment using **SQLite**, without the need for internet or server APIs.

> Built with attention to UI detail and user experience, it provides a clear overview of financial activity with a clean Material Design interface.

---

## 🌟 Features Overview

### ✅ Authentication
- **Login** and **Signup** with validation and error prompts.
- Email uniqueness check.
- Auto-fill login credentials after signup using Intent.

### 🏠 Home Dashboard
- Overview of **income**, **expenses**, and **total balance**.
- Filter transactions by **Daily**, **Monthly**, or **Yearly**.
- Real-time summary updates.

### ➕ Add Transactions
- Add **Income** or **Expense** with:
  - Title
  - Description
  - Date & Time
  - Category (with icon support)
- Stored locally in SQLite.

### 🧾 Transaction History
- Full transaction list with:
  - Category icons
  - Formatted currency
  - Date & time
  - Transaction type (Income/Expense)
- Tap to view **transaction detail** with delete option.

### 👤 User Profile
- Displays name, email, join date, and profile picture.
- Edit profile option included.

### 💼 Wallet Summary
- Shows current **balance**, **total income**, and **total expense**.
- Compares with last month's data (percentage difference).

### 🛠 Settings
- Change **currency symbol** (fully functional).
- Dark mode and notification toggle UI (UI ready, **not implemented yet**).

### 📌 Extras
- Built-in **To-Do List**
- **About Us** page
- All local data saved using **SQLite**
- Persistent user session with SharedPreferences

---

## 🖼 UI Snapshots (Material Design)
> *(Add screenshots here)*

- Clean layout with `MaterialCardView`, `ConstraintLayout`
- Icons for categories
- Stylish transaction cards
- Form input validation with `setError` and Material text fields

---

## 🛠️ Built With

| Tool           | Purpose                          |
|----------------|----------------------------------|
| Java           | Core application logic           |
| SQLite         | Offline data storage             |
| Material Design| Modern UI & components           |
| SharedPreferences | Session & currency storage   |
| ConstraintLayout & LinearLayout | Responsive UI  |

---

## 📂 Folder Structure
Paisafy/
├── activities/
│   ├── LoginActivity.java
│   ├── SignupActivity.java
│   └── TransactionDetailActivity.java
│
├── fragments/
│   ├── HomeFragment.java
│   ├── WalletFragment.java
│   └── ProfileFragment.java
│
├── adapters/
│   └── TransactionAdapter.java
│
├── database/
│   └── DbHelper.java
│
├── models/
│   └── Transaction.java
│
├── utils/
│   └── CurrencyUtil.java
│
├── res/
│   ├── layout/
│   ├── drawable/
│   └── values/
---

## 💾 Database Design

- Local **SQLite database** (no PHP or remote APIs).
- `users` table for user credentials and profile.
- `transactions` table for all incomes and expenses.

---

## 🔐 Validation Details

- **Signup validation**:
  - Name must not be empty
  - Email must be valid and not already registered
  - Password ≥ 6 characters
  - Terms & Conditions checkbox must be checked
- **Error Handling**:
  - Real-time `setError` messages
  - `Toast` for feedback
  - Inputs preserved on error

---

## ⚠️ Roadmap

The following features are **partially implemented or UI-ready**:

- [ ] Dark Mode toggle  
- [ ] Notifications (placeholder UI in settings)  
- [ ] Export transactions to PDF/CSV  
- [ ] Cloud backup & sync (future)

---

## 🙋 About the Developer

**Prakash Sirvi**  
📧 [personalprakash200@gmail.com](mailto:personalprakash200@gmail.com)  
💼 Android Developer | UI Enthusiast | Offline-first Advocate

---

## 📜 License

This project is open-source and available under the [MIT License](LICENSE).

---

## 🔗 GitHub Tags

`#Android` `#ExpenseTracker` `#MaterialDesign` `#SQLite` `#OfflineApp` `#PersonalFinance`