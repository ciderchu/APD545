package com.minifin.view;

import javafx.stage.Stage;
import javafx.scene.Scene;

import com.minifin.controller.TransactionController;
import com.minifin.database.DatabaseManager;
import com.minifin.model.Category;
import com.minifin.model.Transaction;
import com.minifin.model.User;
import com.minifin.util.NotificationUtils;
import com.minifin.util.ValidationUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionView extends VBox {
    private final VBox formBox;
    private final ObservableList<Transaction> transactions;
    private final TableView<Transaction> table;
    private final Label totalLabel;
    private final Label incomeLabel;
    private final Label expenseLabel;
    private final ComboBox<String> monthFilter;
    private final PieChart summaryChart;
    
    private final User currentUser;
    private final TransactionController transactionController;
    private final DatabaseManager dbManager;
    
    // Form fields
    private TextField amountField;
    private TextField descriptionField;
    private ComboBox<String> typeComboBox;
    private ComboBox<Category> categoryComboBox;
    private DatePicker datePicker;

    public TransactionView(User currentUser, TransactionController transactionController) {
        this.currentUser = currentUser;
        this.transactionController = transactionController;
        this.dbManager = DatabaseManager.getInstance();
        
        setSpacing(20);
        setPadding(new Insets(30));
        setMinHeight(600);
        setPrefWidth(USE_COMPUTED_SIZE);

        Label title = new Label("Transactions");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        transactions = FXCollections.observableArrayList();

        formBox = new VBox(10);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #DDD; -fx-border-radius: 5; -fx-background-radius: 5;");
        formBox.setVisible(false);

        Button toggleFormBtn = new Button("+ Add Transaction");
        toggleFormBtn.setOnAction(e -> {
            boolean isVisible = !formBox.isVisible();
            formBox.setVisible(isVisible);
            
            if (isVisible) {
                datePicker.setValue(LocalDate.now());
                loadCategories();
            }
        });

        table = new TableView<>(transactions);
        table.setPlaceholder(new Label("No transactions yet."));
        table.setFixedCellSize(35);
        // Fixed height to show 5 rows + header
        table.setPrefHeight(5 * table.getFixedCellSize() + 30);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                }
            }
        });

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getCategoryName() != null ? data.getValue().getCategoryName() : ""));

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

        TableColumn<Transaction, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setOnAction(e -> {
                    Transaction txn = getTableView().getItems().get(getIndex());
                    if (NotificationUtils.showConfirmation("Confirm Delete", 
                            "Are you sure you want to delete this transaction?")) {
                        
                        transactionController.deleteTransaction(txn.getId(), 
                            () -> {
                                transactions.remove(txn);
                                updateTotal();
                                updateChart();
                                NotificationUtils.showInfo("Success", "Transaction deleted successfully.");
                            },
                            ex -> NotificationUtils.showError("Error", "Failed to delete transaction: " + ex.getMessage())
                        );
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        table.getColumns().addAll(dateCol, descCol, categoryCol, amountCol, typeCol, actionCol);

        // Create form fields
        amountField = new TextField();
        amountField.setPromptText("Amount");

        descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Income", "Expense");
        typeComboBox.setPromptText("Select Type");
        typeComboBox.setMaxWidth(Double.MAX_VALUE);
        typeComboBox.setOnAction(e -> loadCategories());

        categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("Select Category");
        categoryComboBox.setMaxWidth(Double.MAX_VALUE);

        datePicker = new DatePicker(LocalDate.now());

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> saveTransaction());

        formBox.getChildren().addAll(
            new Label("New Transaction"),
            new Label("Amount:"), amountField,
            new Label("Description:"), descriptionField,
            new Label("Type:"), typeComboBox,
            new Label("Category:"), categoryComboBox,
            new Label("Date:"), datePicker,
            saveBtn
        );

        incomeLabel = new Label("Total Income: $0.00");
        expenseLabel = new Label("Total Expense: $0.00");
        totalLabel = new Label("Net Total: $0.00");
        VBox totalsBox = new VBox(5, incomeLabel, expenseLabel, totalLabel);
        totalsBox.setPadding(new Insets(5, 0, 10, 0));
        totalsBox.setAlignment(Pos.CENTER_RIGHT);

        summaryChart = new PieChart();
        summaryChart.setTitle("Income vs Expense");
        summaryChart.setLabelsVisible(true);
        summaryChart.setLegendVisible(true);
        summaryChart.setPrefHeight(300);

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filter by Month:");
        monthFilter = new ComboBox<>();
        monthFilter.getItems().addAll("All", "January", "February", "March", "April", "May", "June", 
                            "July", "August", "September", "October", "November", "December");
        monthFilter.getSelectionModel().select("All");
        monthFilter.setOnAction(e -> loadTransactions());
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadTransactions());
        
        filterBox.getChildren().addAll(filterLabel, monthFilter, refreshBtn);

        VBox tableContainer = new VBox(10, filterBox, table);
        tableContainer.setStyle("-fx-background-color: #FAFAFA; -fx-padding: 10; -fx-border-color: #DDD; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Add a "View More" button to see all transactions
        Button viewMoreBtn = new Button("View More Transactions");
        viewMoreBtn.setOnAction(e -> showAllTransactions());
        tableContainer.getChildren().add(viewMoreBtn);
        
        HBox summaryContainer = new HBox(20, summaryChart, totalsBox);
        summaryContainer.setAlignment(Pos.CENTER);
        summaryContainer.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #DDD; -fx-border-radius: 5;");
        
        getChildren().addAll(title, toggleFormBtn, tableContainer, summaryContainer, formBox);
        
        // Load initial data
        loadTransactions();
    }

    private void showAllTransactions() {
        Stage allTransactionsStage = new Stage();
        allTransactionsStage.setTitle("All Transactions");
        
        TableView<Transaction> allTxnTable = new TableView<>(transactions);
        allTxnTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Create new columns manually instead of trying to copy existing ones
        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                }
            }
        });

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getCategoryName() != null ? data.getValue().getCategoryName() : ""));

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
        
        allTxnTable.getColumns().addAll(dateCol, descCol, categoryCol, amountCol, typeCol);
        
        ScrollPane scrollPane = new ScrollPane(allTxnTable);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        Scene scene = new Scene(scrollPane, 800, 600);
        allTransactionsStage.setScene(scene);
        allTransactionsStage.show();
    }
    private void loadCategories() {
        String selectedType = typeComboBox.getValue();
        if (selectedType == null) {
            categoryComboBox.getItems().clear();
            return;
        }
        
        try {
            List<Category> categories = dbManager.getCategoriesByType(selectedType, currentUser.getId());
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            NotificationUtils.showError("Error", "Failed to load categories: " + e.getMessage());
        }
    }
    
    private void saveTransaction() {
        try {
            // Validate inputs
            String amountStr = amountField.getText();
            String description = descriptionField.getText();
            String type = typeComboBox.getValue();
            Category category = categoryComboBox.getValue();
            LocalDate date = datePicker.getValue();
            
            if (amountStr == null || amountStr.isEmpty() || description == null || description.isEmpty() ||
                type == null || category == null || date == null) {
                NotificationUtils.showWarning("Validation Error", "Please fill in all fields.");
                return;
            }
            
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    throw new NumberFormatException("Amount must be positive");
                }
            } catch (NumberFormatException e) {
                NotificationUtils.showWarning("Invalid Amount", "Please enter a valid positive number.");
                return;
            }
            
            if (!ValidationUtils.isValidTransactionDate(date)) {
                NotificationUtils.showWarning("Invalid Date", "Please select a valid date (not in the future).");
                return;
            }
            
            // Add transaction
            transactionController.addTransaction(date, description, amount, type, category.getId(),
                transaction -> {
                    loadTransactions();
                    resetForm();
                    NotificationUtils.showInfo("Success", "Transaction added successfully.");
                },
                ex -> NotificationUtils.showError("Error", "Failed to add transaction: " + ex.getMessage())
            );
            
        } catch (Exception e) {
            NotificationUtils.showError("Error", "An error occurred: " + e.getMessage());
        }
    }
    
    private void resetForm() {
        amountField.clear();
        descriptionField.clear();
        typeComboBox.setValue(null);
        categoryComboBox.setValue(null);
        datePicker.setValue(LocalDate.now());
        formBox.setVisible(false);
    }

    private void loadTransactions() {
        String selectedMonth = monthFilter.getValue();
        
        if (selectedMonth == null || "All".equals(selectedMonth)) {
            // Load all transactions
            transactionController.getTransactions(
                loadedTransactions -> {
                    transactions.setAll(loadedTransactions);
                    updateTotal();
                    updateChart();
                },
                ex -> NotificationUtils.showError("Error", "Failed to load transactions: " + ex.getMessage())
            );
        } else {
            // Load transactions for selected month
            int monthIndex = monthFilter.getItems().indexOf(selectedMonth);
            if (monthIndex <= 0) return;
            
            int currentYear = LocalDate.now().getYear();
            LocalDate firstDay = LocalDate.of(currentYear, monthIndex, 1);
            LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
            
            transactionController.getTransactions(firstDay, lastDay,
                loadedTransactions -> {
                    transactions.setAll(loadedTransactions);
                    updateTotal();
                    updateChart();
                },
                ex -> NotificationUtils.showError("Error", "Failed to load transactions: " + ex.getMessage())
            );
        }
    }

    private void updateChart() {
        // Calculate totals for income and expense for chart
        double income = transactions.stream()
            .filter(t -> "Income".equalsIgnoreCase(t.getType()))
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        double expense = transactions.stream()
            .filter(t -> "Expense".equalsIgnoreCase(t.getType()))
            .mapToDouble(Transaction::getAmount)
            .sum();

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Income " + currencyFormat.format(income), income),
            new PieChart.Data("Expense " + currencyFormat.format(expense), expense)
        );
        
        summaryChart.setData(pieChartData);
        
        // Update chart colors
        if (!pieChartData.isEmpty()) {
            pieChartData.get(0).getNode().setStyle("-fx-pie-color: #4caf50;"); // Green for income
            if (pieChartData.size() > 1) {
                pieChartData.get(1).getNode().setStyle("-fx-pie-color: #f44336;"); // Red for expense
            }
        }
    }

    private void updateTotal() {
        double income = transactions.stream()
            .filter(t -> "Income".equalsIgnoreCase(t.getType()))
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        double expense = transactions.stream()
            .filter(t -> "Expense".equalsIgnoreCase(t.getType()))
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        double net = income - expense;

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        incomeLabel.setText("Total Income: " + currencyFormat.format(income));
        expenseLabel.setText("Total Expense: " + currencyFormat.format(expense));
        totalLabel.setText("Net Total: " + currencyFormat.format(net));
        
        // Set color for net total
        if (net < 0) {
            totalLabel.setTextFill(Color.RED);
        } else {
            totalLabel.setTextFill(Color.GREEN);
        }
    }
}