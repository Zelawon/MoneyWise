package com.example.moneywise.Models;

public class DailyBalance {
    private String day;
    private double balance;

    public DailyBalance(String day, double balance) {
        this.day = day;
        this.balance = balance;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getDay() {
        return day;
    }

    public double getBalance() {
        return balance;
    }
}