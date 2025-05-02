package edu.uga.cs.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Splash screen activity that handles initial app navigation.
 * Checks authentication status and provides entry points to:
 * - Login for existing users
 * - Signup for new users
 * - Help documentation
 * - Main app interface for authenticated users
 */
public class SplashActivity extends AppCompatActivity {

    // Configuration constants
    private static final int SPLASH_DELAY = 2000; // 2 second delay (currently unused)
    private static final String TAG = "SplashActivity"; // Logging tag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.d(TAG, "SplashActivity started");

        // Authentication check pipeline
        if (isAuthenticated()) {
            handleAuthenticatedUser();
        } else {
            handleUnauthenticatedUser();
        }

        Log.d(TAG, "onCreate completed");
    }

    /**
     * Handles navigation for authenticated users
     * Immediately redirects to MainActivity and finishes splash screen
     */
    private void handleAuthenticatedUser() {
        Log.d(TAG, "User is authenticated, redirecting to MainActivity");
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Configures UI for unauthenticated users
     * Sets up navigation buttons and maintains splash screen visibility
     */
    private void handleUnauthenticatedUser() {
        Log.d(TAG, "User is not authenticated, initializing button controls");

        // Initialize UI components
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        Button btnHelp = findViewById(R.id.btnHelp);

        // Set navigation handlers
        btnLogin.setOnClickListener(v -> navigateTo(LoginActivity.class));
        btnSignUp.setOnClickListener(v -> navigateTo(SignUpActivity.class));
        btnHelp.setOnClickListener(v -> navigateTo(HelpActivity.class));
    }

    /**
     * Generic navigation method for button clicks
     * @param targetActivity The class of the activity to launch
     */
    private void navigateTo(Class<?> targetActivity) {
        Log.d(TAG, "Navigating to: " + targetActivity.getSimpleName());
        startActivity(new Intent(this, targetActivity));
    }

    /**
     * Authentication status check (placeholder implementation)
     * @return true if user is logged in, false otherwise
     * @note Replace with actual authentication check (e.g., Firebase Auth)
     */
    private boolean isAuthenticated() {
        // Temporary implementation using SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isLoggedIn", false);
    }

    // Activity lifecycle tracking methods
    // These help monitor the activity's state transitions during debugging

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
    }
}