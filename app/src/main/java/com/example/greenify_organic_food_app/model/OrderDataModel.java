package com.example.greenify_organic_food_app.model;

public class OrderDataModel {
    private String customerName;
    private String mobile;
    private String email;
    private String address;
    private double totalPrice;
    private String orderId;

    // Constructor
    public OrderDataModel(String customerName, String mobile, String email, String address, double totalPrice, String orderId) {
        this.customerName = customerName;
        this.mobile = mobile;
        this.email = email;
        this.address = address;
        this.totalPrice = totalPrice;
        this.orderId = orderId;
    }

    // Getters
    public String getCustomerName() {
        return customerName;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getOrderId() {
        return orderId;
    }
}
