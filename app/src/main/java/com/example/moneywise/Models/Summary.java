package com.example.moneywise.Models;

public class Summary {
    private String date;
    private double income;
    private double expense;
    private double sum;
    private int numTransactions;
    private String mostSpendingCategory;

    public Summary() {
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

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public int getNumTransactions() {
        return numTransactions;
    }

    public void setNumTransactions(int numTransactions) {
        this.numTransactions = numTransactions;
    }

    public String getMostSpendingCategory() {
        return mostSpendingCategory;
    }

    public void setMostSpendingCategory(String mostSpendingCategory) {
        this.mostSpendingCategory = mostSpendingCategory;
    }
}
