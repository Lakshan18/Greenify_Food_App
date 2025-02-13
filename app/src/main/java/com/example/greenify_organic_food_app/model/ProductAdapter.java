package com.example.greenify_organic_food_app.model;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.R;
import com.example.greenify_organic_food_app.SingleProductActivity;

import java.text.DecimalFormat;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<ProductModel> productList;
    private Context context;

    public ProductAdapter(List<ProductModel> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);

        DecimalFormat decimalFormat = new DecimalFormat("Rs: #,##0.00");
        String formattedPrice = decimalFormat.format(product.getPrice());

        Glide.with(context).load(product.getImageUrl()).into(holder.productImage);
        holder.productTitle.setText(product.getName());
        holder.productPrice.setText(formattedPrice); // Set formatted price
        holder.productRatingText.setText(product.getRating() + "/5");

        holder.itemView.setOnClickListener(v -> {
            // Pass the ProductModel to SingleProductActivity
            Intent intent = new Intent(v.getContext(), SingleProductActivity.class);
            intent.putExtra("p_id",product.getProductId());
            intent.putExtra("product", product);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle, productPrice, productRatingText;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productTitle = itemView.findViewById(R.id.productTitle);
            productPrice = itemView.findViewById(R.id.productPrice);
            productRatingText = itemView.findViewById(R.id.productRatingText);
        }
    }
}
