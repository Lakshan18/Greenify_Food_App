package com.example.greenify_organic_food_app.model;

import java.io.Serializable;

public class ProductModel implements Serializable {
    private String imageUrl;
    private String name;
    private double price;
    private String rating;
    private String category;
    private String description;

    // Constructor with Firebase fields
    public ProductModel(String imageUrl, String name, double price, String rating, String category, String description) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.category = category;
        this.description = description;
    }

    // Getters and setters
    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getRating() {
        return rating;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }
}
