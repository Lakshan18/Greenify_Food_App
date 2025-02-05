package com.example.greenify_organic_food_app.model;

import java.io.Serializable;
import java.util.Date;

public class ProductModel implements Serializable {
    private int product_id;
    private int image;
    private String title;
    private double price;
    private int qty;
    private String description;
    private Date manuf_date;
    private Date expire_date;
    private String rating;

    public ProductModel(int image,String title,double price,String rating){
        this.image = image;
        this.title = title;
        this.price = price;
        this.rating = rating;
    }

//    all parameters needed points.....
    public ProductModel(int product_id, int image, String title, double price, int qty,
                        String description, Date manuf_date, Date expire_date, String rating) {
        this.product_id = product_id;
        this.image = image;
        this.title = title;
        this.price = price;
        this.qty = qty;
        this.description = description;
        this.manuf_date = manuf_date;
        this.expire_date = expire_date;
        this.rating = rating;
    }

    // Getters
    public int getProductId() { return product_id; }
    public int getImage() { return image; }
    public String getTitle() { return title; }
    public double getPrice() { return price; }
    public int getQty() { return qty; }
    public String getDescription() { return description; }
    public Date getManufDate() { return manuf_date; }
    public Date getExpireDate() { return expire_date; }
    public String getRating() { return rating; }
}
