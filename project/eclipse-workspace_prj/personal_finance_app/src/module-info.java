module personal_finance_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
//    requires sqlite.jdbc;
    
    opens com.finance to javafx.graphics, javafx.fxml;
    opens com.finance.view to javafx.fxml, javafx.graphics;
    opens com.finance.model to javafx.base;
    opens com.finance.controller to javafx.fxml;
}