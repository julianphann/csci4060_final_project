package edu.uga.cs.finalproject.ui.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<Integer> points = new MutableLiveData<>();

    public ProfileViewModel() {
        fetchUserData();
    }

    private void fetchUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            email.setValue("Not signed in");
            points.setValue(0);
            return;
        }

        String userEmail = user.getEmail();
        if (userEmail == null) {
            email.setValue("No email found");
            points.setValue(0);
            return;
        }

        email.setValue(userEmail);  // Set LiveData for UI

        String sanitizedEmail = sanitizeEmail(userEmail);

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(sanitizedEmail);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer pointsVal = snapshot.child("ridePoints").getValue(Integer.class);

                if (pointsVal != null) {
                    points.setValue(pointsVal);
                } else {
                    points.setValue(50);  // Default value
                    userRef.child("ridePoints").setValue(50); // Save to database
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                points.setValue(0);
            }
        });
    }

    private String sanitizeEmail(String email) {
        return email.replace(".", ",");
    }

    public LiveData<String> getEmail() {
        return email;
    }

    public LiveData<Integer> getPoints() {
        return points;
    }

    public void setEmail(String emailStr) {
        email.setValue(emailStr);
    }

    public void setPoints(int pointsVal) {
        points.setValue(pointsVal);
    }
}