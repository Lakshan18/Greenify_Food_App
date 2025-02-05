package com.example.greenify_organic_food_app.model;

import java.io.Serializable;

public class NutritionItemModel implements Serializable {
    private String name;
    private int percentage;

    public NutritionItemModel(String name, int percentage) {
        this.name = name;
        this.percentage = percentage;
    }

    public String getName() {
        return name;
    }

    public int getPercentage() {
        return percentage;
    }
}
