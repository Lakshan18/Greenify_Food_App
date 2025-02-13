package com.example.greenify_organic_food_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.model.IngredientsAdapter;
import com.example.greenify_organic_food_app.model.NutritionItemAdapter;
import com.example.greenify_organic_food_app.model.NutritionItemModel;
import com.example.greenify_organic_food_app.model.ProductModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SingleProductActivity extends AppCompatActivity {

    TextView productNameTextView, productPriceTextView, productDescriptionTextView, quantityTextView;
    ImageView productImage, increaseBtn, decreaseBtn, recipeSecBtn;
    ProductModel product;

    private RecyclerView ingredientRecyclerView;
    private IngredientsAdapter ingredientsAdapter;
    private List<String> ingredientImageUrls;

    private RecyclerView nutritionRecyclerView;
    private NutritionItemAdapter nutritionAdapter;
    private List<NutritionItemModel> nutritionList;

    private int quantity = 1;
    private double price;
    private final int MAX_QUANTITY = 10;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product);

        db = FirebaseFirestore.getInstance();

        productNameTextView = findViewById(R.id.productNameTextView);
        productPriceTextView = findViewById(R.id.productPriceTextView);
        productDescriptionTextView = findViewById(R.id.productDescriptionTextView);
        productImage = findViewById(R.id.productImage);
        quantityTextView = findViewById(R.id.sgl_pr_qty);

        String productId = getIntent().getStringExtra("p_id");

        if (productId != null) {
            fetchProductData(productId);
        } else {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
        }

        ImageView backTo = findViewById(R.id.backTo_View1);
        backTo.setOnClickListener(v -> {
            Intent intent = new Intent(SingleProductActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        ingredientRecyclerView = findViewById(R.id.ingredientRecyclerView);
        ingredientRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        nutritionRecyclerView = findViewById(R.id.nutritionRecyclerView);
        nutritionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        nutritionList = new ArrayList<>();
        nutritionAdapter = new NutritionItemAdapter(this, nutritionList);
        nutritionRecyclerView.setAdapter(nutritionAdapter);

        Button orderNowBtn = findViewById(R.id.order_now_btn);
        orderNowBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SingleProductActivity.this, PlaceOrderActivity.class);
            startActivity(intent);
        });

        increaseBtn = findViewById(R.id.sgl_increase_btn);
        decreaseBtn = findViewById(R.id.sgl_decrease_btn);

        increaseBtn.setOnClickListener(v -> {
            if (quantity < MAX_QUANTITY) {
                quantity++;
                quantityTextView.setText(String.valueOf(quantity));
                updatePrice();
            } else {
                Toast.makeText(SingleProductActivity.this, "Maximum quantity is " + MAX_QUANTITY, Toast.LENGTH_SHORT).show();
            }
        });

        decreaseBtn.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityTextView.setText(String.valueOf(quantity));
                updatePrice();
            }
        });

        recipeSecBtn = findViewById(R.id.recipe_sec_btn);
        recipeSecBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SingleProductActivity.this, ProductRecipeActivity.class);
            startActivity(intent);
        });
    }

    private void updatePrice() {
        double totalPrice = price * quantity;
        String formattedPrice = String.format("%.2f", totalPrice);
        productPriceTextView.setText("Rs: " + formattedPrice);
    }

    private void fetchProductData(String productId) {
        db.collection("product").document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String description = documentSnapshot.getString("description");
                        String imageUrl = documentSnapshot.getString("image");
                        price = documentSnapshot.getDouble("price");

                        List<Long> ingListLong = (List<Long>) documentSnapshot.get("ing_list");
                        List<Integer> ingList = new ArrayList<>();
                        if (ingListLong != null) {
                            for (Long id : ingListLong) {
                                ingList.add(id.intValue());

                            }
                        }
                        Log.d("FirestoreDebug", "Fetched Ingredient IDs: " + ingList);
                        productNameTextView.setText(name);
                        productDescriptionTextView.setText(description);
                        updatePrice();

                        Glide.with(this).load(imageUrl).into(productImage);

                        loadIngredientImages(ingList);

                        Map<String, Long> nutritionMap = (Map<String, Long>) documentSnapshot.get("nutrition");
                        if (nutritionMap != null) {
                            nutritionList = new ArrayList<>();
                            for (Map.Entry<String, Integer> entry : nutritionMap.entrySet()) {
                                String nutrientName = entry.getKey();
                                int nutrientPercentage = entry.getValue();
                                nutritionList.add(new NutritionItemModel(nutrientName, nutrientPercentage));
                            }

                            nutritionAdapter = new NutritionItemAdapter(this, nutritionList);
                            nutritionRecyclerView.setAdapter(nutritionAdapter);
                        } else {
                            Log.d("FirestoreDb", "No nutrition data found for this product.");
                        }

                    } else {
                        Toast.makeText(this, "Product details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadIngredientImages(List<Integer> ingredientIds) {
        ingredientImageUrls = new ArrayList<>();

        if (ingredientIds == null || ingredientIds.isEmpty()) {
            Log.d("FirestoreDb", "No ingredient IDs found in product.");
            return;
        }

        db.collection("ingredients")
                .whereIn("ing_id", ingredientIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("FirestoreDb", "No matching ingredients found in Firestore.");
                        return;
                    }

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String imageUrl = document.getString("ing_image");
                        Integer fetchedId = document.getLong("ing_id").intValue(); // Fetch ing_id for debugging

                        if (imageUrl != null) {
                            ingredientImageUrls.add(imageUrl);
                            Log.d("FirestoreDb", "Loaded Image for ing_id " + fetchedId + ": " + imageUrl);
                        } else {
                            Log.d("FirestoreDb", "No image found for ing_id: " + fetchedId);
                        }
                    }

                    ingredientsAdapter = new IngredientsAdapter(SingleProductActivity.this, ingredientImageUrls);
                    ingredientRecyclerView.setAdapter(ingredientsAdapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDb", "Error fetching ingredients: " + e.getMessage());
                });
    }

}
