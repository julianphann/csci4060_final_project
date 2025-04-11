package edu.uga.cs.finalproject;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        TextView helpText = findViewById(R.id.help_content);
        helpText.setText(
                "Juum Help Center\n\n" +
                        "1. **Sign Up**: Create an account using your student email\n" +
                        "2. **Request Rides**: Post ride requests with your destination\n" +
                        "3. **Earn Points**: Give rides to earn ride-points\n" +
                        "4. **Redeem Points**: Use points to request rides from others\n\n" +
                        "Contact: support@juum.app"
        );
    }
}
