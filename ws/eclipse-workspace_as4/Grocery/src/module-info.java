module Grocery {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.groceryapp.view to javafx.fxml;
    opens com.groceryapp.controller to javafx.fxml; // Add this line
    
    exports com.groceryapp;
    exports com.groceryapp.controller;
}