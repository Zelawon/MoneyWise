package com.example.moneywise.Models;

public class Item {
    private String itemName;

    // Constructor
    public Item(String itemName) {
        this.itemName = itemName;
    }

    // Getter and setter for itemName
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
