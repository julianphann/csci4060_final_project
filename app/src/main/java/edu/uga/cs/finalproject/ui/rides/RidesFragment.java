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
        // Inflate the layout without DataBinding
        View rootView = inflater.inflate(R.layout.fragment_accepted_rides, container, false);

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("rides");

        // Query for accepted rides for the current user (either as a rider or a driver)
        String userEmail = mAuth.getCurrentUser().getEmail();
        Query query = dbRef.orderByChild("acceptedBy").equalTo(userEmail);  // filter rides accepted by this user

        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(query, Ride.class)
                .build();

        // Implement OnRideClickListener
        RideAdapter.OnRideClickListener listener = new RideAdapter.OnRideClickListener() {
            @Override
            public void onRideClick(Ride ride, String key) {
                // Handle the click event for the accepted ride (e.g., confirm ride completion)
                confirmRideCompletion(ride, key);
            }
        };

        // Pass the listener to the adapter
        adapter = new RideAdapter(options, listener);

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

    private void confirmRideCompletion(Ride ride, String key) {
        // Logic to confirm ride completion, adjust points, and remove the ride
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "completed");
        updates.put("completedAt", System.currentTimeMillis()); // Record the completion time

        // Adjust points for rider and driver
        // e.g., decrease points for rider and increase points for driver
        adjustPoints(ride);

        // Move the ride to history
        moveToHistory(ride, key);

        // Remove the ride from active accepted rides
        dbRef.child(key).updateChildren(updates).addOnSuccessListener(unused -> {
            Toast.makeText(getContext(), "Ride completed and points adjusted", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to confirm ride: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void adjustPoints(Ride ride) {
        DatabaseReference riderRef = FirebaseDatabase.getInstance().getReference("users").child(ride.getRiderEmail()).child("points");
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("users").child(ride.getDriverEmail()).child("points");

        // Get current rider points
        riderRef.get().addOnSuccessListener(snapshot -> {
            Long riderPoints = snapshot.getValue(Long.class);
            if (riderPoints == null) riderPoints = 0L;
            riderRef.setValue(riderPoints - 50);
        });

        // Get current driver points
        driverRef.get().addOnSuccessListener(snapshot -> {
            Long driverPoints = snapshot.getValue(Long.class);
            if (driverPoints == null) driverPoints = 0L;
            driverRef.setValue(driverPoints + 50);
        });
    }


    private void moveToHistory(Ride ride, String key) {
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("history");

        // Add the ride to history and remove from accepted rides
        historyRef.child(key).setValue(ride);
        dbRef.child(key).removeValue();
    }
}

