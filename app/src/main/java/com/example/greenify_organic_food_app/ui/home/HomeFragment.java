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
        imageUrls.add("https://static.desygner.com/wp-content/uploads/sites/13/2022/05/04141642/Free-Stock-Photos-01-2048x1366.jpg");
        imageUrls.add("https://static.desygner.com/wp-content/uploads/sites/13/2022/05/04150103/Free-Stock-Photos-03-2048x1366.jpg");
        imageUrls.add("https://static.desygner.com/wp-content/uploads/sites/13/2022/05/04154511/Free-Stock-Photos-06-2048x1366.jpg");
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
            // Create a new ImageView for each item
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Optional: for a better display
            return new ImageSliderViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageSliderViewHolder holder, int position) {
            // Load the image URL using Glide into the ImageView
            Glide.with(holder.imageView.getContext())
                    .load(imageUrls.get(position))
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        // ViewHolder class for ImageSlider
        public class ImageSliderViewHolder extends RecyclerView.ViewHolder {

            ImageView imageView;

            public ImageSliderViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;  // No need for findViewById
            }
        }
    }
}

