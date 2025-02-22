package com.example.greenify_organic_food_app.model;

public class OrderHisModel {
    private final String order_id;
    private final String title;
    private final int quantity;
    private final double price;
    private final String order_status;
    private final String date;
    private final String productImage;

    public OrderHisModel(String orderId,String title, int quantity, double price,String order_status, String date, String productImage) {
        this.order_id = orderId;
        this.title = title;
        this.quantity = quantity;
        this.price = price;
        this.order_status = order_status;
        this.date = date;
        this.productImage = productImage;
    }

    public String getOrder_id(){return order_id;}
    public String getTitle() { return title; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getOrder_status(){return order_status;}
    public String getDate() { return date; }
    public String getProductImage() { return productImage; }
}