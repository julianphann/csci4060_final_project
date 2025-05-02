package edu.uga.cs.finalproject.ui.rides;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import edu.uga.cs.finalproject.R;

/**
 * Fragment for posting new ride offers or requests to the Firebase database
 * Handles date/time selection and ride type (offer/request) selection
 */
public class PostRideFragment extends Fragment {

    // Firebase Authentication instance
    private FirebaseAuth mAuth;
    // Reference to Firebase Realtime Database
    private DatabaseReference dbRef;

    // UI components for ride details input
    private EditText pickupEditText, destinationEditText, dateTimePicker;
    // Radio group for selecting ride type (offer/request)
    private RadioGroup radioGroup;
    // Button to submit ride posting
    private Button postRideButton;

    // Calendar instance to store selected date/time
    private Calendar selectedDateTime;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_ride, container, false);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("rides");
        selectedDateTime = Calendar.getInstance();  // Set initial time to current datetime

        // Bind UI components from layout
        pickupEditText = rootView.findViewById(R.id.pickupEditText);
        destinationEditText = rootView.findViewById(R.id.destinationEditText);
        dateTimePicker = rootView.findViewById(R.id.dateTimePicker);
        radioGroup = rootView.findViewById(R.id.radioGroup);
        postRideButton = rootView.findViewById(R.id.postRideButton);

        // Date/Time Picker setup - make it clickable but not directly editable
        dateTimePicker.setFocusable(false);
        dateTimePicker.setClickable(true);
        dateTimePicker.setOnClickListener(v -> showDateTimePicker());

        // Set click listener for ride submission button
        postRideButton.setOnClickListener(v -> postRide());

        return rootView;
    }

    /**
     * Displays a combined date/time picker dialog
     * Restricts date selection to future dates only
     */
    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();

        // Create date picker dialog with current date as default
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    // Set selected date components
                    selectedDateTime.set(year, month, dayOfMonth);

                    // Show time picker after date selection
                    new TimePickerDialog(
                            requireContext(),
                            (timeView, hourOfDay, minute) -> {
                                // Set selected time components
                                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedDateTime.set(Calendar.MINUTE, minute);

                                // Format and display selected datetime
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
                                dateTimePicker.setText(sdf.format(selectedDateTime.getTime()));
                            },
                            currentDate.get(Calendar.HOUR_OF_DAY),  // Initial hour
                            currentDate.get(Calendar.MINUTE),       // Initial minute
                            false                                  // 24-hour format
                    ).show();
                },
                currentDate.get(Calendar.YEAR),     // Initial year
                currentDate.get(Calendar.MONTH),    // Initial month
                currentDate.get(Calendar.DAY_OF_MONTH)  // Initial day
        );

        // Restrict date picker to future dates only
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    /**
     * Validates inputs and posts ride to Firebase database
     * Contains business logic for ride creation
     */
    private void postRide() {
        String userEmail = mAuth.getCurrentUser().getEmail();
        int selectedId = radioGroup.getCheckedRadioButtonId();

        // Get input values from form fields
        String pickup = pickupEditText.getText().toString().trim();
        String destination = destinationEditText.getText().toString().trim();

        // Validate required fields
        if (pickup.isEmpty() || destination.isEmpty() || selectedId == -1) {
            Toast.makeText(getContext(), "Please fill in all fields and select offer/request.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dateTimePicker.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please select a date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine ride type from radio group selection
        String type = selectedId == R.id.radio_offer ? "offer" : "request";

        // Create ride data structure for Firebase
        Map<String, Object> ride = new HashMap<>();
        ride.put("email", userEmail);
        ride.put("type", type);
        ride.put("pickup", pickup);
        ride.put("destination", destination);
        ride.put("timestamp", selectedDateTime.getTimeInMillis());  // Store as timestamp for sorting
        ride.put("status", "pending");  // Initial status
        ride.put("riderConfirmed", false);  // Confirmation flags
        ride.put("driverConfirmed", false);

        // Push new ride to Firebase database
        dbRef.push().setValue(ride)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Ride posted successfully!", Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to post ride: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Resets form fields to their initial state
     * Clears input values and resets datetime picker
     */
    private void clearForm() {
        pickupEditText.setText("");
        destinationEditText.setText("");
        dateTimePicker.setText("");
        radioGroup.clearCheck();
        selectedDateTime = Calendar.getInstance(); // Reset to current time
    }
}