package com.example.greenify_organic_food_app.ui.order_history;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.greenify_organic_food_app.R;
import com.example.greenify_organic_food_app.model.OrderHisModel;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        // Initialize RecyclerView and set its layout manager
        RecyclerView orderRecyclerView = view.findViewById(R.id.orderRecyclerView);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create a list of order history models
        List<OrderHisModel> orderList = new ArrayList<>();
        orderList.add(new OrderHisModel("Special Vegetable Salad", 1, 350.00, "2025.01.20", R.drawable.tasty_vegetable_salad));
        orderList.add(new OrderHisModel("Special Vegetable Salad", 1, 350.00, "2025.01.22", R.drawable.tasty_vegetable_salad));

        // Set up the adapter and attach it to the RecyclerView
        com.example.greenify_organic_food_app.ui.order_history.OrderHisAdapter adapter = new com.example.greenify_organic_food_app.ui.order_history.OrderHisAdapter(orderList);
        orderRecyclerView.setAdapter(adapter);

        return view;
    }
}
