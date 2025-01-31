package com.example.greenify_organic_food_app.model;

import java.io.Serializable;

public class CategoryModel implements Serializable {
    private int image;
    private String title;

    public CategoryModel(){

    }

    public CategoryModel(int image, String title) {
        this.image = image;
        this.title = title;
    }

    public int getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }
}

