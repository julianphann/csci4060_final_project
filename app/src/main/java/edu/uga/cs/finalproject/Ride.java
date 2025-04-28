package edu.uga.cs.finalproject;

import com.google.firebase.database.PropertyName;

public class Ride {
    private String id;
    private String type; // "offer" or "request"
    private String dateTime;
    private String destination;
    private String pickup;
    private String status; // "pending", "accepted", "confirmed"
    private String riderEmail;
    private String driverEmail;
    private String email; // Email of the user who posted the ride
    private long completedAt;
    private boolean riderConfirmed;
    private boolean driverConfirmed;
    private boolean isConfirmed;

    // Default constructor required for Firebase
    public Ride() {
    }

    // Constructor with all fields
    public Ride(String id, String type, String dateTime, String destination, String pickup,
                String status, String riderEmail, String driverEmail, String email) {
        this.id = id;
        this.type = type;
        this.dateTime = dateTime;
        this.destination = destination;
        this.pickup = pickup;
        this.status = status;
        this.riderEmail = riderEmail;
        this.driverEmail = driverEmail;
        this.email = email; // Set email when creating a new Ride
    }

    // Getters and setters for all fields
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @PropertyName("datetime")
    public String getDateTime() {
        return dateTime;
    }

    @PropertyName("datetime")
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRiderEmail() {
        return riderEmail;
    }

    public void setRiderEmail(String riderEmail) {
        this.riderEmail = riderEmail;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public void setDriverEmail(String driverEmail) {
        this.driverEmail = driverEmail;
    }

    public String getEmail() {
        return email; // Get the email of the user who posted the ride
    }

    public void setEmail(String email) {
        this.email = email; // Set the email of the user who posted the ride
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }
    public boolean isRiderConfirmed() {
        return riderConfirmed;
    }

    public void setRiderConfirmed(boolean riderConfirmed) {
        this.riderConfirmed = riderConfirmed;
    }

    public boolean isDriverConfirmed() {
        return driverConfirmed;
    }

    public void setDriverConfirmed(boolean driverConfirmed) {
        this.driverConfirmed = driverConfirmed;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }

}
