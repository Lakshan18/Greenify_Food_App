package com.example.greenify_organic_food_app;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.greenify_organic_food_app.model.CustomerFeedbackAdapter;
import com.example.greenify_organic_food_app.model.CustomerFeedbackModel;
import com.example.greenify_organic_food_app.model.OngoingDeliveriesAdapter;
import com.example.greenify_organic_food_app.model.OngoingDeliveryModel;
import com.example.greenify_organic_food_app.model.RecentDeliveriesAdapter;
import com.example.greenify_organic_food_app.model.RecentDeliveryModel;

import java.util.ArrayList;
import java.util.List;

public class Deliverer_HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAssignedDeliveries, recyclerViewRecentDeliveries, recyclerViewCustomerFeedbacks;
    private OngoingDeliveriesAdapter ongoingDeliveriesAdapter;
    private RecentDeliveriesAdapter recentDeliveriesAdapter;
    private CustomerFeedbackAdapter customerFeedbackAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deliverer_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerViewAssignedDeliveries = findViewById(R.id.recycler_view_assigned_deliveries);
        recyclerViewRecentDeliveries = findViewById(R.id.recycler_view_recent_deliveries);
        recyclerViewCustomerFeedbacks = findViewById(R.id.recycler_view_customer_feedback);

        recyclerViewAssignedDeliveries.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewRecentDeliveries.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewCustomerFeedbacks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        loadOngoingDeliveries();
        loadRecentDeliveries();
        loadCustomerFeedbacks();
    }

    private void loadOngoingDeliveries() {
        List<OngoingDeliveryModel> deliveries = new ArrayList<>();
        deliveries.add(new OngoingDeliveryModel("John Doe", "123 Green St, Colombo", "In Progress"));
        deliveries.add(new OngoingDeliveryModel("Alice Smith", "456 Eco Rd, Kandy", "Out for Delivery"));
        deliveries.add(new OngoingDeliveryModel("Michael Brown", "789 Nature Ave, Galle", "In Progress"));

        ongoingDeliveriesAdapter = new OngoingDeliveriesAdapter(deliveries);
        recyclerViewAssignedDeliveries.setAdapter(ongoingDeliveriesAdapter);
    }

    private void loadRecentDeliveries() {
        List<RecentDeliveryModel> recentDeliveries = new ArrayList<>();
        recentDeliveries.add(new RecentDeliveryModel("Sarah Wilson", "2024-02-06", "Delivered"));
        recentDeliveries.add(new RecentDeliveryModel("David Johnson", "2024-02-05", "Delivered"));
        recentDeliveries.add(new RecentDeliveryModel("Emma Taylor", "2024-02-04", "Delivered"));

        recentDeliveriesAdapter = new RecentDeliveriesAdapter(recentDeliveries);
        recyclerViewRecentDeliveries.setAdapter(recentDeliveriesAdapter);
    }

    private void loadCustomerFeedbacks() {
        List<CustomerFeedbackModel> feedbacks = new ArrayList<>();
        feedbacks.add(new CustomerFeedbackModel("Sarah Wilson", "Fast delivery! Very satisfied.", 5.0f));
        feedbacks.add(new CustomerFeedbackModel("David Johnson", "Great service but delivery was slightly late.", 4.0f));
        feedbacks.add(new CustomerFeedbackModel("Emma Taylor", "Amazing experience, will order again!", 5.0f));

        customerFeedbackAdapter = new CustomerFeedbackAdapter(feedbacks);
        recyclerViewCustomerFeedbacks.setAdapter(customerFeedbackAdapter);
    }
}
