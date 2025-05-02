package edu.uga.cs.finalproject.ui.rides;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

import edu.uga.cs.finalproject.R;
import edu.uga.cs.finalproject.RideAdapter;
import edu.uga.cs.finalproject.Ride;

/**
 * Fragment for managing accepted rides and handling ride completion confirmation
 * Displays rides that have been accepted and allows users to confirm completion
 * Handles points adjustment when both parties confirm ride completion
 */
public class RidesFragment extends Fragment {

    // Firebase authentication instance
    private FirebaseAuth mAuth;
    // Reference to Firebase database node for rides
    private DatabaseReference dbRef;
    // Adapter for RecyclerView of accepted rides
    private RideAdapter adapter;
    // RecyclerView to display accepted rides
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_accepted_rides, container, false);

        // Initialize UI components and Firebase references
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("rides");

        String userEmail = mAuth.getCurrentUser().getEmail();

        // Configure Firebase query to get accepted rides ordered by timestamp
        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(
                        FirebaseDatabase.getInstance().getReference("rides")
                                .orderByChild("status")
                                .equalTo("accepted"),  // Filter for accepted rides only
                        Ride.class
                )
                .build();

        // Click listener for ride items - handles completion confirmation
        RideAdapter.OnRideClickListener listener = new RideAdapter.OnRideClickListener() {
            @Override
            public void onRideClick(Ride ride, String key) {
                if (ride.getStatus() == null || !ride.getStatus().equals("completed")) {
                    confirmRideCompletion(ride, key);
                } else {
                    Toast.makeText(getContext(), "This ride is already completed.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Initialize adapter with custom configuration
        adapter = new RideAdapter(options, listener, "Confirm Ride");

        // Set up RecyclerView with linear layout (chronological order)
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening(); // Begin listening for Firebase data updates
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening(); // Stop Firebase updates when fragment is inactive
        }
    }

    /**
     * Handles ride completion confirmation logic
     * Updates confirmation status for driver/rider and marks ride as completed when both confirm
     * @param ride The Ride object being confirmed
     * @param key Firebase database key for the ride
     */
    private void confirmRideCompletion(Ride ride, String key) {
        String currentUserEmail = mAuth.getCurrentUser().getEmail();
        Map<String, Object> updates = new HashMap<>();

        // Determine user role and update appropriate confirmation flag
        if (currentUserEmail.equals(ride.getDriverEmail())) {
            updates.put("driverConfirmed", true);
        } else if (currentUserEmail.equals(ride.getRiderEmail())) {
            updates.put("riderConfirmed", true);
        } else {
            Toast.makeText(getContext(), "You are not part of this ride.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update confirmation status in Firebase
        dbRef.child(key).updateChildren(updates).addOnSuccessListener(unused -> {
            // Check if both parties have confirmed
            dbRef.child(key).get().addOnSuccessListener(snapshot -> {
                Ride updatedRide = snapshot.getValue(Ride.class);
                if (updatedRide != null && updatedRide.isDriverConfirmed() && updatedRide.isRiderConfirmed()) {
                    // Final updates when both confirm
                    Map<String, Object> finalUpdates = new HashMap<>();
                    finalUpdates.put("isConfirmed", true);
                    finalUpdates.put("status", "completed");
                    finalUpdates.put("completedAt", System.currentTimeMillis());

                    dbRef.child(key).updateChildren(finalUpdates).addOnSuccessListener(unused2 -> {
                        Toast.makeText(getContext(), "Ride fully completed!", Toast.LENGTH_SHORT).show();
                        adjustPoints(updatedRide); // Adjust points for both users
                    });
                } else {
                    Toast.makeText(getContext(), "Confirmation recorded. Waiting for other user.", Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to confirm: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Adjusts ride points for both rider and driver after completed ride
     * @param ride Completed Ride object containing participant emails
     */
    private void adjustPoints(Ride ride) {
        if (ride.getRiderEmail() == null || ride.getDriverEmail() == null) return;

        // Sanitize emails for Firebase key format
        String riderKey = sanitizeEmail(ride.getRiderEmail());
        String driverKey = sanitizeEmail(ride.getDriverEmail());

        // Get references to both users' points
        DatabaseReference riderPointsRef = FirebaseDatabase.getInstance().getReference("users").child(riderKey).child("ridePoints");
        DatabaseReference driverPointsRef = FirebaseDatabase.getInstance().getReference("users").child(driverKey).child("ridePoints");

        // Update rider's points (-50)
        riderPointsRef.get().addOnSuccessListener(snapshot -> {
            Long riderPoints = snapshot.getValue(Long.class);
            if (riderPoints == null) riderPoints = 0L;
            long newPoints = Math.max(0, riderPoints - 50);
            riderPointsRef.setValue(newPoints);
        });

        // Update driver's points (+50)
        driverPointsRef.get().addOnSuccessListener(snapshot -> {
            Long driverPoints = snapshot.getValue(Long.class);
            if (driverPoints == null) driverPoints = 0L;
            driverPointsRef.setValue(driverPoints + 50);
        });
    }

    /**
     * Sanitizes email addresses for Firebase key compatibility
     * Replaces '.' with ',' since Firebase keys cannot contain periods
     * @param email Original email address
     * @return Sanitized email string
     */
    private String sanitizeEmail(String email) {
        return email.replace(".", ",");
    }
}