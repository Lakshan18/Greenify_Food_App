package com.example.greenify_organic_food_app.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.greenify_organic_food_app.R;

import java.util.List;

public class RecentDeliveriesAdapter extends RecyclerView.Adapter<RecentDeliveriesAdapter.ViewHolder> {

    private List<RecentDeliveryModel> recentDeliveries;

    public RecentDeliveriesAdapter(List<RecentDeliveryModel> recentDeliveries) {
        this.recentDeliveries = recentDeliveries;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_delivery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RecentDeliveryModel delivery = recentDeliveries.get(position);
        holder.customerName.setText(delivery.getCustomerName());
        holder.deliveryDate.setText(delivery.getDeliveryDate());
        holder.deliveryStatus.setText(delivery.getDeliveryStatus());
    }

    @Override
    public int getItemCount() {
        return recentDeliveries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, deliveryDate, deliveryStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.txt_customer_name);
            deliveryDate = itemView.findViewById(R.id.txt_delivery_date);
            deliveryStatus = itemView.findViewById(R.id.txt_delivery_status);
        }
    }
}
