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
import edu.uga.cs.finalproject.Ride;
import edu.uga.cs.finalproject.RideAdapter;

public class FindRideFragment extends Fragment {

    private DatabaseReference dbRef;
    private RideAdapter adapter;
    private RecyclerView recyclerView;
    private String currentUserEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_find_ride, container, false);

        // Initialize database reference
        dbRef = FirebaseDatabase.getInstance().getReference("rides");
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupAdapter();
        return rootView;
    }

    private void setupAdapter() {
        // Query to get pending rides sorted by timestamp (soonest first)
        Query query = dbRef.orderByChild("timestamp")
                .startAt(System.currentTimeMillis());  // Only future rides

        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(query, Ride.class)
                .build();

        adapter = new RideAdapter(options, (ride, key) -> acceptRide(ride, key), "Accept Ride") {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (adapter.getItemCount() == 0) {
                    Toast.makeText(getContext(), "No available rides found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull RideAdapter.RideViewHolder holder, int position, @NonNull Ride model) {
                // Filter out user's own rides and non-pending status
                if (shouldShowRide(model)) {
                    super.onBindViewHolder(holder, position, model);
                } else {
                    // Hide the item completely
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            }
        };

        recyclerView.setAdapter(adapter);
    }

    private boolean shouldShowRide(Ride ride) {
        return "pending".equals(ride.getStatus()) &&
                !ride.getEmail().equals(currentUserEmail);
    }

    private void acceptRide(Ride ride, String key) {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Map<String, Object> updates = new HashMap<>();

        updates.put("status", "accepted");
        updates.put("acceptedBy", currentUserEmail);

        if ("offer".equals(ride.getType())) {
            // Offer: accepting user is the rider
            updates.put("riderEmail", currentUserEmail);  // The rider is the one accepting the offer.
            updates.put("driverEmail", ride.getEmail());  // The poster is the driver of the offer.
        } else if ("request".equals(ride.getType())) {
            // Request: accepting user is the driver
            updates.put("driverEmail", currentUserEmail);  // The driver is the one accepting the request.
            updates.put("riderEmail", ride.getEmail());  // The poster is the rider of the request.
        }

        dbRef.child(key).updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Ride accepted!", Toast.LENGTH_LONG).show();
                    addToAcceptedList(ride, key);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void addToAcceptedList(Ride ride, String key) {
        String riderEmail, driverEmail;
        if ("offer".equals(ride.getType())) {
            riderEmail = ride.getEmail();
            driverEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } else {
            driverEmail = ride.getEmail();
            riderEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(riderEmail.replace(".", ",")).child("acceptedRides").child(key).setValue(ride);
        usersRef.child(driverEmail.replace(".", ",")).child("acceptedRides").child(key).setValue(ride);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}