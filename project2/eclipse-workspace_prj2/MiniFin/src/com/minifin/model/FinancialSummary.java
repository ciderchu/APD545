package com.minifin.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinancialSummary {
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final double totalIncome;
    private final double totalExpense;
    private final double netBalance;
    private final Map<String, Double> incomeByCategory;
    private final Map<String, Double> expenseByCategory;
    private final List<Budget> budgets;

    public FinancialSummary(LocalDate fromDate, LocalDate toDate, double totalIncome, double totalExpense, 
                         Map<String, Double> incomeByCategory, Map<String, Double> expenseByCategory, 
                         List<Budget> budgets) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = totalIncome - totalExpense;
        this.incomeByCategory = incomeByCategory != null ? incomeByCategory : new HashMap<>();
        this.expenseByCategory = expenseByCategory != null ? expenseByCategory : new HashMap<>();
        this.budgets = budgets;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getNetBalance() {
        return netBalance;
    }

    public Map<String, Double> getIncomeByCategory() {
        return incomeByCategory;
    }

    public Map<String, Double> getExpenseByCategory() {
        return expenseByCategory;
    }
    
    public List<Budget> getBudgets() {
        return budgets;
    }
    
    public boolean isPositiveBalance() {
        return netBalance >= 0;
    }
    
    public String getTopIncomeCategory() {
        return getTopCategory(incomeByCategory);
    }
    
    public String getTopExpenseCategory() {
        return getTopCategory(expenseByCategory);
    }
    
    private String getTopCategory(Map<String, Double> categoryMap) {
        if (categoryMap.isEmpty()) {
            return "None";
        }
        
        String topCategory = null;
        double maxAmount = 0;
        
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            if (entry.getValue() > maxAmount) {
                maxAmount = entry.getValue();
                topCategory = entry.getKey();
            }
        }
        
        return topCategory;
    }
    
    public double getSavingsRate() {
        if (totalIncome == 0) {
            return 0;
        }
        return (netBalance / totalIncome) * 100;
    }
}