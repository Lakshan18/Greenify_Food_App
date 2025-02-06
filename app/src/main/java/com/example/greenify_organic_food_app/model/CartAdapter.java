package com.example.greenify_organic_food_app.model;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        CartModel item = cartItems.get(position);
        holder.itemName.setText(item.getName());
        holder.itemPrice.setText("Rs: " + item.getPrice());
        holder.itemQuantity.setText(String.valueOf(item.getQuantity()));
        holder.itemImage.setImageResource(item.getImageResource());

        holder.btnIncrease.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position);
            cartUpdateListener.onCartUpdated();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(position);
                cartUpdateListener.onCartUpdated();
            }
        });

        // Handle item removal safely
        holder.itemView.setOnClickListener(v -> {
            if (cartItems.size() > position) {
                cartItems.remove(position);
                notifyItemRemoved(position); // Notify the adapter that an item was removed
                notifyItemRangeChanged(position, cartItems.size()); // Update the remaining items
                cartUpdateListener.onCartUpdated();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
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
