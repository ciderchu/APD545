package com.minifin.database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.minifin.model.Budget;
import com.minifin.model.Category;
import com.minifin.model.Transaction;
import com.minifin.model.User;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:resources/finance.db";
    private static DatabaseManager instance;
    private Connection connection;
    private ExecutorService executor;
    
    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            executor = Executors.newFixedThreadPool(5);
            initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void initializeDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create users table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL, " +
                "email TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Create categories table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "user_id INTEGER, " +
                "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            // Create transactions table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT NOT NULL, " +
                "description TEXT, " +
                "amount REAL NOT NULL, " +
                "type TEXT NOT NULL, " +
                "category_id INTEGER, " +
                "user_id INTEGER NOT NULL, " +
                "FOREIGN KEY (category_id) REFERENCES categories(id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            // Create budgets table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS budgets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "amount REAL NOT NULL, " +
                "period TEXT NOT NULL, " +
                "category_id INTEGER, " +
                "user_id INTEGER NOT NULL, " +
                "FOREIGN KEY (category_id) REFERENCES categories(id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id))");
        }
    }
    
    // User Methods
    public User createUser(String username, String password, String email) throws SQLException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return new User(id, username, password, email);
                }
            }
        }
        return null;
    }
    
    public User getUserById(long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                String email = rs.getString("email");
                return new User(id, username, password, email);
            }
        }
        return null;
    }
    
    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                long id = rs.getLong("id");
                String password = rs.getString("password");
                String email = rs.getString("email");
                return new User(id, username, password, email);
            }
        }
        return null;
    }
    
    // Category Methods
    public Category createCategory(String name, String type, long userId) throws SQLException {
        String sql = "INSERT INTO categories (name, type, user_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.setLong(3, userId);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return new Category(id, name, type, userId);
                }
            }
        }
        return null;
    }
    
    public List<Category> getCategoriesByType(String type, long userId) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE type = ? AND user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setLong(2, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                categories.add(new Category(id, name, type, userId));
            }
        }
        return categories;
    }
    
    public List<Category> getAllCategories(long userId) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                categories.add(new Category(id, name, type, userId));
            }
        }
        return categories;
    }
    
    // Transaction Methods
    public Transaction createTransaction(LocalDate date, String description, double amount, 
                                        String type, Long categoryId, long userId) throws SQLException {
        String sql = "INSERT INTO transactions (date, description, amount, type, category_id, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, date.toString());
            pstmt.setString(2, description);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, type);
            if (categoryId != null) {
                pstmt.setLong(5, categoryId);
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            pstmt.setLong(6, userId);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return new Transaction(id, date, description, amount, type, categoryId, userId);
                }
            }
        }
        return null;
    }
    
    public boolean deleteTransaction(long transactionId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, transactionId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public List<Transaction> getTransactions(long userId) throws SQLException {
        return getTransactions(userId, null, null);
    }
    
    public List<Transaction> getTransactions(long userId, LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM transactions WHERE user_id = ?");
        if (fromDate != null) {
            sqlBuilder.append(" AND date >= ?");
        }
        if (toDate != null) {
            sqlBuilder.append(" AND date <= ?");
        }
        sqlBuilder.append(" ORDER BY date DESC");
        
        try (PreparedStatement pstmt = connection.prepareStatement(sqlBuilder.toString())) {
            pstmt.setLong(1, userId);
            
            int paramIndex = 2;
            if (fromDate != null) {
                pstmt.setString(paramIndex++, fromDate.toString());
            }
            if (toDate != null) {
                pstmt.setString(paramIndex, toDate.toString());
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                long id = rs.getLong("id");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                String description = rs.getString("description");
                double amount = rs.getDouble("amount");
                String type = rs.getString("type");
                long categoryId = rs.getLong("category_id");
                
                transactions.add(new Transaction(id, date, description, amount, type, categoryId, userId));
            }
        }
        return transactions;
    }
    
    // Budget Methods
    public Budget createBudget(double amount, String period, long categoryId, long userId) throws SQLException {
        String sql = "INSERT INTO budgets (amount, period, category_id, user_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, period);
            pstmt.setLong(3, categoryId);
            pstmt.setLong(4, userId);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return new Budget(id, amount, period, categoryId, userId);
                }
            }
        }
        return null;
    }
    
    public Budget updateBudget(long budgetId, double amount, String period) throws SQLException {
        String sql = "UPDATE budgets SET amount = ?, period = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, period);
            pstmt.setLong(3, budgetId);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                return getBudgetById(budgetId);
            }
        }
        return null;
    }
    
    public Budget getBudgetById(long budgetId) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, budgetId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double amount = rs.getDouble("amount");
                String period = rs.getString("period");
                long categoryId = rs.getLong("category_id");
                long userId = rs.getLong("user_id");
                return new Budget(budgetId, amount, period, categoryId, userId);
            }
        }
        return null;
    }
    
    public List<Budget> getBudgetsByUserId(long userId) throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT b.*, c.name as category_name, c.type as category_type FROM budgets b " +
                     "JOIN categories c ON b.category_id = c.id " +
                     "WHERE b.user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                long id = rs.getLong("id");
                double amount = rs.getDouble("amount");
                String period = rs.getString("period");
                long categoryId = rs.getLong("category_id");
                
                Budget budget = new Budget(id, amount, period, categoryId, userId);
                budget.setCategoryName(rs.getString("category_name"));
                budget.setCategoryType(rs.getString("category_type"));
                budgets.add(budget);
            }
        }
        return budgets;
    }
    
    public boolean deleteBudget(long budgetId) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, budgetId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // Analytics and Reporting
    public double getTotalIncome(long userId, LocalDate fromDate, LocalDate toDate) throws SQLException {
        return getTransactionSum("Income", userId, fromDate, toDate);
    }
    
    public double getTotalExpense(long userId, LocalDate fromDate, LocalDate toDate) throws SQLException {
        return getTransactionSum("Expense", userId, fromDate, toDate);
    }
    
    private double getTransactionSum(String type, long userId, LocalDate fromDate, LocalDate toDate) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT SUM(amount) as total FROM transactions WHERE type = ? AND user_id = ?");
        
        if (fromDate != null) {
            sql.append(" AND date >= ?");
        }
        if (toDate != null) {
            sql.append(" AND date <= ?");
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            pstmt.setString(1, type);
            pstmt.setLong(2, userId);
            
            int paramIndex = 3;
            if (fromDate != null) {
                pstmt.setString(paramIndex++, fromDate.toString());
            }
            if (toDate != null) {
                pstmt.setString(paramIndex, toDate.toString());
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}