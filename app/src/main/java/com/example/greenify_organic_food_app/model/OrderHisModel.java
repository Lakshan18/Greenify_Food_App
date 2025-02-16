package com.example.greenify_organic_food_app.model;

public class OrderHisModel {
    private final String title;
    private final int quantity;
    private final double price;
    private final String date;
    private final int imageResource;

    public OrderHisModel(String title, int quantity, double price, String date, int imageResource) {
        this.title = title;
        this.quantity = quantity;
        this.price = price;
        this.date = date;
        this.imageResource = imageResource;
    }

    public String getTitle() { return title; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getDate() { return date; }
    public int getImageResource() { return imageResource; }
}