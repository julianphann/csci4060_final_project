package edu.uga.cs.finalproject.ui.home;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;


import edu.uga.cs.finalproject.R;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout without DataBinding
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Get references to the card views using findViewById
        CardView cardFindRide = rootView.findViewById(R.id.cardFindRide);
        CardView cardPostRide = rootView.findViewById(R.id.cardPostRide);
        CardView cardMyRides = rootView.findViewById(R.id.cardMyRides);

        // Set onClickListeners for each card
        cardFindRide.setOnClickListener(v -> onFindRideClick());
        cardPostRide.setOnClickListener(v -> onPostRideClick());
        cardMyRides.setOnClickListener(v -> onMyRidesClick());

        return rootView;
    }

    private void onFindRideClick() {
        Navigation.findNavController(requireView()).navigate(R.id.findRideFragment);
    }

    private void onPostRideClick() {
        Navigation.findNavController(requireView()).navigate(R.id.postRideFragment);
    }

    private void onMyRidesClick() {
        Navigation.findNavController(requireView()).navigate(R.id.myRidesFragment);
    }

}
