package edu.uga.cs.finalproject.ui.rides;

import android.app.ProgressDialog;
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
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_accepted_rides, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("rides");

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Not signed in", Toast.LENGTH_SHORT).show();
            return rootView;
        }

        String userEmail = mAuth.getCurrentUser().getEmail();
        Query query = dbRef.orderByChild("acceptedBy").equalTo(userEmail);

        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(query, Ride.class)
                .build();

        adapter = new RideAdapter(options, (ride, key) -> confirmRideCompletion(ride, key));

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
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Completing ride...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "completed");
        updates.put("completedAt", System.currentTimeMillis());

        dbRef.child(key).updateChildren(updates).addOnSuccessListener(unused -> {
            adjustPoints(ride, () -> {
                moveToHistory(ride, key);
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Ride completed and points adjusted", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Failed to confirm ride: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void adjustPoints(Ride ride, Runnable onComplete) {
        DatabaseReference riderRef = FirebaseDatabase.getInstance()
                .getReference("users").child(sanitizeEmail(ride.getRiderEmail())).child("points");
        DatabaseReference driverRef = FirebaseDatabase.getInstance()
                .getReference("users").child(sanitizeEmail(ride.getDriverEmail())).child("points");

        riderRef.get().addOnSuccessListener(riderSnapshot -> {
            Long riderPoints = riderSnapshot.getValue(Long.class);
            if (riderPoints == null) riderPoints = 0L;
            riderRef.setValue(riderPoints - 50);

            driverRef.get().addOnSuccessListener(driverSnapshot -> {
                Long driverPoints = driverSnapshot.getValue(Long.class);
                if (driverPoints == null) driverPoints = 0L;
                driverRef.setValue(driverPoints + 50);

                // Only after adjusting points we continue
                if (onComplete != null) {
                    onComplete.run();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to update driver points", Toast.LENGTH_LONG).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to update rider points", Toast.LENGTH_LONG).show();
        });
    }

    private void moveToHistory(Ride ride, String key) {
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("history");

        historyRef.child(key).setValue(ride).addOnSuccessListener(unused -> {
            dbRef.child(key).removeValue(); // Remove after success
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to move ride to history: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private String sanitizeEmail(String email) {
        return email.replace(".", ",");
    }
}
