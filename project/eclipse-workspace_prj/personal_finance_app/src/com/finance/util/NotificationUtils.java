
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

package com.finance.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Utility class for displaying notifications and dialogs
 */
public class NotificationUtils {
    
    /**
     * Shows an error dialog with the given title and message
     */
    public static void showError(String title, String message) {
        Platform.runLater(() -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UTILITY);
            dialog.setTitle(title);
            dialog.setResizable(false);
            
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(400);
            
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> dialog.close());
            
            VBox content = new VBox(10);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(messageLabel, closeButton);
            content.setStyle("-fx-padding: 20; -fx-background-color: #ffebee;");
            
            Scene scene = new Scene(content);
            dialog.setScene(scene);
            dialog.showAndWait();
        });
    }
    
    /**
     * Shows a success notification with the given title and message
     */
    public static void showSuccess(String title, String message) {
        Platform.runLater(() -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UTILITY);
            dialog.setTitle(title);
            dialog.setResizable(false);
            
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(400);
            
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> dialog.close());
            
            VBox content = new VBox(10);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(messageLabel, closeButton);
            content.setStyle("-fx-padding: 20; -fx-background-color: #e8f5e9;");
            
            Scene scene = new Scene(content);
            dialog.setScene(scene);
            dialog.showAndWait();
        });
    }
    
    /**
     * Shows a confirmation dialog with the given title and message
     * Returns true if the user confirms, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        final boolean[] result = {false};
        
        Platform.runLater(() -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UTILITY);
            dialog.setTitle(title);
            dialog.setResizable(false);
            
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(400);
            
            Button yesButton = new Button("Yes");
            yesButton.setOnAction(e -> {
                result[0] = true;
                dialog.close();
            });
            
            Button noButton = new Button("No");
            noButton.setOnAction(e -> {
                result[0] = false;
                dialog.close();
            });
            
            VBox buttonBox = new VBox(10);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().addAll(yesButton, noButton);
            
            VBox content = new VBox(15);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(messageLabel, buttonBox);
            content.setStyle("-fx-padding: 20;");
            
            Scene scene = new Scene(content);
            dialog.setScene(scene);
            dialog.showAndWait();
        });
        
        return result[0];
    }
    
    /**
     * Shows a notification for budget warnings
     */
    public static void showBudgetWarning(String categoryName, double percentUsed, double spent, double limit) {
        Platform.runLater(() -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UTILITY);
            dialog.setTitle("Budget Warning");
            dialog.setResizable(false);
            
            String warningMessage = String.format(
                "Your budget for %s is at %.1f%% of the limit.\n\n" +
                "Spent: $%.2f\n" +
                "Budget: $%.2f\n" +
                "Remaining: $%.2f",
                categoryName, percentUsed, spent, limit, limit - spent
            );
            
            Label messageLabel = new Label(warningMessage);
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(400);
            
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> dialog.close());
            
            VBox content = new VBox(15);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(messageLabel, closeButton);
            content.setStyle("-fx-padding: 20; -fx-background-color: #fff3e0;");
            
            Scene scene = new Scene(content);
            dialog.setScene(scene);
            dialog.showAndWait();
        });
    }
}