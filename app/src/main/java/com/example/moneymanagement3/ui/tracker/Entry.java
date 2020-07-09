package com.example.moneymanagement3.ui.tracker;

public class Entry {

    private String description;
    private String category;
    private String amount;
    private String date;

    public Entry(String description, String category,  String amount, String  date) {
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}



