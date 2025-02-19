package com.example.greenify_organic_food_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.greenify_organic_food_app.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarHome.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);

        fetchCustomerDetails(navigationView);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_my_cart, R.id.nav_order_history, R.id.nav_my_profile)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);

        // Find the sign-out menu item and set its click listener
        MenuItem signOutItem = menu.findItem(R.id.sign_out_btn);
        signOutItem.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(HomeActivity.this, SignOutActivity.class);
            startActivity(intent);
            return true;
        });

        return true;
    }

    private void fetchCustomerDetails(NavigationView navigationView) {
        // Retrieve customer details from SharedPreferences
        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail == null) {
            Log.d("SharedPreferencesDebug", "Customer email not found in SharedPreferences");
            return;
        }

        // Get the header view from the NavigationView
        View headerView = navigationView.getHeaderView(0);

        // Find the TextViews and ImageView in the header
        TextView cusName = headerView.findViewById(R.id.customer_userInfo);
        TextView cusEmail = headerView.findViewById(R.id.customer_email);
        ImageView cusProfImage = headerView.findViewById(R.id.customer_profile_img);

        // Set the customer's email
        cusEmail.setText(customerEmail);

        // Add a Firestore real-time listener
        db.collection("customer")
                .whereEqualTo("email", customerEmail)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FirestoreDebug", "Error fetching customer data: " + error.getMessage());
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        DocumentSnapshot document = value.getDocuments().get(0);
                        String customerName = document.getString("name");
                        String customerMobile = document.getString("mobile");
                        String profileImageUrl = document.getString("profile_img");

                        // Set the customer's name
                        if (customerName != null) {
                            cusName.setText(customerName);
                        } else {
                            cusName.setText("No name set");
                        }

                        // Load the profile image
                        if (customerMobile != null && !customerMobile.isEmpty()) {
                            loadProfileImageFromStorage(customerMobile, cusProfImage);
                        } else if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            // If mobile number is not available, use the profileImageUrl
                            Glide.with(this)
                                    .load(profileImageUrl)
                                    .into(cusProfImage);
                        } else {
                            // Use a default placeholder if no image is available
                            cusProfImage.setImageResource(R.drawable.user);
                        }
                    } else {
                        Log.d("FirestoreDebug", "Customer data not found!");
                        cusProfImage.setImageResource(R.drawable.user);
                    }
                });
    }

    private void loadProfileImageFromStorage(String customerMobile, ImageView imageView) {
        // Construct the path to the profile image in Firebase Storage
        StorageReference storageReference = storage.getReference()
                .child("images/customer_profile_images/" + customerMobile + ".jpg");

        // Get the download URL of the image
        storageReference.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // Use Glide to load the image into the ImageView
                    Glide.with(this)
                            .load(uri)
                            .into(imageView);
                })
                .addOnFailureListener(e -> {
                    // If the image doesn't exist, use a default placeholder
                    imageView.setImageResource(R.drawable.user);
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}