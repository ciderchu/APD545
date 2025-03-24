/**********************************************
Workshop 1
Course: APD 545
Last Name: Yamat
First Name: Oliveir Mari
ID: 135290237
Section: NDD
This assignment represents my own work in accordance with Seneca Academic Policy.
Signature: Oliveir Mari
Date: 2.4.2025
**********************************************/

package carLoanCal;


import javafx.application.Application;
import javafx.geometry.Insets;
//import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.text.NumberFormat;

public class carLoanCal extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Root layout
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Header
        Label header = new Label("Car Loan Calculator");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        root.getChildren().add(header);
        
        // GridPane for input fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        Label lblVehicleType = new Label("Vehicle Type:");
        TextField txtVehicleType = new TextField();
        Label lblAge = new Label("Age of Vehicle:");
        TextField txtAge = new TextField();
        Label lblPrice = new Label("Car Price:");
        TextField txtPrice = new TextField();
        Label lblDownPayment = new Label("Down Payment:");
        TextField txtDownPayment = new TextField();
        Label lblInterestRate = new Label("Monthly Interest Rate (%):");
        TextField txtInterestRate = new TextField();
        Label lblFrequency = new Label("Payment Frequency:");
        ComboBox<String> cmbFrequency = new ComboBox<>();
        cmbFrequency.getItems().addAll("Weekly", "Bi-Weekly", "Monthly");
        cmbFrequency.setValue("Monthly");

        // Loan duration slider
        Label lblDuration = new Label("Loan Duration (Months):");
        Slider slider = new Slider(12, 96, 12);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(12);
        slider.setBlockIncrement(12);
        Label lblDurationValue = new Label("12");
        slider.valueProperty().addListener((obs, oldVal, newVal) -> 
            lblDurationValue.setText(String.valueOf(newVal.intValue())));
        
        // Result label
        Label lblResult = new Label("Monthly Payment: $0.00");
        
        // Calculate button
        Button btnCalculate = new Button("Calculate");
        btnCalculate.setOnAction(e -> {
            try {
                double price = Double.parseDouble(txtPrice.getText());
                double downPayment = Double.parseDouble(txtDownPayment.getText());
                double interestRate = Double.parseDouble(txtInterestRate.getText()) / 100;
                int months = (int) slider.getValue();
                
                double loanAmount = price - downPayment;
                double monthlyRate = interestRate / 12;
                double monthlyPayment = (loanAmount * monthlyRate) / 
                        (1 - Math.pow(1 + monthlyRate, -months));
                
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                lblResult.setText("Monthly Payment: " + currencyFormat.format(monthlyPayment));
            } catch (NumberFormatException ex) {
                lblResult.setText("Invalid input. Please enter numeric values.");
            }
        });

        // Layout setup
        grid.add(lblVehicleType, 0, 0);
        grid.add(txtVehicleType, 1, 0);
        grid.add(lblAge, 0, 1);
        grid.add(txtAge, 1, 1);
        grid.add(lblPrice, 0, 2);
        grid.add(txtPrice, 1, 2);
        grid.add(lblDownPayment, 0, 3);
        grid.add(txtDownPayment, 1, 3);
        grid.add(lblInterestRate, 0, 4);
        grid.add(txtInterestRate, 1, 4);
        grid.add(lblFrequency, 0, 5);
        grid.add(cmbFrequency, 1, 5);
        grid.add(lblDuration, 0, 6);
        grid.add(slider, 1, 6);
        grid.add(lblDurationValue, 2, 6);
        
        root.getChildren().addAll(grid, btnCalculate, lblResult);

        // Scene setup
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("Car Loan Calculator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
}