package com.example.greenify_organic_food_app.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenify_organic_food_app.R;
import com.example.greenify_organic_food_app.model.NutritionItemModel;

import java.util.List;

public class NutritionItemAdapter extends RecyclerView.Adapter<NutritionItemAdapter.ViewHolder> {

    private Context context;
    private List<NutritionItemModel> nutritionList;

    public NutritionItemAdapter(Context context, List<NutritionItemModel> nutritionList) {
        this.context = context;
        this.nutritionList = nutritionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nutrition, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NutritionItemModel item = nutritionList.get(position);
        holder.nutrientName.setText(item.getName());
        holder.nutrientPercentage.setText(item.getPercentage() + "%");

        int maxWidth = 600;
        int progressWidth = (maxWidth * item.getPercentage()) / 100;

        ViewGroup.LayoutParams params = holder.progressBar.getLayoutParams();
        params.width = progressWidth;
        holder.progressBar.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return nutritionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nutrientName, nutrientPercentage;
        View progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nutrientName = itemView.findViewById(R.id.nutrientName);
            nutrientPercentage = itemView.findViewById(R.id.nutrientPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
