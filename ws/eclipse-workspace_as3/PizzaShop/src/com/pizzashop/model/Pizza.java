package com.pizzashop.model;

public class Pizza {
    public enum Type { CHEESE, VEGETARIAN, MEAT_LOVER }
    public enum Size { SMALL, MEDIUM, LARGE }
    
    private Type type;
    private Size size;
    private int quantity;
    private static final double BASE_PRICE = 10.0;
    private static final double TAX_RATE = 0.13;

    
}