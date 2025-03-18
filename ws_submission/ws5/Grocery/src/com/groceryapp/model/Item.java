/**********************************************
Workshop #5
Course: APD545
Last Name: Chu
First Name: Sin Kau
ID: 155131220
Section: NDD
This assignment represents my own work in accordance with Seneca Academic Policy.
Signature Sin Kau Chu
Date: 16-Mar-2025
**********************************************/

package com.groceryapp.model;

import java.io.Serializable;

public class Item implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String unit;
    private double quantity;
    private double price;

    public Item(String name, String unit, double quantity, double price) {
        this.name = name;
        this.unit = unit;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() { return name; }
    public String getUnit() { return unit; }
    public double getQuantity() { return quantity; }
    public double getPrice() { return price; }
    
    @Override
    public String toString() {
        return name; 
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}