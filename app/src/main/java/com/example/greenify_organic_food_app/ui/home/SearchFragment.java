package com.example.greenify_organic_food_app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenify_organic_food_app.R;
import com.example.greenify_organic_food_app.model.ProductAdapter;
import com.example.greenify_organic_food_app.model.ProductModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView searchRecyclerView;
    private ProductAdapter searchAdapter;
    private List<ProductModel> searchResults;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        db = FirebaseFirestore.getInstance();

        searchRecyclerView = view.findViewById(R.id.searchRecyclerView);
        searchRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns

        searchResults = new ArrayList<>();
        searchAdapter = new ProductAdapter(searchResults, getContext());
        searchRecyclerView.setAdapter(searchAdapter);

        // Get search query from arguments
        Bundle args = getArguments();
        if (args != null) {
            String query = args.getString("searchQuery");
            performSearch(query);
        }

        return view;
    }

    private void performSearch(String query) {
        db.collection("product")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ProductModel> searchResults = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productId = document.getId();
                            String name = document.getString("name");
                            String imageUrl = document.getString("image");
                            double price = document.getDouble("price");
                            String rating = document.getString("rating");
                            String category = document.getString("category");
                            String description = document.getString("description");

                            ProductModel product = new ProductModel(productId, imageUrl, name, price, rating, category, description);
                            searchResults.add(product);
                        }
                        // Update the adapter with search results
                        searchAdapter.updateProductList(searchResults);
                    }
                });
    }
}