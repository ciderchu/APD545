package com.minifin.view;

import com.minifin.controller.BudgetController;
import com.minifin.controller.TransactionController;
import com.minifin.model.Budget;
import com.minifin.model.Transaction;
import com.minifin.model.User;
import com.minifin.util.NotificationUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.Map;

public class DashboardView extends VBox {
    private final User currentUser;
    private final TransactionController transactionController;
    private final BudgetController budgetController;
    
    private Label incomeLabel;
    private Label expenseLabel;
    private Label balanceLabel;
    private TableView<Transaction> recentTransactionsTable;
    private VBox budgetProgressBox;

    public DashboardView(User currentUser, TransactionController transactionController, BudgetController budgetController) {
        this.currentUser = currentUser;
        this.transactionController = transactionController;
        this.budgetController = budgetController;
        
        setPadding(new Insets(30));
        setSpacing(30);
        setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Dashboard Overview");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        // Create income/expense/balance cards
        HBox summaryBox = createSummaryBox();
        
        // Recent transactions
        VBox recentTransactionsBox = createRecentTransactionsBox();
        
        // Budget progress section
        VBox budgetBox = createBudgetProgressBox();

        getChildren().addAll(title, summaryBox, recentTransactionsBox, budgetBox);
        
        // Load data
        loadDashboardData();
    }

    private HBox createSummaryBox() {
        HBox summaryBox = new HBox(20);
        summaryBox.setAlignment(Pos.CENTER);

        incomeLabel = createSummaryCard("Income", "$0.00", "-fx-background-color: #e8f5e9;");
        expenseLabel = createSummaryCard("Expense", "$0.00", "-fx-background-color: #ffebee;");
        balanceLabel = createSummaryCard("Balance", "$0.00", "-fx-background-color: #e3f2fd;");

        summaryBox.getChildren().addAll(incomeLabel, expenseLabel, balanceLabel);
        return summaryBox;
    }
    
    private VBox createRecentTransactionsBox() {
        VBox recentTxnBox = new VBox(10);
        recentTxnBox.setAlignment(Pos.TOP_LEFT);
        
        Label sectionTitle = new Label("Recent Transactions");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        recentTransactionsTable = new TableView<>();
        recentTransactionsTable.setPlaceholder(new Label("No recent transactions"));
        
        // Set fixed height to show exactly 5 rows + header
        recentTransactionsTable.setFixedCellSize(35);
        recentTransactionsTable.setPrefHeight(5 * recentTransactionsTable.getFixedCellSize() + 30);
        recentTransactionsTable.setMinHeight(recentTransactionsTable.getPrefHeight());
        recentTransactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    Transaction txn = getTableView().getItems().get(getIndex());
                    setText(NumberFormat.getCurrencyInstance(Locale.US).format(amount));
                    setTextFill("Income".equalsIgnoreCase(txn.getType()) ? Color.GREEN : Color.CRIMSON);
                }
            }
        });
        
        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        recentTransactionsTable.getColumns().addAll(dateCol, descCol, amountCol, typeCol);
        
        Button viewAllBtn = new Button("View All Transactions");
        viewAllBtn.setOnAction(e -> {
            // Get the MainLayout parent through the scene graph
            // Fix for the error when using ScrollPane
            BorderPane borderPane = (BorderPane) getScene().getRoot();
            if (borderPane instanceof MainLayout) {
                MainLayout mainLayout = (MainLayout) borderPane;
                mainLayout.showTransactions();
            }
        });
        
        recentTxnBox.getChildren().addAll(sectionTitle, recentTransactionsTable, viewAllBtn);
        recentTxnBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #DDD; -fx-border-radius: 5;");
        
        return recentTxnBox;
    }
    
    private VBox createBudgetProgressBox() {
        VBox budgetBox = new VBox(10);
        budgetBox.setAlignment(Pos.TOP_LEFT);
        
        Label sectionTitle = new Label("Budget Progress");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        budgetProgressBox = new VBox(15);
        
        Button viewAllBtn = new Button("Manage Budgets");
        viewAllBtn.setOnAction(e -> {
            // Get the MainLayout parent through the scene graph
            // Fix for the error when using ScrollPane
            BorderPane borderPane = (BorderPane) getScene().getRoot();
            if (borderPane instanceof MainLayout) {
                MainLayout mainLayout = (MainLayout) borderPane;
                mainLayout.showBudgets();
            }
        });
        
        budgetBox.getChildren().addAll(sectionTitle, budgetProgressBox, viewAllBtn);
        budgetBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #DDD; -fx-border-radius: 5;");
        
        return budgetBox;
    }

    private Label createSummaryCard(String label, String value, String extraStyle) {
        Label card = new Label(label + "\n" + value);
        card.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        card.setStyle("-fx-border-color: #DDD; -fx-border-radius: 8; -fx-padding: 20; " +
                     "-fx-background-radius: 8; -fx-background-color: #FAFAFA; " + extraStyle);
        card.setMinSize(180, 100);
        card.setMaxWidth(200);
        card.setAlignment(Pos.CENTER);
        card.setWrapText(true);
        return card;
    }
    
    private void loadDashboardData() {
        // Current month date range
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());
        
        // Load summary data
        transactionController.getTransactions(firstDayOfMonth, lastDayOfMonth, transactions -> {
            // Calculate totals
            double totalIncome = transactions.stream()
                .filter(t -> "Income".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
                
            double totalExpense = transactions.stream()
                .filter(t -> "Expense".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
                
            double balance = totalIncome - totalExpense;
            
            // Update summary cards
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            incomeLabel.setText("Income\n" + currencyFormat.format(totalIncome));
            expenseLabel.setText("Expense\n" + currencyFormat.format(totalExpense));
            balanceLabel.setText("Balance\n" + currencyFormat.format(balance));
            
            // Set recent transactions
            ObservableList<Transaction> recentTxns = FXCollections.observableArrayList(transactions);
            recentTxns.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));
            
            if (recentTxns.size() > 5) {
                recentTxns = FXCollections.observableArrayList(recentTxns.subList(0, 5));
            }
            
            recentTransactionsTable.setItems(recentTxns);
            
        }, ex -> {
            NotificationUtils.showError("Error", "Failed to load transaction data: " + ex.getMessage());
        });
        
        // Load budget progress
        budgetController.getBudgets(budgets -> {
            // Calculate spent amount for each budget
            budgetController.calculateBudgetUsage(budgets, firstDayOfMonth, lastDayOfMonth, updatedBudgets -> {
                budgetProgressBox.getChildren().clear();
                
                if (updatedBudgets.isEmpty()) {
                    budgetProgressBox.getChildren().add(new Label("No budgets set up yet."));
                    return;
                }
                
                // Create a progress bar for each budget
                for (Budget budget : updatedBudgets) {
                    // Skip non-expense budgets
                    if (!"Expense".equals(budget.getCategoryType())) {
                        continue;
                    }
                    
                    VBox budgetItem = createBudgetProgressItem(budget);
                    budgetProgressBox.getChildren().add(budgetItem);
                    
                    // Only show top 5 budgets
                    if (budgetProgressBox.getChildren().size() >= 5) break;
                }
                
                // Check for budget warnings
                NotificationUtils.checkBudgetWarnings(updatedBudgets);
                
            }, ex -> {
                NotificationUtils.showError("Error", "Failed to calculate budget usage: " + ex.getMessage());
            });
        }, ex -> {
            NotificationUtils.showError("Error", "Failed to load budget data: " + ex.getMessage());
        });
    }
    
    private VBox createBudgetProgressItem(Budget budget) {
        VBox budgetItem = new VBox(5);
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        String budgetName = budget.getCategoryName();
        double amount = budget.getAmount();
        double spent = budget.getSpent();
        double percentage = budget.getUsagePercentage();
        
        HBox labelBox = new HBox(10);
        Label nameLabel = new Label(budgetName);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        
        Label amountLabel = new Label(String.format("%s / %s (%.1f%%)",
                                      currencyFormat.format(spent),
                                      currencyFormat.format(amount),
                                      percentage));
        amountLabel.setFont(Font.font("System", 12));
        
        ProgressBar progressBar = new ProgressBar(Math.min(percentage / 100.0, 1.0));
        progressBar.setPrefWidth(Double.MAX_VALUE);
        
        // Color based on usage
        if (percentage >= 100) {
            progressBar.setStyle("-fx-accent: #f44336;"); // Red for over budget
        } else if (percentage >= 80) {
            progressBar.setStyle("-fx-accent: #ff9800;"); // Orange for near limit
        } else {
            progressBar.setStyle("-fx-accent: #4caf50;"); // Green for good
        }
        
        labelBox.getChildren().addAll(nameLabel, amountLabel);
        labelBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        budgetItem.getChildren().addAll(labelBox, progressBar);
        return budgetItem;
    }
}