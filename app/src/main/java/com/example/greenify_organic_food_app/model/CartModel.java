package com.example.greenify_organic_food_app.model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class CartModel {
    private final String productName; // Name of the product
    private int quantity; // Quantity of the product in the cart
    private final double price; // Price of the product
    private final String image; // Image URL of the product

    // Default constructor (required for Firestore deserialization)
    public CartModel() {
        this.productName = "";
        this.quantity = 0;
        this.price = 0.0;
        this.image = "";
    }

    // Parameterized constructor
    public CartModel(String productName, int quantity, double price, String image) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.image = image;
    }

    // Getters
    public String getProductName() {
        return productName;
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

    public String getImage() {
        return image;
    }

    // toString() method for debugging
    @NonNull
    @Override
    public String toString() {
        return "CartModel{" +
                "productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", image='" + image + '\'' +
                '}';
    }

    // equals() method for object comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartModel cartModel = (CartModel) o;
        return quantity == cartModel.quantity &&
                Double.compare(cartModel.price, price) == 0 &&
                Objects.equals(productName, cartModel.productName) &&
                Objects.equals(image, cartModel.image);
    }

    // hashCode() method for object comparison in collections
    @Override
    public int hashCode() {
        return Objects.hash(productName, quantity, price, image);
    }
}