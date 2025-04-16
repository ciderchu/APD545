package com.minifin.model;

import java.time.LocalDate;

public class Transaction {
    private final long id;
    private final LocalDate date;
    private final String description;
    private final double amount;
    private final String type;
    private final Long categoryId;
    private final long userId;
    private String categoryName;

    public Transaction(long id, LocalDate date, String description, double amount, String type, Long categoryId, long userId) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.userId = userId;
    }
    
    public Transaction(LocalDate date, String description, double amount, String type, Long categoryId, long userId) {
        this(-1, date, description, amount, type, categoryId, userId);
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public Long getCategoryId() {
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
    
    @Override
    public String toString() {
        return String.format("%s: %s - $%.2f (%s)", date, description, amount, type);
    }
}