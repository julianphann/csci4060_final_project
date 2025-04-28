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

public class PostRideFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private EditText pickupEditText, destinationEditText, dateTimePicker;
    private RadioGroup radioGroup;
    private Button postRideButton;
    private Calendar selectedDateTime;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_ride, container, false);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("rides");
        selectedDateTime = Calendar.getInstance();

        pickupEditText = rootView.findViewById(R.id.pickupEditText);
        destinationEditText = rootView.findViewById(R.id.destinationEditText);
        dateTimePicker = rootView.findViewById(R.id.dateTimePicker);
        radioGroup = rootView.findViewById(R.id.radioGroup);
        postRideButton = rootView.findViewById(R.id.postRideButton);

        // Date/Time Picker setup
        dateTimePicker.setFocusable(false);
        dateTimePicker.setClickable(true);
        dateTimePicker.setOnClickListener(v -> showDateTimePicker());

        postRideButton.setOnClickListener(v -> postRide());

        return rootView;
    }

    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(year, month, dayOfMonth);

                    new TimePickerDialog(
                            requireContext(),
                            (timeView, hourOfDay, minute) -> {
                                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedDateTime.set(Calendar.MINUTE, minute);

                                // Update display format
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
                                dateTimePicker.setText(sdf.format(selectedDateTime.getTime()));
                            },
                            currentDate.get(Calendar.HOUR_OF_DAY),
                            currentDate.get(Calendar.MINUTE),
                            false
                    ).show();
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void postRide() {
        String userEmail = mAuth.getCurrentUser().getEmail();
        int selectedId = radioGroup.getCheckedRadioButtonId();

        String pickup = pickupEditText.getText().toString().trim();
        String destination = destinationEditText.getText().toString().trim();

        if (pickup.isEmpty() || destination.isEmpty() || selectedId == -1) {
            Toast.makeText(getContext(), "Please fill in all fields and select offer/request.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dateTimePicker.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please select a date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = selectedId == R.id.radio_offer ? "offer" : "request";

        Map<String, Object> ride = new HashMap<>();
        ride.put("email", userEmail);
        ride.put("type", type);
        ride.put("pickup", pickup);
        ride.put("destination", destination);
        ride.put("timestamp", selectedDateTime.getTimeInMillis());
        ride.put("status", "pending");
        ride.put("riderConfirmed", false);
        ride.put("driverConfirmed", false);

        dbRef.push().setValue(ride)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Ride posted successfully!", Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to post ride: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void clearForm() {
        pickupEditText.setText("");
        destinationEditText.setText("");
        dateTimePicker.setText("");
        radioGroup.clearCheck();
        selectedDateTime = Calendar.getInstance(); // Reset to current time
    }
}