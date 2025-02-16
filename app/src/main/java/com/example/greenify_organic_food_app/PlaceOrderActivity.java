package com.example.greenify_organic_food_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.model.OrderDataModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

public class PlaceOrderActivity extends AppCompatActivity {

    private CheckBox existingAddressCheckbox;
    private LinearLayout addressInputContainer;
    private Button btnProceed;
    private ImageView backToSingleProduct, ord_p_img;
    private EditText editName, editAddress, editCity, editPhone;
    private TextView ord_p_name, ord_p_price, ord_p_qty;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private static final int PAYHERE_REQUEST = 11001;
    private static final String TAG = "PayHereDemo";
    private Map<String,String> addressMap;
    private final ActivityResultLauncher<Intent> payHereLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                        Serializable serializable = data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

                        if (serializable instanceof PHResponse) {
                            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) serializable;
                            String msg = response.isSuccess() ? "Payment Success" : "Payment Failed.!";
                            Log.d(TAG, msg);
                            if ("Payment Success".equals(msg)) {
                                // Insert order data into Firestore after successful payment
                                CustomToast.showToast(PlaceOrderActivity.this, msg, true);
                                orderConfirmed(addressMap.get("address"),addressMap.get("cusEmail"));
                            } else {
                                CustomToast.showToast(PlaceOrderActivity.this, msg, false);
                            }
                        }
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    CustomToast.showToast(PlaceOrderActivity.this, "Customer canceled the request.!", false);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);

        existingAddressCheckbox = findViewById(R.id.existing_address_checkbox);
        addressInputContainer = findViewById(R.id.address_input_container);
        btnProceed = findViewById(R.id.btn_proceed);
        editName = findViewById(R.id.edit_name);
        editAddress = findViewById(R.id.edit_address);
        editCity = findViewById(R.id.edit_city);
        editPhone = findViewById(R.id.edit_phone);
        backToSingleProduct = findViewById(R.id.backTo_View1);
        ord_p_img = findViewById(R.id.order_product_image);
        ord_p_name = findViewById(R.id.order_product_name);
        ord_p_price = findViewById(R.id.order_total_price);
        ord_p_qty = findViewById(R.id.order_selected_quantity);

        Intent intent = getIntent();
        String productName = intent.getStringExtra("productName");
        String productImageUrl = intent.getStringExtra("productImageUrl");
        double productPrice = intent.getDoubleExtra("productPrice", 0.0);
        int productQuantity = intent.getIntExtra("productQuantity", 1);

        ord_p_name.setText(productName);
        ord_p_price.setText(String.format("Rs: %.2f", productPrice));
        ord_p_qty.setText("Quantity:" + productQuantity);
        Glide.with(this).load(productImageUrl).into(ord_p_img);

        btnProceed.setVisibility(View.VISIBLE);
        btnProceed.setEnabled(false);

        backToSingleProduct.setOnClickListener(v -> {
            Intent intent2 = new Intent(PlaceOrderActivity.this, HomeActivity.class);
            startActivity(intent2);
        });

        existingAddressCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkCustomer();
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

    private void checkCustomer() {
        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail == null) {
            Toast.makeText(this, "Customer email not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("customer")
                .whereEqualTo("email", customerEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String cus_id = document.getId();
                        loadCheckCustomerAddress(cus_id);
                    } else {
                        Toast.makeText(this, "Failed to fetch customer details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCheckCustomerAddress(String customerId) {
        db.collection("customer")
                .document(customerId)
                .collection("customer_address")
                .document("delivery_details")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Address exists, proceed with payment
                            String address = document.getString("address_line1") + document.getString("address_line2");

                            Intent intent = getIntent();
                            double productPrice = intent.getDoubleExtra("productPrice", 0.0);
                            String productName = intent.getStringExtra("productName");
                            String email = sharedPreferences.getString("customerEmail", null);
                            String phone = sharedPreferences.getString("customerMobile", null);
                            String name = sharedPreferences.getString("customerName", null);

                            String orderId = "ord_N" + System.currentTimeMillis();

                            // Enable the proceed button and set the click listener
                            btnProceed.setEnabled(true);
                            OrderDataModel orderDataModel1 = new OrderDataModel(name, phone, email, address, productPrice, productName, orderId);
                            btnProceed.setOnClickListener(v -> initiatePayment(orderDataModel1));
                        } else {
                            // Address does not exist, show the "No Address" dialog
                            showNoAddressDialog();
                        }
                    } else {
                        // Firestore query failed
                        CustomToast.showToast(PlaceOrderActivity.this, "Failed to fetch address details.", false);
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

        Intent intent = getIntent();
        double productPrice = intent.getDoubleExtra("productPrice", 0.0);
        String productName = intent.getStringExtra("productName");
        String email = sharedPreferences.getString("customerEmail", null);

        String orderId = "ord_N" + System.currentTimeMillis();

        btnProceed.setEnabled(!name.isEmpty() && !address.isEmpty() && !city.isEmpty() && !phone.isEmpty());

        OrderDataModel orderDataModel2 = new OrderDataModel(name, phone, email, address, productPrice, productName, orderId);

        btnProceed.setOnClickListener(v -> initiatePayment(orderDataModel2));
    }

    private void initiatePayment(OrderDataModel orderDataModel) {
        InitRequest req = new InitRequest();
        req.setMerchantId("1221485");       // Merchant ID
        req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
        req.setAmount(orderDataModel.getTotalPrice());             // Final Amount to be charged
        req.setOrderId(orderDataModel.getOrderId());        // Unique Reference ID
        req.setItemsDescription(orderDataModel.getProductName());  // Item description title
        req.setCustom1("This is the custom message 1");
        req.setCustom2("This is the custom message 2");
        req.getCustomer().setFirstName(orderDataModel.getCustomerName());
        req.getCustomer().setLastName("");
        req.getCustomer().setEmail(orderDataModel.getEmail());
        req.getCustomer().setPhone(orderDataModel.getMobile());
        req.getCustomer().getAddress().setAddress(orderDataModel.getAddress());
        req.getCustomer().getAddress().setCity("");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        // Optional Params
        req.getCustomer().getDeliveryAddress().setAddress(orderDataModel.getAddress());
        req.getCustomer().getDeliveryAddress().setCity("");
        req.getCustomer().getDeliveryAddress().setCountry("Sri Lanka");
        req.getItems().add(new Item(null, "Door bell wireless", 1, 1000.0));

        req.setNotifyUrl("xxxx");           // Notify Url

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);

        // Enable Sandbox mode
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        payHereLauncher.launch(intent);
        addressMap = new HashMap<>();
        addressMap.put("address",orderDataModel.getAddress());
        addressMap.put("cusEmail",orderDataModel.getEmail());
    }

    private void orderConfirmed(String customer_address,String customer_email) {
        Intent intent = getIntent();
        String productName = intent.getStringExtra("productName");
        double productPrice = intent.getDoubleExtra("productPrice", 0.0);
        int productQuantity = intent.getIntExtra("productQuantity", 1);
        String imageUrl = intent.getStringExtra("productImageUrl");
        String orderId = "ord_N" + System.currentTimeMillis();
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("order_id", orderId);
        orderData.put("product_name", productName);
        orderData.put("purchased_qty", productQuantity);
        orderData.put("unit_price", productPrice);
        orderData.put("total_price", productPrice * productQuantity);
        orderData.put("customer_email", customer_email);
        orderData.put("deliverer_status", "Unassigned");
        orderData.put("order_status", "Pending");
        orderData.put("customer_address",customer_address);
        orderData.put("date_time",currentDateTime);
        orderData.put("product_image",imageUrl);

        db.collection("order")
                .document(orderId)
                .set(orderData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Your order is complete.!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Order is failed." + e.getMessage());
                });


    }
}