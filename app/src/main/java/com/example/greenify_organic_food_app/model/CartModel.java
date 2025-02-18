package com.example.greenify_organic_food_app.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.Objects;

public class CartModel implements Parcelable {
    private final String productName;
    private int quantity;
    private final double price;
    private final String image;
    private boolean selected;
    private String productId; // New field

    // Default constructor
    public CartModel() {
        this.productName = "";
        this.quantity = 0;
        this.price = 0.0;
        this.image = "";
        this.selected = false;
        this.productId = "";
    }

    // Parameterized constructor
    public CartModel(String productName, int quantity, double price, String image, String productId) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.image = image;
        this.selected = false;
        this.productId = productId;
    }

    protected CartModel(Parcel in) {
        productName = in.readString();
        quantity = in.readInt();
        price = in.readDouble();
        image = in.readString();
        selected = in.readByte() != 0;
        productId = in.readString();
    }

    public static final Creator<CartModel> CREATOR = new Creator<CartModel>() {
        @Override
        public CartModel createFromParcel(Parcel in) {
            return new CartModel(in);
        }

        @Override
        public CartModel[] newArray(int size) {
            return new CartModel[size];
        }
    };

    // Getters and setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public String getImage() { return image; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(productName);
        dest.writeInt(quantity);
        dest.writeDouble(price);
        dest.writeString(image);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeString(productId);
    }

    // Rest of existing equals/hashCode/toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartModel cartModel = (CartModel) o;
        return quantity == cartModel.quantity &&
                Double.compare(cartModel.price, price) == 0 &&
                Objects.equals(productName, cartModel.productName) &&
                Objects.equals(image, cartModel.image) &&
                Objects.equals(productId, cartModel.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productName, quantity, price, image, productId);
    }

    @NonNull
    @Override
    public String toString() {
        return "CartModel{" +
                "productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", image='" + image + '\'' +
                ", selected=" + selected +
                ", productId='" + productId + '\'' +
                '}';
    }
}