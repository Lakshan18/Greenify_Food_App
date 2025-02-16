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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private List<OrderHisModel> orderList;
    private SharedPreferences sharedPreferences;
    private com.example.greenify_organic_food_app.ui.order_history.OrderHisAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);

        RecyclerView orderRecyclerView = view.findViewById(R.id.orderRecyclerView);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        adapter = new com.example.greenify_organic_food_app.ui.order_history.OrderHisAdapter(orderList);
        orderRecyclerView.setAdapter(adapter);

        fetchOrders();

        return view;
    }

    private void fetchOrders() {
        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail == null) {
            CustomToast.showToast(getContext(), "Customer email not found!", false);
            return;
        }

        db.collection("order")
                .whereEqualTo("customer_email", customerEmail)
                .orderBy("date_time", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        orderList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productId = document.getString("product_id");
                            int quantity = document.getLong("purchased_qty").intValue();
                            double price = document.getDouble("unit_price");
                            String dateTime = document.getString("date_time");

                            if (productId != null) {
                                db.collection("product")
                                        .document(productId)
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                DocumentSnapshot productDocument = task1.getResult();
                                                if (productDocument.exists()) {
                                                    String productName = productDocument.getString("name");
                                                    String productCategory = productDocument.getString("category");

                                                    // Construct Firebase Storage image URL
                                                    String imageUrl = "images/products/" + productCategory + "_" + productName + ".jpg";
                                                    StorageReference storageRef = storage.getReference().child(imageUrl);

                                                    // Get the download URL for the image
                                                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                        String imageDownloadUrl = uri.toString();

                                                        // Create OrderHisModel and add to the list
                                                        OrderHisModel order = new OrderHisModel(productName, quantity, price, dateTime, imageDownloadUrl);
                                                        orderList.add(order);

                                                        // Notify adapter of data change
                                                        adapter.notifyDataSetChanged();
                                                    }).addOnFailureListener(e -> {
                                                        Log.e("OrderHistoryFragment", "Failed to load image: " + e.getMessage());
                                                        CustomToast.showToast(getContext(), "Failed to load image!", false);
                                                    });
                                                } else {
                                                    Log.e("OrderHistoryFragment", "Product document does not exist.");
                                                    CustomToast.showToast(getContext(), "Product not found!", false);
                                                }
                                            } else {
                                                Log.e("OrderHistoryFragment", "Failed to fetch product details: " + task1.getException().getMessage());
                                                CustomToast.showToast(getContext(), "Failed to fetch product details!", false);
                                            }
                                        });
                            } else {
                                Log.e("OrderHistoryFragment", "Product ID is null.");
                                CustomToast.showToast(getContext(), "Invalid product ID!", false);
                            }
                        }
                    } else {
                        Log.e("OrderHistoryFragment", "Failed to fetch orders: " + task.getException().getMessage());
                        CustomToast.showToast(getContext(), "Failed to fetch orders!", false);
                    }
                });
    }
}