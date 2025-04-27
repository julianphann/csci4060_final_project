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

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView emailTextView = binding.textEmail;
        final TextView pointsTextView = binding.textPoints;

        // Get the current user's email
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        emailTextView.setText(userEmail);

        // Fetch user data from Firebase using User class
        if (userEmail != null) {
            User.fetchUserData(userEmail, new User.UserDataCallback() {
                @Override
                public void onSuccess(User user) {
                    // Set points and other user data to the views
                    pointsTextView.setText("Ride Points: " + user.getRidePoints());
                }

                @Override
                public void onFailure(String error) {
                    // Handle failure to fetch user data
                    Log.e("ProfileFragment", "Failed to fetch user data: " + error);
                    pointsTextView.setText("Ride Points: N/A");
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
