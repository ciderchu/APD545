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


package com.finance.view;

import com.finance.controller.*;
import com.finance.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.HPos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;


import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardView {
    private final TransactionController transactionController;
    private final BudgetController budgetController;
    private final ReportController reportController;
    
    private BorderPane view;
    private Label balanceLabel;
    private Label incomeLabel;
    private Label expenseLabel;
    private PieChart expenseChart;
    private BarChart<String, Number> trendChart;
    private VBox budgetProgressBox;
    private VBox recentTransactionsBox;
    
    private final YearMonth currentMonth = YearMonth.now();
    
    public DashboardView(TransactionController transactionController, 
                         BudgetController budgetController,
                         ReportController reportController) {
        this.transactionController = transactionController;
        this.budgetController = budgetController;
        this.reportController = reportController;
        
        createView();
        loadDashboardData();
    }
    
    public Node getView() {
        return view;
    }
    
    private void createView() {
        view = new BorderPane();
        view.setPadding(new Insets(15));
        
        // Top section - Summary figures
        HBox summaryBox = createSummarySection();
        
        // Center section - Charts
        GridPane centerGrid = new GridPane();
        centerGrid.setHgap(20);
        centerGrid.setVgap(20);
        centerGrid.setPadding(new Insets(10, 0, 10, 0));
        
        // Add expense distribution chart
        VBox expenseChartBox = new VBox(10);
        Label expenseChartTitle = new Label("Expense Distribution");
        expenseChartTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        expenseChart = new PieChart();
        expenseChart.setLabelsVisible(true);
        expenseChart.setLegendVisible(true);
        expenseChart.setTitle("");
        expenseChartBox.getChildren().addAll(expenseChartTitle, expenseChart);
        VBox.setVgrow(expenseChart, Priority.ALWAYS);
        
        // Add income/expense trend chart
        VBox trendChartBox = new VBox(10);
        Label trendChartTitle = new Label("Monthly Trend (Last 6 Months)");
        trendChartTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        trendChart = new BarChart<>(xAxis, yAxis);
        trendChart.setTitle("");
        xAxis.setLabel("Month");
        yAxis.setLabel("Amount");
        trendChart.setLegendVisible(true);
        trendChartBox.getChildren().addAll(trendChartTitle, trendChart);
        VBox.setVgrow(trendChart, Priority.ALWAYS);
        
        GridPane.setConstraints(expenseChartBox, 0, 0);
        GridPane.setConstraints(trendChartBox, 1, 0);
        
        centerGrid.getChildren().addAll(expenseChartBox, trendChartBox);
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        centerGrid.getColumnConstraints().addAll(col1, col2);
        
     // Create a more structured bottom layout
        HBox bottomBox = new HBox(20);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        bottomBox.setPrefWidth(Double.MAX_VALUE); // Make sure it uses all available width

        // Budget progress section - specify proper width
        VBox budgetBox = new VBox(10);
        budgetBox.setPrefWidth(400); // Set a fixed width
        budgetBox.setMinWidth(300); // Minimum width
        Label budgetTitle = new Label("Budget Progress");
        budgetTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        budgetProgressBox = new VBox(10);
        ScrollPane budgetScroll = new ScrollPane(budgetProgressBox);
        budgetScroll.setFitToWidth(true);
        budgetScroll.setPrefHeight(200);
        budgetScroll.setMinWidth(300); // Minimum width
        budgetScroll.setPrefWidth(400); // Preferred width
        budgetBox.getChildren().addAll(budgetTitle, budgetScroll);
        HBox.setHgrow(budgetBox, Priority.ALWAYS);
        
        // Recent transactions section
        VBox transactionsBox = new VBox(10);
        Label transactionsTitle = new Label("Recent Transactions");
        transactionsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        recentTransactionsBox = new VBox(5);
        ScrollPane transactionsScroll = new ScrollPane(recentTransactionsBox);
        transactionsScroll.setFitToWidth(true);
        transactionsScroll.setPrefHeight(200);
        transactionsBox.getChildren().addAll(transactionsTitle, transactionsScroll);
        HBox.setHgrow(transactionsBox, Priority.ALWAYS);
        
        bottomBox.getChildren().addAll(budgetBox, transactionsBox);
        
        // Assemble the main layout
        view.setTop(summaryBox);
        view.setCenter(centerGrid);
        view.setBottom(bottomBox);
    }
    
    private HBox createSummarySection() {
        HBox summaryBox = new HBox(30);
        summaryBox.setPadding(new Insets(0, 0, 15, 0));
        summaryBox.setAlignment(Pos.CENTER);
        
        // Current month label
        Label monthLabel = new Label(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        monthLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        // Current balance
        VBox balanceBox = new VBox(5);
        balanceBox.setAlignment(Pos.CENTER);
        Label balanceTitle = new Label("Current Balance");
        balanceTitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
        balanceLabel = new Label("$0.00");
        balanceLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        balanceBox.getChildren().addAll(balanceTitle, balanceLabel);
        
        // Monthly income
        VBox incomeBox = new VBox(5);
        incomeBox.setAlignment(Pos.CENTER);
        Label incomeTitle = new Label("Monthly Income");
        incomeTitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
        incomeLabel = new Label("$0.00");
        incomeLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        incomeLabel.setTextFill(Color.GREEN);
        incomeBox.getChildren().addAll(incomeTitle, incomeLabel);
        
        // Monthly expenses
        VBox expenseBox = new VBox(5);
        expenseBox.setAlignment(Pos.CENTER);
        Label expenseTitle = new Label("Monthly Expenses");
        expenseTitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
        expenseLabel = new Label("$0.00");
        expenseLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        expenseLabel.setTextFill(Color.RED);
        expenseBox.getChildren().addAll(expenseTitle, expenseLabel);
        
        summaryBox.getChildren().addAll(monthLabel, balanceBox, incomeBox, expenseBox);
        
        return summaryBox;
    }
    
    private void loadDashboardData() {
        // Load monthly summary data
        reportController.getMonthlySummary(currentMonth, 
            summary -> {
                Platform.runLater(() -> {
                    updateSummarySection(summary);
                    updateExpenseChart(summary);
                });
            },
            error -> {
                showError("Failed to load monthly summary", error);
            }
        );
        
        // Load trend data for the last 6 months
        YearMonth startMonth = currentMonth.minusMonths(5);
        reportController.getMonthlyTrend(startMonth, 6,
            summaries -> {
                Platform.runLater(() -> {
                    updateTrendChart(summaries);
                });
            },
            error -> {
                showError("Failed to load monthly trend", error);
            }
        );
        
        // Load budget progress
        budgetController.getBudgets(
            budgets -> {
                Platform.runLater(() -> {
                    updateBudgetProgress(budgets);
                });
            },
            error -> {
                showError("Failed to load budgets", error);
            }
        );
        
        // Load recent transactions
        LocalDate today = LocalDate.now();
        LocalDate oneMonthAgo = today.minusMonths(1);
        transactionController.getTransactions(oneMonthAgo, today,
            transactions -> {
                Platform.runLater(() -> {
                    updateRecentTransactions(transactions);
                });
            },
            error -> {
                showError("Failed to load recent transactions", error);
            }
        );
    }
    
    private void updateSummarySection(FinancialSummary summary) {
        // Format currency values with $ and 2 decimal places
        String incomeText = String.format("$%.2f", summary.getTotalIncome());
        String expenseText = String.format("$%.2f", summary.getTotalExpense());
        String balanceText = String.format("$%.2f", summary.getNetAmount());
        
        incomeLabel.setText(incomeText);
        expenseLabel.setText(expenseText);
        balanceLabel.setText(balanceText);
        
        // Set color for balance (green for positive, red for negative)
        if (summary.getNetAmount() >= 0) {
            balanceLabel.setTextFill(Color.GREEN);
        } else {
            balanceLabel.setTextFill(Color.RED);
        }
    }
    
    private void updateExpenseChart(FinancialSummary summary) {
        // Clear previous data
        expenseChart.getData().clear();
        
        // Create pie chart data from expense categories
        summary.getCategoryTotals().forEach((category, amount) -> {
            if (category.getType() == Category.Type.EXPENSE && amount > 0) {
                PieChart.Data slice = new PieChart.Data(category.getName(), amount);
                expenseChart.getData().add(slice);
                
                // Apply the category color if available
                slice.getNode().setStyle("-fx-pie-color: " + category.getColor() + ";");
            }
        });
    }
    
    private void updateTrendChart(List<FinancialSummary> summaries) {
        // Clear previous data
        trendChart.getData().clear();
        
        // Create series for income and expenses
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");
        
        // Add data points for each month
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
        for (FinancialSummary summary : summaries) {
            String month = summary.getPeriod().format(monthFormatter);
            incomeSeries.getData().add(new XYChart.Data<>(month, summary.getTotalIncome()));
            expenseSeries.getData().add(new XYChart.Data<>(month, summary.getTotalExpense()));
        }
        
        trendChart.getData().addAll(incomeSeries, expenseSeries);
    }
    
    private void updateBudgetProgress(List<Budget> budgets) {
        // Clear previous content
        budgetProgressBox.getChildren().clear();
        
        if (budgets == null || budgets.isEmpty()) {
            Label noDataLabel = new Label("No budgets defined. Visit the Budgets tab to create a budget.");
            noDataLabel.setWrapText(true);
            noDataLabel.setPadding(new Insets(10));
            budgetProgressBox.getChildren().add(noDataLabel);
            return;
        }
        
        // Add budget items
        for (Budget budget : budgets) {
            VBox budgetItem = createBudgetProgressItem(budget);
            budgetProgressBox.getChildren().add(budgetItem);
        }
    }
    
    private VBox createBudgetProgressItem(Budget budget) {
        // Create a simpler layout
        VBox item = new VBox(5);
        item.setPadding(new Insets(10));
        item.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-radius: 3px;");
        
        // Category name and amount
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(budget.getCategory().getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Label amountLabel = new Label(String.format("$%.2f / $%.2f", budget.getCurrentSpent(), budget.getAmount()));
        HBox.setHgrow(amountLabel, Priority.ALWAYS);
        amountLabel.setAlignment(Pos.CENTER_RIGHT);
        
        headerBox.getChildren().addAll(nameLabel, amountLabel);
        
        // Progress bar
        ProgressBar progressBar = new ProgressBar(budget.getPercentUsed() / 100.0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(15);
        
        // Set style based on percentage
        String color = "#4caf50"; // Green
        if (budget.getPercentUsed() >= 90) {
            color = "#f44336"; // Red
        } else if (budget.getPercentUsed() >= 70) {
            color = "#ff9800"; // Orange
        }
        progressBar.setStyle("-fx-accent: " + color + ";");
        
        item.getChildren().addAll(headerBox, progressBar);
        return item;
    }
    
    private void updateRecentTransactions(List<Transaction> transactions) {
        // Clear previous content
        recentTransactionsBox.getChildren().clear();
        
        if (transactions.isEmpty()) {
            Label noDataLabel = new Label("No recent transactions. Go to the Transactions tab to add one.");
            recentTransactionsBox.getChildren().add(noDataLabel);
            return;
        }
        
        // Add only the 10 most recent transactions
        int count = Math.min(transactions.size(), 10);
        for (int i = 0; i < count; i++) {
            HBox transactionItem = createTransactionItem(transactions.get(i));
            recentTransactionsBox.getChildren().add(transactionItem);
        }
    }
    
//    private HBox createTransactionItem(Transaction transaction) {
//        HBox item = new HBox(10);
//        item.setPadding(new Insets(5));
//        item.setAlignment(Pos.CENTER_LEFT);
//        
//        // Date
//        Label dateLabel = new Label(transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("MM/dd")));
//        dateLabel.setPrefWidth(50);
//        
//        // Category
//        Label categoryLabel = new Label(transaction.getCategory().getName());
//        categoryLabel.setPrefWidth(100);
//        
//        // Description
//        Label descLabel = new Label(transaction.getDescription());
//        HBox.setHgrow(descLabel, Priority.ALWAYS);
//        
//        // Create a fixed-width HBox to contain the amount
//        HBox amountBox = new HBox();
//        amountBox.setPrefWidth(100);
//        amountBox.setAlignment(Pos.CENTER_RIGHT);
//        
//        // Amount
//        Label amountLabel = new Label(String.format("$%.2f", transaction.getAmount()));
//        
//        // Color the amount based on transaction type
//        if (transaction.getCategory().getType() == Category.Type.INCOME) {
//            amountLabel.setTextFill(Color.GREEN);
//        } else {
//            amountLabel.setTextFill(Color.RED);
//        }
//        
//        amountBox.getChildren().add(amountLabel);
//        
//        item.getChildren().addAll(dateLabel, categoryLabel, descLabel, amountBox);
//        return item;
//    }
    
    private HBox createTransactionItem(Transaction transaction) {
        // Create HBox container
        HBox item = new HBox(10);
        item.setPadding(new Insets(5));
        item.setAlignment(Pos.CENTER_LEFT);
        
        // Date - fixed width
        Label dateLabel = new Label(transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("MM/dd")));
        dateLabel.setPrefWidth(50);
        dateLabel.setMinWidth(50);
        
        // Category - fixed width
        Label categoryLabel = new Label(transaction.getCategory().getName());
        categoryLabel.setPrefWidth(100);
        categoryLabel.setMinWidth(100);
        
        // Description - expandable
        HBox descBox = new HBox();
        HBox.setHgrow(descBox, Priority.ALWAYS);
        Label descLabel = new Label(transaction.getDescription());
        descBox.getChildren().add(descLabel);
        
        // Amount - fixed position
        HBox amountBox = new HBox();
        amountBox.setPrefWidth(100);
        amountBox.setMinWidth(100);
        amountBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label amountLabel = new Label(String.format("$%.2f", transaction.getAmount()));
        if (transaction.getCategory().getType() == Category.Type.INCOME) {
            amountLabel.setTextFill(Color.GREEN);
        } else {
            amountLabel.setTextFill(Color.RED);
        }
        amountBox.getChildren().add(amountLabel);
        
        item.getChildren().addAll(dateLabel, categoryLabel, descBox, amountBox);
        return item;
    }
    
    private void showError(String header, Exception error) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(header);
            alert.setContentText(error.getMessage());
            alert.showAndWait();
        });
    }
}