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

public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.email_signup);
        etPassword = findViewById(R.id.password_signup);
        etConfirmPassword = findViewById(R.id.confirm_password_signup);
        Button btnSignUp = findViewById(R.id.signup_button);
        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send the user back to the home screen or close the login screen
                Intent intent = new Intent(SignUpActivity.this, SplashActivity.class);
                startActivity(intent);
                finish(); // Optional: closes LoginActivity so user can't return with back button
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignUpActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                } else {
                    // Replace with actual signup logic
                    registerUser(email, password);
//                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
//                    finish();
                }
            }
        });
    }
    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userEmail = firebaseUser.getEmail();
                            String userKey = userEmail.replace(".", ",");

                            // Create a new User object with 50 points
                            User newUser = new User(userEmail, 50); // Default 50 points

                            // Log for debugging
                            Log.d("SignUpActivity", "Creating user with email: " + userEmail);

                            // Save to database
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(userKey)
                                    .setValue(newUser)
                                    .addOnSuccessListener(unused -> {
                                        Log.d("SignUpActivity", "User saved to database with 50 points");

                                        // Once the user is saved, move to MainActivity or relevant screen
                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("SignUpActivity", "Failed to save user: " + e.getMessage());
                                        Toast.makeText(SignUpActivity.this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }
                    } else {
                        Log.e("SignUpActivity", "Registration failed: " + task.getException().getMessage());
                        Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Helper to sanitize email
    private String sanitizeEmail(String email) {
        return email.replace(".", ",");
    }

}
