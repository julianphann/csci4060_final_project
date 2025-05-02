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

/**
 * Adapter class for populating RecyclerView with Ride data from Firebase Realtime Database.
 * Handles display of ride information and status updates, with click listener functionality.
 */
public class RideAdapter extends FirebaseRecyclerAdapter<Ride, RideAdapter.RideViewHolder> {

    /**
     * Interface for handling click events on rides and their action buttons
     */
    public interface OnRideClickListener {
        void onRideClick(Ride ride, String key);
    }

    private OnRideClickListener listener; // Click listener instance
    private String buttonText;            // Text to display on action button

    /**
     * Constructor initializing the adapter
     * @param options FirebaseRecyclerOptions configuration for database queries
     * @param listener Click listener implementation
     * @param buttonText Default text for action buttons
     */
    public RideAdapter(@NonNull FirebaseRecyclerOptions<Ride> options, OnRideClickListener listener, String buttonText) {
        super(options);
        this.listener = listener;
        this.buttonText = buttonText;
    }

    /**
     * Binds ride data to ViewHolder elements and configures UI components
     * @param holder ViewHolder containing ride information views
     * @param position Position in RecyclerView
     * @param model Ride object containing data for this position
     */
    @Override
    protected void onBindViewHolder(@NonNull RideViewHolder holder, int position, @NonNull Ride model) {
        // Convert timestamp to human-readable format
        String formattedDate = formatTimestamp(model.getTimestamp());

        // Set basic ride information
        holder.destination.setText("To: " + model.getDestination());
        holder.pickup.setText("From: " + model.getPickup());
        holder.dateTime.setText("When: " + formattedDate);
        holder.type.setText("Type: " + model.getType());
        holder.email.setText("Posted by: " + model.getEmail());

        // Configure status display and button appearance based on confirmation state
        String statusText;
        int buttonColor;
        boolean buttonEnabled;
        String dynamicButtonText;

        if (model.isRiderConfirmed() && model.isDriverConfirmed()) {
            // Both parties confirmed - completed state
            statusText = "Status: Completed";
            buttonColor = Color.GRAY;
            buttonEnabled = false;
            dynamicButtonText = "Completed";
        } else if (model.isDriverConfirmed()) {
            // Driver accepted - waiting for rider confirmation
            statusText = "Status: Driver Accepted";
            buttonColor = Color.parseColor("#FFA500"); // Orange
            buttonEnabled = true;
            dynamicButtonText = "Confirm as Rider";
        } else if (model.isRiderConfirmed()) {
            // Rider confirmed - waiting for driver confirmation
            statusText = "Status: Rider Confirmed";
            buttonColor = Color.parseColor("#FFA500"); // Orange
            buttonEnabled = true;
            dynamicButtonText = "Confirm as Driver";
        } else {
            // New/unconfirmed ride
            statusText = "Status: " + model.getStatus();
            buttonColor = Color.BLUE;
            buttonEnabled = true;
            dynamicButtonText = this.buttonText;
        }

        // Apply status configuration to views
        holder.status.setText(statusText);
        holder.acceptButton.setBackgroundColor(buttonColor);
        holder.acceptButton.setEnabled(buttonEnabled);
        holder.acceptButton.setText(dynamicButtonText);

        // Configure accepted by information based on ride type
        String acceptedByText;
        if ("offer".equalsIgnoreCase(model.getType())) {
            // Ride offers show rider acceptance
            acceptedByText = model.getRiderEmail() != null
                    ? "Rider: " + model.getRiderEmail()
                    : "Not accepted yet";
        } else if ("request".equalsIgnoreCase(model.getType())) {
            // Ride requests show driver acceptance
            acceptedByText = model.getDriverEmail() != null
                    ? "Driver: " + model.getDriverEmail()
                    : "Not accepted yet";
        } else {
            acceptedByText = "Not accepted yet";
        }
        holder.acceptedBy.setText(acceptedByText);

        // Set click listeners for both item view and action button
        String key = getRef(position).getKey();
        holder.itemView.setOnClickListener(v -> listener.onRideClick(model, key));
        holder.acceptButton.setOnClickListener(v -> listener.onRideClick(model, key));
    }

    /**
     * Helper method to convert timestamp to formatted date string
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date string (e.g., "Wed, Jul 4 • 3:45 PM")
     */
    private String formatTimestamp(long timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return "Date not available";
        }
    }

    /**
     * Creates new ViewHolder instances when needed
     * @param parent Parent ViewGroup
     * @param viewType View type identifier
     * @return New RideViewHolder instance
     */
    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(view);
    }

    /**
     * ViewHolder class containing references to all views in a ride list item
     */
    public static class RideViewHolder extends RecyclerView.ViewHolder {
        // UI component references
        TextView destination, pickup, dateTime, status, type, email, acceptedBy;
        Button acceptButton;

        /**
         * Constructor initializing view references
         * @param itemView Root view of the item layout
         */
        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            destination = itemView.findViewById(R.id.text_destination);
            pickup = itemView.findViewById(R.id.text_pickup);
            dateTime = itemView.findViewById(R.id.text_date_time);
            status = itemView.findViewById(R.id.text_status);
            type = itemView.findViewById(R.id.text_type);
            email = itemView.findViewById(R.id.text_email);
            acceptButton = itemView.findViewById(R.id.button_accept_ride);
            acceptedBy = itemView.findViewById(R.id.text_accepted_by);
        }
    }
}