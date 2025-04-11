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
import javafx.concurrent.Task;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportController {
    private static final Logger LOGGER = Logger.getLogger(ReportController.class.getName());
    private final DatabaseManager dbManager;
    private final ExecutorService executor;
    private final int userId;

    public ReportController() {
        this.dbManager = DatabaseManager.getInstance();
        this.executor = Executors.newCachedThreadPool();
        this.userId = 1; // Using default user for now
    }

    public void shutdown() {
        executor.shutdown();
    }

    // Get monthly financial summary
    public void getMonthlySummary(YearMonth yearMonth, Consumer<FinancialSummary> onSuccess, Consumer<Exception> onError) {
        Task<FinancialSummary> task = new Task<>() {
            @Override
            protected FinancialSummary call() throws Exception {
                return dbManager.getMonthlySummary(userId, yearMonth);
            }
        };

        task.setOnSucceeded(event -> {
            onSuccess.accept(task.getValue());
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            LOGGER.log(Level.WARNING, "Failed to generate monthly summary", exception);
            onError.accept(new Exception("Failed to generate monthly summary", exception));
        });

        executor.submit(task);
    }

    // Get summaries for multiple months (for trend analysis)
    public void getMonthlyTrend(YearMonth startMonth, int numMonths, 
                              Consumer<List<FinancialSummary>> onSuccess, 
                              Consumer<Exception> onError) {
        Task<List<FinancialSummary>> task = new Task<>() {
            @Override
            protected List<FinancialSummary> call() throws Exception {
                List<FinancialSummary> summaries = new ArrayList<>();
                YearMonth currentMonth = startMonth;
                
                for (int i = 0; i < numMonths; i++) {
                    FinancialSummary summary = dbManager.getMonthlySummary(userId, currentMonth);
                    summaries.add(summary);
                    currentMonth = currentMonth.plusMonths(1);
                }
                
                return summaries;
            }
        };

        task.setOnSucceeded(event -> {
            onSuccess.accept(task.getValue());
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            LOGGER.log(Level.WARNING, "Failed to generate monthly trend", exception);
            onError.accept(new Exception("Failed to generate monthly trend", exception));
        });

        executor.submit(task);
    }
}