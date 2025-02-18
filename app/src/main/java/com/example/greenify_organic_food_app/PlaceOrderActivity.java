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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.greenify_organic_food_app.model.CartModel;
import com.example.greenify_organic_food_app.model.OrderDataModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private static final String TAG = "PayHereDemo";
    private Map<String,String> addressMap;

    private RecyclerView orderItemsRecyclerView;
    private OrderItemsAdapter orderItemsAdapter;
    private List<CartModel> selectedItems;
    private boolean isCartOrder = false;
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
                            Log.d(TAG, "Payment Response: " + response.toString());
                            Log.d(TAG, msg);
                            if ("Payment Success".equals(msg)) {
                                // Insert order data into Firestore after successful payment
                                CustomToast.showToast(PlaceOrderActivity.this, msg, true);
                                orderConfirmed(addressMap.get("address"), addressMap.get("cusEmail"));
                            } else {
                                CustomToast.showToast(PlaceOrderActivity.this, msg, false);
                            }
                        }
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Log.d(TAG, "Customer canceled the request.");
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

        orderItemsRecyclerView = findViewById(R.id.order_items_recycler);
        if(getIntent().hasExtra("selectedItems")) {
            isCartOrder = true;
            selectedItems = getIntent().getParcelableArrayListExtra("selectedItems");
            setupCartOrderView();
        } else {
            setupSingleProductView();
        }

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

    private void setupSingleProductView() {
        // Original single product handling
        Intent intent = getIntent();
        String productName = intent.getStringExtra("productName");
        String productImageUrl = intent.getStringExtra("productImageUrl");
        double productPrice = intent.getDoubleExtra("productPrice", 0.0);
        int productQuantity = intent.getIntExtra("productQuantity", 1);

        ord_p_name.setText(productName);
        ord_p_price.setText(String.format("Rs: %.2f", productPrice));
        ord_p_qty.setText("Quantity:" + productQuantity);
        Glide.with(this).load(productImageUrl).into(ord_p_img);
    }

    private void setupCartOrderView() {
        // Hide single product views
        findViewById(R.id.product_details_section).setVisibility(View.GONE);

        // Setup RecyclerView
        orderItemsRecyclerView.setVisibility(View.VISIBLE);
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderItemsAdapter = new OrderItemsAdapter(selectedItems);
        orderItemsRecyclerView.setAdapter(orderItemsAdapter);

        // Calculate total price
        double total = calculateTotalAmount();
        ord_p_price.setText(String.format("Rs: %.2f", total));
    }

    private double calculateTotalAmount() {
        double total = 0;
        if(isCartOrder) {
            for (CartModel item : selectedItems) {
                total += item.getPrice() * item.getQuantity();
            }
        } else {
            total = getIntent().getDoubleExtra("productPrice", 0.0);
        }
        return total;
    }

    private void initiatePayment(OrderDataModel orderDataModel) {
        InitRequest req = new InitRequest();
        req.setMerchantId("1221485");
        req.setCurrency("LKR");
        req.setAmount(calculateTotalAmount());

        // Build items description
        StringBuilder itemsDesc = new StringBuilder();
        List<Item> payhereItems = new ArrayList<>();

        if (isCartOrder) {
            for (CartModel item : selectedItems) {
                itemsDesc.append(item.getProductName()).append(", ");
                payhereItems.add(new Item(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ));
            }
        } else {
            itemsDesc.append(getIntent().getStringExtra("productName"));
            payhereItems.add(new Item(
                    getIntent().getStringExtra("productId"),
                    getIntent().getStringExtra("productName"),
                    getIntent().getIntExtra("productQuantity", 1),
                    getIntent().getDoubleExtra("productPrice", 0.0)
            ));
        }

        if (itemsDesc.length() > 0) {
            itemsDesc.setLength(itemsDesc.length() - 2);
        }

        req.setOrderId(orderDataModel.getOrderId());
        req.setItemsDescription(itemsDesc.toString());
        req.getItems().addAll(payhereItems);

        // Log the request details
        Log.d(TAG, "Initiate Payment Request: " + req.toString());

        // Rest of payment initialization...
        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);

        // Enable Sandbox mode
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        payHereLauncher.launch(intent);
    }

    private void orderConfirmed(String customer_address, String customer_email) {
        String orderId = "ord_N" + System.currentTimeMillis();
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("order_id", orderId);
        orderData.put("customer_email", customer_email);
        orderData.put("deliverer_status", "Unassigned");
        orderData.put("order_status", "Pending");
        orderData.put("customer_address", customer_address);
        orderData.put("date_time", currentDateTime);
        orderData.put("total_amount", calculateTotalAmount());

        // Add items subcollection
        if(isCartOrder) {
            for (CartModel item : selectedItems) {
                addOrderItem(orderId, item);
            }
        } else {
            CartModel singleItem = new CartModel(
                    getIntent().getStringExtra("productName"),
                    getIntent().getIntExtra("productQuantity", 1),
                    getIntent().getDoubleExtra("productPrice", 0.0),
                    getIntent().getStringExtra("productImageUrl"),
                    getIntent().getStringExtra("productId")
            );
            addOrderItem(orderId, singleItem);
        }

        db.collection("order")
                .document(orderId)
                .set(orderData)
                .addOnSuccessListener(aVoid -> {
                    removePurchasedItemsFromCart();
                    Log.d(TAG, "Order completed!");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Order failed: " + e.getMessage()));
    }

    private void addOrderItem(String orderId, CartModel item) {
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("product_id", item.getProductId());
        itemData.put("product_name", item.getProductName());
        itemData.put("quantity", item.getQuantity());
        itemData.put("unit_price", item.getPrice());
        itemData.put("image_url", item.getImage());

        db.collection("order")
                .document(orderId)
                .collection("items")
                .add(itemData);
    }

    private void removePurchasedItemsFromCart() {
        if(!isCartOrder) return;

        String customerEmail = sharedPreferences.getString("customerEmail", null);
        db.collection("customer")
                .whereEqualTo("email", customerEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String customerId = queryDocumentSnapshots.getDocuments().get(0).getId();

                    for (CartModel item : selectedItems) {
                        db.collection("customer")
                                .document(customerId)
                                .collection("cart")
                                .document(item.getProductId())
                                .delete();
                    }
                });
    }

    private static class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.ViewHolder> {
        private final List<CartModel> items;

        OrderItemsAdapter(List<CartModel> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_product, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CartModel item = items.get(position);
            holder.productName.setText(item.getProductName());
            holder.productPrice.setText(String.format("Rs: %.2f x %d",
                    item.getPrice(),
                    item.getQuantity()));
            Glide.with(holder.itemView.getContext())
                    .load(item.getImage())
                    .into(holder.productImage);
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView productImage;
            TextView productName, productPrice;

            ViewHolder(View itemView) {
                super(itemView);
                productImage = itemView.findViewById(R.id.order_item_image);
                productName = itemView.findViewById(R.id.order_item_name);
                productPrice = itemView.findViewById(R.id.order_item_price);
            }
        }
    }
}