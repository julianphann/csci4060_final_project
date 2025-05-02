package edu.uga.cs.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Activity handling user registration process with Firebase Authentication.
 * Creates new user accounts and initializes user data in Realtime Database.
 */
public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private FirebaseAuth mAuth;  // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth and UI components
        mAuth = FirebaseAuth.getInstance();
        initializeUIComponents();
    }

    /**
     * Sets up all UI elements and their click listeners
     */
    private void initializeUIComponents() {
        etEmail = findViewById(R.id.email_signup);
        etPassword = findViewById(R.id.password_signup);
        etConfirmPassword = findViewById(R.id.confirm_password_signup);

        // Configure cancel button to return to splash screen
        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SplashActivity.class);
            startActivity(intent);
            finish();  // Prevent returning to this activity with back button
        });

        // Set up sign-up button with validation logic
        Button btnSignUp = findViewById(R.id.signup_button);
        btnSignUp.setOnClickListener(v -> attemptRegistration());
    }

    /**
     * Validates input fields and initiates user registration process
     */
    private void attemptRegistration() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (fieldsAreInvalid(email, password, confirmPassword)) return;

        registerUser(email, password);
    }

    /**
     * Validates registration form inputs
     * @return true if validation fails, false if inputs are valid
     */
    private boolean fieldsAreInvalid(String email, String password, String confirmPassword) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Please fill all fields");
            return true;
        }
        if (!password.equals(confirmPassword)) {
            showToast("Passwords don't match");
            return true;
        }
        return false;
    }

    /**
     * Handles Firebase user creation and database initialization
     * @param email User's email address
     * @param password User's password
     */
    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            initializeUserInDatabase(firebaseUser);
                        }
                    } else {
                        handleRegistrationFailure(task.getException());
                    }
                });
    }

    /**
     * Creates user record in Realtime Database with initial data
     * @param firebaseUser Authenticated Firebase user object
     */
    private void initializeUserInDatabase(FirebaseUser firebaseUser) {
        String userEmail = firebaseUser.getEmail();
        String userKey = sanitizeEmail(userEmail);

        // Create new user with default 50 points
        User newUser = new User(userEmail, 50);
        Log.d("SignUpActivity", "Creating user: " + userEmail);

        FirebaseDatabase.getInstance().getReference("users")
                .child(userKey)
                .setValue(newUser)
                .addOnSuccessListener(unused -> navigateToMain())
                .addOnFailureListener(e -> handleDatabaseError(e));
    }

    /**
     * Navigates to MainActivity after successful registration
     */
    private void navigateToMain() {
        Log.d("SignUpActivity", "User saved successfully");
        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
        finish();  // Close sign-up activity
    }

    /**
     * Handles database operation errors
     * @param exception Exception thrown during database operation
     */
    private void handleDatabaseError(Exception exception) {
        Log.e("SignUpActivity", "Database error: " + exception.getMessage());
        showToast("Failed to save user: " + exception.getMessage());
    }

    /**
     * Handles authentication failures
     * @param exception Exception thrown during registration
     */
    private void handleRegistrationFailure(Exception exception) {
        Log.e("SignUpActivity", "Registration failed: " + exception.getMessage());
        showToast("Registration failed: " + exception.getMessage());
    }

    /**
     * Utility method for displaying toast messages
     * @param message Message to display
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Sanitizes email address for use as Firebase key
     * Replaces '.' with ',' since Firebase doesn't allow '.' in keys
     * @param email Raw email address
     * @return Sanitized email string
     */
    private String sanitizeEmail(String email) {
        return email.replace(".", ",");
    }
}