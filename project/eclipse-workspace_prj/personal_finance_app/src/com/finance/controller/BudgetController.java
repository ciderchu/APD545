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


package com.finance.controller;

import com.finance.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BudgetController {
    private static final Logger LOGGER = Logger.getLogger(BudgetController.class.getName());
    private final DatabaseManager dbManager;
    private final ExecutorService executor;
    private final int userId;

    public BudgetController() {
        this.dbManager = DatabaseManager.getInstance();
        this.executor = Executors.newCachedThreadPool();
        this.userId = 1; // Using default user for now
    }

    public void shutdown() {
        executor.shutdown();
    }

    // Get all budgets for the user
    public void getBudgets(Consumer<ObservableList<Budget>> onSuccess, Consumer<Exception> onError) {
        Task<ObservableList<Budget>> task = new Task<>() {
            @Override
            protected ObservableList<Budget> call() throws Exception {
                List<Budget> budgets = dbManager.getBudgets(userId);
                return FXCollections.observableArrayList(budgets);
            }
        };

        task.setOnSucceeded(event -> {
            onSuccess.accept(task.getValue());
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            LOGGER.log(Level.WARNING, "Failed to load budgets", exception);
            onError.accept(new Exception("Failed to load budgets", exception));
        });

        executor.submit(task);
    }

    // Add a new budget
    public void addBudget(Budget budget, Runnable onSuccess, Consumer<Exception> onError) {
        dbManager.executeAsync(connection -> {
            budget.setUserId(userId);
            return dbManager.addBudget(budget);
        }, result -> {
            LOGGER.info("Added new budget for category: " + result.getCategoryId());
            onSuccess.run();
        }, exception -> {
            LOGGER.log(Level.WARNING, "Failed to add budget", exception);
            onError.accept(exception);
        });
    }

    // Update an existing budget
    public void updateBudget(Budget budget, Runnable onSuccess, Consumer<Exception> onError) {
        dbManager.executeAsync(connection -> {
            dbManager.updateBudget(budget);
            return budget;
        }, result -> {
            LOGGER.info("Updated budget: " + result.getBudgetId());
            onSuccess.run();
        }, exception -> {
            LOGGER.log(Level.WARNING, "Failed to update budget", exception);
            onError.accept(exception);
        });
    }

    // Delete a budget
    public void deleteBudget(Budget budget, Runnable onSuccess, Consumer<Exception> onError) {
        dbManager.executeAsync(connection -> {
            dbManager.deleteBudget(budget.getBudgetId(), userId);
            return null;
        }, result -> {
            LOGGER.info("Deleted budget: " + budget.getBudgetId());
            onSuccess.run();
        }, exception -> {
            LOGGER.log(Level.WARNING, "Failed to delete budget", exception);
            onError.accept(exception);
        });
    }

    // Check for budget warnings (spending over budget percentage)
    public void checkBudgetWarnings(double warningThreshold, Consumer<List<Budget>> onWarnings) {
        getBudgets(budgets -> {
            List<Budget> warnings = budgets.stream()
                .filter(budget -> budget.getPercentUsed() >= warningThreshold)
                .toList();
            
            if (!warnings.isEmpty()) {
                onWarnings.accept(warnings);
            }
        }, exception -> {
            LOGGER.log(Level.WARNING, "Failed to check budget warnings", exception);
        });
    }
}
