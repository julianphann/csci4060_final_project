package edu.uga.cs.finalproject;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class RideAdapter extends FirebaseRecyclerAdapter<Ride, RideAdapter.RideViewHolder> {

    // Interface for item click handling
    public interface OnRideClickListener {
        void onRideClick(Ride ride, String key);
    }

    private OnRideClickListener listener;
    private String buttonText;

    public RideAdapter(@NonNull FirebaseRecyclerOptions<Ride> options, OnRideClickListener listener, String buttonText) {
        super(options);
        this.listener = listener;
        this.buttonText = buttonText;
    }

    @Override
    protected void onBindViewHolder(@NonNull RideViewHolder holder, int position, @NonNull Ride model) {
        holder.destination.setText("To: " + model.getDestination());
        holder.pickup.setText("From: " + model.getPickup());
        holder.dateTime.setText("Date/Time: " + model.getDateTime());
        holder.type.setText("Type: " + model.getType());
        holder.email.setText("Posted by: " + model.getEmail());

        // Set the status based on the confirmation state
        if (model.isRiderConfirmed() && model.isDriverConfirmed()) {
            holder.status.setText("Status: completed");
            holder.acceptButton.setBackgroundColor(Color.GRAY);  // Gray the button
            holder.acceptButton.setEnabled(false);  // Disable the button
            holder.acceptButton.setText("Ride Completed");
        } else if (model.isDriverConfirmed()) {
            holder.status.setText("Status: Driver Accepted");
            holder.acceptButton.setBackgroundColor(Color.GRAY);  // Gray the button
            holder.acceptButton.setEnabled(true);  // Keep the button enabled for rider confirmation
            holder.acceptButton.setText("Confirm Ride"); // Text indicating rider confirmation needed
        } else if (model.isRiderConfirmed()) {
            holder.status.setText("Status: Rider Confirmed");
            holder.acceptButton.setBackgroundColor(Color.GRAY);  // Gray the button
            holder.acceptButton.setEnabled(true);  // Keep the button enabled for driver confirmation
            holder.acceptButton.setText("Confirm Ride"); // Text indicating driver confirmation needed
        } else {
            holder.status.setText("Status: " + model.getStatus());
            holder.acceptButton.setBackgroundColor(Color.BLUE);  // Active button color
            holder.acceptButton.setEnabled(true);  // Enable the button
            holder.acceptButton.setText("Accept Ride"); // Text for accepting the ride
        }

        // Set the "Accepted by" text to show the driver's email if they have confirmed
        if (model.isDriverConfirmed()) {
            holder.acceptedBy.setText("Accepted by: " + model.getDriverEmail());
        } else {
            holder.acceptedBy.setText("Accepted by: Not accepted yet");
        }

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            String key = getRef(position).getKey();
            listener.onRideClick(model, key);
        });

        // Handle button click (confirm ride completion or edit ride)
        holder.acceptButton.setOnClickListener(v -> {
            String key = getRef(position).getKey();
            // Check if the ride is in MyRides or AcceptedRides
            if (model.isDriverConfirmed() || model.isRiderConfirmed()) {
                // This logic should be for confirming the ride completion
                listener.onRideClick(model, key);
            } else {
                // This logic should be for editing the ride
                listener.onRideClick(model, key);
            }
        });
    }


    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(view);
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView destination, pickup, dateTime, status, type, email, acceptedBy;
        Button acceptButton;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            destination = itemView.findViewById(R.id.text_destination);
            pickup = itemView.findViewById(R.id.text_pickup);
            dateTime = itemView.findViewById(R.id.text_date_time);
            status = itemView.findViewById(R.id.text_status);
            type = itemView.findViewById(R.id.text_type);
            email = itemView.findViewById(R.id.text_email);
            acceptButton = itemView.findViewById(R.id.button_accept_ride);
            acceptedBy = itemView.findViewById(R.id.text_accepted_by); // Add the accepted by field
        }
    }
}


