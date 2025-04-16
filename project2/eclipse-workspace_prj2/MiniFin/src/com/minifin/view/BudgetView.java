package com.minifin.view;

import com.minifin.controller.BudgetController;
import com.minifin.database.DatabaseManager;
import com.minifin.model.Budget;
import com.minifin.model.Category;
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

import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

public class BudgetView extends VBox {
    private final User currentUser;
    private final BudgetController budgetController;
    private final DatabaseManager dbManager;
    
    private final VBox formBox;
    private final TableView<Budget> budgetTable;
    private ComboBox<Category> categoryComboBox;
    private TextField amountField;
    private ComboBox<String> periodComboBox;
    private ObservableList<Budget> budgets;

    public BudgetView(User currentUser, BudgetController budgetController) {
        this.currentUser = currentUser;
        this.budgetController = budgetController;
        this.dbManager = DatabaseManager.getInstance();
        
        setSpacing(20);
        setPadding(new Insets(30));

        Label title = new Label("Budgets");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        // Initialize budget form
        formBox = new VBox(10);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #DDD; -fx-border-radius: 5; -fx-background-radius: 5;");
        formBox.setVisible(false);

        Button toggleFormBtn = new Button("+ Add Budget");
        toggleFormBtn.setOnAction(e -> {
            boolean isVisible = !formBox.isVisible();
            formBox.setVisible(isVisible);
            
            if (isVisible) {
                loadCategories();
            }
        });

        // Initialize budget table
        budgetTable = new TableView<>();
        budgetTable.setPlaceholder(new Label("No budgets added yet."));
        budgetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Budget, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategoryName()));
        
        TableColumn<Budget, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(NumberFormat.getCurrencyInstance(Locale.US).format(amount));
                }
            }
        });
        
        TableColumn<Budget, String> periodCol = new TableColumn<>("Period");
        periodCol.setCellValueFactory(new PropertyValueFactory<>("period"));
        
        TableColumn<Budget, Double> spentCol = new TableColumn<>("Spent");
        spentCol.setCellValueFactory(new PropertyValueFactory<>("spent"));
        spentCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double spent, boolean empty) {
                super.updateItem(spent, empty);
                if (empty || spent == null) {
                    setText(null);
                } else {
                    setText(NumberFormat.getCurrencyInstance(Locale.US).format(spent));
                }
            }
        });
        
        TableColumn<Budget, Double> remainingCol = new TableColumn<>("Remaining");
        remainingCol.setCellValueFactory(new PropertyValueFactory<>("remaining"));
        remainingCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double remaining, boolean empty) {
                super.updateItem(remaining, empty);
                if (empty || remaining == null) {
                    setText(null);
                } else {
                    setText(NumberFormat.getCurrencyInstance(Locale.US).format(remaining));
                    if (remaining < 0) {
                        setTextFill(Color.RED);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });
        
        TableColumn<Budget, Double> progressCol = new TableColumn<>("Progress");
        progressCol.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar progressBar = new ProgressBar();
            
            @Override
            protected void updateItem(Double ignored, boolean empty) {
                super.updateItem(ignored, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Budget budget = getTableView().getItems().get(getIndex());
                    double percentage = budget.getUsagePercentage();
                    progressBar.setProgress(Math.min(percentage / 100.0, 1.0));
                    
                    // Color based on usage
                    if (percentage >= 100) {
                        progressBar.setStyle("-fx-accent: #f44336;"); // Red
                    } else if (percentage >= 80) {
                        progressBar.setStyle("-fx-accent: #ff9800;"); // Orange
                    } else {
                        progressBar.setStyle("-fx-accent: #4caf50;"); // Green
                    }
                    
                    setGraphic(progressBar);
                }
            }
        });
        
        TableColumn<Budget, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            
            {
                deleteBtn.setOnAction(e -> {
                    Budget budget = getTableView().getItems().get(getIndex());
                    if (NotificationUtils.showConfirmation("Confirm Delete", 
                        "Are you sure you want to delete this budget?")) {
                        
                        budgetController.deleteBudget(budget.getId(), 
                            () -> {
                                NotificationUtils.showInfo("Success", "Budget deleted successfully.");
                                loadBudgets();
                            },
                            ex -> NotificationUtils.showError("Error", "Failed to delete budget: " + ex.getMessage())
                        );
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
        
        budgetTable.getColumns().addAll(categoryCol, amountCol, periodCol, spentCol, remainingCol, progressCol, actionsCol);
        
        // Create budget form fields
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("Select Category");
        categoryComboBox.setMaxWidth(Double.MAX_VALUE);
        
        amountField = new TextField();
        amountField.setPromptText("Amount");
        
        periodComboBox = new ComboBox<>();
        periodComboBox.getItems().addAll("Weekly", "Monthly", "Yearly");
        periodComboBox.setPromptText("Select Period");
        periodComboBox.setMaxWidth(Double.MAX_VALUE);
        
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> saveBudget());
        
        formBox.getChildren().addAll(
            new Label("New Budget"), 
            new Label("Category:"), categoryComboBox,
            new Label("Amount:"), amountField,
            new Label("Period:"), periodComboBox,
            saveBtn
        );
        
        // Build the layout
        VBox budgetListContainer = new VBox(10, budgetTable);
        budgetListContainer.setStyle("-fx-background-color: #FAFAFA; -fx-padding: 10; -fx-border-color: #DDD; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        getChildren().addAll(title, toggleFormBtn, budgetListContainer, formBox);
        
        // Load initial data
        loadBudgets();
    }
    
    private void loadCategories() {
        try {
            List<Category> expenseCategories = dbManager.getCategoriesByType("Expense", currentUser.getId());
            categoryComboBox.setItems(FXCollections.observableArrayList(expenseCategories));
        } catch (SQLException e) {
            NotificationUtils.showError("Error", "Failed to load categories: " + e.getMessage());
        }
    }
    
    private void saveBudget() {
        try {
            Category selectedCategory = categoryComboBox.getValue();
            String amountText = amountField.getText();
            String period = periodComboBox.getValue();
            
            if (selectedCategory == null || amountText == null || amountText.isEmpty() || period == null) {
                NotificationUtils.showWarning("Validation Error", "Please fill in all fields.");
                return;
            }
            
            double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    throw new NumberFormatException("Amount must be positive");
                }
            } catch (NumberFormatException e) {
                NotificationUtils.showWarning("Invalid Amount", "Please enter a valid positive number.");
                return;
            }
            
            budgetController.addBudget(amount, period, selectedCategory.getId(),
                budget -> {
                    NotificationUtils.showInfo("Success", "Budget added successfully.");
                    resetForm();
                    loadBudgets();
                },
                ex -> NotificationUtils.showError("Error", "Failed to add budget: " + ex.getMessage())
            );
            
        } catch (Exception e) {
            NotificationUtils.showError("Error", "An error occurred: " + e.getMessage());
        }
    }
    
    private void resetForm() {
        categoryComboBox.setValue(null);
        amountField.clear();
        periodComboBox.setValue(null);
        formBox.setVisible(false);
    }
    
    private void loadBudgets() {
        budgetController.getBudgets(loadedBudgets -> {
            // Get current month date range for calculating spent amounts
            LocalDate now = LocalDate.now();
            LocalDate firstDay = now.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate lastDay = now.with(TemporalAdjusters.lastDayOfMonth());
            
            // Calculate budget usage for the current month
            budgetController.calculateBudgetUsage(loadedBudgets, firstDay, lastDay, 
                updatedBudgets -> {
                    this.budgets = updatedBudgets;
                    budgetTable.setItems(updatedBudgets);
                    
                    // Check for budget warnings
                    NotificationUtils.checkBudgetWarnings(updatedBudgets);
                },
                ex -> NotificationUtils.showError("Error", "Failed to calculate budget usage: " + ex.getMessage())
            );
        }, ex -> {
            NotificationUtils.showError("Error", "Failed to load budgets: " + ex.getMessage());
        });
    }
}