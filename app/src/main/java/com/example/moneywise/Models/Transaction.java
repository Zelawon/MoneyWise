    package com.example.moneywise.Models;

    public class Transaction {
        private String userId;
        private String transactionId;
        private String amount;
        private String category;
        private String subcategory;
        private String type;
        private String title;
        private String date;
        private String note;
        private boolean recurring;
        private String frequency;
        private String endDateFrequency;

        // Default constructor (required by Firebase)
        public Transaction() {
        }

        // Getters and setters for the fields


        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getSubcategory() {
            return subcategory;
        }

        public void setSubcategory(String subcategory) {
            this.subcategory = subcategory;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public boolean isRecurring() {
            return recurring;
        }

        public void setRecurring(boolean recurring) {
            this.recurring = recurring;
        }

        public String getFrequency() {
            return frequency;
        }

        public void setFrequency(String frequency) {
            this.frequency = frequency;
        }

        public String getEndDateFrequency() {
            return endDateFrequency;
        }

        public void setEndDateFrequency(String endDateFrequency) {
            this.endDateFrequency = endDateFrequency;
        }
        // Add other fields and methods as needed
    }
