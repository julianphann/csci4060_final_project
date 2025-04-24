package edu.uga.cs.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

import edu.uga.cs.finalproject.Ride;

public class RideAdapter extends FirebaseRecyclerAdapter<Ride, RideAdapter.RideViewHolder> {

    public RideAdapter(@NonNull FirebaseRecyclerOptions<Ride> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RideViewHolder holder, int position, @NonNull Ride model) {
        // Make sure to handle potential null values
        if (model.getDestination() != null) {
            holder.destination.setText("To: " + model.getDestination());
        } else {
            holder.destination.setText("To: N/A");
        }

        if (model.getPickup() != null) {
            holder.pickup.setText("From: " + model.getPickup());
        } else {
            holder.pickup.setText("From: N/A");
        }

        if (model.getDateTime() != null) {
            holder.dateTime.setText("Date/Time: " + model.getDateTime());
        } else {
            holder.dateTime.setText("Date/Time: N/A");
        }

        if (model.getStatus() != null) {
            holder.status.setText("Status: " + model.getStatus());
        } else {
            holder.status.setText("Status: N/A");
        }
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(view);
    }

    // ViewHolder to hold the view components
    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView destination, pickup, dateTime, status;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            destination = itemView.findViewById(R.id.text_destination);
            pickup = itemView.findViewById(R.id.text_pickup);
            dateTime = itemView.findViewById(R.id.text_date_time);
            status = itemView.findViewById(R.id.text_status);
        }
    }
}
