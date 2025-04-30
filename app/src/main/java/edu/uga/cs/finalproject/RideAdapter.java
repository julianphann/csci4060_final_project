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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        // Format timestamp to readable date
        String formattedDate = formatTimestamp(model.getTimestamp());

        holder.destination.setText("To: " + model.getDestination());
        holder.pickup.setText("From: " + model.getPickup());
        holder.dateTime.setText("When: " + formattedDate);  // Changed to use formatted timestamp
        holder.type.setText("Type: " + model.getType());
        holder.email.setText("Posted by: " + model.getEmail());

        // Status handling based on confirmation state
        String statusText;
        int buttonColor;
        boolean buttonEnabled;
        String buttonText;

        if (model.isRiderConfirmed() && model.isDriverConfirmed()) {
            statusText = "Status: Completed";
            buttonColor = Color.GRAY;
            buttonEnabled = false;
            buttonText = "Completed";
        } else if (model.isDriverConfirmed()) {
            statusText = "Status: Driver Accepted";
            buttonColor = Color.parseColor("#FFA500"); // Orange
            buttonEnabled = true;
            buttonText = "Confirm as Rider";
        } else if (model.isRiderConfirmed()) {
            statusText = "Status: Rider Confirmed";
            buttonColor = Color.parseColor("#FFA500"); // Orange
            buttonEnabled = true;
            buttonText = "Confirm as Driver";
        } else {
            statusText = "Status: " + model.getStatus();
            buttonColor = Color.BLUE;
            buttonEnabled = true;
            buttonText = this.buttonText;
        }

        holder.status.setText(statusText);
        holder.acceptButton.setBackgroundColor(buttonColor);
        holder.acceptButton.setEnabled(buttonEnabled);
        holder.acceptButton.setText(buttonText);


        String acceptedByText;
        if ("offer".equalsIgnoreCase(model.getType())) {
            // If it's an offer, someone (rider) can accept it
            acceptedByText = model.getRiderEmail() != null
                    ? "Rider: " + model.getRiderEmail()
                    : "Not accepted yet";
        } else if ("request".equalsIgnoreCase(model.getType())) {
            // If it's a request, someone (driver) can accept it
            acceptedByText = model.getDriverEmail() != null
                    ? "Driver: " + model.getDriverEmail()
                    : "Not accepted yet";
        } else {
            acceptedByText = "Not accepted yet";
        }

        holder.acceptedBy.setText(acceptedByText);

        // Click listeners
        String key = getRef(position).getKey();
        holder.itemView.setOnClickListener(v -> listener.onRideClick(model, key));
        holder.acceptButton.setOnClickListener(v -> listener.onRideClick(model, key));
    }

    // Add this helper method to format timestamps
    private String formatTimestamp(long timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return "Date not available";
        }
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


