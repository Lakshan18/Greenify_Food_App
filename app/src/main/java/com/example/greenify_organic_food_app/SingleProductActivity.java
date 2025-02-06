package com.example.greenify_organic_food_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenify_organic_food_app.model.IngredientsAdapter;
import com.example.greenify_organic_food_app.model.NutritionItemAdapter;
import com.example.greenify_organic_food_app.model.NutritionItemModel;
import com.example.greenify_organic_food_app.model.ProductModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SingleProductActivity extends AppCompatActivity {

    TextView productNameTextView, productPriceTextView, productDescriptionTextView;
    ImageView productImage;
    ProductModel product;

    private RecyclerView ingredientRecyclerView;
    private IngredientsAdapter ingredientsAdapter;

    private RecyclerView nutritionRecyclerView;
    private NutritionItemAdapter nutritionAdapter;
    private List<NutritionItemModel> nutritionList;

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

        ingredientRecyclerView = findViewById(R.id.ingredientRecyclerView);

        List<Integer> ingredientImages = Arrays.asList(
                R.drawable.green_beans, R.drawable.cabbage, R.drawable.carrot,
                R.drawable.tomato, R.drawable.onion, R.drawable.green_chilli
        );

        ingredientsAdapter = new IngredientsAdapter(this, ingredientImages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        ingredientRecyclerView.setLayoutManager(layoutManager);
        ingredientRecyclerView.setAdapter(ingredientsAdapter);

//        nutritions data....

        nutritionRecyclerView = findViewById(R.id.nutritionRecyclerView);
        nutritionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Dummy data (replace with database data)
        nutritionList = new ArrayList<>();
        nutritionList.add(new NutritionItemModel("Carbohydrate", 50));
        nutritionList.add(new NutritionItemModel("Protein", 25));
        nutritionList.add(new NutritionItemModel("Vitamins", 85));
        nutritionList.add(new NutritionItemModel("Fats", 10));

        nutritionAdapter = new NutritionItemAdapter(this, nutritionList);
        nutritionRecyclerView.setAdapter(nutritionAdapter);

        Button orderNowBtn = findViewById(R.id.order_now_btn);
        orderNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleProductActivity.this,PlaceOrderActivity.class);
                startActivity(intent);
            }
        });
    }
}
