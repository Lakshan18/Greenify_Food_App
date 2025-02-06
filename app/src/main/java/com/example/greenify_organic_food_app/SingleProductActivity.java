package com.example.greenify_organic_food_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    TextView productNameTextView, productPriceTextView, productDescriptionTextView, quantityTextView;
    ImageView productImage,increaseBtn,decreaseBtn,recipeSecBtn;
    ProductModel product;

    private RecyclerView ingredientRecyclerView;
    private IngredientsAdapter ingredientsAdapter;

    private RecyclerView nutritionRecyclerView;
    private NutritionItemAdapter nutritionAdapter;
    private List<NutritionItemModel> nutritionList;

    private int quantity = 1;  // Initial quantity is 1
    private double price;
    private final int MAX_QUANTITY = 10;  // Maximum quantity limit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product);

        productNameTextView = findViewById(R.id.productNameTextView);
        productPriceTextView = findViewById(R.id.productPriceTextView);
        productDescriptionTextView = findViewById(R.id.productDescriptionTextView);
        productImage = findViewById(R.id.productImage);
        quantityTextView = findViewById(R.id.sgl_pr_qty);  // TextView to show quantity

        product = (ProductModel) getIntent().getSerializableExtra("product");

        if (product != null) {
            productNameTextView.setText(product.getTitle());
            price = product.getPrice();  // Get the price
            updatePrice();  // Display the formatted price
            productDescriptionTextView.setText(product.getDescription());
            productImage.setImageResource(product.getImage());
        }

        ImageView backTo = findViewById(R.id.backTo_View1);
        backTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleProductActivity.this, HomeActivity.class);
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

        nutritionRecyclerView = findViewById(R.id.nutritionRecyclerView);
        nutritionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                Intent intent = new Intent(SingleProductActivity.this, PlaceOrderActivity.class);
                startActivity(intent);
            }
        });

        // Increase and decrease buttons
        increaseBtn = findViewById(R.id.sgl_increase_btn);
        decreaseBtn = findViewById(R.id.sgl_decrease_btn);

        increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity < MAX_QUANTITY) {
                    quantity++;
                    quantityTextView.setText(String.valueOf(quantity));
                    updatePrice();
                } else {
                    // Show Toast if quantity exceeds the limit
                    Toast.makeText(SingleProductActivity.this, "Maximum quantity is " + MAX_QUANTITY, Toast.LENGTH_SHORT).show();
                }
            }
        });

        decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 1) {
                    quantity--;
                    quantityTextView.setText(String.valueOf(quantity));
                    updatePrice();
                }
            }
        });

        recipeSecBtn = findViewById(R.id.recipe_sec_btn);
        recipeSecBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleProductActivity.this,ProductRecipeActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updatePrice() {
        double totalPrice = price * quantity;
        String formattedPrice = String.format("%.2f", totalPrice);
        productPriceTextView.setText("Rs: " + formattedPrice);
    }
}
