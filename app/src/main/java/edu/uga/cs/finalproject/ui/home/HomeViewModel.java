package edu.uga.cs.finalproject.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<String> welcomeMessage;

    public HomeViewModel() {
        welcomeMessage = new MutableLiveData<>();
        welcomeMessage.setValue("Welcome to the app!");
    }

    public LiveData<String> getWelcomeMessage() {
        return welcomeMessage;
    }
}

