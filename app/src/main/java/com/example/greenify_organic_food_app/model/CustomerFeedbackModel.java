package com.example.greenify_organic_food_app.model;

public class CustomerFeedbackModel {
    private String customerName;
    private String feedback;
    private float rating;

    public CustomerFeedbackModel(String customerName, String feedback, float rating) {
        this.customerName = customerName;
        this.feedback = feedback;
        this.rating = rating;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getFeedback() {
        return feedback;
    }

    public float getRating() {
        return rating;
    }
}
