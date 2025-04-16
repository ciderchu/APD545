package com.minifin.view;

import java.util.List;
import java.util.Map;

import com.minifin.controller.ReportController;
import com.minifin.model.Budget;
import com.minifin.model.FinancialSummary;
import com.minifin.model.User;
import com.minifin.util.DateTimeUtils;
import com.minifin.util.NotificationUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class ReportView extends VBox {
    private final User currentUser;
    private final ReportController reportController;
    
    private ComboBox<String> reportTypeComboBox;
    private ComboBox<String> yearComboBox;
    private ComboBox<String> monthComboBox;
    private Button generateBtn;
    private VBox reportContainer;
    
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private VBox customDateRangeBox;

    public ReportView(User currentUser, ReportController reportController) {
        this.currentUser = currentUser;
        this.reportController = reportController;
        
        setSpacing(20);
        setPadding(new Insets(30));

        Label title = new Label("Financial Reports");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        
        // Report Controls Section
        VBox controlsBox = createControlsBox();
        
        // Report Content Container
        reportContainer = new VBox(20);
        reportContainer.setAlignment(Pos.TOP_CENTER);
        reportContainer.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #DDD; -fx-border-radius: 5;");
        reportContainer.getChildren().add(new Label("Select a report type and date range, then click 'Generate Report'"));
        
        getChildren().addAll(title, controlsBox, reportContainer);
    }
    
    private VBox createControlsBox() {
        VBox controlsBox = new VBox(10);
        controlsBox.setStyle("-fx-background-color: #F5F5F5; -fx-padding: 15; -fx-border-color: #DDD; -fx-border-radius: 5;");
        
        HBox typeSelectionBox = new HBox(10);
        typeSelectionBox.setAlignment(Pos.CENTER_LEFT);
        
        Label reportTypeLabel = new Label("Report Type:");
        reportTypeComboBox = new ComboBox<>();
        reportTypeComboBox.getItems().addAll("Monthly Summary", "Yearly Summary", "Custom Period");
        reportTypeComboBox.setValue("Monthly Summary");
        reportTypeComboBox.setOnAction(e -> updateDateControlVisibility());
        
        typeSelectionBox.getChildren().addAll(reportTypeLabel, reportTypeComboBox);
        
        // Monthly selection
        HBox monthSelectionBox = new HBox(10);
        monthSelectionBox.setAlignment(Pos.CENTER_LEFT);
        
        Label yearLabel = new Label("Year:");
        yearComboBox = new ComboBox<>();
        // Add current year and 4 previous years
        int currentYear = LocalDate.now().getYear();
        yearComboBox.getItems().addAll(
            String.valueOf(currentYear),
            String.valueOf(currentYear - 1),
            String.valueOf(currentYear - 2),
            String.valueOf(currentYear - 3),
            String.valueOf(currentYear - 4)
        );
        yearComboBox.setValue(String.valueOf(currentYear));
        
        Label monthLabel = new Label("Month:");
        monthComboBox = new ComboBox<>();
        monthComboBox.getItems().addAll(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        );
        // Set current month
        monthComboBox.setValue(Month.of(LocalDate.now().getMonthValue()).name());
        
        monthSelectionBox.getChildren().addAll(yearLabel, yearComboBox, monthLabel, monthComboBox);
        
        // Custom date range selection
        customDateRangeBox = new VBox(10);
        customDateRangeBox.setVisible(false);
        
        HBox datePickerBox = new HBox(10);
        datePickerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label fromLabel = new Label("From:");
        fromDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        
        Label toLabel = new Label("To:");
        toDatePicker = new DatePicker(LocalDate.now());
        
        datePickerBox.getChildren().addAll(fromLabel, fromDatePicker, toLabel, toDatePicker);
        customDateRangeBox.getChildren().add(datePickerBox);
        
        // Generate button
        generateBtn = new Button("Generate Report");
        generateBtn.setOnAction(e -> generateReport());
        
        controlsBox.getChildren().addAll(typeSelectionBox, monthSelectionBox, customDateRangeBox, generateBtn);
        return controlsBox;
    }
    
    private void updateDateControlVisibility() {
        String reportType = reportTypeComboBox.getValue();
        
        if ("Custom Period".equals(reportType)) {
            customDateRangeBox.setVisible(true);
            monthComboBox.setDisable(true);
            yearComboBox.setDisable(true);
        } else if ("Yearly Summary".equals(reportType)) {
            customDateRangeBox.setVisible(false);
            monthComboBox.setDisable(true);
            yearComboBox.setDisable(false);
        } else { // Monthly Summary
            customDateRangeBox.setVisible(false);
            monthComboBox.setDisable(false);
            yearComboBox.setDisable(false);
        }
    }
    
    private void generateReport() {
        String reportType = reportTypeComboBox.getValue();
        
        switch (reportType) {
            case "Monthly Summary":
                generateMonthlySummary();
                break;
            case "Yearly Summary":
                generateYearlySummary();
                break;
            case "Custom Period":
                generateCustomPeriodSummary();
                break;
            default:
                NotificationUtils.showWarning("Invalid Selection", "Please select a valid report type.");
        }
    }
    
    private void generateMonthlySummary() {
        try {
            // Validate inputs
            if (yearComboBox.getValue() == null) {
                NotificationUtils.showWarning("Missing Year", "Please select a year.");
                return;
            }
            
            if (monthComboBox.getValue() == null) {
                NotificationUtils.showWarning("Missing Month", "Please select a month.");
                return;
            }
            
            int year = Integer.parseInt(yearComboBox.getValue());
            String selectedMonth = monthComboBox.getValue();
            
            // Convert month name to number (1-12)
            int month;
            switch (selectedMonth) {
                case "January": month = 1; break;
                case "February": month = 2; break;
                case "March": month = 3; break;
                case "April": month = 4; break;
                case "May": month = 5; break;
                case "June": month = 6; break;
                case "July": month = 7; break;
                case "August": month = 8; break;
                case "September": month = 9; break;
                case "October": month = 10; break;
                case "November": month = 11; break;
                case "December": month = 12; break;
                default:
                    NotificationUtils.showWarning("Invalid Month", "Please select a valid month.");
                    return;
            }
            
            reportController.generateMonthlySummary(year, month,
                summary -> {
                    displaySummaryReport(summary);
                    addMonthlyTrendChart(year);
                },
                ex -> NotificationUtils.showError("Error", "Failed to generate monthly summary: " + ex.getMessage())
            );
        } catch (NumberFormatException e) {
            NotificationUtils.showError("Error", "Invalid year format.");
        }
    }
    
    private void generateYearlySummary() {
        try {
            if (yearComboBox.getValue() == null) {
                NotificationUtils.showWarning("Missing Year", "Please select a year.");
                return;
            }
            
            int year = Integer.parseInt(yearComboBox.getValue());
            
            reportController.generateYearlySummary(year,
                summary -> {
                    displaySummaryReport(summary);
                    addMonthlyTrendChart(year);
                },
                ex -> NotificationUtils.showError("Error", "Failed to generate yearly summary: " + ex.getMessage())
            );
        } catch (NumberFormatException e) {
            NotificationUtils.showError("Error", "Invalid year format.");
        }
    }
    
    private void generateCustomPeriodSummary() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        
        if (fromDate == null || toDate == null) {
            NotificationUtils.showWarning("Missing Dates", "Please select both from and to dates.");
            return;
        }
        
        if (fromDate.isAfter(toDate)) {
            NotificationUtils.showWarning("Invalid Date Range", "From date must be before to date.");
            return;
        }
        
        reportController.generateCustomPeriodSummary(fromDate, toDate,
            summary -> {
                displaySummaryReport(summary);
                // Add trend chart for the selected year if the period spans multiple months
                if (fromDate.getYear() == toDate.getYear() && 
                    fromDate.getMonthValue() != toDate.getMonthValue()) {
                    addMonthlyTrendChart(fromDate.getYear());
                }
            },
            ex -> NotificationUtils.showError("Error", "Failed to generate custom period summary: " + ex.getMessage())
        );
    }
    
    private void displaySummaryReport(FinancialSummary summary) {
        reportContainer.getChildren().clear();
        
        // Period label
        String periodLabel = DateTimeUtils.getPeriodLabel(summary.getFromDate(), summary.getToDate());
        Label periodTitle = new Label("Financial Summary: " + periodLabel);
        periodTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        // Financial overview
        VBox overviewBox = createFinancialOverview(summary);
        
        // Category breakdown
        HBox breakdownBox = createCategoryBreakdown(summary);
        
        // Budget status
        VBox budgetBox = createBudgetStatus(summary);
        
        reportContainer.getChildren().addAll(periodTitle, overviewBox, breakdownBox, budgetBox);
    }
    
    private VBox createFinancialOverview(FinancialSummary summary) {
        VBox overviewBox = new VBox(15);
        overviewBox.setAlignment(Pos.CENTER);
        overviewBox.setPadding(new Insets(10));
        overviewBox.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label sectionTitle = new Label("Financial Overview");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        Label incomeLabel = new Label("Total Income:");
        Label incomeValueLabel = new Label(currencyFormat.format(summary.getTotalIncome()));
        incomeValueLabel.setTextFill(Color.GREEN);
        incomeValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        Label expenseLabel = new Label("Total Expense:");
        Label expenseValueLabel = new Label(currencyFormat.format(summary.getTotalExpense()));
        expenseValueLabel.setTextFill(Color.RED);
        expenseValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        Label balanceLabel = new Label("Net Balance:");
        Label balanceValueLabel = new Label(currencyFormat.format(summary.getNetBalance()));
        if (summary.isPositiveBalance()) {
            balanceValueLabel.setTextFill(Color.GREEN);
        } else {
            balanceValueLabel.setTextFill(Color.RED);
        }
        balanceValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        Label savingsRateLabel = new Label("Savings Rate:");
        Label savingsRateValueLabel = new Label(String.format("%.1f%%", summary.getSavingsRate()));
        if (summary.getSavingsRate() >= 0) {
            savingsRateValueLabel.setTextFill(Color.GREEN);
        } else {
            savingsRateValueLabel.setTextFill(Color.RED);
        }
        savingsRateValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        grid.add(incomeLabel, 0, 0);
        grid.add(incomeValueLabel, 1, 0);
        grid.add(expenseLabel, 0, 1);
        grid.add(expenseValueLabel, 1, 1);
        grid.add(balanceLabel, 0, 2);
        grid.add(balanceValueLabel, 1, 2);
        grid.add(savingsRateLabel, 0, 3);
        grid.add(savingsRateValueLabel, 1, 3);
        
        // Add recommendations based on financial health
        Label recommendationsLabel = new Label();
        recommendationsLabel.setWrapText(true);
        recommendationsLabel.setMaxWidth(500);
        
        if (summary.getSavingsRate() < 0) {
            recommendationsLabel.setText("Warning: You're spending more than you earn. Consider reducing expenses in your top spending categories.");
            recommendationsLabel.setTextFill(Color.RED);
        } else if (summary.getSavingsRate() < 10) {
            recommendationsLabel.setText("Your savings rate is below 10%. Consider finding ways to increase income or reduce expenses to improve your financial health.");
            recommendationsLabel.setTextFill(Color.ORANGE);
        } else if (summary.getSavingsRate() < 20) {
            recommendationsLabel.setText("Your savings rate is good, but could be improved to build financial security faster.");
            recommendationsLabel.setTextFill(Color.BLACK);
        } else {
            recommendationsLabel.setText("Excellent savings rate! You're on track for building solid financial security.");
            recommendationsLabel.setTextFill(Color.GREEN);
        }
        
        overviewBox.getChildren().addAll(sectionTitle, grid, recommendationsLabel);
        return overviewBox;
    }
    
    private HBox createCategoryBreakdown(FinancialSummary summary) {
        HBox breakdownBox = new HBox(20);
        breakdownBox.setAlignment(Pos.CENTER);
        breakdownBox.setPadding(new Insets(10));
        breakdownBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 15;");
        
        // Income breakdown
        VBox incomeBox = new VBox(10);
        incomeBox.setAlignment(Pos.TOP_CENTER);
        
        Label incomeSectionTitle = new Label("Income Sources");
        incomeSectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        PieChart incomeChart = new PieChart();
        incomeChart.setTitle("Income Distribution");
        incomeChart.setLabelsVisible(true);
        incomeChart.setLegendVisible(true);
        incomeChart.setPrefSize(300, 250);
        
        ObservableList<PieChart.Data> incomeData = FXCollections.observableArrayList();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        
        Map<String, Double> incomeByCategory = summary.getIncomeByCategory();
        if (incomeByCategory != null) {
            for (Map.Entry<String, Double> entry : incomeByCategory.entrySet()) {
                incomeData.add(new PieChart.Data(
                    entry.getKey() + ": " + currencyFormat.format(entry.getValue()), 
                    entry.getValue()
                ));
            }
        }
        
        if (incomeData.isEmpty()) {
            incomeData.add(new PieChart.Data("No Data", 1));
        }
        
        incomeChart.setData(incomeData);
        
        incomeBox.getChildren().addAll(incomeSectionTitle, incomeChart);
        
        // Expense breakdown
        VBox expenseBox = new VBox(10);
        expenseBox.setAlignment(Pos.TOP_CENTER);
        
        Label expenseSectionTitle = new Label("Expense Categories");
        expenseSectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        PieChart expenseChart = new PieChart();
        expenseChart.setTitle("Expense Distribution");
        expenseChart.setLabelsVisible(true);
        expenseChart.setLegendVisible(true);
        expenseChart.setPrefSize(300, 250);
        
        ObservableList<PieChart.Data> expenseData = FXCollections.observableArrayList();
        
        Map<String, Double> expenseByCategory = summary.getExpenseByCategory();
        if (expenseByCategory != null) {
            for (Map.Entry<String, Double> entry : expenseByCategory.entrySet()) {
                expenseData.add(new PieChart.Data(
                    entry.getKey() + ": " + currencyFormat.format(entry.getValue()), 
                    entry.getValue()
                ));
            }
        }
        
        if (expenseData.isEmpty()) {
            expenseData.add(new PieChart.Data("No Data", 1));
        }
        
        expenseChart.setData(expenseData);
        
        expenseBox.getChildren().addAll(expenseSectionTitle, expenseChart);
        
        breakdownBox.getChildren().addAll(incomeBox, expenseBox);
        return breakdownBox;
    }
    
    private VBox createBudgetStatus(FinancialSummary summary) {
        VBox budgetBox = new VBox(15);
        budgetBox.setAlignment(Pos.TOP_CENTER);
        budgetBox.setPadding(new Insets(10));
        budgetBox.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label sectionTitle = new Label("Budget Status");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        List<Budget> budgets = summary.getBudgets();
        if (budgets == null || budgets.isEmpty()) {
            budgetBox.getChildren().addAll(sectionTitle, new Label("No budgets set up yet."));
            return budgetBox;
        }
        
        GridPane budgetGrid = new GridPane();
        budgetGrid.setHgap(15);
        budgetGrid.setVgap(10);
        budgetGrid.setPadding(new Insets(10));
        
        // Column headers
        Label categoryHeader = new Label("Category");
        Label budgetHeader = new Label("Budget");
        Label spentHeader = new Label("Spent");
        Label remainingHeader = new Label("Remaining");
        Label progressHeader = new Label("Progress");
        
        categoryHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        budgetHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        spentHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        remainingHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        progressHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        
        budgetGrid.add(categoryHeader, 0, 0);
        budgetGrid.add(budgetHeader, 1, 0);
        budgetGrid.add(spentHeader, 2, 0);
        budgetGrid.add(remainingHeader, 3, 0);
        budgetGrid.add(progressHeader, 4, 0);
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        
        int row = 1;
        for (Budget budget : budgets) {
            if (budget.getCategoryType() == null || !"Expense".equals(budget.getCategoryType())) {
                continue; // Skip non-expense budgets
            }
            
            Label categoryLabel = new Label(budget.getCategoryName() != null ? budget.getCategoryName() : "Unknown");
            Label budgetAmountLabel = new Label(currencyFormat.format(budget.getAmount()));
            Label spentLabel = new Label(currencyFormat.format(budget.getSpent()));
            Label remainingLabel = new Label(currencyFormat.format(budget.getRemaining()));
            
            ProgressBar progressBar = new ProgressBar(Math.min(budget.getUsagePercentage() / 100.0, 1.0));
            progressBar.setPrefWidth(150);
            
            if (budget.getUsagePercentage() >= 100) {
                progressBar.setStyle("-fx-accent: #f44336;"); // Red
                remainingLabel.setTextFill(Color.RED);
            } else if (budget.getUsagePercentage() >= 80) {
                progressBar.setStyle("-fx-accent: #ff9800;"); // Orange
            } else {
                progressBar.setStyle("-fx-accent: #4caf50;"); // Green
            }
            
            budgetGrid.add(categoryLabel, 0, row);
            budgetGrid.add(budgetAmountLabel, 1, row);
            budgetGrid.add(spentLabel, 2, row);
            budgetGrid.add(remainingLabel, 3, row);
            budgetGrid.add(progressBar, 4, row);
            
            row++;
        }
        
        budgetBox.getChildren().addAll(sectionTitle, budgetGrid);
        return budgetBox;
    }
    
    private void addMonthlyTrendChart(int year) {
        reportController.getMonthlyTrend(year,
            monthlyData -> {
                VBox trendBox = new VBox(15);
                trendBox.setAlignment(Pos.TOP_CENTER);
                trendBox.setPadding(new Insets(10));
                trendBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 15;");
                
                Label sectionTitle = new Label("Monthly Trend for " + year);
                sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                
                final CategoryAxis xAxis = new CategoryAxis();
                final NumberAxis yAxis = new NumberAxis();
                xAxis.setLabel("Month");
                yAxis.setLabel("Amount");
                
                final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
                lineChart.setTitle("Income vs Expense Trend");
                lineChart.setPrefSize(800, 400);
                
                XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
                incomeSeries.setName("Income");
                
                XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
                expenseSeries.setName("Expense");
                
                XYChart.Series<String, Number> balanceSeries = new XYChart.Series<>();
                balanceSeries.setName("Balance");
                
                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                Double[] incomeData = monthlyData.get("income");
                Double[] expenseData = monthlyData.get("expense");
                Double[] balanceData = monthlyData.get("balance");
                
                if (incomeData != null && expenseData != null && balanceData != null) {
                    for (int i = 0; i < 12; i++) {
                        incomeSeries.getData().add(new XYChart.Data<>(months[i], incomeData[i]));
                        expenseSeries.getData().add(new XYChart.Data<>(months[i], expenseData[i]));
                        balanceSeries.getData().add(new XYChart.Data<>(months[i], balanceData[i]));
                    }
                    
                    lineChart.getData().addAll(incomeSeries, expenseSeries, balanceSeries);
                } else {
                    Label noDataLabel = new Label("No trend data available for " + year);
                    trendBox.getChildren().addAll(sectionTitle, noDataLabel);
                    reportContainer.getChildren().add(trendBox);
                    return;
                }
                
                trendBox.getChildren().addAll(sectionTitle, lineChart);
                reportContainer.getChildren().add(trendBox);
            },
            ex -> NotificationUtils.showError("Error", "Failed to load monthly trend data: " + ex.getMessage())
        );
    }
}