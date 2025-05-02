package edu.uga.cs.finalproject.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import edu.uga.cs.finalproject.User;
import edu.uga.cs.finalproject.databinding.FragmentProfileBinding;

/**
 * Fragment for displaying user profile information including email and ride points.
 * Fetches and shows data from Firebase Realtime Database.
 */
public class ProfileFragment extends Fragment {

    // View binding instance for the profile fragment
    private FragmentProfileBinding binding;

    /**
     * Inflates the fragment layout using view binding
     * @param inflater LayoutInflater to inflate views
     * @param container Parent view group
     * @param savedInstanceState Saved instance state
     * @return Root view of the inflated layout
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using view binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Configures view components after view creation
     * @param view The created view
     * @param savedInstanceState Saved instance state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize TextView references from binding
        final TextView emailTextView = binding.textEmail;
        final TextView pointsTextView = binding.textPoints;

        // Get current user's email from Firebase Authentication
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        emailTextView.setText(userEmail);

        // Fetch user data from Firebase if email exists
        if (userEmail != null) {
            User.fetchUserData(userEmail, new User.UserDataCallback() {
                /**
                 * Handles successful user data retrieval
                 * @param user User object containing profile data
                 */
                @Override
                public void onSuccess(User user) {
                    // Update UI with retrieved points value
                    pointsTextView.setText("Ride Points: " + user.getRidePoints());
                }

                /**
                 * Handles failures during user data retrieval
                 * @param error Description of the failure reason
                 */
                @Override
                public void onFailure(String error) {
                    // Log error and show placeholder text
                    Log.e("ProfileFragment", "Failed to fetch user data: " + error);
                    pointsTextView.setText("Ride Points: N/A");
                }
            });
        }
    }

    /**
     * Cleans up binding resources when view is destroyed
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}