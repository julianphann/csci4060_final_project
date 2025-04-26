package edu.uga.cs.finalproject;

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

    // Constructor where we pass both Firebase options and the listener
    public RideAdapter(@NonNull FirebaseRecyclerOptions<Ride> options, OnRideClickListener listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull RideViewHolder holder, int position, @NonNull Ride model) {
        holder.destination.setText("To: " + model.getDestination());
        holder.pickup.setText("From: " + model.getPickup());
        holder.dateTime.setText("Date/Time: " + model.getDateTime());
        holder.status.setText("Status: " + model.getStatus());
        holder.type.setText("Type: " + model.getType());
        holder.email.setText("Posted by: " + model.getEmail());  // ← This is what was missing

        holder.itemView.setOnClickListener(v -> {
            String key = getRef(position).getKey();
            listener.onRideClick(model, key);
        });

        holder.acceptButton.setOnClickListener(v -> {
            String key = getRef(position).getKey();
            listener.onRideClick(model, key);
        });
    }


    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each ride
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(view);
    }

    // ViewHolder to hold the individual ride item views
    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView destination, pickup, dateTime, status, type, email; // ← Add email
        Button acceptButton;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            destination = itemView.findViewById(R.id.text_destination);
            pickup = itemView.findViewById(R.id.text_pickup);
            dateTime = itemView.findViewById(R.id.text_date_time);
            status = itemView.findViewById(R.id.text_status);
            type = itemView.findViewById(R.id.text_type);
            email = itemView.findViewById(R.id.text_email); // ← Bind it
            acceptButton = itemView.findViewById(R.id.button_accept_ride);
        }
    }

}
