package com.example.greenify_organic_food_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.greenify_organic_food_app.R;

public class CustomToast {

    public static void showToast(Context context, String message, boolean isSuccess) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        ImageView toastIcon = layout.findViewById(R.id.toast_icon);
        TextView toastText = layout.findViewById(R.id.toast_text);

        toastText.setText(message);
        if (isSuccess) {
            toastIcon.setImageResource(R.drawable.ic_success);
        } else {
            toastIcon.setImageResource(R.drawable.ic_error);
        }

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
