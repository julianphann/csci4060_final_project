package edu.uga.cs.finalproject.ui.rides;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for managing UI-related data for rides functionality.
 * Preserves data during configuration changes and provides lifecycle-aware data management.
 */
public class RidesViewModel extends ViewModel {

    // LiveData holder for UI-related text observations
    private final MutableLiveData<String> mText;

    /**
     * Constructor initializes the ViewModel with default data
     * Sets up initial text value for demonstration purposes
     */
    public RidesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");  // Default placeholder text
    }

    /**
     * Provides read-only access to the text LiveData
     * @return LiveData<String> that can be observed by UI components
     */
    public LiveData<String> getText() {
        return mText;
    }
}