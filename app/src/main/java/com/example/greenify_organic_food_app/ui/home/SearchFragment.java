package com.example.greenify_organic_food_app.ui.home;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenify_organic_food_app.CustomToast;
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

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        searchRecyclerView.setLayoutManager(layoutManager);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing); // Define this in dimens.xml
        searchRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        searchResults = new ArrayList<>();
        searchAdapter = new ProductAdapter(searchResults, getContext());
        searchRecyclerView.setAdapter(searchAdapter);

        Bundle args = getArguments();
        if (args != null) {
            String query = args.getString("searchQuery");
            performSearch(query);
        }

        return view;
    }

    private void performSearch(String query) {
        db.collection("product")
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

                            if (name != null && name.toLowerCase().contains(query.toLowerCase())) {
                                ProductModel product = new ProductModel(productId, imageUrl, name, price, rating, category, description);
                                searchResults.add(product);
                            }
                        }

                        searchAdapter.updateProductList(searchResults);

                        if (searchResults.isEmpty()) {
                            showNoProductsFound();
                        } else {
                            hideNoProductsFound();
                        }
                    } else {
                        CustomToast.showToast(requireContext(),"Error fetching products",false);
                    }
                });
    }

    private void showNoProductsFound() {
        View view = getView();
        if (view != null) {
            TextView noProductsFoundTextView = view.findViewById(R.id.noProductsFoundTextView);
            noProductsFoundTextView.setVisibility(View.VISIBLE);
        }
    }

    private void hideNoProductsFound() {
        View view = getView();
        if (view != null) {
            TextView noProductsFoundTextView = view.findViewById(R.id.noProductsFoundTextView);
            noProductsFoundTextView.setVisibility(View.GONE);
        }
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }
}