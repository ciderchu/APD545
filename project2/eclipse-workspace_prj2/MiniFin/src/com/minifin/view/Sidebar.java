
package com.minifin.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class Sidebar extends VBox {

    public Sidebar(MainLayout mainLayout) {
        setPadding(new Insets(20));
        setSpacing(15);
        setStyle("-fx-background-color: #F0F0F0;");
        setPrefWidth(180);
        setAlignment(Pos.TOP_CENTER);

        Button dashboardBtn = createNavButton("Dashboard", () -> mainLayout.showDashboard());
        Button transactionsBtn = createNavButton("Transactions", () -> mainLayout.showTransactions());
        Button budgetsBtn = createNavButton("Budgets", () -> mainLayout.showBudgets());
        Button reportsBtn = createNavButton("Reports", () -> mainLayout.showReports());

        getChildren().addAll(dashboardBtn, transactionsBtn, budgetsBtn, reportsBtn);
    }

    private Button createNavButton(String label, Runnable action) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-button");
        btn.setOnAction(e -> action.run());
        return btn;
    }
} 
