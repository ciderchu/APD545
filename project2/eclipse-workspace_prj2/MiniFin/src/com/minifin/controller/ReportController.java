package com.minifin.controller;

import com.minifin.database.DatabaseManager;
import com.minifin.model.Budget;
import com.minifin.model.FinancialSummary;
import com.minifin.model.Transaction;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ReportController {
    private final DatabaseManager dbManager;
    private final ExecutorService executor;
    private final BudgetController budgetController;
    private final long userId;

    public ReportController(long userId) {
        this.dbManager = DatabaseManager.getInstance();
        this.executor = Executors.newFixedThreadPool(2);
        this.budgetController = new BudgetController(userId);
        this.userId = userId;
    }

    public void generateMonthlySummary(int year, int month, 
                                     Consumer<FinancialSummary> onSuccess, Consumer<Exception> onError) {
        
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());
        
        generateSummary(startOfMonth, endOfMonth, onSuccess, onError);
    }
    
    public void generateYearlySummary(int year, 
                                    Consumer<FinancialSummary> onSuccess, Consumer<Exception> onError) {
        
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        
        generateSummary(startOfYear, endOfYear, onSuccess, onError);
    }
    
    public void generateCustomPeriodSummary(LocalDate fromDate, LocalDate toDate, 
                                          Consumer<FinancialSummary> onSuccess, Consumer<Exception> onError) {
        
        generateSummary(fromDate, toDate, onSuccess, onError);
    }

    private void generateSummary(LocalDate fromDate, LocalDate toDate, 
                               Consumer<FinancialSummary> onSuccess, Consumer<Exception> onError) {
        
        Task<FinancialSummary> task = new Task<>() {
            @Override
            protected FinancialSummary call() throws Exception {
                // Get total income and expense
                double totalIncome = dbManager.getTotalIncome(userId, fromDate, toDate);
                double totalExpense = dbManager.getTotalExpense(userId, fromDate, toDate);
                
                // Get transactions for category breakdown
                List<Transaction> transactions = dbManager.getTransactions(userId, fromDate, toDate);
                
                // Calculate income by category
                Map<String, Double> incomeByCategory = transactions.stream()
                    .filter(t -> "Income".equals(t.getType()) && t.getCategoryName() != null)
                    .collect(Collectors.groupingBy(
                        Transaction::getCategoryName,
                        Collectors.summingDouble(Transaction::getAmount)
                    ));
                
                // Calculate expense by category
                Map<String, Double> expenseByCategory = transactions.stream()
                    .filter(t -> "Expense".equals(t.getType()) && t.getCategoryName() != null)
                    .collect(Collectors.groupingBy(
                        Transaction::getCategoryName,
                        Collectors.summingDouble(Transaction::getAmount)
                    ));
                
                // Get budget information
                List<Budget> budgets = dbManager.getBudgetsByUserId(userId);
                
                // Calculate spent amount for each budget
                for (Budget budget : budgets) {
                    double spent = transactions.stream()
                        .filter(t -> {
                            Long tCatId = t.getCategoryId();
                            return "Expense".equals(t.getType()) && tCatId != null && tCatId == budget.getCategoryId();
                        })
                        .mapToDouble(Transaction::getAmount)
                        .sum();
                    budget.setSpent(spent);
                }
                
                return new FinancialSummary(fromDate, toDate, totalIncome, totalExpense, 
                                        incomeByCategory, expenseByCategory, budgets);
            }
        };

        task.setOnSucceeded(event -> {
            FinancialSummary summary = task.getValue();
            Platform.runLater(() -> onSuccess.accept(summary));
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> onError.accept(new Exception("Failed to generate summary", exception)));
        });

        executor.submit(task);
    }
    
    public void getMonthlyTrend(int year, 
                              Consumer<Map<String, Double[]>> onSuccess, Consumer<Exception> onError) {
        
        Task<Map<String, Double[]>> task = new Task<>() {
            @Override
            protected Map<String, Double[]> call() throws Exception {
                Map<String, Double[]> monthlyData = new HashMap<>();
                
                // Initialize arrays for income and expense (12 months)
                Double[] monthlyIncome = new Double[12];
                Double[] monthlyExpense = new Double[12];
                Double[] monthlyBalance = new Double[12];
                
                for (int i = 0; i < 12; i++) {
                    LocalDate startOfMonth = LocalDate.of(year, i + 1, 1);
                    LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());
                    
                    double income = dbManager.getTotalIncome(userId, startOfMonth, endOfMonth);
                    double expense = dbManager.getTotalExpense(userId, startOfMonth, endOfMonth);
                    double balance = income - expense;
                    
                    monthlyIncome[i] = income;
                    monthlyExpense[i] = expense;
                    monthlyBalance[i] = balance;
                }
                
                monthlyData.put("income", monthlyIncome);
                monthlyData.put("expense", monthlyExpense);
                monthlyData.put("balance", monthlyBalance);
                
                return monthlyData;
            }
        };

        task.setOnSucceeded(event -> {
            Map<String, Double[]> monthlyData = task.getValue();
            Platform.runLater(() -> onSuccess.accept(monthlyData));
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> onError.accept(new Exception("Failed to generate monthly trend", exception)));
        });

        executor.submit(task);
    }
    
    public void shutdown() {
        executor.shutdown();
        budgetController.shutdown();
    }
}