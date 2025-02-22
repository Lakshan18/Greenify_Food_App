package com.example.greenify_organic_food_app.ui.order_history;

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
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
            return view;
        }

        RecyclerView orderRecyclerView = view.findViewById(R.id.orderRecyclerView);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        ordHisAdapter = new OrderHisAdapter(orderList,getContext());
        orderRecyclerView.setAdapter(ordHisAdapter);

        loadOrders(customerEmail);
        return view;
    }

    private void loadOrders(String customerEmail) {
        db.collection("order")
                .whereEqualTo("customer_email", customerEmail)
                .orderBy("date_time", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        orderList.clear();
                        List<OrderHisModel> tempOrderList = new ArrayList<>();

                        for (DocumentSnapshot orderDocument : task.getResult()) {
                            com.google.firebase.Timestamp timestamp = orderDocument.getTimestamp("date_time");
                            String dateTime = formatTimestamp(timestamp);
                            String orderId = orderDocument.getId();
                            double totalPrice = orderDocument.getDouble("total_price");
                            String orderStatus = orderDocument.getString("order_status");

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

                                                OrderHisModel orderItem = new OrderHisModel(
                                                        orderId,
                                                        productName,
                                                        quantity,
                                                        unitPrice * quantity,
                                                        orderStatus,
                                                        dateTime,
                                                        imageUrl);
                                                tempOrderList.add(orderItem);
                                            }

                                            tempOrderList.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));

                                            orderList.clear();
                                            orderList.addAll(tempOrderList);
                                            ordHisAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    } else {
                        Log.d("OrderHistory", "Error getting orders: ", task.getException());
                    }
                });
    }

    private String formatTimestamp(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) {
            return "No date available";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = timestamp.toDate();
        return sdf.format(date);
    }
}