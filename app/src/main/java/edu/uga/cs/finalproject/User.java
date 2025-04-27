package edu.uga.cs.finalproject;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {
    private String email;
    private int ridePoints;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {
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

    // Fetch user data from Firebase Realtime Database
    public static void fetchUserData(String userEmail, UserDataCallback callback) {
        // Ensure we handle '.' as Firebase keys can't contain dots, replace it with commas
        String userKey = userEmail.replace(".", ",");

        // Reference to the user's data in the "users" node
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userKey);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onFailure("User data not found");
                }
            } else {
                callback.onFailure("User not found in database");
            }
        }).addOnFailureListener(e -> {
            callback.onFailure(e.getMessage());
        });
    }

    // Callback interface for fetching user data asynchronously
    public interface UserDataCallback {
        void onSuccess(User user);

        void onFailure(String error);
    }
}

