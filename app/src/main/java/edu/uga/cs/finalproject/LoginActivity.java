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


/**
 * Login page and logic. Creates an instance of the firebase to connect the database.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.email_input);
        etPassword = findViewById(R.id.password_input);
        Button btnLogin = findViewById(R.id.login_button);
        Button cancelBtn = findViewById(R.id.cancel_button);

        /**
         * If the user presses cancel, it'll go back to splash screen.
         */
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send the user back to the home screen or close the login screen
                Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
                startActivity(intent);
                finish(); // Optional: closes LoginActivity so user can't return with back button
            }
        });

        /**
         * Takes the user's input and checks for errors. Also checks if the user is an actual user in the database.
         * Shows appropriate toast messages for errors or if it is a successful login.
         */
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Basic validation
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInWithEmailSuccessful");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(LoginActivity.this, "Welcome Back, " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

}
