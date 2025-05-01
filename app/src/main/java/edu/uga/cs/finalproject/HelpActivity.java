package edu.uga.cs.finalproject;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * This is the view for the help page.
 */
public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        TextView helpText = findViewById(R.id.help_content);
        helpText.setText(
                "Juum Help Center\n\n" +
                        "1. Sign Up: Create an account using your student email\n" +
                        "2. Request Rides: Post ride requests with your destination\n" +
                        "3. Offer Rides: Post ride offers with your destination\n" +
                        "4. Earn Points: Give rides to earn 50 ride-points.\n" +
                        "5. Redeem Points: Use 50 points to request rides from others\n\n" +
                        "Contact: support@juum.app"
        );
    }
}
