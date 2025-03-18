/**********************************************
Workshop #5
Course: APD545
Last Name: Chu
First Name: Sin Kau
ID: 155131220
Section: NDD
This assignment represents my own work in accordance with Seneca Academic Policy.
Signature Sin Kau Chu
Date: 16-Mar-2025
**********************************************/

package com.groceryapp.controller;

import com.groceryapp.model.Cart;
import com.groceryapp.model.CartManager;
import com.groceryapp.model.Item;
import com.groceryapp.model.ItemModel;

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
import java.util.Optional;

public class CartController {
    @FXML private ComboBox<Item> itemsComboBox;
    @FXML private Slider quantitySlider;
    @FXML private Label unitPriceLabel, totalPriceLabel;
    @FXML private Button addToCartButton, removeButton, saveCartButton, checkoutButton, viewSavedCartsButton;
    @FXML private TableView<Item> cartTableView;
    @FXML private TableColumn<Item, String> itemColumn;
    @FXML private TableColumn<Item, Double> priceColumn;
    @FXML private TableColumn<Item, Double> quantityColumn;
    @FXML private Label selectedQuantityLabel;
    @FXML private Label purchasedUnitsLabel;
    @FXML private Label unitLabel;
    @FXML private Label cartIdLabel;
 
    private ItemModel itemModel = new ItemModel();
    private ObservableList<Item> cartItems = FXCollections.observableArrayList();
    private Cart currentCart;
    private SimpleDoubleProperty totalPrice = new SimpleDoubleProperty(0.0);

    @FXML
    public void initialize() {
        // Create a new cart
        createNewCart();
        
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
        
        // Set up button event handlers
        addToCartButton.setOnAction(e -> addToCart());
        removeButton.setOnAction(e -> removeFromCart());
        saveCartButton.setOnAction(e -> saveCart());
        checkoutButton.setOnAction(e -> checkoutCart());
        viewSavedCartsButton.setOnAction(e -> openSavedCartsWindow());

        // Bind total price to the label with formatting
        totalPriceLabel.textProperty().bind(
            Bindings.createStringBinding(() -> 
                String.format("$%.2f", totalPrice.get()), totalPrice)
        );
        
        // Set the TableView items
        cartTableView.setItems(cartItems);
    }

    private void createNewCart() {
        currentCart = new Cart(CartManager.getNextCartId());
        cartItems.clear();
        updateCartIdLabel();
        updateTotalPrice();
    }
    
    private void updateCartIdLabel() {
        if (cartIdLabel != null) {
            cartIdLabel.setText("Cart #" + currentCart.getCartId());
        }
    }

    private void addToCart() {
        Item selectedItem = itemsComboBox.getValue();
        if (selectedItem != null) {
            double quantityToBuy = quantitySlider.getValue();

            if (quantityToBuy <= 0) {
                System.out.println("Please select a valid quantity.");
                return;
            }

            // check if item exists in the cart and update quantity
            for (Item cartItem : cartItems) {
                if (cartItem.getName().equals(selectedItem.getName())) {
                    cartItem.setQuantity(cartItem.getQuantity() + quantityToBuy);
                    cartTableView.refresh();
                    updateTotalPrice();
                    return;
                }
            }

            // create new item and add to cart
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

    // Method to update total price
    private void updateTotalPrice() {
        double total = 0.0;
        for (Item item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        totalPrice.set(total);
    }

    private void removeFromCart() {
        Item selectedItem = cartTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            cartItems.remove(selectedItem);
            updateTotalPrice();
        }
    }
    
    // Save current cart
    private void saveCart() {
        if (cartItems.isEmpty()) {
            showAlert(AlertType.WARNING, "Empty Cart", "Your cart is empty. Please add items before saving.");
            return;
        }
        
        // Update current cart with items
        currentCart.getItems().clear();  // Clear previous items
        for (Item item : cartItems) {
            currentCart.addItem(new Item(item.getName(), item.getUnit(), item.getQuantity(), item.getPrice()));
        }
        
        // Save cart to file
        try {
            CartManager.saveCart(currentCart);
            showAlert(AlertType.INFORMATION, "Cart Saved", 
                "Cart #" + currentCart.getCartId() + " has been saved successfully.");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Save Error", 
                "Could not save cart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Checkout cart
    private void checkoutCart() {
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
            // Update cart in file as completed
            currentCart.getItems().clear();  // Clear previous items
            for (Item item : cartItems) {
                currentCart.addItem(new Item(item.getName(), item.getUnit(), item.getQuantity(), item.getPrice()));
            }
            currentCart.setCompleted(true);
            
            try {
                CartManager.saveCart(currentCart);
                
                // Clear cart and create a new one
                cartItems.clear();
                createNewCart();
                
                showAlert(AlertType.INFORMATION, "Checkout Complete", 
                    "Thank you for your purchase! Your cart has been checked out.");
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Checkout Error", 
                    "Could not complete checkout: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // Open saved carts window
    private void openSavedCartsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groceryapp/view/SavedCarts.fxml"));
            Parent root = loader.load();
            
            SavedCartsController controller = loader.getController();
            controller.setMainController(this);
            
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
    
    // Load a saved cart
    public void loadCart(Cart cart) {
        this.currentCart = cart;
        cartItems.clear();
        
        // Add deep copies of each item in the cart
        for (Item item : cart.getItems()) {
            cartItems.add(new Item(item.getName(), item.getUnit(), item.getQuantity(), item.getPrice()));
        }
        
        updateCartIdLabel();
        updateTotalPrice();
    }
    
    // Show alert dialog
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}