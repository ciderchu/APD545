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
package com.groceryapp;

import com.groceryapp.client.GroceryClient;
import javafx.application.Application;

/**
 * Main entry point for the networked grocery application.
 * This class simply delegates to the GroceryClient class.
 */
public class NetworkMain {
    public static void main(String[] args) {
        // Starting the networked client application
        Application.launch(GroceryClient.class, args);
    }
}