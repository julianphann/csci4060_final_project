package edu.uga.cs.finalproject.ui.rides;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
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

public class MyRidesFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private RideAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_rides, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("rides");

        String userEmail = mAuth.getCurrentUser().getEmail();

        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(dbRef, Ride.class) // Load all rides
                .build();

        RideAdapter.OnRideClickListener listener = new RideAdapter.OnRideClickListener() {
            @Override
            public void onRideClick(Ride ride, String key) {
                if (ride.getEmail() != null && ride.getEmail().equals(userEmail)) {
                    // Only allow edit if user posted this ride
                    editRideDialog(ride, key);
                }
            }
        };

        adapter = new RideAdapter(options, listener, "Edit Ride") {
            @Override
            protected void onBindViewHolder(@NonNull RideViewHolder holder, int position, @NonNull Ride model) {
                String email = mAuth.getCurrentUser().getEmail();

                boolean isPostedByUser = model.getEmail() != null && model.getEmail().equals(email);
                boolean isAcceptedByUser = (model.getRiderEmail() != null && model.getRiderEmail().equals(email)) ||
                        (model.getDriverEmail() != null && model.getDriverEmail().equals(email));

                if (isPostedByUser || isAcceptedByUser) {
                    super.onBindViewHolder(holder, position, model);
                } else {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void showDateTimePicker(EditText inputDateTime) {
        final Calendar calendar = Calendar.getInstance();

        // First, show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // After picking date, show TimePickerDialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                // Format selected date and time
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                String selectedDateTime = sdf.format(calendar.getTime());

                                inputDateTime.setText(selectedDateTime);
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }


    private void editRideDialog(Ride ride, String key) {
        // Prevent editing if status is accepted
        if ("accepted".equalsIgnoreCase(ride.getStatus())) {
            Toast.makeText(requireContext(), "Cannot edit a ride that has been accepted.", Toast.LENGTH_SHORT).show();
            return; // Do not open the edit dialog
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Ride");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputDestination = new EditText(requireContext());
        inputDestination.setHint("Destination");
        inputDestination.setText(ride.getDestination());
        layout.addView(inputDestination);

        final EditText inputPickup = new EditText(requireContext());
        inputPickup.setHint("Pickup");
        inputPickup.setText(ride.getPickup());
        layout.addView(inputPickup);

        final EditText inputDateTime = new EditText(requireContext());
        inputDateTime.setHint("Date/Time");
        inputDateTime.setText(ride.getDateTime());
        inputDateTime.setFocusable(false);
        inputDateTime.setClickable(true);
        inputDateTime.setOnClickListener(v -> showDateTimePicker(inputDateTime));
        layout.addView(inputDateTime);

        builder.setView(layout);

        builder.setNeutralButton("Delete Ride", (dialog, which) -> {
            dbRef.child(key).removeValue();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.setPositiveButton("Save", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newDestination = inputDestination.getText().toString().trim();
            String newPickup = inputPickup.getText().toString().trim();
            String newDateTime = inputDateTime.getText().toString().trim();

            if (!newDestination.isEmpty()) {
                dbRef.child(key).child("destination").setValue(newDestination);
            }
            if (!newPickup.isEmpty()) {
                dbRef.child(key).child("pickup").setValue(newPickup);
            }
            if (!newDateTime.isEmpty()) {
                dbRef.child(key).child("datetime").setValue(newDateTime);

                // **NEW: Also update the timestamp**
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    long newTimestamp = sdf.parse(newDateTime).getTime();
                    dbRef.child(key).child("timestamp").setValue(newTimestamp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            dialog.dismiss();
            Toast.makeText(requireContext(), "Ride updated successfully!", Toast.LENGTH_SHORT).show();
        });

    }
}