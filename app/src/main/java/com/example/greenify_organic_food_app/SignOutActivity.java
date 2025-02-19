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

        // Clear the last login time to force the user to sign in again
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("lastLoginTime", -1);
        editor.putString("signOutCustomer","signOut");// Reset the last login time
        editor.apply();

        // Navigate back to the SignInActivity
        Intent intent = new Intent(SignOutActivity.this, SignInActivity.class);
        startActivity(intent);
        finish(); // Close the SignOutActivity to prevent going back
    }
}