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

import com.finance.controller.TransactionController;
import com.finance.model.Category;
import com.finance.model.Transaction;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class TransactionView {
    private final TransactionController controller;
    
    private BorderPane view;
    private TableView<Transaction> transactionTable;
    private ObservableList<Transaction> transactionList;
    private ComboBox<Category> categoryComboBox;
    private ComboBox<Category.Type> typeComboBox;
    private DatePicker datePicker;
    private TextField amountField;
    private TextField descriptionField;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    
    private Transaction selectedTransaction;
    
    public TransactionView(TransactionController controller) {
        this.controller = controller;
        
        createView();
        loadTransactions();
    }
    
    public Node getView() {
        return view;
    }
    
    private void createView() {
        view = new BorderPane();
        view.setPadding(new Insets(15));
        
        // Create top filter section
        HBox filterBox = createFilterSection();
        
        // Create transaction table
        transactionTable = createTransactionTable();
        
        // Create form for adding/editing transactions
        VBox formBox = createTransactionForm();
        
        // Arrange layout
        view.setTop(filterBox);
        view.setCenter(transactionTable);
        view.setRight(formBox);
    }
    
    private HBox createFilterSection() {
        HBox filterBox = new HBox(15);
        filterBox.setPadding(new Insets(0, 0, 15, 0));
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        // Date range selectors
        Label fromLabel = new Label("From:");
        fromDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        
        Label toLabel = new Label("To:");
        toDatePicker = new DatePicker(LocalDate.now());
        
        Button filterButton = new Button("Apply Filter");
        filterButton.setOnAction(e -> loadTransactions());
        
        filterBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker, filterButton);
        
        return filterBox;
    }
    
    private TableView<Transaction> createTransactionTable() {
        transactionList = FXCollections.observableArrayList();
        TableView<Transaction> table = new TableView<>(transactionList);
        table.setPlaceholder(new Label("No transactions found for the selected date range"));
        
        // Define table columns
        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        dateCol.setCellFactory(column -> {
            return new TableCell<Transaction, LocalDate>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                    }
                }
            };
        });
        dateCol.setPrefWidth(100);
        
        TableColumn<Transaction, Category> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);
        
        TableColumn<Transaction, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setPrefWidth(250);
        
        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(column -> {
            return new TableCell<Transaction, Double>() {
                @Override
                protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", amount));
                        
                        // Color based on transaction type
                        Transaction transaction = getTableView().getItems().get(getIndex());
                        if (transaction.getCategory() != null) {
                            if (transaction.getCategory().getType() == Category.Type.INCOME) {
                                setTextFill(Color.GREEN);
                            } else {
                                setTextFill(Color.RED);
                            }
                        }
                    }
                }
            };
        });
        amountCol.setPrefWidth(100);
        
        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> {
            Category category = cellData.getValue().getCategory();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> category != null ? category.getType().toString() : ""
            );
        });
        typeCol.setPrefWidth(80);
        
        // Add action column with edit and delete buttons
        TableColumn<Transaction, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5, editButton, deleteButton);
            
            {
                editButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    editTransaction(transaction);
                });
                
                deleteButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    deleteTransaction(transaction);
                });
                
                pane.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        actionCol.setPrefWidth(120);
        
        table.getColumns().addAll(dateCol, typeCol, categoryCol, descriptionCol, amountCol, actionCol);
        
        // Set selection handler
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedTransaction = newSelection;
                populateFormWithTransaction(newSelection);
            }
        });
        
        return table;
    }
    
    private VBox createTransactionForm() {
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(0, 0, 0, 15));
        formBox.setPrefWidth(300);
        
        // Form title
        Label formTitle = new Label("Add New Transaction");
        formTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        
        // Transaction type selector
        Label typeLabel = new Label("Transaction Type:");
        typeComboBox = new ComboBox<>(FXCollections.observableArrayList(Category.Type.values()));
        typeComboBox.getSelectionModel().selectFirst();
        typeComboBox.setMaxWidth(Double.MAX_VALUE);
        typeComboBox.setOnAction(e -> loadCategories());
        
        // Category selector
        Label categoryLabel = new Label("Category:");
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Date picker
        Label dateLabel = new Label("Date:");
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        
        // Amount field with validator
        Label amountLabel = new Label("Amount:");
        amountField = new TextField();
        amountField.setPromptText("Enter amount");
        
        // Set text formatter to only allow numbers and decimal point
        UnaryOperator<TextFormatter.Change> amountFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("^\\d*\\.?\\d{0,2}$")) {
                return change;
            }
            return null;
        };
        amountField.setTextFormatter(new TextFormatter<>(amountFilter));
        
        // Description field
        Label descriptionLabel = new Label("Description:");
        descriptionField = new TextField();
        descriptionField.setPromptText("Enter description (optional)");
        
        // Buttons
        HBox buttonBox = new HBox(10);
        Button saveButton = new Button("Save");
        Button clearButton = new Button("Clear");
        
        saveButton.setOnAction(e -> saveTransaction());
        clearButton.setOnAction(e -> clearForm());
        
        buttonBox.getChildren().addAll(saveButton, clearButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Assemble the form
        formBox.getChildren().addAll(
            formTitle, 
            typeLabel, typeComboBox,
            categoryLabel, categoryComboBox,
            dateLabel, datePicker,
            amountLabel, amountField,
            descriptionLabel, descriptionField,
            buttonBox
        );
        
        // Initial load of categories
        loadCategories();
        
        return formBox;
    }
    
    private void loadTransactions() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        
        if (fromDate == null || toDate == null) {
            showError("Invalid Date Range", new Exception("Please select valid from and to dates."));
            return;
        }
        
        if (fromDate.isAfter(toDate)) {
            showError("Invalid Date Range", new Exception("From date cannot be after to date."));
            return;
        }
        
        controller.getTransactions(fromDate, toDate,
            transactions -> {
                Platform.runLater(() -> {
                    transactionList.clear();
                    transactionList.addAll(transactions);
                });
            },
            error -> {
                showError("Failed to load transactions", error);
            }
        );
    }
    
    private void loadCategories() {
        Category.Type selectedType = typeComboBox.getValue();
        if (selectedType == null) {
            return;
        }
        
        controller.getCategories(selectedType,
            categories -> {
                Platform.runLater(() -> {
                    categoryComboBox.setItems(categories);
                    if (!categories.isEmpty()) {
                        categoryComboBox.getSelectionModel().selectFirst();
                    }
                });
            },
            error -> {
                showError("Failed to load categories", error);
            }
        );
    }
    
    private void saveTransaction() {
        // Validate input
        if (!validateForm()) {
            return;
        }
        
        // Get form values
        Category category = categoryComboBox.getValue();
        LocalDate date = datePicker.getValue();
        double amount = Double.parseDouble(amountField.getText());
        String description = descriptionField.getText();
        
        if (selectedTransaction == null) {
            // Create new transaction
            Transaction newTransaction = new Transaction(
                0, // ID will be set by database
                1, // Default user ID
                category.getCategoryId(),
                amount,
                description,
                date,
                null // Created timestamp will be set by database
            );
            newTransaction.setCategory(category);
            
            controller.addTransaction(newTransaction,
                () -> {
                    Platform.runLater(() -> {
                        clearForm();
                        loadTransactions();
                    });
                },
                error -> {
                    showError("Failed to add transaction", error);
                }
            );
        } else {
            // Update existing transaction
            selectedTransaction.setCategory(category);
            selectedTransaction.setTransactionDate(date);
            selectedTransaction.setAmount(amount);
            selectedTransaction.setDescription(description);
            
            controller.updateTransaction(selectedTransaction,
                () -> {
                    Platform.runLater(() -> {
                        clearForm();
                        loadTransactions();
                        selectedTransaction = null;
                    });
                },
                error -> {
                    showError("Failed to update transaction", error);
                }
            );
        }
    }
    
    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (categoryComboBox.getValue() == null) {
            errorMessage.append("Please select a category.\n");
        }
        
        if (datePicker.getValue() == null) {
            errorMessage.append("Please select a date.\n");
        }
        
        if (amountField.getText() == null || amountField.getText().isEmpty()) {
            errorMessage.append("Please enter an amount.\n");
        } else {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    errorMessage.append("Amount must be greater than zero.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Amount must be a valid number.\n");
            }
        }
        
        if (errorMessage.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    private void clearForm() {
        typeComboBox.getSelectionModel().selectFirst();
        datePicker.setValue(LocalDate.now());
        amountField.clear();
        descriptionField.clear();
        
        // Reset selection
        selectedTransaction = null;
        transactionTable.getSelectionModel().clearSelection();
        
        // Update form title
        Label formTitle = (Label) ((VBox) view.getRight()).getChildren().get(0);
        formTitle.setText("Add New Transaction");
    }
    
    private void editTransaction(Transaction transaction) {
        selectedTransaction = transaction;
        populateFormWithTransaction(transaction);
    }
    
    private void populateFormWithTransaction(Transaction transaction) {
        // Update form title
        Label formTitle = (Label) ((VBox) view.getRight()).getChildren().get(0);
        formTitle.setText("Edit Transaction");
        
        // Set form values from transaction
        typeComboBox.setValue(transaction.getCategory().getType());
        
        // Need to load categories first, then select the right one
        controller.getCategories(transaction.getCategory().getType(),
            categories -> {
                Platform.runLater(() -> {
                    categoryComboBox.setItems(categories);
                    
                    // Find and select the matching category
                    for (Category category : categories) {
                        if (category.getCategoryId() == transaction.getCategoryId()) {
                            categoryComboBox.setValue(category);
                            break;
                        }
                    }
                });
            },
            error -> showError("Failed to load categories", error)
        );
        
        datePicker.setValue(transaction.getTransactionDate());
        amountField.setText(String.format("%.2f", transaction.getAmount()));
        descriptionField.setText(transaction.getDescription());
    }
    
    private void deleteTransaction(Transaction transaction) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Transaction");
        confirmAlert.setContentText("Are you sure you want to delete this transaction?\n\n" +
                                    "Date: " + transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + "\n" +
                                    "Category: " + transaction.getCategory().getName() + "\n" +
                                    "Amount: $" + String.format("%.2f", transaction.getAmount()));
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            controller.deleteTransaction(transaction,
                () -> {
                    Platform.runLater(() -> {
                        transactionList.remove(transaction);
                        if (selectedTransaction == transaction) {
                            clearForm();
                        }
                    });
                },
                error -> {
                    showError("Failed to delete transaction", error);
                }
            );
        }
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