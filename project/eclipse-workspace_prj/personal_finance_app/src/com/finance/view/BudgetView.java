
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

import com.finance.controller.BudgetController;
import com.finance.controller.TransactionController;
import com.finance.model.Budget;
import com.finance.model.Category;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class BudgetView {
    private final BudgetController budgetController;
    private final TransactionController transactionController;
    
    private BorderPane view;
    private TableView<Budget> budgetTable;
    private ObservableList<Budget> budgetList;
    private ComboBox<Category> categoryComboBox;
    private ComboBox<Budget.Period> periodComboBox;
    private TextField amountField;
    private DatePicker startDatePicker;
    private PieChart budgetDistributionChart;
    
    private Budget selectedBudget;
    
    public BudgetView(BudgetController budgetController, TransactionController transactionController) {
        this.budgetController = budgetController;
        this.transactionController = transactionController;
        
        createView();
        loadBudgets();
        loadCategories();
    }
    
    public Node getView() {
        return view;
    }
    
    private void createView() {
        view = new BorderPane();
        view.setPadding(new Insets(15));
        
        // Left side: Budget table and chart
        VBox leftSection = new VBox(15);
        
        // Budget table section
        VBox tableSection = new VBox(10);
        Label tableTitle = new Label("Your Budgets");
        tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        budgetTable = createBudgetTable();
        VBox.setVgrow(budgetTable, Priority.ALWAYS);
        tableSection.getChildren().addAll(tableTitle, budgetTable);
        
        // Budget distribution chart section
        VBox chartSection = new VBox(10);
        Label chartTitle = new Label("Budget Distribution");
        chartTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        budgetDistributionChart = new PieChart();
        budgetDistributionChart.setTitle("");
        budgetDistributionChart.setLabelsVisible(true);
        budgetDistributionChart.setLegendVisible(true);
        VBox.setVgrow(budgetDistributionChart, Priority.ALWAYS);
        chartSection.getChildren().addAll(chartTitle, budgetDistributionChart);
        
        leftSection.getChildren().addAll(tableSection, chartSection);
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        
        // Right side: Budget form
        VBox formSection = createBudgetForm();
        
        // Set layout
        view.setCenter(leftSection);
        view.setRight(formSection);
    }
    
    private TableView<Budget> createBudgetTable() {
        budgetList = FXCollections.observableArrayList();
        TableView<Budget> table = new TableView<>(budgetList);
        table.setPlaceholder(new Label("No budgets defined. Create one using the form."));
        
        // Define columns
        TableColumn<Budget, Category> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);
        
        TableColumn<Budget, Double> amountCol = new TableColumn<>("Budget Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(col -> new TableCell<Budget, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
        amountCol.setPrefWidth(100);
        
        TableColumn<Budget, Double> spentCol = new TableColumn<>("Spent");
        spentCol.setCellValueFactory(new PropertyValueFactory<>("currentSpent"));
        spentCol.setCellFactory(col -> new TableCell<Budget, Double>() {
            @Override
            protected void updateItem(Double spent, boolean empty) {
                super.updateItem(spent, empty);
                if (empty || spent == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", spent));
                }
            }
        });
        spentCol.setPrefWidth(100);
        
        TableColumn<Budget, Double> remainingCol = new TableColumn<>("Remaining");
        remainingCol.setCellValueFactory(new PropertyValueFactory<>("remainingAmount"));
        remainingCol.setCellFactory(col -> new TableCell<Budget, Double>() {
            @Override
            protected void updateItem(Double remaining, boolean empty) {
                super.updateItem(remaining, empty);
                if (empty || remaining == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", remaining));
                    
                    // Color based on remaining amount
                    if (remaining < 0) {
                        setTextFill(Color.RED);
                    } else if (remaining < getTableView().getItems().get(getIndex()).getAmount() * 0.2) {
                        setTextFill(Color.ORANGE);
                    } else {
                        setTextFill(Color.GREEN);
                    }
                }
            }
        });
        remainingCol.setPrefWidth(100);
        
        TableColumn<Budget, Double> percentCol = new TableColumn<>("% Used");
        percentCol.setCellValueFactory(new PropertyValueFactory<>("percentUsed"));
        percentCol.setCellFactory(col -> new TableCell<Budget, Double>() {
            @Override
            protected void updateItem(Double percent, boolean empty) {
                super.updateItem(percent, empty);
                if (empty || percent == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", percent));
                    
                    // Color based on percentage used
                    if (percent >= 100) {
                        setTextFill(Color.RED);
                    } else if (percent >= 80) {
                        setTextFill(Color.ORANGE);
                    } else {
                        setTextFill(Color.GREEN);
                    }
                }
            }
        });
        percentCol.setPrefWidth(80);
        
        TableColumn<Budget, Budget.Period> periodCol = new TableColumn<>("Period");
        periodCol.setCellValueFactory(new PropertyValueFactory<>("period"));
        periodCol.setPrefWidth(80);
        
        // Add action column with edit and delete buttons
        TableColumn<Budget, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5, editButton, deleteButton);
            
            {
                editButton.setOnAction(event -> {
                    Budget budget = getTableView().getItems().get(getIndex());
                    editBudget(budget);
                });
                
                deleteButton.setOnAction(event -> {
                    Budget budget = getTableView().getItems().get(getIndex());
                    deleteBudget(budget);
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
        
        table.getColumns().addAll(categoryCol, amountCol, spentCol, remainingCol, percentCol, periodCol, actionCol);
        
        // Set selection handler
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedBudget = newSelection;
                populateFormWithBudget(newSelection);
            }
        });
        
        return table;
    }
    
    private VBox createBudgetForm() {
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(0, 0, 0, 15));
        formBox.setPrefWidth(300);
        
        // Form title
        Label formTitle = new Label("Create New Budget");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        // Category selector (only expense categories)
        Label categoryLabel = new Label("Category:");
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Budget period
        Label periodLabel = new Label("Budget Period:");
        periodComboBox = new ComboBox<>(FXCollections.observableArrayList(Budget.Period.values()));
        periodComboBox.setValue(Budget.Period.MONTHLY);
        periodComboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Start date
        Label startDateLabel = new Label("Start Date:");
        startDatePicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
        startDatePicker.setMaxWidth(Double.MAX_VALUE);
        
        // Amount field with validator
        Label amountLabel = new Label("Budget Amount:");
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
        
        // Buttons
        HBox buttonBox = new HBox(10);
        Button saveButton = new Button("Save");
        Button clearButton = new Button("Clear");
        
        saveButton.setOnAction(e -> saveBudget());
        clearButton.setOnAction(e -> clearForm());
        
        buttonBox.getChildren().addAll(saveButton, clearButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Assemble the form
        formBox.getChildren().addAll(
            formTitle, 
            categoryLabel, categoryComboBox,
            periodLabel, periodComboBox,
            startDateLabel, startDatePicker,
            amountLabel, amountField,
            buttonBox
        );
        
        return formBox;
    }
    
    private void loadBudgets() {
        budgetController.getBudgets(
            budgets -> {
                Platform.runLater(() -> {
                    budgetList.clear();
                    budgetList.addAll(budgets);
                    updateBudgetChart();
                });
            },
            error -> {
                showError("Failed to load budgets", error);
            }
        );
    }
    
    private void loadCategories() {
        // Only load expense categories for budget setting
        transactionController.getCategories(Category.Type.EXPENSE,
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
    
    private void updateBudgetChart() {
        budgetDistributionChart.getData().clear();
        
        if (budgetList.isEmpty()) {
            return;
        }
        
        for (Budget budget : budgetList) {
            PieChart.Data slice = new PieChart.Data(
                budget.getCategory().getName() + " (" + String.format("$%.0f", budget.getAmount()) + ")", 
                budget.getAmount()
            );
            budgetDistributionChart.getData().add(slice);
            
            // Apply the category color if available
            slice.getNode().setStyle("-fx-pie-color: " + budget.getCategory().getColor() + ";");
        }
    }
    
    private void saveBudget() {
        // Validate input
        if (!validateForm()) {
            return;
        }
        
        // Get form values
        Category category = categoryComboBox.getValue();
        Budget.Period period = periodComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        double amount = Double.parseDouble(amountField.getText());
        
        if (selectedBudget == null) {
            // Create new budget
            Budget newBudget = new Budget(
                0, // ID will be set by database
                1, // Default user ID
                category.getCategoryId(),
                amount,
                period,
                startDate
            );
            newBudget.setCategory(category);
            
            budgetController.addBudget(newBudget,
                () -> {
                    Platform.runLater(() -> {
                        clearForm();
                        loadBudgets();
                    });
                },
                error -> {
                    showError("Failed to add budget", error);
                }
            );
        } else {
            // Update existing budget
            selectedBudget.setCategory(category);
            selectedBudget.setPeriod(period);
            selectedBudget.setStartDate(startDate);
            selectedBudget.setAmount(amount);
            
            budgetController.updateBudget(selectedBudget,
                () -> {
                    Platform.runLater(() -> {
                        clearForm();
                        loadBudgets();
                        selectedBudget = null;
                    });
                },
                error -> {
                    showError("Failed to update budget", error);
                }
            );
        }
    }
    
    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (categoryComboBox.getValue() == null) {
            errorMessage.append("Please select a category.\n");
        }
        
        if (periodComboBox.getValue() == null) {
            errorMessage.append("Please select a budget period.\n");
        }
        
        if (startDatePicker.getValue() == null) {
            errorMessage.append("Please select a start date.\n");
        }
        
        if (amountField.getText() == null || amountField.getText().isEmpty()) {
            errorMessage.append("Please enter a budget amount.\n");
        } else {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    errorMessage.append("Budget amount must be greater than zero.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Budget amount must be a valid number.\n");
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
        if (!categoryComboBox.getItems().isEmpty()) {
            categoryComboBox.getSelectionModel().selectFirst();
        }
        periodComboBox.setValue(Budget.Period.MONTHLY);
        startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        amountField.clear();
        
        // Reset selection
        selectedBudget = null;
        budgetTable.getSelectionModel().clearSelection();
        
        // Update form title
        Label formTitle = (Label) ((VBox) view.getRight()).getChildren().get(0);
        formTitle.setText("Create New Budget");
    }
    
    private void editBudget(Budget budget) {
        selectedBudget = budget;
        populateFormWithBudget(budget);
    }
    
    private void populateFormWithBudget(Budget budget) {
        // Update form title
        Label formTitle = (Label) ((VBox) view.getRight()).getChildren().get(0);
        formTitle.setText("Edit Budget");
        
        // Set form values from budget
        for (Category category : categoryComboBox.getItems()) {
            if (category.getCategoryId() == budget.getCategoryId()) {
                categoryComboBox.setValue(category);
                break;
            }
        }
        
        periodComboBox.setValue(budget.getPeriod());
        startDatePicker.setValue(budget.getStartDate());
        amountField.setText(String.format("%.2f", budget.getAmount()));
    }
    
    private void deleteBudget(Budget budget) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Budget");
        confirmAlert.setContentText("Are you sure you want to delete the budget for " + 
                                   budget.getCategory().getName() + "?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            budgetController.deleteBudget(budget,
                () -> {
                    Platform.runLater(() -> {
                        budgetList.remove(budget);
                        updateBudgetChart();
                        if (selectedBudget == budget) {
                            clearForm();
                        }
                    });
                },
                error -> {
                    showError("Failed to delete budget", error);
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