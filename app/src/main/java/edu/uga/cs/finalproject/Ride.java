package edu.uga.cs.finalproject;

public class Ride {
    private String id;
    private String type; // "offer" or "request"
    private String dateTime;
    private String destination;
    private String pickup;
    private String status; // "pending", "accepted", "confirmed"
    private String riderEmail;
    private String driverEmail;

    // Default constructor required for Firebase
    public Ride() {
    }

    // Constructor with all fields
    public Ride(String id, String type, String dateTime, String destination, String pickup,
                String status, String riderEmail, String driverEmail) {
        this.id = id;
        this.type = type;
        this.dateTime = dateTime;
        this.destination = destination;
        this.pickup = pickup;
        this.status = status;
        this.riderEmail = riderEmail;
        this.driverEmail = driverEmail;
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

    public String getDateTime() {
        return dateTime;
    }

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
}
