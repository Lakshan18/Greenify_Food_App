package com.example.greenify_organic_food_app.ui.order_history;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.OrderInvoiceActivity;
import com.example.greenify_organic_food_app.OrderTrackingActivity;
import com.example.greenify_organic_food_app.R;
import com.example.greenify_organic_food_app.model.OrderHisModel;

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
        holder.orderTitle.setText(order.getTitle());
        holder.orderQuantity.setText("X " + order.getQuantity());
        holder.orderPrice.setText(String.format("Rs: %.2f", order.getPrice()));
        holder.orderDate.setText(order.getDate());
        holder.order_status_in_history.setText(order.getOrder_status());

        int color;
        switch (order.getOrder_status()) {
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

        holder.itemView.setOnClickListener(v -> {
            String status = order.getOrder_status();
            if (status.equals("Pending") || status.equals("Preparing") || status.equals("Ready for Pickup")) {
                Intent intent = new Intent(context, OrderTrackingActivity.class);
                intent.putExtra("orderId", order.getOrder_id());
                intent.putExtra("orderStatus", order.getOrder_status());
                context.startActivity(intent);
            } else if (status.equals("Out for Delivery") || status.equals("Delivered") || status.equals("Completed")) {
                Intent intent = new Intent(context, OrderInvoiceActivity.class);
                intent.putExtra("productName",order.getTitle());
                intent.putExtra("productImage",order.getProductImage());
                intent.putExtra("orderId", order.getOrder_id());
                intent.putExtra("orderDate", order.getDate());
                intent.putExtra("orderStatus", order.getOrder_status());
                intent.putExtra("orderTotal", order.getPrice());
                context.startActivity(intent);
            }
        });

        Glide.with(holder.itemView.getContext())
                .load(order.getProductImage())
                .into(holder.orderImage);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderHisViewHolder extends RecyclerView.ViewHolder {
        ImageView orderImage;
        TextView orderTitle, orderQuantity, orderPrice, orderDate, order_status_in_history;

        public OrderHisViewHolder(@NonNull View itemView) {
            super(itemView);
            orderImage = itemView.findViewById(R.id.orderImage);
            orderTitle = itemView.findViewById(R.id.orderTitle);
            orderQuantity = itemView.findViewById(R.id.orderQuantity);
            order_status_in_history = itemView.findViewById(R.id.order_status_in_history);
            orderPrice = itemView.findViewById(R.id.orderPrice);
            orderDate = itemView.findViewById(R.id.orderDate);
        }
    }
}
