package edu.uga.cs.finalproject.ui.rides;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.uga.cs.finalproject.R;
import edu.uga.cs.finalproject.RideAdapter;
import edu.uga.cs.finalproject.Ride;

/**
 * Fragment for managing user's rides - both created and accepted.
 * Provides functionality to view, edit, and delete rides.
 * Uses FirebaseRecyclerAdapter to display rides in real-time.
 */
public class MyRidesFragment extends Fragment {

    // Firebase components
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private RideAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_rides, container, false);

        // Initialize UI components and Firebase references
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("rides");

        // Get current user's email for ride filtering
        String userEmail = mAuth.getCurrentUser().getEmail();

        // Configure FirebaseRecyclerOptions to load all rides
        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(dbRef, Ride.class)
                .build();

        // Click listener for ride items - only allows editing of user's own rides
        RideAdapter.OnRideClickListener listener = (ride, key) -> {
            if (ride.getEmail() != null && ride.getEmail().equals(userEmail)) {
                editRideDialog(ride, key);
            }
        };

        // Custom adapter with filtering logic
        adapter = new RideAdapter(options, listener, "Edit Ride") {
            @Override
            protected void onBindViewHolder(@NonNull RideViewHolder holder, int position, @NonNull Ride model) {
                String email = mAuth.getCurrentUser().getEmail();

                // Determine if ride should be displayed based on user's involvement
                boolean isPostedByUser = model.getEmail() != null && model.getEmail().equals(email);
                boolean isAcceptedByUser = (model.getRiderEmail() != null && model.getRiderEmail().equals(email)) ||
                        (model.getDriverEmail() != null && model.getDriverEmail().equals(email));

                if (isPostedByUser || isAcceptedByUser) {
                    super.onBindViewHolder(holder, position, model);
                } else {
                    // Hide unrelated rides by collapsing their layout space
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            }
        };

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening(); // Begin listening for Firebase data changes
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening(); // Stop Firebase data updates when fragment is inactive
    }

    /**
     * Shows combined date and time picker dialog
     * @param inputDateTime EditText field to update with selected datetime
     */
    private void showDateTimePicker(EditText inputDateTime) {
        final Calendar calendar = Calendar.getInstance();

        // Date picker configuration
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Time picker shown after date selection
                    TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                // Format selected datetime and update input field
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                inputDateTime.setText(sdf.format(calendar.getTime()));
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    /**
     * Displays ride editing dialog with validation
     * @param ride Ride object to edit
     * @param key Firebase database key for the ride
     */
    private void editRideDialog(Ride ride, String key) {
        // Prevent editing of accepted rides
        if ("accepted".equalsIgnoreCase(ride.getStatus())) {
            Toast.makeText(requireContext(), "Cannot edit accepted rides", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Ride");

        // Dialog layout setup
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Create input fields with current values
        final EditText inputDestination = createInputField("Destination", ride.getDestination());
        final EditText inputPickup = createInputField("Pickup", ride.getPickup());
        final EditText inputDateTime = createDateTimeField(ride.getDateTime());

        // Add fields to layout
        layout.addView(inputDestination);
        layout.addView(inputPickup);
        layout.addView(inputDateTime);

        builder.setView(layout);

        // Dialog button configuration
        builder.setNeutralButton("Delete Ride", (dialog, which) -> deleteRide(key));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setPositiveButton("Save", null); // Listener set later to prevent auto-dismiss

        AlertDialog dialog = builder.create();
        dialog.show();

        // Custom save button handling
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> handleSaveAction(
                key,
                inputDestination.getText().toString().trim(),
                inputPickup.getText().toString().trim(),
                inputDateTime.getText().toString().trim(),
                dialog
        ));
    }

    /**
     * Creates a standard input field with hint and initial value
     */
    private EditText createInputField(String hint, String initialValue) {
        EditText field = new EditText(requireContext());
        field.setHint(hint);
        field.setText(initialValue);
        return field;
    }

    /**
     * Creates datetime input field with picker interaction
     */
    private EditText createDateTimeField(String initialValue) {
        EditText field = new EditText(requireContext());
        field.setHint("Date/Time");
        field.setText(initialValue);
        field.setFocusable(false);
        field.setOnClickListener(v -> showDateTimePicker(field));
        return field;
    }

    /**
     * Handles ride deletion in Firebase
     * @param key Firebase database key of the ride to delete
     */
    private void deleteRide(String key) {
        dbRef.child(key).removeValue()
                .addOnSuccessListener(unused ->
                        Toast.makeText(requireContext(), "Ride deleted", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Validates and saves updated ride information
     */
    private void handleSaveAction(String key, String newDestination,
                                  String newPickup, String newDateTime,
                                  AlertDialog dialog) {
        // Update changed fields in Firebase
        if (!newDestination.isEmpty()) {
            dbRef.child(key).child("destination").setValue(newDestination);
        }
        if (!newPickup.isEmpty()) {
            dbRef.child(key).child("pickup").setValue(newPickup);
        }
        if (!newDateTime.isEmpty()) {
            updateDateTimeFields(key, newDateTime);
        }

        dialog.dismiss();
        Toast.makeText(requireContext(), "Ride updated!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Updates both datetime string and timestamp in Firebase
     */
    private void updateDateTimeFields(String key, String newDateTime) {
        dbRef.child(key).child("datetime").setValue(newDateTime);
        try {
            // Convert datetime string to timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            long newTimestamp = sdf.parse(newDateTime).getTime();
            dbRef.child(key).child("timestamp").setValue(newTimestamp);
        } catch (Exception e) {
            Log.e("DateTimeUpdate", "Error parsing datetime", e);
        }
    }
}