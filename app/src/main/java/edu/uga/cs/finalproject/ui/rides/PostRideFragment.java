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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_ride, container, false);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("rides");

        pickupEditText = rootView.findViewById(R.id.pickupEditText);
        destinationEditText = rootView.findViewById(R.id.destinationEditText);
        dateTimePicker = rootView.findViewById(R.id.dateTimePicker);
        radioGroup = rootView.findViewById(R.id.radioGroup);
        postRideButton = rootView.findViewById(R.id.postRideButton);

        // Show date and time picker when clicked
        dateTimePicker.setFocusable(false);
        dateTimePicker.setClickable(true);
        dateTimePicker.setOnClickListener(v -> showDateTimePicker());

        postRideButton.setOnClickListener(v -> postRide());

        return rootView;
    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            requireContext(),
                            (TimePicker timeView, int hourOfDay, int minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                dateTimePicker.setText(sdf.format(calendar.getTime()));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                    );

                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Optional: Only allow dates from today onward
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void postRide() {
        String userEmail = mAuth.getCurrentUser().getEmail();
        int selectedId = radioGroup.getCheckedRadioButtonId();

        String pickup = pickupEditText.getText().toString().trim();
        String destination = destinationEditText.getText().toString().trim();
        String datetime = dateTimePicker.getText().toString().trim();

        if (pickup.isEmpty() || destination.isEmpty() || datetime.isEmpty() || selectedId == -1) {
            Toast.makeText(getContext(), "Please fill in all fields and select offer/request.", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = selectedId == R.id.radio_offer ? "offer" : "request";

        Map<String, Object> ride = new HashMap<>();
        ride.put("email", userEmail);
        ride.put("type", type);
        ride.put("pickup", pickup);
        ride.put("destination", destination);
        ride.put("datetime", datetime);  // this will not be null now
        ride.put("status", "pending");

        dbRef.push().setValue(ride).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Ride posted successfully!", Toast.LENGTH_SHORT).show();

            pickupEditText.setText("");
            destinationEditText.setText("");
            dateTimePicker.setText("");
            radioGroup.clearCheck();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to post ride: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

}
