package com.minifin.controller;

import com.minifin.database.DatabaseManager;
import com.minifin.model.Budget;
import com.minifin.model.Transaction;
import com.minifin.util.ValidationUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BudgetController {
    private final DatabaseManager dbManager;
    private final ExecutorService executor;
    private final long userId;

    public BudgetController(long userId) {
        this.dbManager = DatabaseManager.getInstance();
        this.executor = Executors.newFixedThreadPool(2);
        this.userId = userId;
    }

    public void addBudget(double amount, String period, long categoryId, 
                         Consumer<Budget> onSuccess, Consumer<Exception> onError) {
        
        // Validate inputs
        if (!ValidationUtils.isValidBudget(amount, period, categoryId)) {
            onError.accept(new IllegalArgumentException("Invalid budget data"));
            return;
        }

        Task<Budget> task = new Task<>() {
            @Override
            protected Budget call() throws Exception {
                return dbManager.createBudget(amount, period, categoryId, userId);
            }
        };

        task.setOnSucceeded(event -> {
            Budget budget = task.getValue();
            Platform.runLater(() -> onSuccess.accept(budget));
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> onError.accept(new Exception("Failed to add budget", exception)));
        });

        executor.submit(task);
    }

    public void updateBudget(long budgetId, double amount, String period, 
                            Consumer<Budget> onSuccess, Consumer<Exception> onError) {
        
        // Validate inputs
        if (!ValidationUtils.isValidBudgetAmount(amount) || !ValidationUtils.isValidBudgetPeriod(period)) {
            onError.accept(new IllegalArgumentException("Invalid budget data"));
            return;
        }

        Task<Budget> task = new Task<>() {
            @Override
            protected Budget call() throws Exception {
                return dbManager.updateBudget(budgetId, amount, period);
            }
        };

        task.setOnSucceeded(event -> {
            Budget budget = task.getValue();
            if (budget != null) {
                Platform.runLater(() -> onSuccess.accept(budget));
            } else {
                Platform.runLater(() -> onError.accept(new Exception("Budget not found")));
            }
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> onError.accept(new Exception("Failed to update budget", exception)));
        });

        executor.submit(task);
    }

    public void deleteBudget(long budgetId, Runnable onSuccess, Consumer<Exception> onError) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return dbManager.deleteBudget(budgetId);
            }
        };

        task.setOnSucceeded(event -> {
            boolean success = task.getValue();
            if (success) {
                Platform.runLater(onSuccess);
            } else {
                Platform.runLater(() -> onError.accept(new Exception("Budget not found")));
            }
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> onError.accept(new Exception("Failed to delete budget", exception)));
        });

        executor.submit(task);
    }

    public void getBudgets(Consumer<ObservableList<Budget>> onSuccess, Consumer<Exception> onError) {
        Task<ObservableList<Budget>> task = new Task<>() {
            @Override
            protected ObservableList<Budget> call() throws Exception {
                List<Budget> budgets = dbManager.getBudgetsByUserId(userId);
                return FXCollections.observableArrayList(budgets);
            }
        };

        task.setOnSucceeded(event -> {
            ObservableList<Budget> budgets = task.getValue();
            Platform.runLater(() -> onSuccess.accept(budgets));
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> onError.accept(new Exception("Failed to load budgets", exception)));
        });

        executor.submit(task);
    }
    
    public void calculateBudgetUsage(List<Budget> budgets, LocalDate fromDate, LocalDate toDate, 
                                   Consumer<ObservableList<Budget>> onSuccess, Consumer<Exception> onError) {
        Task<ObservableList<Budget>> task = new Task<>() {
            @Override
            protected ObservableList<Budget> call() throws Exception {
                // Get all transactions for the specified period
                List<Transaction> transactions = dbManager.getTransactions(userId, fromDate, toDate);
                
                // Calculate spent amount for each budget
                for (Budget budget : budgets) {
                    double spent = calculateSpentForCategory(transactions, budget.getCategoryId());
                    budget.setSpent(spent);
                }
                
                return FXCollections.observableArrayList(budgets);
            }
        };

        task.setOnSucceeded(event -> {
            ObservableList<Budget> updatedBudgets = task.getValue();
            Platform.runLater(() -> onSuccess.accept(updatedBudgets));
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> onError.accept(new Exception("Failed to calculate budget usage", exception)));
        });

        executor.submit(task);
    }
    
    private double calculateSpentForCategory(List<Transaction> transactions, long categoryId) {
        return transactions.stream()
            .filter(t -> {
                Long tCatId = t.getCategoryId();
                return "Expense".equals(t.getType()) && tCatId != null && tCatId == categoryId;
            })
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}