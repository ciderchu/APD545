-- Users table (for future multi-user support)
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categories table for transaction categorization
CREATE TABLE IF NOT EXISTS categories (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    color TEXT DEFAULT '#CCCCCC',
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    UNIQUE(user_id, name, type)
);

-- Transactions table for income and expenses
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    amount REAL NOT NULL CHECK (amount > 0),
    description TEXT,
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- Budget table for setting spending limits
CREATE TABLE IF NOT EXISTS budgets (
    budget_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    amount REAL NOT NULL CHECK (amount > 0),
    period TEXT NOT NULL CHECK (period IN ('MONTHLY', 'WEEKLY', 'YEARLY')),
    start_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    UNIQUE(user_id, category_id, period)
);

-- Insert default user
INSERT OR IGNORE INTO users (user_id, username, password_hash) 
VALUES (1, 'default', 'default_hash');

-- Insert default categories
INSERT OR IGNORE INTO categories (category_id, user_id, name, type, color) 
VALUES 
(1, 1, 'Salary', 'INCOME', '#4CAF50'),
(2, 1, 'Bonus', 'INCOME', '#8BC34A'),
(3, 1, 'Investment', 'INCOME', '#CDDC39'),
(4, 1, 'Food', 'EXPENSE', '#FF5722'),
(5, 1, 'Rent', 'EXPENSE', '#F44336'),
(6, 1, 'Utilities', 'EXPENSE', '#9C27B0'),
(7, 1, 'Transportation', 'EXPENSE', '#2196F3'),
(8, 1, 'Entertainment', 'EXPENSE', '#FF9800'),
(9, 1, 'Healthcare', 'EXPENSE', '#03A9F4'),
(10, 1, 'Shopping', 'EXPENSE', '#E91E63'),
(11, 1, 'Other', 'EXPENSE', '#9E9E9E'),
(12, 1, 'Other', 'INCOME', '#9E9E9E');


/*
Schema Explanation
Users Table

user_id: Unique identifier for users (primary key)
username: User login name (unique)
password_hash: Hashed password for security
created_at: Timestamp when the user was created

Categories Table

category_id: Unique identifier for categories (primary key)
user_id: Reference to the user who owns this category
name: Category name (e.g., "Salary", "Food", "Rent")
type: Category type, either "INCOME" or "EXPENSE"
color: Hex color code for visual representation in charts
Has a unique constraint to prevent duplicate categories per user

Transactions Table

transaction_id: Unique identifier for transactions (primary key)
user_id: Reference to the user who made this transaction
category_id: Reference to the transaction category
amount: Transaction amount (must be positive)
description: Optional description of the transaction
transaction_date: Date when the transaction occurred
created_at: Timestamp when the transaction was recorded

Budgets Table

budget_id: Unique identifier for budgets (primary key)
user_id: Reference to the user who set this budget
category_id: Reference to the expense category for this budget
amount: Budget limit amount (must be positive)
period: Budget period, one of "MONTHLY", "WEEKLY", or "YEARLY"
start_date: Date when the budget period starts
Has a unique constraint to prevent multiple budgets for the same user, category, and period
*/