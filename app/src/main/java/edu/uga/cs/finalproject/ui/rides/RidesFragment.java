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
 * Fragment for managing accepted rides that the current user is involved in.
 * Displays rides in chronological order (soonest first) and handles ride completion confirmation.
 * Provides functionality for points adjustment when both parties confirm completion.
 */
public class RidesFragment extends Fragment {

    /** Firebase Authentication instance for user verification */
    private FirebaseAuth mAuth;

    /** Reference to the Firebase Realtime Database rides node */
    private DatabaseReference dbRef;

    /** Adapter for the RecyclerView of accepted rides */
    private RideAdapter adapter;

    /** RecyclerView to display the list of accepted rides */
    private RecyclerView recyclerView;

    /** Current user's email address for ride association checks */
    private String currentUserEmail;

    /**
     * Creates and configures the fragment's view hierarchy
     * @param inflater Layout inflater service
     * @param container Parent view group
     * @param savedInstanceState Saved instance state
     * @return Configured view for the fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_accepted_rides, container, false);

        // Initialize UI components and Firebase references
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("rides");
        currentUserEmail = mAuth.getCurrentUser().getEmail();

        // Configure Firebase query for future rides ordered chronologically
        Query query = dbRef.orderByChild("timestamp")
                .startAt(System.currentTimeMillis());

        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(query, Ride.class)
                .build();

        // Click listener for ride confirmation actions
        RideAdapter.OnRideClickListener listener = (ride, key) -> {
            if (ride.getStatus() == null || !ride.getStatus().equals("completed")) {
                confirmRideCompletion(ride, key);
            } else {
                Toast.makeText(getContext(), "This ride is already completed.", Toast.LENGTH_SHORT).show();
            }
        };

        // Configure adapter with custom filtering logic
        adapter = new RideAdapter(options, listener, "Confirm Ride") {
            /**
             * Binds ride data to view holder with user-specific filtering
             * @param holder ViewHolder to populate
             * @param position Item position in list
             * @param model Ride data object
             */
            @Override
            protected void onBindViewHolder(@NonNull RideAdapter.RideViewHolder holder, int position, @NonNull Ride model) {
                boolean isUserRide = currentUserEmail.equals(model.getDriverEmail()) ||
                        currentUserEmail.equals(model.getRiderEmail());

                if ("accepted".equals(model.getStatus()) && isUserRide) {
                    super.onBindViewHolder(holder, position, model);
                } else {
                    // Hide non-relevant rides from view
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    /**
     * Starts Firebase adapter data listening when fragment becomes visible
     */
    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    /**
     * Stops Firebase adapter data listening when fragment loses visibility
     */
    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    /**
     * Handles ride completion confirmation workflow
     * @param ride Ride object being confirmed
     * @param key Firebase database key for the ride
     */
    private void confirmRideCompletion(Ride ride, String key) {
        String currentUserEmail = mAuth.getCurrentUser().getEmail();
        Map<String, Object> updates = new HashMap<>();

        // Determine user's role in the ride
        if (currentUserEmail.equals(ride.getDriverEmail())) {
            updates.put("driverConfirmed", true);
        } else if (currentUserEmail.equals(ride.getRiderEmail())) {
            updates.put("riderConfirmed", true);
        } else {
            Toast.makeText(getContext(), "You are not part of this ride.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update confirmation status in database
        dbRef.child(key).updateChildren(updates).addOnSuccessListener(unused -> {
            dbRef.child(key).get().addOnSuccessListener(snapshot -> {
                Ride updatedRide = snapshot.getValue(Ride.class);
                if (updatedRide != null && updatedRide.isDriverConfirmed() && updatedRide.isRiderConfirmed()) {
                    // Finalize ride completion when both parties confirm
                    Map<String, Object> finalUpdates = new HashMap<>();
                    finalUpdates.put("isConfirmed", true);
                    finalUpdates.put("status", "completed");
                    finalUpdates.put("completedAt", System.currentTimeMillis());

                    dbRef.child(key).updateChildren(finalUpdates).addOnSuccessListener(unused2 -> {
                        Toast.makeText(getContext(), "Ride fully completed!", Toast.LENGTH_SHORT).show();
                        adjustPoints(updatedRide);
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
     * Adjusts points balance for ride participants
     * @param ride Completed ride object containing participant information
     */
    private void adjustPoints(Ride ride) {
        if (ride.getRiderEmail() == null || ride.getDriverEmail() == null) return;

        // Sanitize emails for Firebase key format
        String riderKey = sanitizeEmail(ride.getRiderEmail());
        String driverKey = sanitizeEmail(ride.getDriverEmail());

        // Update rider's points (deduct 50)
        DatabaseReference riderPointsRef = FirebaseDatabase.getInstance().getReference("users").child(riderKey).child("ridePoints");
        riderPointsRef.get().addOnSuccessListener(snapshot -> {
            Long riderPoints = snapshot.getValue(Long.class);
            if (riderPoints == null) riderPoints = 0L;
            long newPoints = Math.max(0, riderPoints - 50);
            riderPointsRef.setValue(newPoints);
        });

        // Update driver's points (add 50)
        DatabaseReference driverPointsRef = FirebaseDatabase.getInstance().getReference("users").child(driverKey).child("ridePoints");
        driverPointsRef.get().addOnSuccessListener(snapshot -> {
            Long driverPoints = snapshot.getValue(Long.class);
            if (driverPoints == null) driverPoints = 0L;
            driverPointsRef.setValue(driverPoints + 50);
        });
    }

    /**
     * Converts email address to Firebase-compatible key format
     * @param email Original email address
     * @return Sanitized email string with periods replaced by commas
     */
    private String sanitizeEmail(String email) {
        return email.replace(".", ",");
    }
}