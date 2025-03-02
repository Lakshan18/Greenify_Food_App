package com.example.greenify_organic_food_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.HashMap;
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
    private SharedPreferences sharedPreferences;
    private boolean isProfileUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);
        String customerEmail = sharedPreferences.getString("customerEmail", null);

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

        Button addToCarBtn = findViewById(R.id.add_to_cart_btn);
        Button orderNowBtn = findViewById(R.id.order_now_btn);

        addToCarBtn.setOnClickListener(v -> {
            checkProfileAndPerformAction(() -> addProductToCart());
        });

        orderNowBtn.setOnClickListener(v -> {
            checkProfileAndPerformAction(() -> placeOrder());
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

    private void checkProfileAndPerformAction(Runnable action) {
        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail == null) {
            Toast.makeText(this, "Customer not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("customer")
                .whereEqualTo("email", customerEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String customerId = document.getId();

                        db.collection("customer")
                                .document(customerId)
                                .collection("customer_address")
                                .document("delivery_details")
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        if (task1.getResult().exists()) {
                                            isProfileUpdated = true;
                                            action.run();
                                        } else {
                                            CustomToast.showToast(SingleProductActivity.this, "Please update your profile first!", false);
                                        }
                                    } else {
                                        CustomToast.showToast(SingleProductActivity.this, "Failed to fetch profile details!", false);
                                    }
                                });
                    } else {
                        CustomToast.showToast(SingleProductActivity.this, "Customer data not found!", false);
                    }
                });
    }

    private void addProductToCart() {
        if (product == null) {
            Toast.makeText(this, "Product data is not available!", Toast.LENGTH_SHORT).show();
            return;
        }

        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail == null) {
            Toast.makeText(this, "Customer not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("customer")
                .whereEqualTo("email", customerEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String customerId = document.getId();

                        String productId = product.getProductId();
                        String productName = product.getName();
                        double productPrice = product.getPrice();
                        String productImageUrl = product.getImageUrl();
                        int productQuantity = quantity;

                        Map<String, Object> cartItem = new HashMap<>();
                        cartItem.put("productName", productName);
                        cartItem.put("price", productPrice);
                        cartItem.put("image", productImageUrl);
                        cartItem.put("quantity", productQuantity);

                        db.collection("customer")
                                .document(customerId)
                                .collection("cart")
                                .document(productId)
                                .set(cartItem)
                                .addOnSuccessListener(aVoid -> {
                                    CustomToast.showToast(SingleProductActivity.this, "Product added to cart!", true);
                                })
                                .addOnFailureListener(e -> {
                                    CustomToast.showToast(SingleProductActivity.this, "Failed to add product to cart!", false);
                                });
                    } else {
                        CustomToast.showToast(SingleProductActivity.this, "Customer data not found!", false);
                    }
                });
    }

    private void placeOrder() {
        if (product == null) return;

        Intent intent = new Intent(SingleProductActivity.this, PlaceOrderActivity.class);
        intent.putExtra("productName", product.getName());
        intent.putExtra("productImageUrl", product.getImageUrl());
        intent.putExtra("productPrice", product.getPrice() * quantity);
        intent.putExtra("productQuantity", quantity);
        intent.putExtra("productId", product.getProductId());
        startActivity(intent);
    }

    private void updatePrice() {
        double totalPrice = price * quantity;
        String formattedPrice = String.format("Rs: %.2f", totalPrice);
        productPriceTextView.setText(formattedPrice);
    }

    private void fetchProductData(String productId) {
        db.collection("product").document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        product = new ProductModel();

                        product.setProductId(documentSnapshot.getId());
                        product.setName(documentSnapshot.getString("name"));
                        product.setDescription(documentSnapshot.getString("description"));
                        product.setImageUrl(documentSnapshot.getString("image"));
                        double fetchedPrice = documentSnapshot.getDouble("price");
                        product.setPrice(fetchedPrice);
                        price = fetchedPrice;

                        productNameTextView.setText(product.getName());
                        productDescriptionTextView.setText(product.getDescription());
                        updatePrice();
                        Glide.with(this).load(product.getImageUrl()).into(productImage);

                        if (documentSnapshot.contains("ing_list")) {
                            List<Integer> ingList = (List<Integer>) documentSnapshot.get("ing_list");
                            product.setIngList(ingList);
                        } else {
                        Log.d("Single Product:", "Ing list field not found in document");
                        }

                        if (product.getIngList() != null) {
                            loadIngredientImages(product.getIngList());
                        } else {
                            Log.d("Single Product:", "Ing list is null");
                        }

                        Map<String, Long> nutritionMapLong = (Map<String, Long>) documentSnapshot.get("nutrition");
                        if (nutritionMapLong != null) {
                            nutritionList = new ArrayList<>();
                            for (Map.Entry<String, Long> entry : nutritionMapLong.entrySet()) {
                                String nutrientName = entry.getKey();
                                int nutrientPercentage = entry.getValue().intValue();
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

        Log.d("FirestoreDb", "Ingredient IDs: " + ingredientIds.toString());

        db.collection("ingredients")
                .whereIn("ing_id", ingredientIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("FirestoreDb", "No matching ingredients found in Firestore.");
                        return;
                    }

                    Log.d("FirestoreDb", "Number of ingredients found: " + queryDocumentSnapshots.size());

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String imageUrl = document.getString("ing_image");
                        if (imageUrl != null) {
                            ingredientImageUrls.add(imageUrl);
                        } else {
                            Log.d("FirestoreDb", "No image URL found for ingredient: " + document.getId());
                        }
                    }

                    Log.d("FirestoreDb", "Ingredient Image URLs: " + ingredientImageUrls.toString());

                    ingredientsAdapter = new IngredientsAdapter(SingleProductActivity.this, ingredientImageUrls);
                    ingredientRecyclerView.setAdapter(ingredientsAdapter);
                    ingredientsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDb", "Error fetching ingredients: " + e.getMessage());
                });
    }
}