package com.example.greenify_organic_food_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class OrderTrackingActivity extends AppCompatActivity {
    private TextView orderStatus, prepTime, deliveryTime, delivererEta;
    private Button viewMapBtn, confirmDeliveryBtn;
    private ProgressBar progressBar;
    private LinearLayout loadingState;
    private MaterialCardView delivererTrackingCard, deliveryConfirmationCard;
    private FirebaseFirestore db;
    private String orderId, delivererId;
    private ListenerRegistration orderListener, delivererListener;
    private double delivererLatitude, delivererLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        // Initialize views
        orderStatus = findViewById(R.id.order_status);
        prepTime = findViewById(R.id.preparation_time);
        deliveryTime = findViewById(R.id.delivery_time);
        delivererEta = findViewById(R.id.deliverer_eta);
        viewMapBtn = findViewById(R.id.view_map_btn);
        confirmDeliveryBtn = findViewById(R.id.confirm_delivery_btn);
        progressBar = findViewById(R.id.progress_bar);
        delivererTrackingCard = findViewById(R.id.deliverer_tracking_card);
        deliveryConfirmationCard = findViewById(R.id.delivery_confirmation_card);
        loadingState = findViewById(R.id.loading_state);

        db = FirebaseFirestore.getInstance();

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            Toast.makeText(this, "Order ID not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRealTimeUpdates();

        viewMapBtn.setOnClickListener(v -> openDeliveryMap());
        confirmDeliveryBtn.setOnClickListener(v -> confirmDelivery());
    }

    private void setupRealTimeUpdates() {
        loadingState.setVisibility(View.VISIBLE);

        orderListener = db.collection("order").document(orderId)
                .addSnapshotListener((snapshot, error) -> {
                    loadingState.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(this, "Error fetching order details", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        String status = snapshot.getString("order_status");
                        delivererId = snapshot.getString("deliverer");

                        updateUI(status);
                        if ("Out for Delivery".equals(status) && delivererId != null) {
                            setupDeliveryTracking(delivererId);
                        }
                    }
                });
    }

    private void setupDeliveryTracking(String delivererId) {
        if (delivererListener != null) {
            delivererListener.remove();
        }

        delivererListener = db.collection("deliverer").document(delivererId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error tracking deliverer", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Map<String, Object> location = (Map<String, Object>) snapshot.get("current_location");
                        if (location != null) {
                            delivererLatitude = (double) location.get("latitude");
                            delivererLongitude = (double) location.get("longitude");
                        }
                    }
                });
    }

    private void updateUI(String status) {
        orderStatus.setText(status);

        switch (status) {
            case "Preparing":
                progressBar.setVisibility(View.VISIBLE);
                delivererTrackingCard.setVisibility(View.GONE);
                deliveryConfirmationCard.setVisibility(View.GONE);
                confirmDeliveryBtn.setVisibility(View.GONE); // Hide confirm button
                break;
            case "Out for Delivery":
                progressBar.setVisibility(View.GONE);
                delivererTrackingCard.setVisibility(View.VISIBLE);
                deliveryConfirmationCard.setVisibility(View.GONE);
                confirmDeliveryBtn.setVisibility(View.VISIBLE); // Show confirm button
                break;
            case "Delivered":
                progressBar.setVisibility(View.GONE);
                delivererTrackingCard.setVisibility(View.GONE);
                deliveryConfirmationCard.setVisibility(View.VISIBLE);
                confirmDeliveryBtn.setVisibility(View.VISIBLE); // Show confirm button
                break;
            case "Completed":
                progressBar.setVisibility(View.GONE);
                delivererTrackingCard.setVisibility(View.GONE);
                deliveryConfirmationCard.setVisibility(View.VISIBLE);
                confirmDeliveryBtn.setVisibility(View.GONE); // Hide confirm button
                Toast.makeText(this, "Order completed successfully!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void openDeliveryMap() {
        if (delivererId == null) {
            Toast.makeText(this, "Deliverer information not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + delivererLatitude + "," + delivererLongitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            openWebMaps();
        }
    }

    private void openWebMaps() {
        Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + delivererLatitude + "," + delivererLongitude);
        startActivity(new Intent(Intent.ACTION_VIEW, webUri));
    }

    private void confirmDelivery() {
        db.collection("order").document(orderId)
                .update("order_status", "Delivered","delivered_time",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()))
                .addOnSuccessListener(aVoid -> {
                    confirmDeliveryBtn.setEnabled(false);
                    Toast.makeText(this, "Delivery confirmed!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to confirm delivery", Toast.LENGTH_SHORT).show();
                });
    }
}
