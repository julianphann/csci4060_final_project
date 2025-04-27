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
    private String currentUserEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_find_ride, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        dbRef = FirebaseDatabase.getInstance().getReference("rides");
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Query query = dbRef.orderByChild("status").equalTo("pending");

        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(query, Ride.class)
                .build();

        adapter = new RideAdapter(options, (ride, key) -> acceptRide(ride, key)) {
            @Override
            protected void onBindViewHolder(@NonNull RideViewHolder holder, int position, @NonNull Ride model) {
                // Check if the ride belongs to current user
                if (model.getEmail() != null && model.getEmail().equals(currentUserEmail)) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0)); // Hide it
                } else {
                    super.onBindViewHolder(holder, position, model); // Otherwise show normally
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

        updates.put("status", "accepted");
        updates.put("acceptedBy", currentUserEmail);

        if ("offer".equals(ride.getType())) {
            updates.put("riderEmail", currentUserEmail);
            updates.put("driverEmail", ride.getEmail());
        } else if ("request".equals(ride.getType())) {
            updates.put("driverEmail", currentUserEmail);
            updates.put("riderEmail", ride.getEmail());
        }

        dbRef.child(key).updateChildren(updates).addOnSuccessListener(unused -> {
            Toast.makeText(getContext(), "Ride accepted!", Toast.LENGTH_SHORT).show();
            addToAcceptedList(ride, key);
            navigateToRideFragment();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to accept ride: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

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
            return;
        }

        DatabaseReference riderRef = FirebaseDatabase.getInstance().getReference("users")
                .child(riderEmail.replace(".", ",")).child("acceptedRides");
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("users")
                .child(driverEmail.replace(".", ",")).child("acceptedRides");

        riderRef.child(key).setValue(ride);
        driverRef.child(key).setValue(ride);
    }

    private void navigateToRideFragment() {
        View view = getView();
        if (view != null) {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_findRideFragment_to_RidesFragment);
        }
    }
}

