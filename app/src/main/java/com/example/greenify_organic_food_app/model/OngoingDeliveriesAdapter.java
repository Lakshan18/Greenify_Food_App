package com.example.greenify_organic_food_app.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenify_organic_food_app.R;

import java.util.List;

public class OngoingDeliveriesAdapter extends RecyclerView.Adapter<OngoingDeliveriesAdapter.ViewHolder> {

    private List<OngoingDeliveryModel> ongoingDeliveries;
    private OnAcceptClickListener acceptClickListener;

    public OngoingDeliveriesAdapter(List<OngoingDeliveryModel> ongoingDeliveries) {
        this.ongoingDeliveries = ongoingDeliveries;
        this.acceptClickListener = acceptClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ongoing_delivery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OngoingDeliveryModel delivery = ongoingDeliveries.get(position);
        holder.customerName.setText(delivery.getCustomerName());
        holder.deliveryAddress.setText(delivery.getDeliveryAddress());
        holder.status.setText(delivery.getStatus());

        holder.acceptButton.setOnClickListener(view -> {
            if (acceptClickListener != null) {
                acceptClickListener.onAcceptClicked(delivery);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ongoingDeliveries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, deliveryAddress, status;
        Button acceptButton;

        public ViewHolder(View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customer_name);
            deliveryAddress = itemView.findViewById(R.id.delivery_address);
            status = itemView.findViewById(R.id.status);
            acceptButton = itemView.findViewById(R.id.btn_accept);
        }
    }

    public interface OnAcceptClickListener {
        void onAcceptClicked(OngoingDeliveryModel delivery);
    }
}
