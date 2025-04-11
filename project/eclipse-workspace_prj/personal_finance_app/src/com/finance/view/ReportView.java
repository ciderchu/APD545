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

import com.finance.controller.ReportController;
import com.finance.model.Category;
import com.finance.model.FinancialSummary;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReportView {
    private final ReportController controller;
    
    private BorderPane view;
    private ComboBox<YearMonth> monthSelector;
    private Label incomeLabel;
    private Label expenseLabel;
    private Label balanceLabel;
    private PieChart incomePieChart;
    private PieChart expensePieChart;
    private LineChart<String, Number> trendLineChart;
    private BarChart<String, Number> categoryBarChart;
    
    public ReportView(ReportController controller) {
        this.controller = controller;
        
        createView();
        initializeMonthSelector();
        loadReportData();
    }
    
    public Node getView() {
        return view;
    }
    
    private void createView() {
        view = new BorderPane();
        view.setPadding(new Insets(15));
        
        // Top section with month selector and summary
        HBox topSection = createTopSection();
        view.setTop(topSection);
        
        // Center section with charts
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Monthly Summary Tab
        Tab monthlySummaryTab = new Tab("Monthly Summary");
        monthlySummaryTab.setContent(createMonthlySummaryView());
        
        // Trends Tab
        Tab trendsTab = new Tab("Trends");
        trendsTab.setContent(createTrendsView());
        
        // Category Analysis Tab
        Tab categoryTab = new Tab("Category Analysis");
        categoryTab.setContent(createCategoryAnalysisView());
        
        tabPane.getTabs().addAll(monthlySummaryTab, trendsTab, categoryTab);
        view.setCenter(tabPane);
    }
    
    private HBox createTopSection() {
        HBox topSection = new HBox(20);
        topSection.setPadding(new Insets(0, 0, 15, 0));
        topSection.setAlignment(Pos.CENTER_LEFT);
        
        // Month selector
        Label monthLabel = new Label("Select Month:");
        monthSelector = new ComboBox<>();
        monthSelector.setPrefWidth(150);
        monthSelector.setOnAction(e -> loadReportData());
        
        // Summary cards
        HBox summaryCards = new HBox(20);
        summaryCards.setAlignment(Pos.CENTER);
        HBox.setHgrow(summaryCards, Priority.ALWAYS);
        
        // Income card
        VBox incomeCard = new VBox(5);
        incomeCard.setAlignment(Pos.CENTER);
        incomeCard.setPadding(new Insets(10));
        incomeCard.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 5;");
        Label incomeTitleLabel = new Label("Income");
        incomeTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        incomeLabel = new Label("$0.00");
        incomeLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        incomeLabel.setTextFill(Color.GREEN);
        incomeCard.getChildren().addAll(incomeTitleLabel, incomeLabel);
        
        // Expense card
        VBox expenseCard = new VBox(5);
        expenseCard.setAlignment(Pos.CENTER);
        expenseCard.setPadding(new Insets(10));
        expenseCard.setStyle("-fx-background-color: #ffebee; -fx-background-radius: 5;");
        Label expenseTitleLabel = new Label("Expenses");
        expenseTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        expenseLabel = new Label("$0.00");
        expenseLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        expenseLabel.setTextFill(Color.RED);
        expenseCard.getChildren().addAll(expenseTitleLabel, expenseLabel);
        
        // Balance card
        VBox balanceCard = new VBox(5);
        balanceCard.setAlignment(Pos.CENTER);
        balanceCard.setPadding(new Insets(10));
        balanceCard.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 5;");
        Label balanceTitleLabel = new Label("Net Balance");
        balanceTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        balanceLabel = new Label("$0.00");
        balanceLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        balanceCard.getChildren().addAll(balanceTitleLabel, balanceLabel);
        
        summaryCards.getChildren().addAll(incomeCard, expenseCard, balanceCard);
        
        topSection.getChildren().addAll(monthLabel, monthSelector, summaryCards);
        return topSection;
    }
    
    private Node createMonthlySummaryView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(15));
        
        // Pie charts row
        HBox chartsRow = new HBox(20);
        chartsRow.setAlignment(Pos.CENTER);
        
        // Income distribution
        VBox incomeChartBox = new VBox(10);
        incomeChartBox.setAlignment(Pos.CENTER);
        Label incomeTitleLabel = new Label("Income Sources");
        incomeTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        incomePieChart = new PieChart();
        incomePieChart.setLabelsVisible(true);
        incomePieChart.setPrefSize(400, 300);
        incomeChartBox.getChildren().addAll(incomeTitleLabel, incomePieChart);
        
        // Expense distribution
        VBox expenseChartBox = new VBox(10);
        expenseChartBox.setAlignment(Pos.CENTER);
        Label expenseTitleLabel = new Label("Expense Distribution");
        expenseTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        expensePieChart = new PieChart();
        expensePieChart.setLabelsVisible(true);
        expensePieChart.setPrefSize(400, 300);
        expenseChartBox.getChildren().addAll(expenseTitleLabel, expensePieChart);
        
        chartsRow.getChildren().addAll(incomeChartBox, expenseChartBox);
        
        container.getChildren().addAll(chartsRow);
        return container;
    }
    
    private Node createTrendsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(15));
        
        // Period selector
        HBox periodSelectorBox = new HBox(10);
        periodSelectorBox.setAlignment(Pos.CENTER_LEFT);
        
        Label periodLabel = new Label("Time Range:");
        ComboBox<String> periodComboBox = new ComboBox<>();
        periodComboBox.getItems().addAll("Last 6 Months", "Last 12 Months");
        periodComboBox.setValue("Last 6 Months");
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            int months = periodComboBox.getValue().equals("Last 6 Months") ? 6 : 12;
            loadTrendData(months);
        });
        
        periodSelectorBox.getChildren().addAll(periodLabel, periodComboBox, refreshButton);
        
        // Trend chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        trendLineChart = new LineChart<>(xAxis, yAxis);
        trendLineChart.setTitle("Income vs Expenses Trend");
        xAxis.setLabel("Month");
        yAxis.setLabel("Amount");
        trendLineChart.setLegendVisible(true);
        trendLineChart.setAnimated(false);
        
        container.getChildren().addAll(periodSelectorBox, trendLineChart);
        VBox.setVgrow(trendLineChart, Priority.ALWAYS);
        
        // Initial load with 6 months
        loadTrendData(6);
        
        return container;
    }
    
    private Node createCategoryAnalysisView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(15));
        
        // Category type selector
        HBox typeSelectorBox = new HBox(10);
        typeSelectorBox.setAlignment(Pos.CENTER_LEFT);
        
        Label typeLabel = new Label("Category Type:");
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Expenses", "Income");
        typeComboBox.setValue("Expenses");
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            loadCategoryData(
                typeComboBox.getValue().equals("Income") ? Category.Type.INCOME : Category.Type.EXPENSE,
                monthSelector.getValue()
            );
        });
        
        typeSelectorBox.getChildren().addAll(typeLabel, typeComboBox, refreshButton);
        
        // Category analysis chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        categoryBarChart = new BarChart<>(xAxis, yAxis);
        categoryBarChart.setTitle("Category Breakdown");
        xAxis.setLabel("Category");
        yAxis.setLabel("Amount");
        categoryBarChart.setLegendVisible(false);
        
        container.getChildren().addAll(typeSelectorBox, categoryBarChart);
        VBox.setVgrow(categoryBarChart, Priority.ALWAYS);
        
        // Initial load with expense categories
        loadCategoryData(Category.Type.EXPENSE, YearMonth.now());
        
        return container;
    }
    
    private void initializeMonthSelector() {
        // Populate month selector with the current month and previous 11 months
        ObservableList<YearMonth> months = FXCollections.observableArrayList();
        YearMonth current = YearMonth.now();
        
        for (int i = 0; i < 12; i++) {
            months.add(current.minusMonths(i));
        }
        
        monthSelector.setItems(months);
        monthSelector.setValue(current);
        
        // Set cell factory to format month display
        monthSelector.setCellFactory(param -> new ListCell<YearMonth>() {
            @Override
            protected void updateItem(YearMonth item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
                }
            }
        });
        
        // Set converter for display format
        monthSelector.setConverter(new javafx.util.StringConverter<YearMonth>() {
            @Override
            public String toString(YearMonth object) {
                if (object == null) {
                    return null;
                }
                return object.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            }
            
            @Override
            public YearMonth fromString(String string) {
                // This method is not needed for this use case
                return null;
            }
        });
    }
    
    private void loadReportData() {
        YearMonth selectedMonth = monthSelector.getValue();
        if (selectedMonth == null) {
            return;
        }
        
        controller.getMonthlySummary(selectedMonth,
            summary -> {
                Platform.runLater(() -> {
                    updateSummaryLabels(summary);
                    updatePieCharts(summary);
                    loadCategoryData(Category.Type.EXPENSE, selectedMonth);
                });
            },
            error -> {
                showError("Failed to load monthly summary", error);
            }
        );
    }
    
    private void loadTrendData(int numMonths) {
        YearMonth endMonth = monthSelector.getValue() != null ? monthSelector.getValue() : YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(numMonths - 1);
        
        controller.getMonthlyTrend(startMonth, numMonths,
            summaries -> {
                Platform.runLater(() -> {
                    updateTrendChart(summaries);
                });
            },
            error -> {
                showError("Failed to load trend data", error);
            }
        );
    }
    
    private void loadCategoryData(Category.Type type, YearMonth month) {
        controller.getMonthlySummary(month,
            summary -> {
                Platform.runLater(() -> {
                    updateCategoryChart(summary, type);
                });
            },
            error -> {
                showError("Failed to load category data", error);
            }
        );
    }
    
    private void updateSummaryLabels(FinancialSummary summary) {
        incomeLabel.setText(String.format("$%.2f", summary.getTotalIncome()));
        expenseLabel.setText(String.format("$%.2f", summary.getTotalExpense()));
        balanceLabel.setText(String.format("$%.2f", summary.getNetAmount()));
        
        // Set balance color based on value
        if (summary.getNetAmount() >= 0) {
            balanceLabel.setTextFill(Color.GREEN);
        } else {
            balanceLabel.setTextFill(Color.RED);
        }
    }
    
    private void updatePieCharts(FinancialSummary summary) {
        // Clear previous data
        incomePieChart.getData().clear();
        expensePieChart.getData().clear();
        
        // Process income categories
        List<PieChart.Data> incomeData = new ArrayList<>();
        List<PieChart.Data> expenseData = new ArrayList<>();
        
        summary.getCategoryTotals().forEach((category, amount) -> {
            if (amount > 0) {
                PieChart.Data slice = new PieChart.Data(
                    category.getName() + " (" + String.format("$%.0f", amount) + ")", 
                    amount
                );
                
                if (category.getType() == Category.Type.INCOME) {
                    incomeData.add(slice);
                } else {
                    expenseData.add(slice);
                }
            }
        });
        
        incomePieChart.getData().addAll(incomeData);
        expensePieChart.getData().addAll(expenseData);
        
        // Apply category colors to slices
        int incomeIndex = 0;
        for (PieChart.Data slice : incomePieChart.getData()) {
            for (Map.Entry<Category, Double> entry : summary.getCategoryTotals().entrySet()) {
                if (slice.getName().startsWith(entry.getKey().getName()) && 
                    entry.getKey().getType() == Category.Type.INCOME) {
                    slice.getNode().setStyle("-fx-pie-color: " + entry.getKey().getColor() + ";");
                    break;
                }
            }
            incomeIndex++;
        }
        
        int expenseIndex = 0;
        for (PieChart.Data slice : expensePieChart.getData()) {
            for (Map.Entry<Category, Double> entry : summary.getCategoryTotals().entrySet()) {
                if (slice.getName().startsWith(entry.getKey().getName()) && 
                    entry.getKey().getType() == Category.Type.EXPENSE) {
                    slice.getNode().setStyle("-fx-pie-color: " + entry.getKey().getColor() + ";");
                    break;
                }
            }
            expenseIndex++;
        }
        
        // Show message if no data
        if (incomeData.isEmpty()) {
            incomePieChart.setTitle("No income data for selected month");
        } else {
            incomePieChart.setTitle("");
        }
        
        if (expenseData.isEmpty()) {
            expensePieChart.setTitle("No expense data for selected month");
        } else {
            expensePieChart.setTitle("");
        }
    }
    
    private void updateTrendChart(List<FinancialSummary> summaries) {
        // Clear previous data
        trendLineChart.getData().clear();
        
        // Create series for income and expenses
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");
        
        XYChart.Series<String, Number> balanceSeries = new XYChart.Series<>();
        balanceSeries.setName("Net Balance");
        
        // Add data points for each month, starting from oldest to newest
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yy");
        
        for (int i = 0; i < summaries.size(); i++) {
            FinancialSummary summary = summaries.get(i);
            String month = summary.getPeriod().format(monthFormatter);
            
            incomeSeries.getData().add(new XYChart.Data<>(month, summary.getTotalIncome()));
            expenseSeries.getData().add(new XYChart.Data<>(month, summary.getTotalExpense()));
            balanceSeries.getData().add(new XYChart.Data<>(month, summary.getNetAmount()));
        }
        
        trendLineChart.getData().addAll(incomeSeries, expenseSeries, balanceSeries);
        
        // Apply styles to the lines
        incomeSeries.getNode().setStyle("-fx-stroke: green;");
        expenseSeries.getNode().setStyle("-fx-stroke: red;");
        balanceSeries.getNode().setStyle("-fx-stroke: blue;");
        
        // Style the balance series data points to indicate positive/negative
        for (XYChart.Data<String, Number> data : balanceSeries.getData()) {
            if (data.getYValue().doubleValue() < 0) {
                data.getNode().setStyle("-fx-background-color: red;");
            } else {
                data.getNode().setStyle("-fx-background-color: green;");
            }
        }
    }
    
    private void updateCategoryChart(FinancialSummary summary, Category.Type type) {
        // Clear previous data
        categoryBarChart.getData().clear();
        
        // Create series for selected category type
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(type.name());
        
        // Filter and sort categories by amount
        List<Map.Entry<Category, Double>> sortedEntries = summary.getCategoryTotals().entrySet().stream()
            .filter(entry -> entry.getKey().getType() == type && entry.getValue() > 0)
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .collect(Collectors.toList());
        
        // Add data for each category
        for (Map.Entry<Category, Double> entry : sortedEntries) {
            series.getData().add(new XYChart.Data<>(entry.getKey().getName(), entry.getValue()));
        }
        
        categoryBarChart.getData().add(series);
        
        // Apply custom colors to bars based on category color
        int index = 0;
        for (XYChart.Data<String, Number> data : series.getData()) {
            for (Map.Entry<Category, Double> entry : sortedEntries) {
                if (data.getXValue().equals(entry.getKey().getName())) {
                    data.getNode().setStyle("-fx-bar-fill: " + entry.getKey().getColor() + ";");
                    break;
                }
            }
            index++;
        }
        
        // Update chart title based on type
        categoryBarChart.setTitle(type.name() + " Categories for " + 
                                 monthSelector.getValue().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
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