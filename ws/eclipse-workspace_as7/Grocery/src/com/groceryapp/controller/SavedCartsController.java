/**********************************************
Workshop #6
Course: APD545
Last Name: Chu
First Name: Sin Kau
ID: 155131220
Section: NDD
This assignment represents my own work in accordance with Seneca Academic Policy.
Signature Sin Kau Chu
Date: 27-Mar-2025
**********************************************/

package com.groceryapp.controller;

import com.groceryapp.model.Cart;
import com.groceryapp.model.JdbcDao;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.sql.SQLException;

public class SavedCartsController {
    @FXML private TableView<Cart> cartsTableView;
    @FXML private TableColumn<Cart, Number> cartIdColumn;
    @FXML private TableColumn<Cart, Number> priceColumn;
    @FXML private Button loadCartButton;
    
    private ObservableList<Cart> cartsList = FXCollections.observableArrayList();
    private CartController mainController;
    private int userId; // Store the user ID
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    @FXML
    public void initialize() {
        // setting up table columns
        cartIdColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getCartId()));
        
        priceColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getTotalPrice()));
        
        // formating price column as currency
        priceColumn.setCellFactory(col -> new javafx.scene.control.TableCell<Cart, Number>() {
            @Override
            protected void updateItem(Number price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price.doubleValue()));
                }
            }
        });
        
        // loading button action
        loadCartButton.setOnAction(e -> loadSelectedCart());
        
        cartsTableView.setItems(cartsList);
    }
    
    public void setMainController(CartController controller) {
        this.mainController = controller;
    }
    
    public void loadSavedCarts() {
        try {
            cartsList.clear();
            
            // getting incomplete carts from database
            JdbcDao jdbcDao = new JdbcDao();
            cartsList.addAll(jdbcDao.getIncompleteCarts(userId));
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", 
                "Could not load saved carts: " + e.getMessage());
        }
    }
    
    private void loadSelectedCart() {
        Cart selectedCart = cartsTableView.getSelectionModel().getSelectedItem();
        if (selectedCart != null && mainController != null) {
            mainController.loadCart(selectedCart);
            closeWindow();
        } else if (selectedCart == null) {
            showAlert(AlertType.WARNING, "Selection Required", 
                "Please select a cart to load.");
        }
    }
    
    private void closeWindow() {
        Stage stage = (Stage) loadCartButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}