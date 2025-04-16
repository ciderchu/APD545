package com.minifin.view;

import com.minifin.controller.BudgetController;
import com.minifin.controller.ReportController;
import com.minifin.controller.TransactionController;
import com.minifin.model.User;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class MainLayout extends BorderPane {

    private final Sidebar sidebar;
    private final StackPane contentArea;
    private final User currentUser;
    private final TransactionController transactionController;
    private final BudgetController budgetController;
    private final ReportController reportController;

    public MainLayout(User currentUser) {
        this.currentUser = currentUser;
        
        // Initialize controllers
        this.transactionController = new TransactionController(currentUser.getId());
        this.budgetController = new BudgetController(currentUser.getId());
        this.reportController = new ReportController(currentUser.getId());
        
        // Sidebar navigation
        sidebar = new Sidebar(this);
        setLeft(sidebar);

        // Main content area with scroll pane
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #FAFAFA;");
        
        // Create a scroll pane to contain the content
        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        
        setCenter(scrollPane);

        // Load default view
        showDashboard();
    }

    public void showDashboard() {
        DashboardView dashboard = new DashboardView(currentUser, transactionController, budgetController);
        contentArea.getChildren().setAll(dashboard);
    }

    public void showTransactions() {
        // Since TransactionView is now a ScrollPane itself, we need to handle it differently
        TransactionView transactionView = new TransactionView(currentUser, transactionController);
        contentArea.getChildren().setAll(transactionView);
    }

    public void showBudgets() {
        BudgetView budgetView = new BudgetView(currentUser, budgetController);
        contentArea.getChildren().setAll(budgetView);
    }

    public void showReports() {
        ReportView reportView = new ReportView(currentUser, reportController);
        contentArea.getChildren().setAll(reportView);
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public TransactionController getTransactionController() {
        return transactionController;
    }
    
    public BudgetController getBudgetController() {
        return budgetController;
    }
    
    public ReportController getReportController() {
        return reportController;
    }
}