package com.example.greenify_organic_food_app.ui.order_history;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.greenify_organic_food_app.CustomToast;
import com.example.greenify_organic_food_app.R;
import com.example.greenify_organic_food_app.model.OrderHisModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;
    private List<OrderHisModel> orderList;
    private OrderHisAdapter ordHisAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);
        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail == null) {
            CustomToast.showToast(getContext(), "Please log in to view orders.", false);
            return view; // Exit early
        }

        RecyclerView orderRecyclerView = view.findViewById(R.id.orderRecyclerView);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        ordHisAdapter = new OrderHisAdapter(orderList);
        orderRecyclerView.setAdapter(ordHisAdapter);

        loadOrders(customerEmail);
        return view;
    }

    private void loadOrders(String customerEmail) {
        db.collection("order")
                .whereEqualTo("customer_email", customerEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        orderList.clear();
                        for (DocumentSnapshot orderDocument : task.getResult()) {
                            // Get order-level fields
                            String dateTime = orderDocument.getString("date_time");
                            double totalPrice = orderDocument.getDouble("total_price");
                            String orderStatus = orderDocument.getString("order_status");

                            // Fetch items subcollection for this order
                            orderDocument.getReference().collection("items")
                                    .get()
                                    .addOnCompleteListener(itemsTask -> {
                                        if (itemsTask.isSuccessful()) {
                                            for (DocumentSnapshot itemDocument : itemsTask.getResult()) {
                                                String productName = itemDocument.getString("product_name");
                                                Long quantityLong = itemDocument.getLong("quantity");
                                                int quantity = quantityLong != null ? quantityLong.intValue() : 0;
                                                String imageUrl = itemDocument.getString("image_url");
                                                double unitPrice = itemDocument.getDouble("unit_price");

                                                // Create OrderHisModel with item data + order metadata
                                                OrderHisModel orderItem = new OrderHisModel(
                                                        productName,
                                                        quantity,
                                                        unitPrice * quantity,
                                                        dateTime,
                                                        imageUrl);
                                                orderList.add(orderItem);
                                            }
                                            ordHisAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    } else {
                        Log.d("OrderHistory", "Error getting orders: ", task.getException());
                    }
                });
    }
}