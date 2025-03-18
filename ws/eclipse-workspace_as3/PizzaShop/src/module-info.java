module PizzaShop {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.pizzashop to javafx.fxml;
    opens com.pizzashop.controller to javafx.fxml;
    opens com.pizzashop.view to javafx.fxml;

    exports com.pizzashop;
    exports com.pizzashop.controller;
    exports com.pizzashop.view;
}
