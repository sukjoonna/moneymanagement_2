package com.example.moneymanagement3.ui.budget;

public class CategoryBudget {

    private String category;
    private String amount;
    private String budget;

    public CategoryBudget(String category,  String amount, String  budget) {
        this.category = category;
        this.amount = amount;
        this.budget = budget;
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

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }


}



