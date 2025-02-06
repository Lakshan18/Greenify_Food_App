package com.example.greenify_organic_food_app.ui.my_cart;

import android.os.Bundle;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_cart, container, false);

        cartRecyclerView = view.findViewById(R.id.cartRecyclerView);
        txtSubtotal = view.findViewById(R.id.subTotal);
        txtDiscount = view.findViewById(R.id.discount);
        txtTotal = view.findViewById(R.id.totalAmount);
        btnCheckout = view.findViewById(R.id.checkoutButton);
        emptyCartMessage = view.findViewById(R.id.emptyCartMessage);
        cartSummaryLayout = view.findViewById(R.id.cartSummaryLayout);  // Initialize cartSummaryLayout

        cartItems = new ArrayList<>();
        cartItems.add(new CartModel("Special Vegetable Salad", 1, 350.00, R.drawable.pumpkin_pancackes));
        cartItems.add(new CartModel("Special Vegetable Salad", 1, 350.00, R.drawable.pumpkin_pancackes));

        cartAdapter = new CartAdapter(getContext(), cartItems, this::updateCartSummary);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartRecyclerView.setAdapter(cartAdapter);

        updateCartSummary();

        btnCheckout.setOnClickListener(v -> {
            // Implement checkout functionality here...
            Toast.makeText(getContext(), "Proceeding to checkout...", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void updateCartSummary() {
        double subtotal = 0;
        for (CartModel item : cartItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        double discount = subtotal * 0.05; // 5% Discount
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
