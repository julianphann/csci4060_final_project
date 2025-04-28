package edu.uga.cs.finalproject.ui.rides;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import edu.uga.cs.finalproject.R;
import edu.uga.cs.finalproject.RideAdapter;
import edu.uga.cs.finalproject.Ride;

public class MyRidesFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private RideAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_rides, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("rides");

        String userEmail = mAuth.getCurrentUser().getEmail();

        FirebaseRecyclerOptions<Ride> options = new FirebaseRecyclerOptions.Builder<Ride>()
                .setQuery(dbRef, Ride.class) // Load all rides
                .build();

        RideAdapter.OnRideClickListener listener = new RideAdapter.OnRideClickListener() {
            @Override
            public void onRideClick(Ride ride, String key) {
                if (ride.getEmail() != null && ride.getEmail().equals(userEmail)) {
                    // Only allow edit if user posted this ride
                    editRideDialog(ride, key);
                }
            }
        };

        adapter = new RideAdapter(options, listener, "Edit Ride") {
            @Override
            protected void onBindViewHolder(@NonNull RideViewHolder holder, int position, @NonNull Ride model) {
                String email = mAuth.getCurrentUser().getEmail();

                boolean isPostedByUser = model.getEmail() != null && model.getEmail().equals(email);
                boolean isAcceptedByUser = (model.getRiderEmail() != null && model.getRiderEmail().equals(email)) ||
                        (model.getDriverEmail() != null && model.getDriverEmail().equals(email));

                if (isPostedByUser || isAcceptedByUser) {
                    super.onBindViewHolder(holder, position, model);
                } else {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
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
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void editRideDialog(Ride ride, String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Ride");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputDestination = new EditText(requireContext());
        inputDestination.setHint("Destination");
        inputDestination.setText(ride.getDestination());
        layout.addView(inputDestination);

        final EditText inputPickup = new EditText(requireContext());
        inputPickup.setHint("Pickup");
        inputPickup.setText(ride.getPickup());
        layout.addView(inputPickup);

        final EditText inputDateTime = new EditText(requireContext());
        inputDateTime.setHint("Date/Time");
        inputDateTime.setText(ride.getDateTime());
        layout.addView(inputDateTime);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newDestination = inputDestination.getText().toString();
            String newPickup = inputPickup.getText().toString();
            String newDateTime = inputDateTime.getText().toString();

            if (!newDestination.isEmpty()) {
                dbRef.child(key).child("destination").setValue(newDestination);
            }
            if (!newPickup.isEmpty()) {
                dbRef.child(key).child("pickup").setValue(newPickup);
            }
            if (!newDateTime.isEmpty()) {
                dbRef.child(key).child("dateTime").setValue(newDateTime);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.setNeutralButton("Delete Ride", (dialog, which) -> {
            dbRef.child(key).removeValue();
        });

        builder.show();
    }
}
