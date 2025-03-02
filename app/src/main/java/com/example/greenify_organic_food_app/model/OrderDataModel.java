package com.example.greenify_organic_food_app.model;

public class OrderDataModel {
    private String customerName;
    private String mobile;
    private String email;
    private String address;
    private double totalPrice;
    private String productName;
    private String orderId;

    public OrderDataModel(String customerName, String mobile, String email, String address, double totalPrice, String productName, String orderId) {
        this.customerName = customerName;
        this.mobile = mobile;
        this.email = email;
        this.address = address;
        this.totalPrice = totalPrice;
        this.productName = productName;
        this.orderId = orderId;
    }

    public String getCustomerName() { return customerName; }
    public String getMobile() { return mobile; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public double getTotalPrice() { return totalPrice; }
    public String getProductName() { return productName; }
    public String getOrderId() { return orderId; }
}