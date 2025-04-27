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
        Query query = dbRef.orderByChild("acceptedBy").equalTo(userEmail);

        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(query, Ride.class)
                .build();

        RideAdapter.OnRideClickListener listener = new RideAdapter.OnRideClickListener() {
            @Override
            public void onRideClick(Ride ride, String key) {
                confirmRideCompletion(ride, key);
            }
        };

        adapter = new RideAdapter(options, listener);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
        // First move the ride to history
        moveToHistory(ride, key);

        // Then adjust points
        adjustPoints(ride);

        // Finally delete from active rides
        dbRef.child(key).removeValue().addOnSuccessListener(unused -> {
            Toast.makeText(getContext(), "Ride completed and removed from active rides.", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to remove ride: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void adjustPoints(Ride ride) {
        String riderKey = sanitizeEmail(ride.getRiderEmail());
        String driverKey = sanitizeEmail(ride.getDriverEmail());

        DatabaseReference riderPointsRef = FirebaseDatabase.getInstance().getReference("users").child(riderKey).child("ridePoints");
        DatabaseReference driverPointsRef = FirebaseDatabase.getInstance().getReference("users").child(driverKey).child("ridePoints");

        // Atomically adjust rider points
        riderPointsRef.get().addOnSuccessListener(snapshot -> {
            Long riderPoints = snapshot.getValue(Long.class);
            if (riderPoints == null) riderPoints = 0L;
            long newPoints = Math.max(0, riderPoints - 50);  // Avoid negative points
            riderPointsRef.setValue(newPoints);
        });

        // Atomically adjust driver points
        driverPointsRef.get().addOnSuccessListener(snapshot -> {
            Long driverPoints = snapshot.getValue(Long.class);
            if (driverPoints == null) driverPoints = 0L;
            driverPointsRef.setValue(driverPoints + 50);
        });
    }

    private void moveToHistory(Ride ride, String key) {
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("history");
        ride.setStatus("completed");  // set ride status before moving
        ride.setCompletedAt(System.currentTimeMillis());  // record completed time
        historyRef.child(key).setValue(ride);
    }

    private String sanitizeEmail(String email) {
        if (email == null) return null;
        return email.replace(".", ",");
    }
}
