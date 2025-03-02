package com.example.greenify_organic_food_app.ui.order_history;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.OrderInvoiceActivity;
import com.example.greenify_organic_food_app.OrderTrackingActivity;
import com.example.greenify_organic_food_app.R;
import com.example.greenify_organic_food_app.model.CartModel;
import com.example.greenify_organic_food_app.model.ImageSliderAdapter;
import com.example.greenify_organic_food_app.model.OrderHisModel;

import java.util.ArrayList;
import java.util.List;

public class OrderHisAdapter extends RecyclerView.Adapter<OrderHisAdapter.OrderHisViewHolder> {

    private final List<OrderHisModel> orderList;
    private final Context context;

    public OrderHisAdapter(List<OrderHisModel> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderHisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_in_history, parent, false);
        return new OrderHisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHisViewHolder holder, int position) {
        OrderHisModel order = orderList.get(position);
        List<CartModel> products = order.getProducts();

        if (products.size() == 1) {
            // Single product order
            CartModel product = products.get(0);
            holder.orderTitle.setText(product.getProductName());
            holder.orderQuantity.setText("X " + product.getQuantity());
            holder.orderPrice.setText(String.format("Rs: %.2f", product.getPrice() * product.getQuantity()));
            holder.orderDate.setText(order.getDate());
            holder.order_status_in_history.setText(order.getOrderStatus());

            // Show single product image and hide image slider
            holder.orderImage.setVisibility(View.VISIBLE);
            holder.imageSlider2.setVisibility(View.GONE);
            holder.productDetails.setVisibility(View.GONE);

            Glide.with(holder.itemView.getContext())
                    .load(product.getImage())
                    .override(getScreenWidth() / 3) // 1/3 of screen width
                    .centerCrop()
                    .into(holder.orderImage);
        } else {
            // Multiple product order
            holder.orderTitle.setText("Cart Order");
            holder.orderQuantity.setText("X " + products.size());
            holder.orderPrice.setText(String.format("Rs: %.2f", order.getTotalPrice()));
            holder.orderDate.setText(order.getDate());
            holder.order_status_in_history.setText(order.getOrderStatus());

            // Show image slider and hide single product image
            holder.orderImage.setVisibility(View.GONE);
            holder.imageSlider2.setVisibility(View.VISIBLE);
            holder.productDetails.setVisibility(View.VISIBLE);

            // Set up image slider for multiple products
            List<String> imageUrls = new ArrayList<>();
            for (CartModel product : products) {
                imageUrls.add(product.getImage());
            }

            // Initialize and set the adapter for the image slider
            ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(holder.itemView.getContext(), imageUrls);
            holder.imageSlider2.setAdapter(sliderAdapter);

            // Auto-play functionality for the image slider
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                int currentPage = 0;
                @Override
                public void run() {
                    if (currentPage == imageUrls.size()) {
                        currentPage = 0;
                    }
                    holder.imageSlider2.setCurrentItem(currentPage++, true);
                    handler.postDelayed(this, 3000); // Change image every 3 seconds
                }
            };
            handler.postDelayed(runnable, 3000); // Start auto-play after 3 seconds

            // Display product details
            StringBuilder productDetails = new StringBuilder();
            double totalOrderPrice = 0.0;
            for (CartModel product : products) {
                double itemTotal = product.getPrice() * product.getQuantity();
                double itemDiscount = 0.0;
                if (product.getQuantity() >= 3) {
                    itemDiscount = itemTotal * 0.05; // 5% discount for quantities >= 3
                }
                double itemFinalPrice = itemTotal - itemDiscount;
                totalOrderPrice += itemFinalPrice;

                productDetails.append(product.getProductName())
                        .append(" (X").append(product.getQuantity()).append(") - Rs: ")
                        .append(String.format("%.2f", itemFinalPrice)).append("\n");
            }
            holder.productDetails.setText(productDetails.toString());

            // Update the total price for the order
            holder.orderPrice.setText(String.format("Rs: %.2f", totalOrderPrice));
        }

        // Set order status color
        int color;
        switch (order.getOrderStatus()) {
            case "Pending":
                color = Color.MAGENTA;
                break;
            case "Preparing":
                color = Color.parseColor("#FFA500");
                break;
            case "Ready for Pickup":
                color = Color.BLUE;
                break;
            case "Out for Delivery":
                color = Color.parseColor("#87CEEB");
                break;
            case "Delivered":
                color = Color.parseColor("#50C878");
                break;
            case "Completed":
                color = Color.GREEN;
                break;
            default:
                color = Color.GRAY;
                break;
        }
        holder.order_status_in_history.setTextColor(color);

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            String status = order.getOrderStatus();
            if (status.equals("Pending") || status.equals("Preparing") || status.equals("Ready for Pickup")) {
                Intent intent = new Intent(context, OrderTrackingActivity.class);
                intent.putExtra("orderId", order.getOrderId());
                intent.putExtra("orderStatus", order.getOrderStatus());
                intent.putExtra("productImageUrl", products.get(0).getImage());
                context.startActivity(intent);
            } else if (status.equals("Out for Delivery") || status.equals("Delivered") || status.equals("Completed")) {
                Intent intent = new Intent(context, OrderInvoiceActivity.class);
                intent.putExtra("productName", products.get(0).getProductName());
                intent.putExtra("productImage", products.get(0).getImage());
                intent.putExtra("orderId", order.getOrderId());
                intent.putExtra("orderDate", order.getDate());
                intent.putExtra("orderStatus", order.getOrderStatus());
                intent.putExtra("orderTotal", order.getTotalPrice());
                context.startActivity(intent);
            }
        });
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderHisViewHolder extends RecyclerView.ViewHolder {
        ImageView orderImage;
        TextView orderTitle, orderQuantity, orderPrice, orderDate, order_status_in_history, productDetails;
        ViewPager2 imageSlider2;

        public OrderHisViewHolder(@NonNull View itemView) {
            super(itemView);
            orderImage = itemView.findViewById(R.id.orderImage);
            orderTitle = itemView.findViewById(R.id.orderTitle);
            orderQuantity = itemView.findViewById(R.id.orderQuantity);
            order_status_in_history = itemView.findViewById(R.id.order_status_in_history);
            orderPrice = itemView.findViewById(R.id.orderPrice);
            orderDate = itemView.findViewById(R.id.orderDate);
            imageSlider2 = itemView.findViewById(R.id.imageSlider2);
            productDetails = itemView.findViewById(R.id.productDetails);
        }
    }
}