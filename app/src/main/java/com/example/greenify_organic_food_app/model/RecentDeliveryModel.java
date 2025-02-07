package com.example.greenify_organic_food_app.model;

public class RecentDeliveryModel {
    private String customerName;
    private String deliveryDate;
    private String deliveryStatus;

    public RecentDeliveryModel(String customerName, String deliveryDate, String deliveryStatus) {
        this.customerName = customerName;
        this.deliveryDate = deliveryDate;
        this.deliveryStatus = deliveryStatus;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }
}
