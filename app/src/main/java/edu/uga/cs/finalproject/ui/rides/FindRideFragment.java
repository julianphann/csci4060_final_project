package edu.uga.cs.finalproject.ui.rides;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_find_ride, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        dbRef = FirebaseDatabase.getInstance().getReference("rides");

        Query query = dbRef.orderByChild("status").equalTo("pending");

        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(query, Ride.class)
                .build();

        adapter = new RideAdapter(options, (ride, key) -> acceptRide(ride, key));

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

    private void acceptRide(Ride ride, String key) {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Map<String, Object> updates = new HashMap<>();

        // Update the ride status to accepted
        updates.put("status", "accepted");
        updates.put("acceptedBy", currentUserEmail);

        // Update rider/driver info based on ride type
        if ("offer".equals(ride.getType())) {
            updates.put("riderEmail", currentUserEmail);
            updates.put("driverEmail", ride.getEmail());
        } else if ("request".equals(ride.getType())) {
            updates.put("driverEmail", currentUserEmail);
            updates.put("riderEmail", ride.getEmail());
        }

        // Update the ride in the database (remove from requested, add to accepted)
        dbRef.child(key).updateChildren(updates).addOnSuccessListener(unused -> {
            Toast.makeText(getContext(), "Ride accepted!", Toast.LENGTH_SHORT).show();

            // Add the ride to the accepted list for both the rider and the driver
            addToAcceptedList(ride, key);

            // Navigate to the AcceptedRidesFragment
            navigateToRideFragment();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to accept ride: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    // Method to add the accepted ride to both rider and driver's accepted list
    private void addToAcceptedList(Ride ride, String key) {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        String riderEmail, driverEmail;
        if ("offer".equals(ride.getType())) {
            riderEmail = currentUserEmail;
            driverEmail = ride.getEmail();
        } else if ("request".equals(ride.getType())) {
            driverEmail = currentUserEmail;
            riderEmail = ride.getEmail();
        } else {
            return; // Unknown type, fail silently
        }

        DatabaseReference riderRef = FirebaseDatabase.getInstance().getReference("users")
                .child(riderEmail.replace(".", ",")).child("acceptedRides");
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("users")
                .child(driverEmail.replace(".", ",")).child("acceptedRides");

        riderRef.child(key).setValue(ride);
        driverRef.child(key).setValue(ride);
    }

    // Navigate to the AcceptedRidesFragment
    private void navigateToRideFragment() {
        // Replace this with actual navigation code
        // Example: using Navigation Component
        View view = getView();
        if (view != null) {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_findRideFragment_to_RidesFragment);
        }
    }

}
