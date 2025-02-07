package com.example.greenify_organic_food_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignInActivity extends AppCompatActivity {

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

        TextView textView = findViewById(R.id.go_to_signup);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        Button signin_btn = findViewById(R.id.signin_btn);
        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                TextView textView1 = findViewById(R.id.email_textfield);
//                TextView textView2 = findViewById(R.id.password_textfield);
//
//                String email = String.valueOf(textView1.getText());
//                String password = String.valueOf(textView2.getText());
//
//                if(email.isBlank()){
//                    Toast.makeText(SignInActivity.this,"please enter your email.!",Toast.LENGTH_SHORT).show();
//                }else if(password.isBlank()){
//                    Toast.makeText(SignInActivity.this,"please enter your password.!",Toast.LENGTH_SHORT).show();
//                }else if(email.equals("Lucky12@") && password.equals("1234")){
//                    Toast.makeText(SignInActivity.this,"login success.!",Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignInActivity.this,HomeActivity.class);
                    startActivity(intent);
//                }
            }
        });
    }
}