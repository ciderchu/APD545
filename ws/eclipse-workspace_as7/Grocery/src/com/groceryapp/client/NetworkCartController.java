/**********************************************
Workshop #7
Course: APD545
Last Name: Chu
First Name: Sin Kau
ID: 155131220
Section: NDD
This assignment represents my own work in accordance with Seneca Academic Policy.
Signature Sin Kau Chu
Date: 31-Mar-2025
**********************************************/

package com.groceryapp.client;

import com.groceryapp.controller.CartController;
import com.groceryapp.model.Cart;
import com.groceryapp.model.Item;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Extended cart controller that communicates with the server for cart operations.
 * This controller sends cart actions to the server instead of directly accessing the database.
 */
public class NetworkCartController extends CartController {
    
    private GroceryClient client;
    
    public void setClient(GroceryClient client) {
        this.client = client;
    }
    
    @Override
    public void initData(int userId) {
        super.initData(userId);
        
        // updating welcome message to indicate networked mode
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome to Network Grocery Store! User ID: " + userId);
        }
    }
    
    @Override
    protected void addToCart() {
        // calling the parent implementation first
        super.addToCart();
        
        // sending the action to server
        if (client != null && client.isConnected()) {
            Item selectedItem = itemsComboBox.getValue();
            if (selectedItem != null) {
                double quantity = quantitySlider.getValue();
                client.sendCartAction("ADD:" + selectedItem.getName() + ":" + quantity);
            }
        }
    }
    
    @Override
    protected void removeFromCart() {
        // getting the selected item before removing it
        Item selectedItem = cartTableView.getSelectionModel().getSelectedItem();
        
        // calling the parent implementation
        super.removeFromCart();
        
        // sending the action to server
        if (client != null && client.isConnected() && selectedItem != null) {
            client.sendCartAction("REMOVE:" + selectedItem.getName());
        }
    }
    
    @Override
    protected void saveCart() {
        if (client == null || !client.isConnected()) {
            showAlert(AlertType.ERROR, "Connection Error", 
                "Not connected to server. Cannot save cart.");
            return;
        }
        
        // constructing cart data to send
        StringBuilder cartData = new StringBuilder("SAVE:");
        cartData.append(getCurrentCart().getCartId()).append(":");
        
        for (Item item : getCartItems()) {
            cartData.append(item.getName()).append(",")
                   .append(item.getQuantity()).append(",")
                   .append(item.getPrice()).append(";");
        }
        
        // sending the action to server
        client.sendCartAction(cartData.toString());
        
        showAlert(AlertType.INFORMATION, "Cart Saved", 
            "Cart information sent to server for saving.");
    }
    
    @Override
    protected void checkoutCart() {
        if (client == null || !client.isConnected()) {
            showAlert(AlertType.ERROR, "Connection Error", 
                "Not connected to server. Cannot checkout cart.");
            return;
        }
        
        // checking if cart is empty
        if (getCartItems().isEmpty()) {
            showAlert(AlertType.WARNING, "Empty Cart", 
                "Your cart is empty. Please add items before checking out.");
            return;
        }
        
        // sending checkout action to server
        client.sendCartAction("CHECKOUT:" + getCurrentCart().getCartId());
        
        // clearing cart locally
        getCartItems().clear();
        createNewCart();
        
        showAlert(AlertType.INFORMATION, "Checkout Complete", 
            "Thank you for your purchase! Your cart has been checked out.");
    }
    
    @Override
    protected void openSavedCartsWindow() {
        if (client == null || !client.isConnected()) {
            showAlert(AlertType.ERROR, "Connection Error", 
                "Not connected to server. Cannot retrieve saved carts.");
            return;
        }
        
        // requesting saved carts from server
        client.sendCartAction("GET_SAVED_CARTS");
        
        // In a complete implementation, we would handle the server response
        // and display the saved carts. For now, we just show a message.
        showAlert(AlertType.INFORMATION, "Saved Carts", 
            "Retrieved saved carts from server.");
    }
    
    // helper methods to access protected fields from parent class
    protected Cart getCurrentCart() {
        return currentCart;
    }
    
    @Override
    public void loadCart(Cart cart) {
        super.loadCart(cart);
        
        if (client != null && client.isConnected()) {
            client.sendCartAction("LOAD:" + cart.getCartId());
        }
    }
}