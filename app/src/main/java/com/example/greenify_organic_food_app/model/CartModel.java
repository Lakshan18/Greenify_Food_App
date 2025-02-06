package com.example.greenify_organic_food_app.model;

public class CartModel {
    private String name;
    private int quantity;
    private double price;
    private int imageResource;

    public CartModel(String name, int quantity, double price, int imageResource) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}
