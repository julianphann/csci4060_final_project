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

/**
 * Fragment for finding and accepting available rides.
 * Displays a list of pending rides that users can accept, filtered to exclude the current user's own rides.
 * Handles ride acceptance logic and updates Firebase database accordingly.
 */
public class FindRideFragment extends Fragment {

    // Firebase database references
    private DatabaseReference dbRef;
    private RideAdapter adapter;
    private RecyclerView recyclerView;
    private String currentUserEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_find_ride, container, false);

        // Initialize Firebase database reference and get current user email
        dbRef = FirebaseDatabase.getInstance().getReference("rides");
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Set up RecyclerView with linear layout manager
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configure the FirebaseRecyclerAdapter
        setupAdapter();
        return rootView;
    }

    /**
     * Configures the FirebaseRecyclerAdapter with custom filtering and display logic
     */
    private void setupAdapter() {
        // Query to get future rides sorted by timestamp (ascending)
        Query query = dbRef.orderByChild("timestamp")
                .startAt(System.currentTimeMillis());  // Filter out past rides

        // Configure FirebaseRecyclerOptions for the adapter
        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(query, Ride.class)
                .build();

        // Create custom adapter with ride acceptance functionality
        adapter = new RideAdapter(options, (ride, key) -> acceptRide(ride, key), "Accept Ride") {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                // Show message when no rides available
                if (adapter.getItemCount() == 0) {
                    Toast.makeText(getContext(), "No available rides found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull RideAdapter.RideViewHolder holder, int position, @NonNull Ride model) {
                // Filter out ineligible rides (user's own rides or non-pending status)
                if (shouldShowRide(model)) {
                    super.onBindViewHolder(holder, position, model);
                } else {
                    // Hide item completely by collapsing its layout space
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            }
        };

        recyclerView.setAdapter(adapter);
    }

    /**
     * Determines if a ride should be displayed in the list
     * @param ride Ride object to check
     * @return true if ride is pending and not created by current user
     */
    private boolean shouldShowRide(Ride ride) {
        return "pending".equals(ride.getStatus()) &&
                !ride.getEmail().equals(currentUserEmail);
    }

    /**
     * Handles ride acceptance logic by updating database records
     * @param ride The Ride object being accepted
     * @param key Firebase database key for the ride
     */
    private void acceptRide(Ride ride, String key) {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Map<String, Object> updates = new HashMap<>();

        // Base updates for all ride types
        updates.put("status", "accepted");
        updates.put("acceptedBy", currentUserEmail);

        // Determine role assignments based on ride type
        if ("offer".equals(ride.getType())) {
            // Accepting an offer: current user becomes rider, original poster is driver
            updates.put("riderEmail", currentUserEmail);
            updates.put("driverEmail", ride.getEmail());
        } else if ("request".equals(ride.getType())) {
            // Accepting a request: current user becomes driver, original poster is rider
            updates.put("driverEmail", currentUserEmail);
            updates.put("riderEmail", ride.getEmail());
        }

        // Update ride status and roles in database
        dbRef.child(key).updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Ride accepted!", Toast.LENGTH_LONG).show();
                    addToAcceptedList(ride, key);  // Add to both users' accepted lists
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Adds accepted ride to both users' "acceptedRides" lists in database
     * @param ride Accepted Ride object
     * @param key Firebase database key for the ride
     */
    private void addToAcceptedList(Ride ride, String key) {
        String riderEmail, driverEmail;

        // Determine role assignments based on ride type
        if ("offer".equals(ride.getType())) {
            riderEmail = ride.getEmail();  // Original poster is driver
            driverEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } else {
            driverEmail = ride.getEmail();  // Original poster is rider
            riderEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }

        // Sanitize emails for Firebase key compatibility
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(riderEmail.replace(".", ",")).child("acceptedRides").child(key).setValue(ride);
        usersRef.child(driverEmail.replace(".", ",")).child("acceptedRides").child(key).setValue(ride);
    }

    // Lifecycle methods for adapter listening
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