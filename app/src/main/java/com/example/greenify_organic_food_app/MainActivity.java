package com.example.greenify_organic_food_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);

        long lastLoginTime = sharedPreferences.getLong("lastLoginTime", -1);
        String option = sharedPreferences.getString("signOutCustomer","signOut");

        if (lastLoginTime == -1 && !"signOut".equals(option)) {
            // User is not logged in, show the "Get Started" button
            Button button = findViewById(R.id.started_btn);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            // User is logged in, check if the session is still valid
            long currentTime = System.currentTimeMillis();
            long timeDifference = currentTime - lastLoginTime;

            if (timeDifference > 48 * 60 * 60 * 1000) {  // 48 hours in milliseconds
                // Session expired, redirect to SignInActivity
                Intent intent1 = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent1);
                finish();
            } else {
                // Session is valid, redirect to HomeActivity
                Intent intent2 = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent2);
                finish();
            }
        }
    }
}