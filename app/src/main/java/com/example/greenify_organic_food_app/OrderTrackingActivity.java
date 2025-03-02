package com.example.greenify_organic_food_app;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OrderTrackingActivity extends AppCompatActivity {
    private TextView orderStatus, prepTime, deliveryTime, delivererEta, productName, totalAmount;
    private ImageView productImg;
    private Button viewMapBtn, confirmDeliveryBtn;
    private ProgressBar progressBar;
    private LinearLayout loadingState;
    private MaterialCardView delivererTrackingCard, deliveryConfirmationCard;
    private FirebaseFirestore db;
    private String orderId, delivererId;
    private ListenerRegistration orderListener, delivererListener;
    private double delivererLatitude, delivererLongitude;
    private int selectedRating = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        orderStatus = findViewById(R.id.order_status);
        prepTime = findViewById(R.id.preparation_time);
        deliveryTime = findViewById(R.id.delivery_time);
        viewMapBtn = findViewById(R.id.view_map_btn);
        confirmDeliveryBtn = findViewById(R.id.confirm_delivery_btn);
        progressBar = findViewById(R.id.progress_bar);
        delivererTrackingCard = findViewById(R.id.deliverer_tracking_card);
        deliveryConfirmationCard = findViewById(R.id.delivery_confirmation_card);
        loadingState = findViewById(R.id.loading_state);
        productImg = findViewById(R.id.product_img_in_ord_track);
        productName = findViewById(R.id.product_name);
        totalAmount = findViewById(R.id.total_amount);

        db = FirebaseFirestore.getInstance();

        orderId = getIntent().getStringExtra("orderId");

        String productImageUrl = getIntent().getStringExtra("productImageUrl");
        if (productImageUrl != null && !productImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(productImageUrl)
                    .placeholder(R.drawable.ic_greenify)
                    .error(R.drawable.ic_error)
                    .into(productImg);
        } else {
            productImg.setImageResource(R.drawable.ic_greenify);
        }

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
                    if (error != null) {
                        Toast.makeText(this, "Error fetching order details", Toast.LENGTH_SHORT).show();
                        loadingState.setVisibility(View.GONE);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        String status = snapshot.getString("order_status");
                        delivererId = snapshot.getString("deliverer");

                        double totalPrice = snapshot.getDouble("total_price") - snapshot.getDouble("discount");
                        if (totalPrice > 0) {
                            totalAmount.setText(String.format("Rs %.2f", totalPrice));
                        }

                        db.collection("order").document(orderId).collection("items")
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                                            productName.setText("Cart Items");
                                        } else {
                                            String productNameText = snapshot.getString("product_name");
                                            if (productNameText != null) {
                                                productName.setText(productNameText);
                                            }
                                        }
                                    } else {
                                        Toast.makeText(this, "Failed to fetch order items", Toast.LENGTH_SHORT).show();
                                    }
                                });

                        updateUI(status);

                        if (!"Pending".equals(status) && !"Preparing".equals(status)) {
                            loadingState.setVisibility(View.GONE);
                        }

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
            case "Pending":
            case "Preparing":
                progressBar.setVisibility(View.VISIBLE);
                delivererTrackingCard.setVisibility(View.GONE);
                deliveryConfirmationCard.setVisibility(View.GONE);
                confirmDeliveryBtn.setVisibility(View.GONE);
                viewMapBtn.setVisibility(View.GONE);
                break;
            case "Out for Delivery":
                progressBar.setVisibility(View.GONE);
                delivererTrackingCard.setVisibility(View.VISIBLE);
                deliveryConfirmationCard.setVisibility(View.GONE);
                confirmDeliveryBtn.setVisibility(View.VISIBLE);
                viewMapBtn.setVisibility(View.VISIBLE);
                break;
            case "Delivered":
                progressBar.setVisibility(View.GONE);
                delivererTrackingCard.setVisibility(View.GONE);
                deliveryConfirmationCard.setVisibility(View.VISIBLE);
                confirmDeliveryBtn.setVisibility(View.VISIBLE);
                viewMapBtn.setVisibility(View.GONE);
                break;
            case "Completed":
                progressBar.setVisibility(View.GONE);
                delivererTrackingCard.setVisibility(View.GONE);
                deliveryConfirmationCard.setVisibility(View.VISIBLE);
                confirmDeliveryBtn.setVisibility(View.GONE);
                viewMapBtn.setVisibility(View.GONE);
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
                .update("order_status", "Delivered", "delivered_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()))
                .addOnSuccessListener(aVoid -> {
                    confirmDeliveryBtn.setEnabled(false);
                    CustomToast.showToast(OrderTrackingActivity.this, "Delivery confirmed!", true);
                    showFeedbackDialog();
                })
                .addOnFailureListener(e -> {
                    CustomToast.showToast(OrderTrackingActivity.this, "Failed to confirm delivery", false);
                });
    }

    private void showFeedbackDialog() {
        Dialog feedbackDialog = new Dialog(this);
        feedbackDialog.setContentView(R.layout.dialog_feedback);
        feedbackDialog.setCancelable(false);

        ImageView star1 = feedbackDialog.findViewById(R.id.star1);
        ImageView star2 = feedbackDialog.findViewById(R.id.star2);
        ImageView star3 = feedbackDialog.findViewById(R.id.star3);
        ImageView star4 = feedbackDialog.findViewById(R.id.star4);
        ImageView star5 = feedbackDialog.findViewById(R.id.star5);
        EditText feedbackMessage = feedbackDialog.findViewById(R.id.feedback_message);
        Button submitFeedbackBtn = feedbackDialog.findViewById(R.id.submit_feedback_btn);
        ImageView closeBtn = feedbackDialog.findViewById(R.id.close_btn); // Close button

        closeBtn.setOnClickListener(v -> feedbackDialog.dismiss());

        View.OnClickListener starClickListener = v -> {
            selectedRating = 0;
            star1.setImageResource(R.drawable.ic_star_outline);
            star2.setImageResource(R.drawable.ic_star_outline);
            star3.setImageResource(R.drawable.ic_star_outline);
            star4.setImageResource(R.drawable.ic_star_outline);
            star5.setImageResource(R.drawable.ic_star_outline);

            int clickedStarId = v.getId();

            if (clickedStarId == R.id.star1) {
                selectedRating = 1;
                star1.setImageResource(R.drawable.ic_star_filled);
            } else if (clickedStarId == R.id.star2) {
                selectedRating = 2;
                star1.setImageResource(R.drawable.ic_star_filled);
                star2.setImageResource(R.drawable.ic_star_filled);
            } else if (clickedStarId == R.id.star3) {
                selectedRating = 3;
                star1.setImageResource(R.drawable.ic_star_filled);
                star2.setImageResource(R.drawable.ic_star_filled);
                star3.setImageResource(R.drawable.ic_star_filled);
            } else if (clickedStarId == R.id.star4) {
                selectedRating = 4;
                star1.setImageResource(R.drawable.ic_star_filled);
                star2.setImageResource(R.drawable.ic_star_filled);
                star3.setImageResource(R.drawable.ic_star_filled);
                star4.setImageResource(R.drawable.ic_star_filled);
            } else if (clickedStarId == R.id.star5) {
                selectedRating = 5;
                star1.setImageResource(R.drawable.ic_star_filled);
                star2.setImageResource(R.drawable.ic_star_filled);
                star3.setImageResource(R.drawable.ic_star_filled);
                star4.setImageResource(R.drawable.ic_star_filled);
                star5.setImageResource(R.drawable.ic_star_filled);
            }
        };

        star1.setOnClickListener(starClickListener);
        star2.setOnClickListener(starClickListener);
        star3.setOnClickListener(starClickListener);
        star4.setOnClickListener(starClickListener);
        star5.setOnClickListener(starClickListener);

        submitFeedbackBtn.setOnClickListener(v -> {
            String feedback = feedbackMessage.getText().toString().trim();
            if (selectedRating == 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            saveFeedback(selectedRating, feedback, delivererId);
            feedbackDialog.dismiss();
        });

        feedbackDialog.show();
    }

    private void saveFeedback(int rating, String feedbackMessage, String delivererId) {
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("rating", rating);
        feedbackData.put("Message", feedbackMessage);
        feedbackData.put("orderId", orderId);
        feedbackData.put("delivererId", delivererId);
        feedbackData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        db.collection("feedback").document(orderId)
                .set(feedbackData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            orderListener.remove();
        }
        if (delivererListener != null) {
            delivererListener.remove();
        }
    }
}