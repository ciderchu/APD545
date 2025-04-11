/**********************************************
Project
Course: APD545
Last Name: Chu
First Name: Sin Kau
ID: 155131220
Section: NDD
This assignment represents my own work in accordance with Seneca Academic Policy.
Signature Sin Kau Chu
Date: 11-Apr-2025
**********************************************/


package com.finance.model;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL = "jdbc:sqlite:finance.db";
    private static DatabaseManager instance;
    private Connection connection;
    private ExecutorService executor;
    
    private DatabaseManager() {
        try {
            // Make sure SQLite JDBC driver is loaded
            Class.forName("org.sqlite.JDBC");
            // Create a connection to the database
            connection = DriverManager.getConnection(DB_URL);
            
            // Enable foreign keys in SQLite
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            
            // Initialize the database schema if it doesn't exist
            initializeDatabase();
            
            // Create thread pool for async database operations
            executor = Executors.newFixedThreadPool(4);
            
            LOGGER.info("Database connection established successfully");
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void initializeDatabase() {
        // Execute the SQL script to create tables
        try (Statement stmt = connection.createStatement()) {
            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password_hash TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");
                
            // Create categories table
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                "category_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "type TEXT NOT NULL CHECK (type IN ('INCOME', 'EXPENSE'))," +
                "color TEXT DEFAULT '#CCCCCC'," +
                "FOREIGN KEY (user_id) REFERENCES users(user_id)," +
                "UNIQUE(user_id, name, type)" +
                ")");
                
            // Create transactions table
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "category_id INTEGER NOT NULL," +
                "amount REAL NOT NULL CHECK (amount > 0)," +
                "description TEXT," +
                "transaction_date DATE NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(user_id)," +
                "FOREIGN KEY (category_id) REFERENCES categories(category_id)" +
                ")");
                
            // Create budgets table
            stmt.execute("CREATE TABLE IF NOT EXISTS budgets (" +
                "budget_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "category_id INTEGER NOT NULL," +
                "amount REAL NOT NULL CHECK (amount > 0)," +
                "period TEXT NOT NULL CHECK (period IN ('MONTHLY', 'WEEKLY', 'YEARLY'))," +
                "start_date DATE NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES users(user_id)," +
                "FOREIGN KEY (category_id) REFERENCES categories(category_id)," +
                "UNIQUE(user_id, category_id, period)" +
                ")");
                
            // Insert default user if not exists
            stmt.execute("INSERT OR IGNORE INTO users (user_id, username, password_hash) " +
                "VALUES (1, 'default', 'default_hash')");
                
            // Insert default categories if not exists
            String[] incomeCategories = {"Salary", "Bonus", "Investment", "Other"};
            String[] incomeColors = {"#4CAF50", "#8BC34A", "#CDDC39", "#9E9E9E"};
            
            String[] expenseCategories = {"Food", "Rent", "Utilities", "Transportation", 
                "Entertainment", "Healthcare", "Shopping", "Other"};
            String[] expenseColors = {"#FF5722", "#F44336", "#9C27B0", "#2196F3", 
                "#FF9800", "#03A9F4", "#E91E63", "#9E9E9E"};
                
            // Insert income categories
            for (int i = 0; i < incomeCategories.length; i++) {
                stmt.execute("INSERT OR IGNORE INTO categories (user_id, name, type, color) " +
                    "VALUES (1, '" + incomeCategories[i] + "', 'INCOME', '" + incomeColors[i] + "')");
            }
            
            // Insert expense categories
            for (int i = 0; i < expenseCategories.length; i++) {
                stmt.execute("INSERT OR IGNORE INTO categories (user_id, name, type, color) " +
                    "VALUES (1, '" + expenseCategories[i] + "', 'EXPENSE', '" + expenseColors[i] + "')");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating database schema", e);
            throw new RuntimeException("Failed to create database schema", e);
        }
    }
    
    // Close resources when application closes
    public void shutdown() {
        executor.shutdown();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing database connection", e);
        }
    }
    
    // Asynchronous database operations
    public <T> void executeAsync(DatabaseTask<T> task, Consumer<T> onSuccess, Consumer<Exception> onError) {
        executor.submit(() -> {
            try {
                T result = task.execute(connection);
                if (onSuccess != null) {
                    onSuccess.accept(result);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error executing database task", e);
                if (onError != null) {
                    onError.accept(e);
                }
            }
        });
    }
    
    // Interface for database tasks
    public interface DatabaseTask<T> {
        T execute(Connection connection) throws Exception;
    }
    
    // =========== Category operations ===========
    
    public List<Category> getCategories(int userId, Category.Type type) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE user_id = ? AND type = ? ORDER BY name";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, type.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(
                        rs.getInt("category_id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        Category.Type.valueOf(rs.getString("type")),
                        rs.getString("color")
                    ));
                }
            }
        }
        
        return categories;
    }
    
    public Category addCategory(Category category) throws SQLException {
        String sql = "INSERT INTO categories (user_id, name, type, color) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, category.getUserId());
            stmt.setString(2, category.getName());
            stmt.setString(3, category.getType().toString());
            stmt.setString(4, category.getColor());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setCategoryId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        }
        
        return category;
    }
    
    public void updateCategory(Category category) throws SQLException {
        String sql = "UPDATE categories SET name = ?, color = ? WHERE category_id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getColor());
            stmt.setInt(3, category.getCategoryId());
            stmt.setInt(4, category.getUserId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating category failed, no rows affected.");
            }
        }
    }
    
    public void deleteCategory(int categoryId, int userId) throws SQLException {
        String sql = "DELETE FROM categories WHERE category_id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            stmt.setInt(2, userId);
            
            stmt.executeUpdate();
        }
    }
    
    // =========== Transaction operations ===========
    
    public List<Transaction> getTransactions(int userId, LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, c.name as category_name, c.type as category_type, c.color as category_color " +
                    "FROM transactions t " +
                    "JOIN categories c ON t.category_id = c.category_id " +
                    "WHERE t.user_id = ? AND t.transaction_date BETWEEN ? AND ? " +
                    "ORDER BY t.transaction_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, fromDate.toString());
            stmt.setString(3, toDate.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getInt("user_id"),
                        rs.getInt("category_id"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        LocalDate.parse(rs.getString("transaction_date")),
                        rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    
                    // Set category for convenience
                    Category category = new Category(
                        rs.getInt("category_id"),
                        rs.getInt("user_id"),
                        rs.getString("category_name"),
                        Category.Type.valueOf(rs.getString("category_type")),
                        rs.getString("category_color")
                    );
                    transaction.setCategory(category);
                    
                    transactions.add(transaction);
                }
            }
        }
        
        return transactions;
    }
    
    public Transaction addTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, category_id, amount, description, transaction_date) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, transaction.getUserId());
            stmt.setInt(2, transaction.getCategoryId());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setString(4, transaction.getDescription());
            stmt.setString(5, transaction.getTransactionDate().toString());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating transaction failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setTransactionId(generatedKeys.getInt(1));
                    transaction.setCreatedAt(LocalDateTime.now());
                } else {
                    throw new SQLException("Creating transaction failed, no ID obtained.");
                }
            }
        }
        
        return transaction;
    }
    
    public void updateTransaction(Transaction transaction) throws SQLException {
        String sql = "UPDATE transactions SET category_id = ?, amount = ?, description = ?, " +
                    "transaction_date = ? WHERE transaction_id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, transaction.getCategoryId());
            stmt.setDouble(2, transaction.getAmount());
            stmt.setString(3, transaction.getDescription());
            stmt.setString(4, transaction.getTransactionDate().toString());
            stmt.setInt(5, transaction.getTransactionId());
            stmt.setInt(6, transaction.getUserId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating transaction failed, no rows affected.");
            }
        }
    }
    
    public void deleteTransaction(int transactionId, int userId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE transaction_id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            stmt.setInt(2, userId);
            
            stmt.executeUpdate();
        }
    }
    
    // =========== Budget operations ===========
    
    public List<Budget> getBudgets(int userId) throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT b.*, c.name as category_name, c.type as category_type, c.color as category_color " +
                    "FROM budgets b " +
                    "JOIN categories c ON b.category_id = c.category_id " +
                    "WHERE b.user_id = ? AND c.type = 'EXPENSE' " +
                    "ORDER BY b.amount DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Budget budget = new Budget(
                        rs.getInt("budget_id"),
                        rs.getInt("user_id"),
                        rs.getInt("category_id"),
                        rs.getDouble("amount"),
                        Budget.Period.valueOf(rs.getString("period")),
                        LocalDate.parse(rs.getString("start_date"))
                    );
                    
                    // Set category for convenience
                    Category category = new Category(
                        rs.getInt("category_id"),
                        rs.getInt("user_id"),
                        rs.getString("category_name"),
                        Category.Type.valueOf(rs.getString("category_type")),
                        rs.getString("category_color")
                    );
                    budget.setCategory(category);
                    
                    // Calculate current spending for this budget period
                    budget.setCurrentSpent(calculateCurrentSpending(budget));
                    
                    budgets.add(budget);
                }
            }
        }
        
        return budgets;
    }
    
    private double calculateCurrentSpending(Budget budget) throws SQLException {
        LocalDate startDate;
        LocalDate endDate = LocalDate.now();
        
        // Calculate the start date based on the budget period
        switch (budget.getPeriod()) {
            case WEEKLY:
                startDate = endDate.minusDays(endDate.getDayOfWeek().getValue() - 1);
                break;
            case MONTHLY:
                startDate = endDate.withDayOfMonth(1);
                break;
            case YEARLY:
                startDate = endDate.withDayOfYear(1);
                break;
            default:
                startDate = budget.getStartDate();
        }
        
        String sql = "SELECT SUM(amount) as total FROM transactions " +
                    "WHERE user_id = ? AND category_id = ? AND transaction_date BETWEEN ? AND ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, budget.getUserId());
            stmt.setInt(2, budget.getCategoryId());
            stmt.setString(3, startDate.toString());
            stmt.setString(4, endDate.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        
        return 0;
    }
    
    public Budget addBudget(Budget budget) throws SQLException {
        String sql = "INSERT INTO budgets (user_id, category_id, amount, period, start_date) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, budget.getUserId());
            stmt.setInt(2, budget.getCategoryId());
            stmt.setDouble(3, budget.getAmount());
            stmt.setString(4, budget.getPeriod().toString());
            stmt.setString(5, budget.getStartDate().toString());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating budget failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    budget.setBudgetId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating budget failed, no ID obtained.");
                }
            }
        }
        
        return budget;
    }
    
    public void updateBudget(Budget budget) throws SQLException {
        String sql = "UPDATE budgets SET amount = ?, period = ?, start_date = ? " +
                    "WHERE budget_id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, budget.getAmount());
            stmt.setString(2, budget.getPeriod().toString());
            stmt.setString(3, budget.getStartDate().toString());
            stmt.setInt(4, budget.getBudgetId());
            stmt.setInt(5, budget.getUserId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating budget failed, no rows affected.");
            }
        }
    }
    
    public void deleteBudget(int budgetId, int userId) throws SQLException {
        String sql = "DELETE FROM budgets WHERE budget_id = ? AND user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, budgetId);
            stmt.setInt(2, userId);
            
            stmt.executeUpdate();
        }
    }
    
    
 // / ========== Adding for the Sample Data / ==========
    public void insertSampleData() {
        try {
            // Current date to calculate past months
            LocalDate now = LocalDate.now();
            
            // Get category IDs for common categories
            int salaryId = getCategoryIdByName("Salary", 1); // Income
            int bonusId = getCategoryIdByName("Bonus", 1);   // Income
            int investmentId = getCategoryIdByName("Investment", 1); // Income
            
            int foodId = getCategoryIdByName("Food", 1);     // Expense
            int rentId = getCategoryIdByName("Rent", 1);     // Expense
            int entertainmentId = getCategoryIdByName("Entertainment", 1); // Expense
            int transportationId = getCategoryIdByName("Transportation", 1); // Expense
            
            // Sample transactions for current month
            addSampleTransaction(1, salaryId, 3500.00, "Monthly salary", now.withDayOfMonth(5));
            addSampleTransaction(1, foodId, 420.00, "Grocery shopping", now.withDayOfMonth(10));
            addSampleTransaction(1, rentId, 1200.00, "Monthly rent", now.withDayOfMonth(1));
            addSampleTransaction(1, entertainmentId, 85.00, "Movie night", now.withDayOfMonth(15));
            addSampleTransaction(1, transportationId, 150.00, "Gas", now.withDayOfMonth(8));
            addSampleTransaction(1, investmentId, 200.00, "Stock dividends", now.withDayOfMonth(22));
            
            // Last month
            LocalDate lastMonth = now.minusMonths(1);
            addSampleTransaction(1, salaryId, 3500.00, "Monthly salary", lastMonth.withDayOfMonth(5));
            addSampleTransaction(1, bonusId, 500.00, "Project completion bonus", lastMonth.withDayOfMonth(15));
            addSampleTransaction(1, foodId, 380.00, "Grocery shopping", lastMonth.withDayOfMonth(8));
            addSampleTransaction(1, rentId, 1200.00, "Monthly rent", lastMonth.withDayOfMonth(1));
            addSampleTransaction(1, entertainmentId, 120.00, "Concert tickets", lastMonth.withDayOfMonth(18));
            addSampleTransaction(1, transportationId, 180.00, "Gas and parking", lastMonth.withDayOfMonth(12));
            
            // Two months ago
            LocalDate twoMonthsAgo = now.minusMonths(2);
            addSampleTransaction(1, salaryId, 3500.00, "Monthly salary", twoMonthsAgo.withDayOfMonth(5));
            addSampleTransaction(1, foodId, 410.00, "Grocery shopping", twoMonthsAgo.withDayOfMonth(9));
            addSampleTransaction(1, rentId, 1200.00, "Monthly rent", twoMonthsAgo.withDayOfMonth(1));
            addSampleTransaction(1, entertainmentId, 65.00, "Streaming subscriptions", twoMonthsAgo.withDayOfMonth(14));
            addSampleTransaction(1, transportationId, 160.00, "Gas", twoMonthsAgo.withDayOfMonth(7));
            
            System.out.println("Sample data inserted successfully!");
        } catch (SQLException e) {
            System.err.println("Error inserting sample data: " + e.getMessage());
        }
    }

    // Helper methods
    private int getCategoryIdByName(String name, int userId) throws SQLException {
        String sql = "SELECT category_id FROM categories WHERE name = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }
        }
        // Default to "Other" category if not found
        return name.equals("Salary") ? 1 : (name.equals("Food") ? 4 : 11);
    }

    private void addSampleTransaction(int userId, int categoryId, double amount, 
                                     String description, LocalDate date) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, category_id, amount, description, transaction_date) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, categoryId);
            stmt.setDouble(3, amount);
            stmt.setString(4, description);
            stmt.setString(5, date.toString());
            
            stmt.executeUpdate();
        }
    }
    
    
    // =========== Financial Summary operations ===========
    
    public FinancialSummary getMonthlySummary(int userId, YearMonth yearMonth) throws SQLException {
        FinancialSummary summary = new FinancialSummary(yearMonth);
        
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // Get category totals for the month
        String sql = "SELECT t.category_id, c.name, c.type, c.color, SUM(t.amount) as total " +
                    "FROM transactions t " +
                    "JOIN categories c ON t.category_id = c.category_id " +
                    "WHERE t.user_id = ? AND t.transaction_date BETWEEN ? AND ? " +
                    "GROUP BY t.category_id";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, startDate.toString());
            stmt.setString(3, endDate.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Category category = new Category(
                        rs.getInt("category_id"),
                        userId,
                        rs.getString("name"),
                        Category.Type.valueOf(rs.getString("type")),
                        rs.getString("color")
                    );
                    
                    double amount = rs.getDouble("total");
                    summary.addCategoryTotal(category, amount);
                }
            }
        }
        
        return summary;
    }
}