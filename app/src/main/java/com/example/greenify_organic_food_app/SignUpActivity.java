package com.example.greenify_organic_food_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements SensorEventListener {

    private EditText nameInput, mobileInput, emailInput, passwordInput;
    private Button signupBtn;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isShakeDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("CustomerSession", Context.MODE_PRIVATE);

        nameInput = findViewById(R.id.name_signup_EditText1);
        mobileInput = findViewById(R.id.mobile_signup_EditText1);
        emailInput = findViewById(R.id.email_signup_EditText1);
        passwordInput = findViewById(R.id.password_signup_EditText1);
        signupBtn = findViewById(R.id.signup_btn);

        TextView goToSignIn = findViewById(R.id.go_to_signin);
        goToSignIn.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            finish();
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

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
        } else if (TextUtils.isEmpty(cusEmail) || !Patterns.EMAIL_ADDRESS.matcher(cusEmail).matches()) {
            emailInput.setError("Enter a valid email address");
        } else if (TextUtils.isEmpty(cusPassword) || cusPassword.length() < 10) {
            passwordInput.setError("Password must be at least 10 characters");
        } else {
            startHumanVerification();
        }
    }

    private void startHumanVerification() {
        Toast.makeText(this, "Please shake your device to verify you are human.", Toast.LENGTH_LONG).show();

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Shake detection not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            detectShake(event);
        }

        if (isShakeDetected) {
            sensorManager.unregisterListener(this);
            completeRegistration();
        }
    }

    private void detectShake(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > 1.5) {
            isShakeDetected = true;
            Toast.makeText(this, "Shake detected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void completeRegistration() {
        String cusName = nameInput.getText().toString().trim();
        String cusMobile = mobileInput.getText().toString().trim();
        String cusEmail = emailInput.getText().toString().trim();
        String cusPassword = passwordInput.getText().toString().trim();

        Map<String, Object> customer = new HashMap<>();
        customer.put("name", cusName);
        customer.put("mobile", cusMobile);
        customer.put("email", cusEmail);
        customer.put("password", cusPassword);
        customer.put("username", null);
        customer.put("profile_img", null);
        customer.put("isFirstTime", false);

        db.collection("customer")
                .add(customer)
                .addOnSuccessListener(documentReference -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isFirstTimeUser", false);
                    editor.putString("customerEmail", cusEmail);
                    editor.apply();

                    Toast.makeText(SignUpActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(SignUpActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
}