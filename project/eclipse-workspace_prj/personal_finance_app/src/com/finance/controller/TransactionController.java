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

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionController {
    private static final Logger LOGGER = Logger.getLogger(TransactionController.class.getName());
    private final DatabaseManager dbManager;
    private final ExecutorService executor;
    private final int userId;

    public TransactionController() {
        this.dbManager = DatabaseManager.getInstance();
        this.executor = Executors.newCachedThreadPool();
        this.userId = 1; // Using default user for now
    }

    public void shutdown() {
        executor.shutdown();
    }

    // Get categories for income or expense
    public void getCategories(Category.Type type, Consumer<ObservableList<Category>> onSuccess, Consumer<Exception> onError) {
        Task<ObservableList<Category>> task = new Task<>() {
            @Override
            protected ObservableList<Category> call() throws Exception {
                List<Category> categories = dbManager.getCategories(userId, type);
                return FXCollections.observableArrayList(categories);
            }
        };

        task.setOnSucceeded(event -> {
            onSuccess.accept(task.getValue());
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            LOGGER.log(Level.WARNING, "Failed to load categories", exception);
            onError.accept(new Exception("Failed to load categories", exception));
        });

        executor.submit(task);
    }

    // Add a new category
    public void addCategory(Category category, Runnable onSuccess, Consumer<Exception> onError) {
        dbManager.executeAsync(connection -> {
            category.setUserId(userId);
            return dbManager.addCategory(category);
        }, result -> {
            LOGGER.info("Added new category: " + result.getName());
            onSuccess.run();
        }, exception -> {
            LOGGER.log(Level.WARNING, "Failed to add category", exception);
            onError.accept(exception);
        });
    }

    // Get transactions for a date range
    public void getTransactions(LocalDate fromDate, LocalDate toDate, 
                               Consumer<ObservableList<Transaction>> onSuccess, 
                               Consumer<Exception> onError) {
        Task<ObservableList<Transaction>> task = new Task<>() {
            @Override
            protected ObservableList<Transaction> call() throws Exception {
                List<Transaction> transactions = dbManager.getTransactions(userId, fromDate, toDate);
                return FXCollections.observableArrayList(transactions);
            }
        };

        task.setOnSucceeded(event -> {
            onSuccess.accept(task.getValue());
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            LOGGER.log(Level.WARNING, "Failed to load transactions", exception);
            onError.accept(new Exception("Failed to load transactions", exception));
        });

        executor.submit(task);
    }

    // Add a new transaction
    public void addTransaction(Transaction transaction, Runnable onSuccess, Consumer<Exception> onError) {
        dbManager.executeAsync(connection -> {
            transaction.setUserId(userId);
            return dbManager.addTransaction(transaction);
        }, result -> {
            LOGGER.info("Added new transaction: " + result.getDescription());
            onSuccess.run();
        }, exception -> {
            LOGGER.log(Level.WARNING, "Failed to add transaction", exception);
            onError.accept(exception);
        });
    }

    // Update an existing transaction
    public void updateTransaction(Transaction transaction, Runnable onSuccess, Consumer<Exception> onError) {
        dbManager.executeAsync(connection -> {
            dbManager.updateTransaction(transaction);
            return transaction;
        }, result -> {
            LOGGER.info("Updated transaction: " + result.getTransactionId());
            onSuccess.run();
        }, exception -> {
            LOGGER.log(Level.WARNING, "Failed to update transaction", exception);
            onError.accept(exception);
        });
    }

    // Delete a transaction
    public void deleteTransaction(Transaction transaction, Runnable onSuccess, Consumer<Exception> onError) {
        dbManager.executeAsync(connection -> {
            dbManager.deleteTransaction(transaction.getTransactionId(), userId);
            return null;
        }, result -> {
            LOGGER.info("Deleted transaction: " + transaction.getTransactionId());
            onSuccess.run();
        }, exception -> {
            LOGGER.log(Level.WARNING, "Failed to delete transaction", exception);
            onError.accept(exception);
        });
    }
}
