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
import com.groceryapp.model.Item;
import com.groceryapp.model.ItemModel;
import com.groceryapp.model.JdbcDao;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class CartController {
    @FXML protected ComboBox<Item> itemsComboBox;
    @FXML protected Slider quantitySlider;
    @FXML protected Label unitPriceLabel, totalPriceLabel;
    @FXML protected Button addToCartButton, removeButton, saveCartButton, checkoutButton, viewSavedCartsButton;
    @FXML protected Button loadCartFromDBButton; // New button for loading cart from DB
    @FXML protected TableView<Item> cartTableView;
    @FXML protected TableColumn<Item, String> itemColumn;
    @FXML protected TableColumn<Item, Double> priceColumn;
    @FXML protected TableColumn<Item, Double> quantityColumn;
    @FXML protected Label selectedQuantityLabel;
    @FXML protected Label purchasedUnitsLabel;
    @FXML protected Label unitLabel;
    @FXML protected Label cartIdLabel;
    @FXML protected Label welcomeLabel; // New label to welcome the user
 
    protected ItemModel itemModel = new ItemModel();
    protected ObservableList<Item> cartItems = FXCollections.observableArrayList();
    protected Cart currentCart;
    protected SimpleDoubleProperty totalPrice = new SimpleDoubleProperty(0.0);
    protected int userId; // Store the logged-in user's ID

    // Initialize with user ID
    public void initData(int userId) {
        this.userId = userId;
        createNewCart();
        
        // Update welcome message if needed
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome to Grocery Store! User ID: " + userId);
        }
    }

    @FXML
    public void initialize() {
        // load data into the combo box
        itemModel.loadData();
        itemsComboBox.setItems(itemModel.getItemsList());
        
        // set cell value factories for your TableView columns
        itemColumn.setCellValueFactory(cellData -> 
            Bindings.createObjectBinding(() -> cellData.getValue().getName())
        );
        priceColumn.setCellValueFactory(cellData -> 
            Bindings.createObjectBinding(() -> cellData.getValue().getPrice())
        );
        quantityColumn.setCellValueFactory(cellData -> 
            Bindings.createObjectBinding(() -> cellData.getValue().getQuantity())
        );

        // item is selected in the combo box, update its unit price label
        itemsComboBox.valueProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                unitPriceLabel.setText(String.format("$%.2f", newItem.getPrice()));
            } else {
                unitPriceLabel.setText("$0.00");
            }
        });
        
        // set the unit (e.g 1 gallon)
        itemsComboBox.valueProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                String text = (double) newItem.getQuantity() + " " + newItem.getUnit();
                unitLabel.setText(text);
            } else {
                // Clear if no item is selected
                unitLabel.setText("");
            }
        });

        // update selectedQuantityLabel whenever the slider changes
        quantitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedQuantityLabel.setText(String.format("%.0f", newVal.doubleValue()));
            purchasedUnitsLabel.setText(String.format("%.0f", newVal.doubleValue()));
        });
        
        // set up button event handlers
        addToCartButton.setOnAction(e -> addToCart());
        removeButton.setOnAction(e -> removeFromCart());
        saveCartButton.setOnAction(e -> saveCart());
        checkoutButton.setOnAction(e -> checkoutCart());
        viewSavedCartsButton.setOnAction(e -> openSavedCartsWindow());
        
        // add handler for the new load cart from DB button
        if (loadCartFromDBButton != null) {
            loadCartFromDBButton.setOnAction(e -> openSavedCartsWindow());
        }

        // binding total price to the label with formatting
        totalPriceLabel.textProperty().bind(
            Bindings.createStringBinding(() -> 
                String.format("$%.2f", totalPrice.get()), totalPrice)
        );
        
        // setting the TableView items
        cartTableView.setItems(cartItems);
    }

    protected void createNewCart() {
        try {
            // getting the next cart ID from database for this user
            JdbcDao jdbcDao = new JdbcDao();
            int nextCartId = jdbcDao.getNextCartId(userId);
            
            currentCart = new Cart(nextCartId);
            cartItems.clear();
            updateCartIdLabel();
            updateTotalPrice();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", 
                "Could not create a new cart: " + e.getMessage());
        }
    }
    
    protected void updateCartIdLabel() {
        if (cartIdLabel != null) {
            cartIdLabel.setText("Cart #" + currentCart.getCartId());
        }
    }

    protected void addToCart() {
        Item selectedItem = itemsComboBox.getValue();
        if (selectedItem != null) {
            double quantityToBuy = quantitySlider.getValue();

            if (quantityToBuy <= 0) {
                System.out.println("Please select a valid quantity.");
                return;
            }

            // checking if item exists in the cart and update quantity
            for (Item cartItem : cartItems) {
                if (cartItem.getName().equals(selectedItem.getName())) {
                    cartItem.setQuantity(cartItem.getQuantity() + quantityToBuy);
                    cartTableView.refresh();
                    updateTotalPrice();
                    return;
                }
            }

            // creating new item and add to cart
            Item itemForCart = new Item(
                selectedItem.getName(),
                selectedItem.getUnit(),
                quantityToBuy,
                selectedItem.getPrice()
            );

            cartItems.add(itemForCart);
            updateTotalPrice();
        }
    }

    // method to update total price
    protected void updateTotalPrice() {
        double total = 0.0;
        for (Item item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        totalPrice.set(total);
    }

    protected void removeFromCart() {
        Item selectedItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            cartItems.remove(selectedItem);
            updateTotalPrice();
        }
    }
    
    // saving current cart to database
    protected void saveCart() {
        if (cartItems.isEmpty()) {
            showAlert(AlertType.WARNING, "Empty Cart", "Your cart is empty. Please add items before saving.");
            return;
        }
        
        // updating current cart with items
        currentCart.getItems().clear();  // Clear previous items
        for (Item item : cartItems) {
            currentCart.addItem(new Item(item.getName(), item.getUnit(), item.getQuantity(), item.getPrice()));
        }
        
        // saving cart to database
        try {
            JdbcDao jdbcDao = new JdbcDao();
            boolean success = jdbcDao.saveCart(currentCart, userId);
            
            if (success) {
                showAlert(AlertType.INFORMATION, "Cart Saved", 
                    "Cart #" + currentCart.getCartId() + " has been saved successfully to the database.");
            } else {
                showAlert(AlertType.ERROR, "Save Error", 
                    "Could not save cart to database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", 
                "Could not save cart: " + e.getMessage());
        }
    }
    
    // checking out cart
    protected void checkoutCart() {
        if (cartItems.isEmpty()) {
            showAlert(AlertType.WARNING, "Empty Cart", "Your cart is empty. Please add items before checking out.");
            return;
        }
        
        // Show confirmation dialog
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Checkout Confirmation");
        alert.setHeaderText("Complete Your Purchase");
        alert.setContentText("Are you done with your shopping and ready to check out?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // updating cart in database as completed
            currentCart.getItems().clear();  // clearing previous items
            for (Item item : cartItems) {
                currentCart.addItem(new Item(item.getName(), item.getUnit(), item.getQuantity(), item.getPrice()));
            }
            currentCart.setCompleted(true);
            
            try {
                JdbcDao jdbcDao = new JdbcDao();
                boolean success = jdbcDao.saveCart(currentCart, userId);
                
                if (success) {
                    // clearing cart and create a new one
                    cartItems.clear();
                    createNewCart();
                    
                    showAlert(AlertType.INFORMATION, "Checkout Complete", 
                        "Thank you for your purchase! Your cart has been checked out.");
                } else {
                    showAlert(AlertType.ERROR, "Checkout Error", 
                        "Could not complete checkout in database.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Database Error", 
                    "Could not complete checkout: " + e.getMessage());
            }
        }
    }
    
    // opening saved carts window
    protected void openSavedCartsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groceryapp/view/SavedCarts.fxml"));
            Parent root = loader.load();
            
            SavedCartsController controller = loader.getController();
            controller.setMainController(this);
            controller.setUserId(userId); // Pass the user ID
            controller.loadSavedCarts(); // Load carts for this user
            
            Stage stage = new Stage();
            stage.setTitle("Saved Carts");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Could not open saved carts window: " + e.getMessage());
        }
    }
    
    // loading a saved cart
    public void loadCart(Cart cart) {
        this.currentCart = cart;
        cartItems.clear();
        
        // adding deep copies of each item in the cart
        for (Item item : cart.getItems()) {
            cartItems.add(new Item(item.getName(), item.getUnit(), item.getQuantity(), item.getPrice()));
        }
        
        updateCartIdLabel();
        updateTotalPrice();
    }
    
    // showing alert dialog
    protected void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Add getter methods to access protected fields
    protected Cart getCurrentCart() {
        return currentCart;
    }
    
    protected ObservableList<Item> getCartItems() {
        return cartItems;
    }
}