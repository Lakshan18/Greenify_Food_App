package com.example.greenify_organic_food_app.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.R;
import com.example.greenify_organic_food_app.SearchResultActivity;
import com.example.greenify_organic_food_app.model.CategoryAdapter;
import com.example.greenify_organic_food_app.model.CategoryModel;
import com.example.greenify_organic_food_app.model.ProductAdapter;
import com.example.greenify_organic_food_app.model.ProductModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private RecyclerView categoryRecyclerView;
    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<ProductModel> productList;
    private CategoryAdapter categoryAdapter;
    private List<CategoryModel> categoryList;
    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable;

    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();

        viewPager = view.findViewById(R.id.viewPager);

        List<String> imageList = getImageList();
        ImageSliderAdapter adapter = new ImageSliderAdapter(imageList);
        viewPager.setAdapter(adapter);

        setupPageTransformer();

        startAutoSlide(imageList.size());

        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        categoryList = new ArrayList<>();

        fetchCategoriesFromFirebase();

        categoryAdapter = new CategoryAdapter(getContext(), categoryList);
        categoryRecyclerView.setAdapter(categoryAdapter);

        productRecyclerView = view.findViewById(R.id.productRecyclerView);
        productRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, getContext());
        productRecyclerView.setAdapter(productAdapter);
        fetchProductsFromFirebase();

        EditText homeSearchField = view.findViewById(R.id.home_searchField1);
        homeSearchField.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                String query = homeSearchField.getText().toString().trim();
                if (!query.isEmpty()) {
                    navigateToSearchFragment(query);
                }
                return true;
            }
            return false;
        });

        return view;
    }

    private void navigateToSearchFragment(String query) {
        SearchFragment searchFragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString("searchQuery", query);
        searchFragment.setArguments(args);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment_content_home, searchFragment)
                .addToBackStack(null)
                .commit();
    }

    private void fetchProductsFromFirebase() {
        db.collection("product")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String productId = document.getId();
                                String name = document.getString("name");
                                String imageUrl = document.getString("image");
                                double price = document.getDouble("price");
                                String rating = document.getString("rating");
                                String category = document.getString("category");
                                String description = document.getString("description");

                                ProductModel product = new ProductModel(productId,imageUrl, name, price, rating, category, description);
                                productList.add(product);
                            }
                            productAdapter.notifyDataSetChanged();
                        } else {
                            task.getException().printStackTrace();
                        }
                    }
                });
    }


    private void fetchCategoriesFromFirebase(){

        db.collection("category")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        QuerySnapshot querySnapshot = task.getResult();
                        if(querySnapshot != null){
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String categoryName = document.getString("name");
                                assert categoryName != null;
                                CategoryModel category = new CategoryModel(getCategoryImageResource(categoryName), categoryName);
                                categoryList.add(category);
                            }
                            categoryAdapter.notifyDataSetChanged();
                        }else{
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    private int getCategoryImageResource(String categoryName) {
        switch (categoryName) {
            case "Organic Food":
                return R.drawable.org_food1;
            case "Vegetable Salad":
                return R.drawable.tasty_vegetable_salad;
            case "Appetizers":
                return R.drawable.appetizers1;
            case "Breakfast":
                return R.drawable.org_breakfast;
            case "Gluten Free":
                return R.drawable.gluten_free;
            case "Entrees":
                return R.drawable.entrees;
            case "Kid Recipes":
                return R.drawable.kid_recipies;
            case "Sides":
                return R.drawable.sides;
            case "Drinks":
                return R.drawable.food_drinks;
            case "Desserts":
                return R.drawable.org_desserts;
            default:
                return R.drawable.default_category;
        }
    }

    private List<String> getImageList() {
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("https://miro.medium.com/v2/resize:fit:740/1*Gnf8_1da3bh6fI7Re1shpQ.jpeg");
        imageUrls.add("https://restaurantindia.s3.ap-south-1.amazonaws.com/s3fs-public/news15171.jpg");
        imageUrls.add("https://tinybeans.com/wp-content/uploads/2022/04/creative-kids-lunches.jpg");
        imageUrls.add("https://www.buffalomarket.com/hubfs/Canva%20images/Natural%20Food.png");
        imageUrls.add("https://t3.ftcdn.net/jpg/03/32/35/08/360_F_332350888_TGngvcPHa7wXrY1MFFAofZLTyZcIun4D.jpg");
        return imageUrls;
    }

    private void startAutoSlide(int totalImages) {
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPager.getCurrentItem();
                if (currentItem < totalImages - 1) {
                    viewPager.setCurrentItem(currentItem + 1);
                } else {
                    viewPager.setCurrentItem(0, false);
                }
                sliderHandler.postDelayed(this, 5000);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 5000);
    }

    private void setupPageTransformer() {
        viewPager.setPageTransformer((page, position) -> {
            float absPosition = Math.abs(position);

            page.setScaleY(1 - (0.35f * absPosition));

            page.setAlpha(2 - absPosition);
        });
    }

    public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageSliderViewHolder> {

        private List<String> imageUrls;

        public ImageSliderAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ImageSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImageSliderViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageSliderViewHolder holder, int position) {
            Glide.with(holder.imageView.getContext())
                    .load(imageUrls.get(position))
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        public class ImageSliderViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ImageSliderViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }
}
