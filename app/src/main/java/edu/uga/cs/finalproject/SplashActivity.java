package edu.uga.cs.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private static final String TAG = "SplashActivity"; // Tag for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d(TAG, "SplashActivity started"); // Log when activity starts

        // Check if user is already authenticated
        if (isAuthenticated()) {
            // If authenticated, redirect to MainActivity
            Log.d(TAG, "User is authenticated, redirecting to MainActivity");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // If not authenticated, stay on SplashActivity or navigate to login/signup
            Log.d(TAG, "User is not authenticated, staying on SplashActivity");
            // Optional: Remove the auto-redirect delay if you want the buttons to be the only way to proceed
            // new Handler().postDelayed(() -> {}, SPLASH_DELAY);

            // Keep your button logic
            Button btnLogin = findViewById(R.id.btnLogin);
            Button btnSignUp = findViewById(R.id.btnSignUp);
            Button btnHelp = findViewById(R.id.btnHelp);

            btnLogin.setOnClickListener(v -> {
                Log.d(TAG, "Login button clicked");
                startActivity(new Intent(this, LoginActivity.class));
            });

            btnSignUp.setOnClickListener(v -> {
                Log.d(TAG, "Sign Up button clicked");
                startActivity(new Intent(this, SignUpActivity.class));
            });

            btnHelp.setOnClickListener(v -> {
                Log.d(TAG, "Help button clicked");
                startActivity(new Intent(this, HelpActivity.class));
            });
        }

        Log.d(TAG, "onCreate completed"); // Log when onCreate is finished
    }

    // Method to check if user is authenticated (replace with your actual authentication check)
    private boolean isAuthenticated() {
        // Example using SharedPreferences:
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isLoggedIn", false); // Return true if user is logged in
        // Replace with your actual authentication check logic (Firebase, etc.)
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called"); // Log when onStart is called
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called"); // Log when onResume is called
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called"); // Log when onPause is called
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called"); // Log when onStop is called
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called"); // Log when onDestroy is called
    }
}
