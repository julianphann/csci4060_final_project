package edu.uga.cs.finalproject;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Represents a user entity in the application and handles Firebase data operations.
 * Manages user data including email and ride points balance.
 * Provides method to fetch user data asynchronously from Firebase Realtime Database.
 */
public class User {
    private String email;
    private int ridePoints;

    /**
     * Default constructor required for Firebase deserialization
     */
    public User() {
    }

    /**
     * Creates a User with specified email and initial ride points
     * @param email User's email address (unique identifier)
     * @param ridePoints Initial balance of ride points
     */
    public User(String email, int ridePoints) {
        this.email = email;
        this.ridePoints = ridePoints;
    }

    // Basic getters and setters required for Firebase data mapping

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

    /**
     * Fetches user data from Firebase Realtime Database asynchronously
     * @param userEmail Email address of user to retrieve (will be sanitized)
     * @param callback Handler for asynchronous response containing User object or error
     */
    public static void fetchUserData(String userEmail, UserDataCallback callback) {
        // Sanitize email for Firebase key compliance
        String userKey = userEmail.replace(".", ",");

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userKey);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onFailure("Data format mismatch for user: " + userEmail);
                }
            } else {
                callback.onFailure("User account not found: " + userEmail);
            }
        }).addOnFailureListener(e -> {
            callback.onFailure("Database error: " + e.getMessage());
        });
    }

    /**
     * Callback interface for handling asynchronous user data operations
     */
    public interface UserDataCallback {
        /**
         * Called when user data is successfully retrieved
         * @param user Retrieved User object with current data
         */
        void onSuccess(User user);

        /**
         * Called when user data retrieval fails
         * @param error Description of failure reason
         */
        void onFailure(String error);
    }
}