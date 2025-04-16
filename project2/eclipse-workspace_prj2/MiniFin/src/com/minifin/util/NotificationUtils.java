package com.minifin.util;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.ButtonBar.ButtonData;

import java.util.Optional;
import java.util.function.Consumer;

public class NotificationUtils {
    
    // Show information alert
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Show error alert
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Show warning alert
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Show confirmation dialog
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    // Show confirmation dialog with custom buttons
    public static String showCustomConfirmation(String title, String message, String... options) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Remove default buttons
        alert.getButtonTypes().clear();
        
        // Add custom buttons
        for (String option : options) {
            alert.getButtonTypes().add(new ButtonType(option));
        }
        alert.getButtonTypes().add(new ButtonType("Cancel", ButtonData.CANCEL_CLOSE));
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && !result.get().getButtonData().equals(ButtonData.CANCEL_CLOSE)) {
            return result.get().getText();
        }
        return null;
    }
    
    // Show budget warning
    public static void showBudgetWarning(String categoryName, double budgetAmount, double spentAmount, double percentage) {
        String message = String.format(
            "Your spending in category '%s' has reached %.1f%% of your budget.\n\n" +
            "Budget: $%.2f\n" +
            "Spent: $%.2f\n" +
            "Remaining: $%.2f", 
            categoryName, percentage, budgetAmount, spentAmount, budgetAmount - spentAmount);
        
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Budget Warning");
        alert.setHeaderText("Budget Limit Approaching");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Show budget exceeded alert
    public static void showBudgetExceeded(String categoryName, double budgetAmount, double spentAmount) {
        String message = String.format(
            "Your spending in category '%s' has exceeded your budget!\n\n" +
            "Budget: $%.2f\n" +
            "Spent: $%.2f\n" +
            "Overspent: $%.2f", 
            categoryName, budgetAmount, spentAmount, spentAmount - budgetAmount);
        
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Budget Exceeded");
        alert.setHeaderText("Budget Limit Exceeded");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Show success notification
    public static void showSuccess(Stage owner, String message) {
        showNotification(owner, message, "success-notification");
    }
    
    // Show warning notification
    public static void showNotificationWarning(Stage owner, String message) {
        showNotification(owner, message, "warning-notification");
    }
    
    // Generic notification
    private static void showNotification(Stage owner, String message, String styleClass) {
        Stage notificationStage = new Stage();
        notificationStage.initOwner(owner);
        notificationStage.initModality(Modality.NONE);
        notificationStage.setX(owner.getX() + owner.getWidth() - 300);
        notificationStage.setY(owner.getY() + 20);
        
        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(280);
        
        VBox root = new VBox(msgLabel);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 15; -fx-border-color: #CCCCCC; -fx-border-radius: 5;");
        root.getStyleClass().add(styleClass);
        
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        notificationStage.setScene(scene);
        notificationStage.setWidth(300);
        notificationStage.setHeight(100);
        notificationStage.show();
        
        // Auto-hide after 3 seconds
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> notificationStage.close());
        delay.play();
    }
    
    // Check budgets and show warnings
    public static void checkBudgetWarnings(java.util.List<com.minifin.model.Budget> budgets) {
        for (com.minifin.model.Budget budget : budgets) {
            if (budget.isOverBudget()) {
                showBudgetExceeded(budget.getCategoryName(), budget.getAmount(), budget.getSpent());
            } else if (budget.isNearLimit()) {
                showBudgetWarning(budget.getCategoryName(), budget.getAmount(), 
                                  budget.getSpent(), budget.getUsagePercentage());
            }
        }
    }
}