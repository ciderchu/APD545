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

import java.time.LocalDate;

public class Budget {
    public enum Period {
        WEEKLY, MONTHLY, YEARLY
    }

    private int budgetId;
    private int userId;
    private int categoryId;
    private double amount;
    private Period period;
    private LocalDate startDate;
    
    // For UI convenience
    private Category category;
    private double currentSpent;
    private double remainingAmount;
    private double percentUsed;

    public Budget(int budgetId, int userId, int categoryId, double amount, 
                 Period period, LocalDate startDate) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.period = period;
        this.startDate = startDate;
    }

    // Getters and setters
    public int getBudgetId() { return budgetId; }
    public void setBudgetId(int budgetId) { this.budgetId = budgetId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public Period getPeriod() { return period; }
    public void setPeriod(Period period) { this.period = period; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { 
        this.category = category;
        this.categoryId = category.getCategoryId();
    }
    public double getCurrentSpent() { return currentSpent; }
    public void setCurrentSpent(double currentSpent) { 
        this.currentSpent = currentSpent;
        this.remainingAmount = amount - currentSpent;
        this.percentUsed = (currentSpent / amount) * 100;
    }
    public double getRemainingAmount() { return remainingAmount; }
    public double getPercentUsed() { return percentUsed; }
}
