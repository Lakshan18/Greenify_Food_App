package com.example.greenify_organic_food_app.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenify_organic_food_app.R;

import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {

    private Context context;
    private List<Integer> ingredientsImages;

    public IngredientsAdapter(Context context,List<Integer> ingredientsImages){
        this.context = context;
        this.ingredientsImages = ingredientsImages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(com.example.greenify_organic_food_app.R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.ingredientImage.setImageResource(ingredientsImages.get(position));
    }

    @Override
    public int getItemCount() {
        return ingredientsImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ingredientImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientImage = itemView.findViewById(R.id.ingredientImage);
        }
    }

}
