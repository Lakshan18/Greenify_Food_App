package com.example.greenify_organic_food_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenify_organic_food_app.model.ProductAdapter;
import com.example.greenify_organic_food_app.model.ProductModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchResultActivity extends AppCompatActivity {

    private RecyclerView searchResultsRecyclerView;
    private ProductAdapter searchResultsAdapter;
    private List<ProductModel> searchResults;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        db = FirebaseFirestore.getInstance();

        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        searchResults = new ArrayList<>();
        searchResultsAdapter = new ProductAdapter(searchResults, this);
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        // Get the search query from the intent
        String query = getIntent().getStringExtra("searchQuery");
        if (query != null && !query.isEmpty()) {
            performSearch(query);
        }
    }

    private void performSearch(String query) {
        // Use a Set to avoid duplicate results
        Set<ProductModel> uniqueResults = new HashSet<>();

        // Query for name
        db.collection("product")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ProductModel product = createProductModelFromDocument(document);
                            uniqueResults.add(product);
                        }
                        // After name query, query for category
                        queryCategory(query, uniqueResults);
                    }
                });
    }

    private void queryCategory(String query, Set<ProductModel> uniqueResults) {
        db.collection("product")
                .whereGreaterThanOrEqualTo("category", query)
                .whereLessThanOrEqualTo("category", query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ProductModel product = createProductModelFromDocument(document);
                            uniqueResults.add(product);
                        }
                        // After category query, query for description
                        queryDescription(query, uniqueResults);
                    }
                });
    }

    private void queryDescription(String query, Set<ProductModel> uniqueResults) {
        db.collection("product")
                .whereGreaterThanOrEqualTo("description", query)
                .whereLessThanOrEqualTo("description", query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ProductModel product = createProductModelFromDocument(document);
                            uniqueResults.add(product);
                        }
                        // After all queries, update the RecyclerView
                        updateRecyclerView(uniqueResults);
                    }
                });
    }

    private ProductModel createProductModelFromDocument(QueryDocumentSnapshot document) {
        String productId = document.getId();
        String name = document.getString("name");
        String imageUrl = document.getString("image");
        double price = document.getDouble("price");
        String rating = document.getString("rating");
        String category = document.getString("category");
        String description = document.getString("description");

        return new ProductModel(productId, imageUrl, name, price, rating, category, description);
    }

    private void updateRecyclerView(Set<ProductModel> uniqueResults) {
        searchResults.clear();
        searchResults.addAll(uniqueResults);
        searchResultsAdapter.notifyDataSetChanged();
    }
}