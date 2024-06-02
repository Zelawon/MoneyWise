package com.example.moneywise.Models;

public class DailyTotals {
    private String date;
    private double income;
    private double expense;

    public DailyTotals() {
        this.income = 0;
        this.expense = 0;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public double getExpense() {
        return expense;
    }

    public void setExpense(double expense) {
        this.expense = expense;
    }
}