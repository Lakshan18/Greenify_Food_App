package com.example.greenify_organic_food_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class PlaceOrderActivity extends AppCompatActivity {

    private CheckBox existingAddressCheckbox;
    private LinearLayout addressInputContainer;
    private Button btnProceed;
    private ImageView backToSingleProduct;
    private EditText editName, editAddress, editCity, editPhone;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        db = FirebaseFirestore.getInstance();

        existingAddressCheckbox = findViewById(R.id.existing_address_checkbox);
        addressInputContainer = findViewById(R.id.address_input_container);
        btnProceed = findViewById(R.id.btn_proceed);
        editName = findViewById(R.id.edit_name);
        editAddress = findViewById(R.id.edit_address);
        editCity = findViewById(R.id.edit_city);
        editPhone = findViewById(R.id.edit_phone);
        backToSingleProduct = findViewById(R.id.backTo_View1);

        btnProceed.setVisibility(View.VISIBLE);
        btnProceed.setEnabled(false);

        backToSingleProduct.setOnClickListener(v -> {
            Intent intent = new Intent(PlaceOrderActivity.this, SingleProductActivity.class);
            startActivity(intent);
        });

        existingAddressCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkCustomerAddress();
            } else {
                addressInputContainer.setVisibility(View.VISIBLE);
                checkFieldsForEmptyValues();
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFieldsForEmptyValues();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editName.addTextChangedListener(textWatcher);
        editAddress.addTextChangedListener(textWatcher);
        editCity.addTextChangedListener(textWatcher);
        editPhone.addTextChangedListener(textWatcher);
    }

    private void checkCustomerAddress() {
        String customerEmail = sharedPreferences.getString("customerEmail",null); // Replace with actual customer email (e.g., from SharedPreferences)

        db.collection("customer_address")
                .whereEqualTo("email", customerEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            showNoAddressDialog();
                        } else {
                            addressInputContainer.setVisibility(View.GONE);
                            btnProceed.setEnabled(true);
                            btnProceed.setOnClickListener(v -> openPayHereWeb());
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch address details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showNoAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_no_address, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        Button btnGoToProfile = dialogView.findViewById(R.id.btn_go_to_profile);
        Button btnClose = dialogView.findViewById(R.id.btn_close);

        btnGoToProfile.setOnClickListener(v -> {
            Intent intent = new Intent(PlaceOrderActivity.this, MainActivity.class); // Replace with your MainActivity
            intent.putExtra("fragment", "MyProfileFragment"); // Pass fragment name as extra
            startActivity(intent);
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> {
            existingAddressCheckbox.setChecked(false);
            addressInputContainer.setVisibility(View.VISIBLE);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void checkFieldsForEmptyValues() {
        String name = editName.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String city = editCity.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        btnProceed.setEnabled(!name.isEmpty() && !address.isEmpty() && !city.isEmpty() && !phone.isEmpty());
        btnProceed.setOnClickListener(v -> openPayHereWeb());
    }

    private void openPayHereWeb() {
        String merchantId = "1221485"; // Replace with actual merchant ID
        String orderId = "ORDER_12345"; // Replace with unique order ID
        String amount = "1000.00"; // Set order amount
        String currency = "LKR"; // Use "LKR" for Sri Lankan Rupees
        String firstName = editName.getText().toString().trim();
        String lastName = "";
        String email = "customer@example.com";
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String city = editCity.getText().toString().trim();

        // PayHere Payment URL
        String url = "https://www.payhere.lk/pay/" + merchantId +
                "?order_id=" + orderId +
                "&amount=" + amount +
                "&currency=" + currency +
                "&first_name=" + firstName +
                "&last_name=" + lastName +
                "&email=" + email +
                "&phone=" + phone +
                "&address=" + address +
                "&city=" + city;

        // Open in external browser
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
        startActivity(browserIntent);
    }
}