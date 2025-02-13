package com.example.greenify_organic_food_app.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.R;

import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {

    private Context context;
    private List<String> ingredientImageUrls; // Change to List<String> for image URLs

    public IngredientsAdapter(Context context, List<String> ingredientImageUrls){
        this.context = context;
        this.ingredientImageUrls = ingredientImageUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = ingredientImageUrls.get(position);

        // Load image from URL using Glide
        Glide.with(context)
                .load(imageUrl)
                .into(holder.ingredientImage);
    }

    @Override
    public int getItemCount() {
        return ingredientImageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ingredientImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientImage = itemView.findViewById(R.id.ingredientImage);
        }
    }
}
