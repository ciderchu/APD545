module Grocery {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires java.sql;
    requires mysql.connector.j;

    opens com.groceryapp.view to javafx.fxml;
    opens com.groceryapp.controller to javafx.fxml;
    opens com.groceryapp.model to javafx.base;
    opens com.groceryapp.client to javafx.fxml;  // Add this

    exports com.groceryapp;
    exports com.groceryapp.controller;
    exports com.groceryapp.model;
    exports com.groceryapp.client;  // Add this
    exports com.groceryapp.server;  // Add this
}