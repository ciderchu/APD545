/**********************************************
Workshop 1
Course: APD 545
Last Name: CHU
First Name: SIN KAU
ID: 155131220
Section: NDD
This assignment represents my own work in accordance with Seneca Academic Policy.
Signature: SIN KAU, CHU
Date: 2.16.2025
**********************************************/

package carLoanCalculator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CarLoanCalculator extends Application {
    
    // creating control fields
    private TextField vehicleTypeField;
    private TextField vehicleAgeField;
    private TextField priceField;
    private TextField downPaymentField;
    private TextField interestRateField;
    private Slider loanPeriodSlider;
    private ComboBox<String> paymentFrequencyCombo;
    private TextField resultField;
    
    @Override
    public void start(Stage primaryStage) {
        // creating main container
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(15));
        
        // creating grid for form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        
        // starting all UI controls
        initializeControls();
        
        // adding controls to grid
        setupGridControls(grid);
        
        // creating and add buttons
        HBox buttonBox = createButtons();
        
        // creating result field layout
        HBox resultBox = new HBox(10);
        resultBox.setAlignment(Pos.CENTER);
        Label resultLabel = new Label("Your Estimated Fixed Rate\nLoan Payment is:");
        resultBox.getChildren().addAll(resultLabel, resultField);
        
        // adding grid and buttons to main layout
        mainLayout.getChildren().addAll(grid, buttonBox, resultBox);
        
        // creating and set scene
        Scene scene = new Scene(mainLayout);
        primaryStage.setTitle("Car Loan Calculator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void initializeControls() {
        // txt field for vehicle type 
    	vehicleTypeField = new TextField();
    	vehicleTypeField.setPromptText("Car/Truck");
        
        // txt field for vehicle age
    	vehicleAgeField = new TextField();
    	vehicleAgeField.setPromptText("New/Used");
        
        // txt field for price 
        priceField = new TextField();
        priceField.setPromptText("$0.00");
        
        // txt field for down payment
        downPaymentField = new TextField();
        downPaymentField.setPromptText("$0.00");
        
        // txt field for interest rate
        interestRateField = new TextField();
        interestRateField.setPromptText("0.00%");
        
        // period slider for loan period
        loanPeriodSlider = new Slider(12, 96, 12);
        loanPeriodSlider.setShowTickLabels(true);
        loanPeriodSlider.setShowTickMarks(true);
        loanPeriodSlider.setMajorTickUnit(12);
        loanPeriodSlider.setBlockIncrement(12);
        loanPeriodSlider.setSnapToTicks(true);
        
        // combo box for payment frequency
        paymentFrequencyCombo = new ComboBox<>();
        paymentFrequencyCombo.getItems().addAll("Weekly", "Bi-Weekly", "Monthly");
        paymentFrequencyCombo.setPromptText("Weekly/Bi-Weekly/Monthly");
        
        // result field
        resultField = new TextField();
        resultField.setEditable(false);
        resultField.setPromptText("$0.00");
        
        // adding the change listener to loan period slider
        loanPeriodSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // round to the nearest month for calculation
            int months = (int) Math.round(newValue.doubleValue());
            loanPeriodSlider.setValue(months);
        });
        
    }
    
    private void setupGridControls(GridPane grid) {
        // adding all controls to grid with labels
        grid.add(new Label("Type of Vehicle:"), 0, 0);
        grid.add(vehicleTypeField, 1, 0);
        
        grid.add(new Label("Age of Vehicle:"), 0, 1);
        grid.add(vehicleAgeField, 1, 1);
        
        grid.add(new Label("Price of the Vehicle: $"), 0, 2);
        grid.add(priceField, 1, 2);
        
        grid.add(new Label("Down Payment: $"), 0, 3);
        grid.add(downPaymentField, 1, 3);
        
        grid.add(new Label("Interest Rate: %"), 0, 4);
        grid.add(interestRateField, 1, 4);
        
        grid.add(new Label("Loan Payment Period:"), 0, 5);
        grid.add(loanPeriodSlider, 1, 5);
        
        grid.add(new Label("Loan Payment Frequency:"), 0, 6);
        grid.add(paymentFrequencyCombo, 1, 6);
      
    }
    
    private HBox createButtons() {
        Button clearButton = new Button("Clear");
        Button calculateButton = new Button("Get Results");
        
        // clearing the button for the event handler
        clearButton.setOnAction(e -> {
            vehicleTypeField.clear();
            vehicleAgeField.clear();
            priceField.clear();
            downPaymentField.clear();
            interestRateField.clear();
            loanPeriodSlider.setValue(12); // reseting the value to minimum value
            paymentFrequencyCombo.setValue(null);
            resultField.clear();
        });
        
        // event handler for the calculate button
        calculateButton.setOnAction(e -> calculateLoanPayment());
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(clearButton, calculateButton);
        
        return buttonBox;
    }
    
    private void calculateLoanPayment() {
        try {
            // getting value from the input field
            double vehiclePrice = Double.parseDouble(priceField.getText().replace("$", "").replace(",", ""));
            double downPayment = Double.parseDouble(downPaymentField.getText().replace("$", "").replace(",", ""));
            double annualInterestRate = Double.parseDouble(interestRateField.getText().replace("%", "")) / 100.0;
            int loanPeriodMonths = (int) loanPeriodSlider.getValue();
            String paymentFrequency = paymentFrequencyCombo.getValue();
            
            // validating the input
            if (vehiclePrice <= 0 || downPayment < 0 || annualInterestRate < 0 || loanPeriodMonths <= 0) {
                showError("Please enter valid positive numbers for all fields.");
                return;
            }
            
            if (downPayment >= vehiclePrice) {
                showError("Down payment cannot be greater than or equal to vehicle price.");
                return;
            }
            
            if (paymentFrequency == null) {
                showError("Please select a payment frequency.");
                return;
            }
            
            // calculating the loan amount
            double loanAmount = vehiclePrice - downPayment;
            
            // calculating the  monthly interest rate
            double monthlyInterestRate = annualInterestRate / 12.0;
            
            // calculating the base monthly payment using loan amortization formula
            double monthlyPayment = (loanAmount * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, loanPeriodMonths))
                    / (Math.pow(1 + monthlyInterestRate, loanPeriodMonths) - 1);
            
            // adjusting the payment frequency
            double adjustedPayment;
            switch (paymentFrequency) {
                case "Weekly":
                    adjustedPayment = (monthlyPayment * 12) / 52;
                    break;
                case "Bi-Weekly":
                    adjustedPayment = (monthlyPayment * 12) / 26;
                    break;
                default: // Monthly
                    adjustedPayment = monthlyPayment;
                    break;
            }
            
            // aligning the format and display result
            String formattedPayment = String.format("$%.2f", adjustedPayment);
            resultField.setText(formattedPayment);
            
        } catch (NumberFormatException ex) {
            showError("Please enter valid numbers for price, down payment, and interest rate.");
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}