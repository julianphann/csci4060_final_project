package edu.uga.cs.finalproject.ui.rides;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import edu.uga.cs.finalproject.R;

public class PostRideFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private EditText pickupEditText, destinationEditText, dateTimePicker;
    private RadioGroup radioGroup;
    private Button postRideButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout without DataBinding
        View rootView = inflater.inflate(R.layout.fragment_post_ride, container, false);

        // Initialize Views using findViewById
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("rides");

        pickupEditText = rootView.findViewById(R.id.pickupEditText);
        destinationEditText = rootView.findViewById(R.id.destinationEditText);
        dateTimePicker = rootView.findViewById(R.id.dateTimePicker);
        radioGroup = rootView.findViewById(R.id.radioGroup);
        postRideButton = rootView.findViewById(R.id.postRideButton);

        // Set the button click listener
        postRideButton.setOnClickListener(v -> postRide());

        return rootView;
    }

    private void postRide() {
        String userEmail = mAuth.getCurrentUser().getEmail();
        String type = radioGroup.getCheckedRadioButtonId() == R.id.radio_offer ? "offer" : "request";

        Map<String, Object> ride = new HashMap<>();
        ride.put("email", userEmail);
        ride.put("type", type);
        ride.put("pickup", pickupEditText.getText().toString());
        ride.put("destination", destinationEditText.getText().toString());
        ride.put("datetime", dateTimePicker.getText().toString());
        ride.put("status", "pending");

        dbRef.push().setValue(ride).addOnSuccessListener(aVoid -> {
            // Show confirmation
            Toast.makeText(getContext(), "Ride posted successfully!", Toast.LENGTH_SHORT).show();

            // Clear fields
            pickupEditText.setText("");
            destinationEditText.setText("");
            dateTimePicker.setText("");
            radioGroup.clearCheck();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to post ride: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

}
