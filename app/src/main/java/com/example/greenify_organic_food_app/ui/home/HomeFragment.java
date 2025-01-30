package com.example.greenify_organic_food_app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize ViewPager2
        viewPager = view.findViewById(R.id.viewPager);

        // Set the adapter for ViewPager
        viewPager.setAdapter(new ImageSliderAdapter(getImageList()));

        return view;
    }

    // This method returns a list of image URLs (you can replace them with your actual images)
    private List<String> getImageList() {
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("https://www.example.com/image1.jpg");
        imageUrls.add("https://www.example.com/image2.jpg");
        imageUrls.add("https://www.example.com/image3.jpg");
        return imageUrls;
    }

    // Adapter class for ImageSlider
    public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageSliderViewHolder> {

        private List<String> imageUrls;

        public ImageSliderAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ImageSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the layout for each image in the ViewPager
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            return new ImageSliderViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageSliderViewHolder holder, int position) {
            // Use Glide or any other image loading library to load images into the ImageView
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
                imageView = itemView.findViewById(android.R.id.icon); // Fixed issue, use correct ID for ImageView
            }
        }
    }
}
