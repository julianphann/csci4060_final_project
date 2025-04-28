package edu.uga.cs.finalproject.ui.rides;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

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
        Query query = dbRef.orderByChild("email").equalTo(userEmail);

        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(query, Ride.class)
                .build();

        RideAdapter.OnRideClickListener listener = new RideAdapter.OnRideClickListener() {
            @Override
            public void onRideClick(Ride ride, String key) {
                // When user clicks on their own ride, let them edit it
                editRideDialog(ride, key);
            }
        };

        adapter = new RideAdapter(options, listener, "Edit Ride");

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

    private void editRideDialog(Ride ride, String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Ride");

        // Create a layout for multiple input fields
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Destination input
        final EditText inputDestination = new EditText(requireContext());
        inputDestination.setHint("Destination");
        inputDestination.setText(ride.getDestination());
        layout.addView(inputDestination);

        // Pickup input
        final EditText inputPickup = new EditText(requireContext());
        inputPickup.setHint("Pickup");
        inputPickup.setText(ride.getPickup());
        layout.addView(inputPickup);

        // Date/Time input
        final EditText inputDateTime = new EditText(requireContext());
        inputDateTime.setHint("Date/Time");
        inputDateTime.setText(ride.getDateTime());
        layout.addView(inputDateTime);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            // Update ride in database
            String newDestination = inputDestination.getText().toString();
            String newPickup = inputPickup.getText().toString();
            String newDateTime = inputDateTime.getText().toString();

            if (!newDestination.isEmpty()) {
                dbRef.child(key).child("destination").setValue(newDestination);
            }
            if (!newPickup.isEmpty()) {
                dbRef.child(key).child("pickup").setValue(newPickup);
            }
            if (!newDateTime.isEmpty()) {
                dbRef.child(key).child("dateTime").setValue(newDateTime);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
