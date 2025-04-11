
/**********************************************
Project
Course: APD545
Last Name: Chu
First Name: Sin Kau
ID: 155131220
Section: NDD
This assignment represents my own work in accordance with Seneca Academic Policy.
Signature Sin Kau Chu
Date: 11-Apr-2025
**********************************************/

package com.finance.util;

import java.util.function.UnaryOperator;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;

/**
 * Utility class for input validation
 */
public class ValidationUtils {
    
    /**
     * Creates a TextFormatter that only allows decimal numbers with up to 2 decimal places
     */
    public static TextFormatter<String> createCurrencyFormatter() {
        UnaryOperator<Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("^\\d*\\.?\\d{0,2}$")) {
                return change;
            }
            return null;
        };
        
        return new TextFormatter<>(filter);
    }
    
    /**
     * Creates a TextFormatter that only allows integers
     */
    public static TextFormatter<String> createIntegerFormatter() {
        UnaryOperator<Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("^\\d*$")) {
                return change;
            }
            return null;
        };
        
        return new TextFormatter<>(filter);
    }
    
    /**
     * Validates that a string can be parsed as a positive number
     */
    public static boolean isPositiveNumber(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        try {
            double value = Double.parseDouble(text);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Formats a double as a currency string with 2 decimal places
     */
    public static String formatCurrency(double amount) {
        return String.format("$%.2f", amount);
    }
}
