package com.example.greenify_organic_food_app.model;

import java.io.Serializable;
import java.util.Date;

public class ProductModel implements Serializable {

    int product_id;
    String title;
    double price;
    int qty;
    String description;
    Date manuf_date;
    Date expire_date;

}
