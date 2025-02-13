package com.example.greenify_organic_food_app.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.R;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartModel> cartItems;
    private Context context;
    private OnCartUpdateListener cartUpdateListener;

    public interface OnCartUpdateListener {
        void onCartUpdated();
    }

    public CartAdapter(Context context, List<CartModel> cartItems, OnCartUpdateListener cartUpdateListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.cartUpdateListener = cartUpdateListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartModel cartItem = cartItems.get(position);

        holder.itemName.setText(cartItem.getProductName());
        String formattedPrice = String.format("Rs: %.2f", cartItem.getPrice());
        holder.itemPrice.setText(formattedPrice);
        holder.itemQuantity.setText(String.valueOf(cartItem.getQuantity()));

        Glide.with(context)
                .load(cartItem.getImage())
                .into(holder.itemImage);

        holder.btnIncrease.setOnClickListener(v -> {
            int currentQuantity = cartItem.getQuantity();
            cartItem.setQuantity(currentQuantity + 1);
            notifyItemChanged(position);
            cartUpdateListener.onCartUpdated();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int currentQuantity = cartItem.getQuantity();
            if (currentQuantity > 1) {
                cartItem.setQuantity(currentQuantity - 1);
                notifyItemChanged(position);
                cartUpdateListener.onCartUpdated();
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (position < cartItems.size()) {
                cartItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartItems.size());
                cartUpdateListener.onCartUpdated();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage, btnIncrease, btnDecrease, btnRemove;
        TextView itemName, itemPrice, itemQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.cartItemImage);
            itemName = itemView.findViewById(R.id.cartItemName);
            itemPrice = itemView.findViewById(R.id.cartItemPrice);
            itemQuantity = itemView.findViewById(R.id.cart_select_qty);
            btnIncrease = itemView.findViewById(R.id.cart_btn_increase);
            btnDecrease = itemView.findViewById(R.id.cart_btn_decrease);
            btnRemove = itemView.findViewById(R.id.remove_cart_item);
        }
    }
}