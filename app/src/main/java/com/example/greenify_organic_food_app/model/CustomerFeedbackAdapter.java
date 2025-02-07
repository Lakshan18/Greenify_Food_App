package com.example.greenify_organic_food_app.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.greenify_organic_food_app.R;
import com.example.greenify_organic_food_app.model.CustomerFeedbackModel;

import java.util.List;

public class CustomerFeedbackAdapter extends RecyclerView.Adapter<CustomerFeedbackAdapter.ViewHolder> {

    private List<CustomerFeedbackModel> customerFeedbacks;

    public CustomerFeedbackAdapter(List<CustomerFeedbackModel> customerFeedbacks) {
        this.customerFeedbacks = customerFeedbacks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_feedback, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CustomerFeedbackModel feedback = customerFeedbacks.get(position);
        holder.customerName.setText(feedback.getCustomerName());
        holder.feedback.setText(feedback.getFeedback());
        holder.rating.setText(String.valueOf(feedback.getRating()));
    }

    @Override
    public int getItemCount() {
        return customerFeedbacks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, feedback, rating;

        public ViewHolder(View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.txt_feedback_customer_name);
            feedback = itemView.findViewById(R.id.txt_feedback_message);
            rating = itemView.findViewById(R.id.txt_feedback_rating);
        }
    }
}
