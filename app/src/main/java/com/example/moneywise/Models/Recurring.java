package com.example.moneywise.Models;

public class Recurring {
    private String userId;
    private String transactionId;
    private String amount;
    private String subcategory;
    private String type;
    private String date;
    private String endDateFrequency;

    // Default constructor (required by Firebase)
    public Recurring() {
    }

    // Getters and setters for the fields

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEndDateFrequency() {
        return endDateFrequency;
    }

    public void setEndDateFrequency(String endDateFrequency) {
        this.endDateFrequency = endDateFrequency;
    }
}
