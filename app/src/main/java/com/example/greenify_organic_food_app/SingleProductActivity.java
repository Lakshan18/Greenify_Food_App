package com.example.greenify_organic_food_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.greenify_organic_food_app.model.ProductModel;

public class SingleProductActivity extends AppCompatActivity {

    TextView productNameTextView, productPriceTextView, productDescriptionTextView;
    ImageView productImage;
    ProductModel product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product);  // Use your layout for product details


        productNameTextView = findViewById(R.id.productNameTextView);
        productPriceTextView = findViewById(R.id.productPriceTextView);
        productDescriptionTextView = findViewById(R.id.productDescriptionTextView);
        productImage = findViewById(R.id.productImage);

        // Retrieve the ProductModel object from the Intent
        product = (ProductModel) getIntent().getSerializableExtra("product");

        if (product != null) {
            // Set the product details in the views
            productNameTextView.setText(product.getTitle());
            productPriceTextView.setText("Rs: " + product.getPrice());
            productDescriptionTextView.setText(product.getDescription());

            // Set the product image
            productImage.setImageResource(product.getImage());  // Set image using the resource ID
        }

        ImageView backTo = findViewById(R.id.backTo_View1);
        backTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleProductActivity.this,HomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
