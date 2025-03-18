module Grocery {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;

    opens com.groceryapp.view to javafx.fxml;
    opens com.groceryapp.controller to javafx.fxml;
    opens com.groceryapp.model to javafx.base;
    
    exports com.groceryapp;
    exports com.groceryapp.controller;
    exports com.groceryapp.model;
}