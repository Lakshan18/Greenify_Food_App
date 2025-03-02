package com.example.greenify_organic_food_app.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartModel> cartItems;
    private Context context;
    private OnCartUpdateListener cartUpdateListener;
    private FirebaseFirestore db;

    public interface OnCartUpdateListener {
        void onCartUpdated();
    }

    public CartAdapter(Context context, List<CartModel> cartItems, OnCartUpdateListener cartUpdateListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.cartUpdateListener = cartUpdateListener;
        this.db = FirebaseFirestore.getInstance();
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
        holder.itemPrice.setText(String.format("Rs: %.2f", cartItem.getPrice()));
        holder.itemQuantity.setText(String.valueOf(cartItem.getQuantity()));

        Glide.with(context).load(cartItem.getImage()).into(holder.itemImage);

        holder.checkBoxSelect.setChecked(cartItem.isSelected());
        holder.checkBoxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartItem.setSelected(isChecked);
            cartUpdateListener.onCartUpdated();
        });

        holder.btnIncrease.setOnClickListener(v -> {
            if (cartItem.isSelected()) {
                int currentQuantity = cartItem.getQuantity();
                cartItem.setQuantity(currentQuantity + 1);
                notifyItemChanged(position);
                cartUpdateListener.onCartUpdated();
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (cartItem.isSelected()) {
                int currentQuantity = cartItem.getQuantity();
                if (currentQuantity > 1) {
                    cartItem.setQuantity(currentQuantity - 1);
                    notifyItemChanged(position);
                    cartUpdateListener.onCartUpdated();
                }
            }
        });

        holder.btnRemove.setOnClickListener(v -> removeCartItemFromFirestore(cartItem, position));
    }

    private void removeCartItemFromFirestore(CartModel cartItem, int position) {
        String customerEmail = context.getSharedPreferences("CustomerSession", Context.MODE_PRIVATE)
                .getString("customerEmail", null);

        if (customerEmail == null) {
            Toast.makeText(context, "Customer not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("customer")
                .whereEqualTo("email", customerEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String customerId = task.getResult().getDocuments().get(0).getId();

                        db.collection("customer")
                                .document(customerId)
                                .collection("cart")
                                .whereEqualTo("productName", cartItem.getProductName())
                                .get()
                                .addOnCompleteListener(cartTask -> {
                                    if (cartTask.isSuccessful() && !cartTask.getResult().isEmpty()) {
                                        String cartItemId = cartTask.getResult().getDocuments().get(0).getId();

                                        db.collection("customer")
                                                .document(customerId)
                                                .collection("cart")
                                                .document(cartItemId)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    cartItems.remove(position);
                                                    notifyItemRemoved(position);
                                                    notifyItemRangeChanged(position, cartItems.size());
                                                    cartUpdateListener.onCartUpdated();
                                                    Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Failed to remove item from cart", Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                });
                    }
                });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxSelect;
        ImageView itemImage, btnIncrease, btnDecrease, btnRemove;
        TextView itemName, itemPrice, itemQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxSelect = itemView.findViewById(R.id.checkBoxSelect);
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