package com.example.greenify_organic_food_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);

        long lastLoginTime = sharedPreferences.getLong("lastLoginTime", -1);
        String option = sharedPreferences.getString("signOutCustomer", "signOut");
        boolean isFirstTimeUser = sharedPreferences.getBoolean("isFirstTimeUser", true);
        String customerEmail = sharedPreferences.getString("customerEmail", null);

        if (customerEmail == null) {
            Log.d("MainActivity", "New customer, showing Get Started screen");
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            Button button = findViewById(R.id.started_btn);
            if (button != null) {
                Log.d("MainActivity", "Setting button visibility and listener");
                button.setVisibility(View.VISIBLE);
                button.setOnClickListener(v -> {
                    Log.d("MainActivity", "Button clicked, starting SignUpActivity");
                    Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
            return;
        }

        if (lastLoginTime != -1) {
            long currentTime = System.currentTimeMillis();
            long timeDifference = currentTime - lastLoginTime;

            if (timeDifference <= 48 * 60 * 60 * 1000) {
                Log.d("MainActivity", "Session valid, redirecting to HomeActivity");
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db.collection("customer")
                .whereEqualTo("email", customerEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            boolean startup = documentSnapshot.contains("isFirstTime")
                                    ? documentSnapshot.getBoolean("isFirstTime")
                                    : false;
                            Log.d("MainActivity", "Firestore query successful, isFirstTime: " + startup);

                            if (isFirstTimeUser && startup) {
                                Button button = findViewById(R.id.started_btn);
                                if (button != null) {
                                    Log.d("MainActivity", "Setting button visibility and listener");
                                    button.setVisibility(View.VISIBLE);
                                    button.setOnClickListener(v -> {
                                        Log.d("MainActivity", "Button clicked, starting SignUpActivity");
                                        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                }
                            } else {
                                Log.d("MainActivity", "User is not a first-time user or startup is false");
                            }
                        } else {
                            Log.e("MainActivity", "No documents found in Firestore query");
                        }
                    } else {
                        Log.e("MainActivity", "Firestore query failed", task.getException());
                    }
                });
    }
}