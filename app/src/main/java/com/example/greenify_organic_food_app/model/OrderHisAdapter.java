package com.example.greenify_organic_food_app.ui.order_history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.R;
import com.example.greenify_organic_food_app.model.OrderHisModel;

import java.util.List;

public class OrderHisAdapter extends RecyclerView.Adapter<OrderHisAdapter.OrderHisViewHolder> {

    private final List<OrderHisModel> orderList;

    public OrderHisAdapter(List<OrderHisModel> orderList) {
        this.orderList = orderList;
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
        holder.orderPrice.setText("Rs: " + order.getPrice());
        holder.orderDate.setText(order.getDate());

        // Load image from Firebase Storage using Glide
        Glide.with(holder.itemView.getContext())
                .load(order.getImageUrl())
                .into(holder.orderImage);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderHisViewHolder extends RecyclerView.ViewHolder {
        ImageView orderImage;
        TextView orderTitle, orderQuantity, orderPrice, orderDate;

        public OrderHisViewHolder(@NonNull View itemView) {
            super(itemView);
            orderImage = itemView.findViewById(R.id.orderImage);
            orderTitle = itemView.findViewById(R.id.orderTitle);
            orderQuantity = itemView.findViewById(R.id.orderQuantity);
            orderPrice = itemView.findViewById(R.id.orderPrice);
            orderDate = itemView.findViewById(R.id.orderDate);
        }
    }
}