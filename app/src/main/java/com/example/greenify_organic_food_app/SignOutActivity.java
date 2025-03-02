package com.example.greenify_organic_food_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SignOutActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("lastLoginTime", -1);
        editor.putString("signOutCustomer","signOut");
        editor.apply();

        Intent intent = new Intent(SignOutActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
}