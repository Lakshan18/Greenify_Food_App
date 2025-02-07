package com.example.greenify_organic_food_app.model;

public class OngoingDeliveryModel {
    private String customerName;
    private String deliveryAddress;
    private String status;

    public OngoingDeliveryModel(String customerName, String deliveryAddress, String status) {
        this.customerName = customerName;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getStatus() {
        return status;
    }
}
