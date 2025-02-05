package com.example.greenify_organic_food_app.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.R;
import com.example.greenify_organic_food_app.model.CategoryAdapter;
import com.example.greenify_organic_food_app.model.CategoryModel;
import com.example.greenify_organic_food_app.model.ProductAdapter;
import com.example.greenify_organic_food_app.model.ProductModel;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewPager = view.findViewById(R.id.viewPager);

        List<String> imageList = getImageList();
        ImageSliderAdapter adapter = new ImageSliderAdapter(imageList);
        viewPager.setAdapter(adapter);

        setupPageTransformer();

        startAutoSlide(imageList.size());

        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        categoryList = new ArrayList<>();
        categoryList.add(new CategoryModel(R.drawable.org_food1, "Organic Food"));
        categoryList.add(new CategoryModel(R.drawable.tasty_vegetable_salad, "Vegetable Salad"));
        categoryList.add(new CategoryModel(R.drawable.appetizers1, "Appetizers"));
        categoryList.add(new CategoryModel(R.drawable.org_breakfast, "Breakfast"));
        categoryList.add(new CategoryModel(R.drawable.gluten_free, "Gluten Free"));
        categoryList.add(new CategoryModel(R.drawable.entrees, "Entrees"));
        categoryList.add(new CategoryModel(R.drawable.kid_recipies, "Kid Recipes"));
        categoryList.add(new CategoryModel(R.drawable.sides, "Sides"));
        categoryList.add(new CategoryModel(R.drawable.food_drinks, "Drinks"));

        categoryAdapter = new CategoryAdapter(getContext(), categoryList);
        categoryRecyclerView.setAdapter(categoryAdapter);

        productRecyclerView = view.findViewById(R.id.productRecyclerView);
        productRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns

        // Initialize Product List
        productList = new ArrayList<>();
        productList.add(new ProductModel(R.drawable.hashbrown_waffles, "Hashbrown Waffles", 1500, "4.1"));
        productList.add(new ProductModel(R.drawable.pumpkin_pancackes, "Pumpkin Pancakes", 1200, "3.5"));
        productList.add(new ProductModel(R.drawable.squash_muffins, "Squash Muffins", 750, "4.0"));
        productList.add(new ProductModel(R.drawable.white_bean_salad, "White Bean Salad", 880, "4.2"));

        // Set Adapter
        productAdapter = new ProductAdapter(productList, getContext());
        productRecyclerView.setAdapter(productAdapter);

        return view;
    }

    private List<String> getImageList() {
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("https://static.desygner.com/wp-content/uploads/sites/13/2022/05/04141642/Free-Stock-Photos-01-2048x1366.jpg");
        imageUrls.add("https://static.desygner.com/wp-content/uploads/sites/13/2022/05/04150103/Free-Stock-Photos-03-2048x1366.jpg");
        imageUrls.add("https://static.desygner.com/wp-content/uploads/sites/13/2022/05/04154511/Free-Stock-Photos-06-2048x1366.jpg");
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
