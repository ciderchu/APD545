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

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class FinancialSummary {
    private YearMonth period;
    private double totalIncome;
    private double totalExpense;
    private double netAmount;
    private Map<Category, Double> categoryTotals;
    
    public FinancialSummary(YearMonth period) {
        this.period = period;
        this.totalIncome = 0;
        this.totalExpense = 0;
        this.netAmount = 0;
        this.categoryTotals = new HashMap<>();
    }
    
    // Getters and setters
    public YearMonth getPeriod() { return period; }
    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { 
        this.totalIncome = totalIncome;
        updateNetAmount();
    }
    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double totalExpense) { 
        this.totalExpense = totalExpense;
        updateNetAmount();
    }
    public double getNetAmount() { return netAmount; }
    
    public Map<Category, Double> getCategoryTotals() { return categoryTotals; }
    
    public void addCategoryTotal(Category category, double amount) {
        categoryTotals.put(category, amount);
        if (category.getType() == Category.Type.INCOME) {
            totalIncome += amount;
        } else {
            totalExpense += amount;
        }
        updateNetAmount();
    }
    
    private void updateNetAmount() {
        this.netAmount = totalIncome - totalExpense;
    }
}