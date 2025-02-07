package com.example.greenify_organic_food_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class PlaceOrderActivity extends AppCompatActivity {

    private CheckBox existingAddressCheckbox;
    private LinearLayout addressInputContainer;
    private Button btnProceed;
    private EditText editName, editAddress, editCity, editPhone;

//    private static final String PAYMENT_INTENT_URL = "http://10.0.2.2:8080/Greenify_Food_App/create-payment-intent"; // Adjust URL if needed
//    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
//    private OkHttpClient client = new OkHttpClient();

//    private PaymentSheet paymentSheet;
//    private String clientSecret;
//    private String publishableKey = "pk_test_51QMMxsLqvkTJPbtHnXyl0b44FnacDU1A9Z2UXWrq2KLUnNfhhX8fyaABY3KRHgEttNU1juDoOhB5SI6P8CCj1t6Z00bEcNFj71";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        // Initialize Views
        existingAddressCheckbox = findViewById(R.id.existing_address_checkbox);
        addressInputContainer = findViewById(R.id.address_input_container);
        btnProceed = findViewById(R.id.btn_proceed);
        editName = findViewById(R.id.edit_name);
        editAddress = findViewById(R.id.edit_address);
        editCity = findViewById(R.id.edit_city);
        editPhone = findViewById(R.id.edit_phone);

        btnProceed.setVisibility(View.VISIBLE);
        btnProceed.setEnabled(false);


//        PaymentConfiguration.init(this, publishableKey);
//
//        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        // Set listeners
        existingAddressCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addressInputContainer.setVisibility(View.GONE);
                btnProceed.setEnabled(true);
                btnProceed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPayHereWeb();
                    }
                });
            } else {
                addressInputContainer.setVisibility(View.VISIBLE);
                checkFieldsForEmptyValues();
            }
        });

        // Text Watchers
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

    // Check if fields are filled and enable/disable the proceed button
    private void checkFieldsForEmptyValues() {
        String name = editName.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String city = editCity.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        btnProceed.setEnabled(!name.isEmpty() && !address.isEmpty() && !city.isEmpty() && !phone.isEmpty());
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               openPayHereWeb();
            }
        });
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

    // Fetch Payment Intent from your backend
//    private void fetchPaymentIntent() {
//        // You should send necessary order details (e.g., amount, currency, etc.) with the request
//        String orderDetails = "{ \"amount\": 5000, \"currency\": \"usd\" }"; // Example data for testing
//
//        RequestBody body = RequestBody.create(orderDetails, JSON);
//
//        Request request = new Request.Builder()
//                .url(PAYMENT_INTENT_URL)
//                .post(body)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e("Stripe", "API Request Failed: " + e.getMessage());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    Log.e("Stripe", "Unexpected response: " + response.body().string());
//                    return;
//                }
//
//                String responseBody = response.body().string();
//                Log.d("Stripe", "Response: " + responseBody);
//
//                try {
//                    JSONObject jsonObject = new JSONObject(responseBody);
//                    clientSecret = jsonObject.getString("clientSecret");
//
//                    runOnUiThread(() -> openStripePayment(clientSecret));
//                } catch (JSONException e) {
//                    Log.e("Stripe", "JSON Parsing Error: " + e.getMessage());
//                }
//            }
//        });
//    }
//
//    // Open the Stripe Payment Sheet
//    private void openStripePayment(String clientSecret) {
//        this.clientSecret = clientSecret;
//
//        runOnUiThread(() -> {
//            paymentSheet.presentWithPaymentIntent(clientSecret,
//                    new PaymentSheet.Configuration("Greenify Organic Food Store"));
//        });
//    }
//
//    // Handle payment result
//    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
//        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
//            Log.d("Stripe", "Payment Successful!");
//            Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();
//        } else {
//            Log.e("Stripe", "Payment Failed!");
//            Toast.makeText(this, "Payment Failed!", Toast.LENGTH_SHORT).show();
//        }
//    }
}
