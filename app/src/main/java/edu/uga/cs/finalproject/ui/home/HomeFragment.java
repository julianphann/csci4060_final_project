package edu.uga.cs.finalproject.ui.home;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import androidx.databinding.DataBindingUtil;
import edu.uga.cs.finalproject.R;
import edu.uga.cs.finalproject.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        FragmentHomeBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding.setViewModel(homeViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        return binding.getRoot();
    }
}


