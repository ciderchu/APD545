package com.minifin.controller;

import com.minifin.database.DatabaseManager;
import com.minifin.model.Transaction;
import com.minifin.util.ValidationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TransactionController {
    private final DatabaseManager dbManager;
    private final ExecutorService executor;
    private final long userId;

    public TransactionController(long userId) {
        this.dbManager = DatabaseManager.getInstance();
        this.executor = Executors.newFixedThreadPool(3);
        this.userId = userId;
    }

    public void addTransaction(LocalDate date, String description, double amount, String type, 
                              Long categoryId, Consumer<Transaction> onSuccess, Consumer<Exception> onError) {
        
        // Validate inputs
        if (!ValidationUtils.isValidTransaction(date, description, amount, type)) {
            onError.accept(new IllegalArgumentException("Invalid transaction data"));
            return;
        }

        Task<Transaction> task = new Task<>() {
            @Override
            protected Transaction call() throws Exception {
                return dbManager.createTransaction(date, description, amount, type, categoryId, userId);
            }
        };

        task.setOnSucceeded(event -> {
            Transaction txn = task.getValue();
            Platform.runLater(() -> onSuccess.accept(txn));
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> onError.accept(new Exception("Failed to add transaction", exception)));
        });

        executor.submit(task);
    }

    public void deleteTransaction(long transactionId, Runnable onSuccess, Consumer<Exception> onError) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return dbManager.deleteTransaction(transactionId);
            }
        };

        task.setOnSucceeded(event -> {
            boolean success = task.getValue();
            if (success) {
                Platform.runLater(onSuccess);
            } else {
                Platform.runLater(() -> onError.accept(new Exception("Transaction not found")));
            }
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> onError.accept(new Exception("Failed to delete transaction", exception)));
        });

        executor.submit(task);
    }

    public void getTransactions(Consumer<ObservableList<Transaction>> onSuccess, Consumer<Exception> onError) {
        getTransactions(null, null, onSuccess, onError);
    }

    public void getTransactions(LocalDate fromDate, LocalDate toDate,
                               Consumer<ObservableList<Transaction>> onSuccess, Consumer<Exception> onError) {
        
        Task<ObservableList<Transaction>> task = new Task<>() {
            @Override
            protected ObservableList<Transaction> call() throws Exception {
                List<Transaction> transactions = dbManager.getTransactions(userId, fromDate, toDate);
                return FXCollections.observableArrayList(transactions);
            }
        };

        task.setOnSucceeded(event -> {
            ObservableList<Transaction> transactions = task.getValue();
            Platform.runLater(() -> onSuccess.accept(transactions));
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> onError.accept(new Exception("Failed to load transactions", exception)));
        });

        executor.submit(task);
    }
    
    public void getTransactionsByMonth(int month, int year, Consumer<ObservableList<Transaction>> onSuccess, Consumer<Exception> onError) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
        
        getTransactions(startOfMonth, endOfMonth, onSuccess, onError);
    }
    
    public void getTransactionsByCategory(long categoryId, Consumer<ObservableList<Transaction>> onSuccess, Consumer<Exception> onError) {
        Task<ObservableList<Transaction>> task = new Task<>() {
            @Override
            protected ObservableList<Transaction> call() throws Exception {
                List<Transaction> allTransactions = dbManager.getTransactions(userId);
                List<Transaction> filteredTransactions = allTransactions.stream()
                    .filter(t -> {
                        Long tCatId = t.getCategoryId();
                        return tCatId != null && tCatId == categoryId;
                    })
                    .collect(Collectors.toList());
                return FXCollections.observableArrayList(filteredTransactions);
            }
        };

        task.setOnSucceeded(event -> {
            ObservableList<Transaction> transactions = task.getValue();
            Platform.runLater(() -> onSuccess.accept(transactions));
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> onError.accept(new Exception("Failed to load transactions by category", exception)));
        });

        executor.submit(task);
    }
    
    public void getCategoryTotals(String type, Consumer<Map<String, Double>> onSuccess, Consumer<Exception> onError) {
        Task<Map<String, Double>> task = new Task<>() {
            @Override
            protected Map<String, Double> call() throws Exception {
                List<Transaction> transactions = dbManager.getTransactions(userId);
                
                return transactions.stream()
                    .filter(t -> t.getType().equals(type) && t.getCategoryName() != null)
                    .collect(Collectors.groupingBy(
                        Transaction::getCategoryName,
                        Collectors.summingDouble(Transaction::getAmount)
                    ));
            }
        };

        task.setOnSucceeded(event -> {
            Map<String, Double> categoryTotals = task.getValue();
            Platform.runLater(() -> onSuccess.accept(categoryTotals));
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> onError.accept(new Exception("Failed to calculate category totals", exception)));
        });

        executor.submit(task);
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}