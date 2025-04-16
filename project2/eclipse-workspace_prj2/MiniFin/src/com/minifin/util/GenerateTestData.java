package com.minifin.util;

import com.minifin.database.DatabaseManager;
import com.minifin.model.Category;
import com.minifin.model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Utility class to generate test data for the MiniFin application.
 * Run this class to populate the database with sample data.
 */
public class GenerateTestData {
    private static final DatabaseManager dbManager = DatabaseManager.getInstance();
    private static final Random random = new Random();
    
    // Category IDs - will be populated during execution
    private static final Map<String, Long> categories = new HashMap<>();
    
    public static void main(String[] args) {
        try {
            System.out.println("Starting test data generation...");
            
            // Create test user if it doesn't exist
            User user = dbManager.getUserByUsername("demo");
            if (user == null) {
                user = dbManager.createUser("demo", "Demo@123", "demo@example.com");
                System.out.println("Created demo user: " + user.getUsername());
            } else {
                System.out.println("Using existing demo user: " + user.getUsername());
            }
            
            long userId = user.getId();
            
            // Create categories if they don't exist
            createAndStoreCategories(userId);
            
            // Generate transactions
            generateIncomeTransactions(userId);
            generateExpenseTransactions(userId);
            
            // Create budgets
            createBudgets(userId);
            
            System.out.println("Test data generation completed successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error generating test data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            dbManager.shutdown();
        }
    }
    
    private static void createAndStoreCategories(long userId) throws SQLException {
        // Income categories
        String[] incomeCategories = {"Salary", "Freelance", "Investments", "Other Income"};
        for (String categoryName : incomeCategories) {
            Category category = getCategoryByName(categoryName, "Income", userId);
            categories.put(categoryName, category.getId());
        }
        
        // Expense categories
        String[] expenseCategories = {
            "Housing", "Utilities", "Groceries", "Transportation", "Entertainment", 
            "Healthcare", "Dining Out", "Shopping", "Personal Care", "Education", "Other Expenses"
        };
        for (String categoryName : expenseCategories) {
            Category category = getCategoryByName(categoryName, "Expense", userId);
            categories.put(categoryName, category.getId());
        }
        
        System.out.println("Categories created/retrieved and stored.");
    }
    
    private static Category getCategoryByName(String name, String type, long userId) throws SQLException {
        // Try to find existing category
        for (Category category : dbManager.getAllCategories(userId)) {
            if (category.getName().equals(name) && category.getType().equals(type)) {
                return category;
            }
        }
        
        // Create new category if not found
        return dbManager.createCategory(name, type, userId);
    }
    
    private static void generateIncomeTransactions(long userId) throws SQLException {
        LocalDate currentDate = LocalDate.now();
        
        // Generate salary transactions (monthly)
        for (int i = 0; i < 6; i++) {
            LocalDate date = currentDate.minusMonths(i).withDayOfMonth(random.nextInt(5) + 1);
            double amount = 3000 + (random.nextDouble() * 500);
            
            dbManager.createTransaction(
                date, 
                "Monthly Salary", 
                amount, 
                "Income", 
                categories.get("Salary"), 
                userId
            );
        }
        
        // Generate freelance income (random)
        for (int i = 0; i < 8; i++) {
            LocalDate date = currentDate.minusDays(random.nextInt(180));
            double amount = 200 + (random.nextDouble() * 800);
            
            dbManager.createTransaction(
                date, 
                "Freelance Project " + (i + 1), 
                amount, 
                "Income", 
                categories.get("Freelance"), 
                userId
            );
        }
        
        // Generate investment income (quarterly)
        for (int i = 0; i < 2; i++) {
            LocalDate date = currentDate.minusMonths(i * 3);
            double amount = 100 + (random.nextDouble() * 400);
            
            dbManager.createTransaction(
                date, 
                "Dividend Payment", 
                amount, 
                "Income", 
                categories.get("Investments"), 
                userId
            );
        }
        
        // Other income (random)
        for (int i = 0; i < 3; i++) {
            LocalDate date = currentDate.minusDays(random.nextInt(180));
            double amount = 50 + (random.nextDouble() * 150);
            
            dbManager.createTransaction(
                date, 
                "Miscellaneous Income", 
                amount, 
                "Income", 
                categories.get("Other Income"), 
                userId
            );
        }
        
        System.out.println("Generated income transactions.");
    }
    
    private static void generateExpenseTransactions(long userId) throws SQLException {
        LocalDate currentDate = LocalDate.now();
        
        // Housing (monthly)
        for (int i = 0; i < 6; i++) {
            LocalDate date = currentDate.minusMonths(i).withDayOfMonth(random.nextInt(5) + 1);
            double amount = 1200 + (random.nextDouble() * 100);
            
            dbManager.createTransaction(
                date, 
                "Rent/Mortgage", 
                amount, 
                "Expense", 
                categories.get("Housing"), 
                userId
            );
        }
        
        // Utilities (monthly)
        String[] utilities = {"Electricity", "Water", "Internet", "Phone"};
        for (int i = 0; i < 6; i++) {
            for (String utility : utilities) {
                if (random.nextDouble() > 0.05) { // 5% chance to skip (simulate missing bill)
                    LocalDate date = currentDate.minusMonths(i).withDayOfMonth(10 + random.nextInt(15));
                    double amount = 30 + (random.nextDouble() * 120);
                    
                    dbManager.createTransaction(
                        date, 
                        utility + " Bill", 
                        amount, 
                        "Expense", 
                        categories.get("Utilities"), 
                        userId
                    );
                }
            }
        }
        
        // Groceries (weekly)
        for (int i = 0; i < 24; i++) {
            LocalDate date = currentDate.minusDays(i * 7 + random.nextInt(3));
            double amount = 70 + (random.nextDouble() * 60);
            
            dbManager.createTransaction(
                date, 
                "Grocery Shopping", 
                amount, 
                "Expense", 
                categories.get("Groceries"), 
                userId
            );
        }
        
        // Transportation
        String[] transportationTypes = {"Gas", "Car Maintenance", "Public Transport", "Ride Share"};
        for (int i = 0; i < 15; i++) {
            LocalDate date = currentDate.minusDays(random.nextInt(180));
            double amount = 20 + (random.nextDouble() * 80);
            String type = transportationTypes[random.nextInt(transportationTypes.length)];
            
            dbManager.createTransaction(
                date, 
                type, 
                amount, 
                "Expense", 
                categories.get("Transportation"), 
                userId
            );
        }
        
        // Entertainment
        String[] entertainmentTypes = {"Movie", "Concert", "Subscription", "Games", "Books"};
        for (int i = 0; i < 12; i++) {
            LocalDate date = currentDate.minusDays(random.nextInt(180));
            double amount = 15 + (random.nextDouble() * 85);
            String type = entertainmentTypes[random.nextInt(entertainmentTypes.length)];
            
            dbManager.createTransaction(
                date, 
                type, 
                amount, 
                "Expense", 
                categories.get("Entertainment"), 
                userId
            );
        }
        
        // Healthcare
        for (int i = 0; i < 5; i++) {
            LocalDate date = currentDate.minusDays(random.nextInt(180));
            double amount = 30 + (random.nextDouble() * 170);
            
            dbManager.createTransaction(
                date, 
                "Medical Expense", 
                amount, 
                "Expense", 
                categories.get("Healthcare"), 
                userId
            );
        }
        
        // Dining Out
        for (int i = 0; i < 20; i++) {
            LocalDate date = currentDate.minusDays(random.nextInt(180));
            double amount = 20 + (random.nextDouble() * 80);
            
            dbManager.createTransaction(
                date, 
                "Restaurant/Cafe", 
                amount, 
                "Expense", 
                categories.get("Dining Out"), 
                userId
            );
        }
        
        // Shopping
        String[] shoppingTypes = {"Clothing", "Electronics", "Home Goods", "Gifts"};
        for (int i = 0; i < 10; i++) {
            LocalDate date = currentDate.minusDays(random.nextInt(180));
            double amount = 30 + (random.nextDouble() * 170);
            String type = shoppingTypes[random.nextInt(shoppingTypes.length)];
            
            dbManager.createTransaction(
                date, 
                type, 
                amount, 
                "Expense", 
                categories.get("Shopping"), 
                userId
            );
        }
        
        // Personal Care
        for (int i = 0; i < 8; i++) {
            LocalDate date = currentDate.minusDays(random.nextInt(180));
            double amount = 20 + (random.nextDouble() * 80);
            
            dbManager.createTransaction(
                date, 
                "Personal Care", 
                amount, 
                "Expense", 
                categories.get("Personal Care"), 
                userId
            );
        }
        
        // Education
        for (int i = 0; i < 3; i++) {
            LocalDate date = currentDate.minusDays(random.nextInt(180));
            double amount = 50 + (random.nextDouble() * 200);
            
            dbManager.createTransaction(
                date, 
                "Education Expense", 
                amount, 
                "Expense", 
                categories.get("Education"), 
                userId
            );
        }
        
        // Other Expenses
        for (int i = 0; i < 7; i++) {
            LocalDate date = currentDate.minusDays(random.nextInt(180));
            double amount = 10 + (random.nextDouble() * 90);
            
            dbManager.createTransaction(
                date, 
                "Miscellaneous Expense", 
                amount, 
                "Expense", 
                categories.get("Other Expenses"), 
                userId
            );
        }
        
        System.out.println("Generated expense transactions.");
    }
    
    private static void createBudgets(long userId) throws SQLException {
        // Create budgets for expense categories
        Map<String, Double> budgetAmounts = new HashMap<>();
        budgetAmounts.put("Housing", 1300.0);
        budgetAmounts.put("Utilities", 300.0);
        budgetAmounts.put("Groceries", 400.0);
        budgetAmounts.put("Transportation", 200.0);
        budgetAmounts.put("Entertainment", 150.0);
        budgetAmounts.put("Healthcare", 100.0);
        budgetAmounts.put("Dining Out", 200.0);
        budgetAmounts.put("Shopping", 200.0);
        budgetAmounts.put("Personal Care", 100.0);
        budgetAmounts.put("Education", 50.0);
        budgetAmounts.put("Other Expenses", 100.0);
        
        for (Map.Entry<String, Double> entry : budgetAmounts.entrySet()) {
            String categoryName = entry.getKey();
            double amount = entry.getValue();
            
            // Get category ID
            Long categoryId = categories.get(categoryName);
            if (categoryId != null) {
                dbManager.createBudget(amount, "Monthly", categoryId, userId);
            }
        }
        
        System.out.println("Created budget items.");
    }
}