package edu.uga.cs.finalproject;

public class User {
    private String email;
    private int ridePoints;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, int ridePoints) {
        this.email = email;
        this.ridePoints = ridePoints;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRidePoints() {
        return ridePoints;
    }

    public void setRidePoints(int ridePoints) {
        this.ridePoints = ridePoints;
    }
}
