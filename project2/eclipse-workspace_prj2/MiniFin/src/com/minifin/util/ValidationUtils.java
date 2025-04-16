package com.minifin.util;

import java.time.LocalDate;

public class ValidationUtils {
    
    // Transaction validation
    public static boolean isValidTransaction(LocalDate date, String description, double amount, String type) {
        return isValidTransactionDate(date) && 
               isValidTransactionDescription(description) && 
               isValidTransactionAmount(amount) && 
               isValidTransactionType(type);
    }
    
    public static boolean isValidTransactionDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        LocalDate currentDate = LocalDate.now();
        // Date should not be in the future
        return !date.isAfter(currentDate);
    }
    
    public static boolean isValidTransactionDescription(String description) {
        return description != null && !description.trim().isEmpty() && description.length() <= 100;
    }
    
    public static boolean isValidTransactionAmount(double amount) {
        return amount > 0 && amount <= 1000000; // Assuming reasonable limits
    }
    
    public static boolean isValidTransactionType(String type) {
        return type != null && (type.equals("Income") || type.equals("Expense"));
    }
    
    // Budget validation
    public static boolean isValidBudget(double amount, String period, long categoryId) {
        return isValidBudgetAmount(amount) && isValidBudgetPeriod(period) && categoryId > 0;
    }
    
    public static boolean isValidBudgetAmount(double amount) {
        return amount > 0 && amount <= 1000000; // Assuming reasonable limits
    }
    
    public static boolean isValidBudgetPeriod(String period) {
        if (period == null) {
            return false;
        }
        return period.equals("Weekly") || period.equals("Monthly") || period.equals("Yearly");
    }
    
    // User validation
    public static boolean isValidUsername(String username) {
        return username != null && username.matches("[a-zA-Z0-9_]{3,30}");
    }
    
    public static boolean isValidPassword(String password) {
        // Password should be at least 8 characters and contain at least 
        // one digit, one lowercase letter, and one uppercase letter
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasDigit = false;
        boolean hasLower = false;
        boolean hasUpper = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isUpperCase(c)) {
                hasUpper = true;
            }
        }
        
        return hasDigit && hasLower && hasUpper;
    }
    
    public static boolean isValidEmail(String email) {
        // Simple email validation pattern
        return email != null && email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    }
    
    // Category validation
    public static boolean isValidCategoryName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() <= 50;
    }
    
    public static boolean isValidCategoryType(String type) {
        return type != null && (type.equals("Income") || type.equals("Expense"));
    }
    
    // Date range validation
    public static boolean isValidDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            return false;
        }
        return !fromDate.isAfter(toDate) && !toDate.isAfter(LocalDate.now());
    }
}