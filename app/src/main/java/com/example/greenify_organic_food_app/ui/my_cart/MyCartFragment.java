package com.example.greenify_organic_food_app.ui.my_cart;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenify_organic_food_app.R;
import com.example.greenify_organic_food_app.model.CartAdapter;
import com.example.greenify_organic_food_app.model.CartModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyCartFragment extends Fragment {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<CartModel> cartItems;
    private TextView txtSubtotal, txtDiscount, txtTotal;
    private Button btnCheckout;
    private TextView emptyCartMessage;
    private View cartSummaryLayout;
    private static final int MAX_QUANTITY = 5;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_cart, container, false);

        db = FirebaseFirestore.getInstance();

        cartRecyclerView = view.findViewById(R.id.cartRecyclerView);
        txtSubtotal = view.findViewById(R.id.subTotal);
        txtDiscount = view.findViewById(R.id.discount);
        txtTotal = view.findViewById(R.id.totalAmount);
        btnCheckout = view.findViewById(R.id.checkoutButton);
        emptyCartMessage = view.findViewById(R.id.emptyCartMessage);
        cartSummaryLayout = view.findViewById(R.id.cartSummaryLayout);

        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(getContext(), cartItems, this::updateCartSummary);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartRecyclerView.setAdapter(cartAdapter);

        fetchCartItems();

        btnCheckout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Proceeding to checkout...", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void fetchCartItems() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);
        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail == null) {
            Toast.makeText(getContext(), "Customer not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("customer")
                .whereEqualTo("email", customerEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String customerId = document.getId(); // Get the customer ID

                        db.collection("customer")
                                .document(customerId)
                                .collection("cart")
                                .get()
                                .addOnCompleteListener(cartTask -> {
                                    if (cartTask.isSuccessful()) {
                                        cartItems.clear();
                                        for (DocumentSnapshot cartDocument : cartTask.getResult()) {
                                            String productName = cartDocument.getString("productName");
                                            Double price = cartDocument.getDouble("price");
                                            String image = cartDocument.getString("image");
                                            Long quantityLong = cartDocument.getLong("quantity");

                                            if (productName == null || price == null || image == null || quantityLong == null) {
                                                Log.e("MyCartFragment", "Invalid cart item data: " + cartDocument.getId());
                                                continue;
                                            }

                                            int quantity = quantityLong.intValue();

                                            CartModel cartItem = new CartModel(productName, quantity, price, image);
                                            cartItems.add(cartItem);
                                        }

                                        cartAdapter.notifyDataSetChanged();
                                        updateCartSummary();
                                    } else {
                                        Log.e("MyCartFragment", "Failed to fetch cart items: " + cartTask.getException().getMessage());
                                        Toast.makeText(getContext(), "Failed to fetch cart items.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.e("MyCartFragment", "Customer data not found for email: " + customerEmail);
                        Toast.makeText(getContext(), "Customer data not found!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCartSummary() {
        double subtotal = 0;
        for (CartModel item : cartItems) {
            if (item.getQuantity() > MAX_QUANTITY) {
                item.setQuantity(MAX_QUANTITY);
                Toast.makeText(getContext(), "Maximum quantity reached (5 items per product).", Toast.LENGTH_SHORT).show();
            }
            subtotal += item.getPrice() * item.getQuantity();
        }

        double discount = subtotal * 0.05;
        double total = subtotal - discount;

        txtSubtotal.setText(String.format("Rs: %.2f", subtotal));
        txtDiscount.setText(String.format("Rs: %.2f", discount));
        txtTotal.setText(String.format("Rs: %.2f", total));

        if (cartItems.isEmpty()) {
            emptyCartMessage.setVisibility(View.VISIBLE);
            cartSummaryLayout.setVisibility(View.GONE);
        } else {
            emptyCartMessage.setVisibility(View.GONE);
            cartSummaryLayout.setVisibility(View.VISIBLE);
        }
    }
}