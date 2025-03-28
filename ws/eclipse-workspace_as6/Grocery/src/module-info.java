module Grocery {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires java.sql;        // Add this for JDBC classes
    requires mysql.connector.j;  // Add this for MySQL Connector

    opens com.groceryapp.view to javafx.fxml;
    opens com.groceryapp.controller to javafx.fxml;
    opens com.groceryapp.model to javafx.base;

    exports com.groceryapp;
    exports com.groceryapp.controller;
    exports com.groceryapp.model;
}