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

public class RidesFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private RideAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_accepted_rides, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("rides");

        String userEmail = mAuth.getCurrentUser().getEmail();

        // 🔥 Query: Order by timestamp (soonest to latest)
        Query query = dbRef.orderByChild("timestamp");

        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(query, Ride.class)
                .build();

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

        adapter = new RideAdapter(options, listener, "Confirm Ride");

        // 🔥 LayoutManager to show soonest → latest (no reverse needed)
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    private void confirmRideCompletion(Ride ride, String key) {
        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        Map<String, Object> updates = new HashMap<>();

        if (currentUserEmail.equals(ride.getDriverEmail())) {
            updates.put("driverConfirmed", true);
        } else if (currentUserEmail.equals(ride.getRiderEmail())) {
            updates.put("riderConfirmed", true);
        } else {
            Toast.makeText(getContext(), "You are not part of this ride.", Toast.LENGTH_SHORT).show();
            return;
        }

        dbRef.child(key).updateChildren(updates).addOnSuccessListener(unused -> {
            dbRef.child(key).get().addOnSuccessListener(snapshot -> {
                Ride updatedRide = snapshot.getValue(Ride.class);
                if (updatedRide != null && updatedRide.isDriverConfirmed() && updatedRide.isRiderConfirmed()) {
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

    private void adjustPoints(Ride ride) {
        if (ride.getRiderEmail() == null || ride.getDriverEmail() == null) return;

        String riderKey = sanitizeEmail(ride.getRiderEmail());
        String driverKey = sanitizeEmail(ride.getDriverEmail());

        DatabaseReference riderPointsRef = FirebaseDatabase.getInstance().getReference("users").child(riderKey).child("ridePoints");
        DatabaseReference driverPointsRef = FirebaseDatabase.getInstance().getReference("users").child(driverKey).child("ridePoints");

        riderPointsRef.get().addOnSuccessListener(snapshot -> {
            Long riderPoints = snapshot.getValue(Long.class);
            if (riderPoints == null) riderPoints = 0L;
            long newPoints = Math.max(0, riderPoints - 50);
            riderPointsRef.setValue(newPoints);
        });

        driverPointsRef.get().addOnSuccessListener(snapshot -> {
            Long driverPoints = snapshot.getValue(Long.class);
            if (driverPoints == null) driverPoints = 0L;
            driverPointsRef.setValue(driverPoints + 50);
        });
    }

    private String sanitizeEmail(String email) {
        return email.replace(".", ",");
    }
}
