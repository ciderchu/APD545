
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



package com.finance;

import com.finance.controller.*;
import com.finance.model.*;
import com.finance.view.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.net.URL;

import java.util.logging.Logger;

public class Main extends Application {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    // Controllers
    private TransactionController transactionController;
    private BudgetController budgetController;
    private ReportController reportController;
    
    // UI components
    private BorderPane mainLayout;
    private TabPane tabPane;
    
    // View components
    private DashboardView dashboardView;
    private TransactionView transactionView;
    private BudgetView budgetView;
    private ReportView reportView;

    @Override
    public void start(Stage primaryStage) {
        // Initialize controllers
        transactionController = new TransactionController();
        budgetController = new BudgetController();
        reportController = new ReportController();
        
        // Add test data
//        DatabaseManager.getInstance().insertSampleData();
        
        // Create the main layout
        mainLayout = new BorderPane();
        
        // Create tabs for different sections
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Initialize views
        dashboardView = new DashboardView(transactionController, budgetController, reportController);
        transactionView = new TransactionView(transactionController);
        budgetView = new BudgetView(budgetController, transactionController);
        reportView = new ReportView(reportController);
        
        // Create tabs
        Tab dashboardTab = new Tab("Dashboard", dashboardView.getView());
        Tab transactionsTab = new Tab("Transactions", transactionView.getView());
        Tab budgetsTab = new Tab("Budgets", budgetView.getView());
        Tab reportsTab = new Tab("Reports", reportView.getView());
        
        tabPane.getTabs().addAll(dashboardTab, transactionsTab, budgetsTab, reportsTab);
        
        mainLayout.setCenter(tabPane);
        
        // Set up the scene
        Scene scene = new Scene(mainLayout, 1000, 700);
        
        try {
            URL cssUrl = getClass().getResource("/styles/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("CSS file not found: /styles/styles.css");
            }
        } catch (Exception e) {
            System.out.println("Error loading CSS: " + e.getMessage());
        }
        
        // Configure the stage
        primaryStage.setTitle("Personal Finance Manager");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            shutdown();
        });
        
        // Show the application
        primaryStage.show();
        
        // Check for budget warnings on startup
        budgetController.checkBudgetWarnings(80.0, budgets -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Budget Warning");
                alert.setHeaderText("Some budgets are close to or exceeding their limits!");
                
                StringBuilder content = new StringBuilder();
                budgets.forEach(budget -> {
                    content.append(String.format("%s: %.2f%% used (%.2f / %.2f)\n", 
                        budget.getCategory().getName(),
                        budget.getPercentUsed(),
                        budget.getCurrentSpent(),
                        budget.getAmount()));
                });
                
                alert.setContentText(content.toString());
                alert.show();
            });
        });
    }
    
    @Override
    public void stop() {
        shutdown();
    }
    
    private void shutdown() {
        // Clean up resources
        if (transactionController != null) {
            transactionController.shutdown();
        }
        if (budgetController != null) {
            budgetController.shutdown();
        }
        if (reportController != null) {
            reportController.shutdown();
        }
        
        // Close database connection
        DatabaseManager.getInstance().shutdown();
        
        LOGGER.info("Application shutdown complete");
    }

    public static void main(String[] args) {
        launch(args);
    }
}