package com.minifin.model;

public class Budget {
    private final long id;
    private final double amount;
    private final String period;
    private final long categoryId;
    private final long userId;
    private String categoryName;
    private String categoryType;
    private double spent;
    private double remaining;
    private double usagePercentage;

    public Budget(long id, double amount, String period, long categoryId, long userId) {
        this.id = id;
        this.amount = amount;
        this.period = period;
        this.categoryId = categoryId;
        this.userId = userId;
        this.spent = 0;
        this.remaining = amount;
        this.usagePercentage = 0;
    }
    
    public Budget(double amount, String period, long categoryId, long userId) {
        this(-1, amount, period, categoryId, userId);
    }

    public long getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getPeriod() {
        return period;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public long getUserId() {
        return userId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }
    
    public double getSpent() {
        return spent;
    }
    
    public void setSpent(double spent) {
        this.spent = spent;
        this.remaining = amount - spent;
        this.usagePercentage = (spent / amount) * 100;
    }
    
    public double getRemaining() {
        return remaining;
    }
    
    public double getUsagePercentage() {
        return usagePercentage;
    }
    
    public boolean isOverBudget() {
        return spent > amount;
    }
    
    public boolean isNearLimit() {
        return usagePercentage >= 80 && usagePercentage < 100;
    }
}