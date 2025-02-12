package com.example.greenify_organic_food_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameInput, mobileInput, emailInput, passwordInput;
    private Button signupBtn;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);

        nameInput = findViewById(R.id.name_signup_EditText1);
        mobileInput = findViewById(R.id.mobile_signup_EditText1);
        emailInput = findViewById(R.id.email_signup_EditText1);
        passwordInput = findViewById(R.id.password_signup_EditText1);
        signupBtn = findViewById(R.id.signup_btn);

        TextView textView = findViewById(R.id.go_to_signin);
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        signupBtn.setOnClickListener(v -> validateAndRegisterUser());
    }

    private void validateAndRegisterUser() {
        String cusName = nameInput.getText().toString().trim();
        String cusMobile = mobileInput.getText().toString().trim();
        String cusEmail = emailInput.getText().toString().trim();
        String cusPassword = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(cusName)) {
            nameInput.setError("Name is required");
        } else if (TextUtils.isEmpty(cusMobile) || cusMobile.length() != 10) {
            mobileInput.setError("Enter a valid 10-digit mobile number");
        }else if (TextUtils.isEmpty(cusEmail) || !Patterns.EMAIL_ADDRESS.matcher(cusEmail).matches()) {
            emailInput.setError("Enter a valid email address");
        }else if (TextUtils.isEmpty(cusPassword) || cusPassword.length() < 10) {
            passwordInput.setError("Password must be at least 10 characters");
        }else {

            Map<String, Object> customer = new HashMap<>();
            customer.put("name", cusName);
            customer.put("mobile", cusMobile);
            customer.put("email", cusEmail);
            customer.put("password", cusPassword);
            customer.put("username", null);
            customer.put("profile_img", null);

            db.collection("customer")
                    .add(customer)
                    .addOnSuccessListener(documentReference -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isFirstTimeUser", false); // Mark as not first time user
                        editor.apply();

                        CustomToast.showToast(SignUpActivity.this, "Registration Successful!", true);
                        startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> CustomToast.showToast(SignUpActivity.this, "Something went wrong.!", false));
        }
    }
}
