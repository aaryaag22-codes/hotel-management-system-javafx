package com.hotel.model;

import java.io.Serializable;

/*
FoodItem.java - Represents a food/beverage item on the hotel menu
 */
public class FoodItem implements Serializable {

    // Week 2: Enum for food category
    public enum FoodCategory { BREAKFAST, LUNCH, DINNER, BEVERAGE, SNACKS }

    private int itemId;
    private String name;
    private double price;
    private FoodCategory category;
    private boolean vegetarian;

    public FoodItem(int itemId, String name, double price,
                    FoodCategory category, boolean vegetarian) {
        this.itemId     = itemId;
        this.name       = name;
        this.price      = price;
        this.category   = category;
        this.vegetarian = vegetarian;
    }

    public int          getItemId()     { return itemId; }
    public String       getName()       { return name; }
    public double       getPrice()      { return price; }
    public FoodCategory getCategory()   { return category; }
    public boolean      isVegetarian()  { return vegetarian; }

    @Override
    public String toString() {
        return name + " - ₹" + price + " [" + category + "]"
               + (vegetarian ? " 🌿" : " 🍗");
    }
}