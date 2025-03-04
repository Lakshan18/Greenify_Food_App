package com.example.greenify_organic_food_app.model;

import java.io.Serializable;
import java.util.List;

public class ProductModel implements Serializable {
    private String productId;
    private String imageUrl;
    private String name;
    private double price;
    private String rating;
    private String category;
    private String description;
    private List<Integer> ing_list;

    public ProductModel() {
    }

    public ProductModel(String productId, String imageUrl, String name, double price, String rating, String category, String description) {
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.category = category;
        this.description = description;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Integer> getIngList() {
        return ing_list;
    }

    public void setIngList(List<Integer> ing_list) {
        this.ing_list = ing_list;
    }
}