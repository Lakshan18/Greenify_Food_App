package com.example.greenify_organic_food_app.model;

import java.util.List;

public class OrderHisModel {
    private String orderId;
    private String orderStatus;
    private String date;
    private List<CartModel> products;
    private double totalPrice;
    private double totalDiscount;

    public OrderHisModel(String orderId, String orderStatus, String date, List<CartModel> products, double totalPrice, double totalDiscount) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.date = date;
        this.products = products;
        this.totalPrice = totalPrice;
        this.totalDiscount = totalDiscount;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getDate() {
        return date;
    }

    public List<CartModel> getProducts() {
        return products;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }
}