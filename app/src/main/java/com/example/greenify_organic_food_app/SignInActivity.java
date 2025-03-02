package com.example.greenify_organic_food_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SignInActivity extends AppCompatActivity {

    private Button signin_btn;
    private TextView textView,emailInput,passwordInput;

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textView = findViewById(R.id.go_to_signup);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        signin_btn = findViewById(R.id.signin_btn);

        db = FirebaseFirestore.getInstance();

        sharedPreferences = getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);

        emailInput = findViewById(R.id.email_signin_EditText1);
        passwordInput = findViewById(R.id.password_signin_EditText1);

        signin_btn.setOnClickListener(v -> validateAndSignInUser());
    }

    private void validateAndSignInUser(){
        String cusEmail = emailInput.getText().toString().trim();
        String cusPassword = passwordInput.getText().toString().trim();

        if(TextUtils.isEmpty(cusEmail) || !Patterns.EMAIL_ADDRESS.matcher(cusEmail).matches()){
            emailInput.setError("Please enter a valid Email.!");
        }else if(TextUtils.isEmpty(cusPassword) || cusPassword.length() < 10){
            passwordInput.setError("Password must be at least 10 characters");
        }else{
         checkUserInDb(cusEmail,cusPassword);
        }
    }

    private void checkUserInDb(String email, String enteredPassword) {
        String lowercaseEmail = email.toLowerCase();

        Log.d("Sign In Activity:" , lowercaseEmail);

        db.collection("customer")
                .whereEqualTo("email", lowercaseEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                                Log.d("Firestore Data", documentSnapshot.getData().toString());

                                String dbPassword = documentSnapshot.getString("password");
                                String dbName = documentSnapshot.getString("name");
                                String dbMobile = documentSnapshot.getString("mobile");
                                String dbCusId = documentSnapshot.getId();

                                if (enteredPassword.equals(dbPassword)) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("customerName", dbName);
                                    editor.putString("customerId",dbCusId);
                                    editor.putString("customerMobile", dbMobile);
                                    editor.putString("customerEmail", lowercaseEmail);
                                    editor.putLong("lastLoginTime", System.currentTimeMillis());
                                    editor.apply();

                                    CustomToast.showToast(SignInActivity.this, "Sign In Successfully.!", true);
                                    Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    CustomToast.showToast(SignInActivity.this, "Invalid Credentials.!", false);
                                }
                            }
                        } else {
                            CustomToast.showToast(SignInActivity.this, "Email not registered", false);
                        }
                    } else {
                        CustomToast.showToast(SignInActivity.this, "Error fetching data.", false);
                        Log.e("Firestore Error", "Error fetching data", task.getException());
                    }
                });
    }

}