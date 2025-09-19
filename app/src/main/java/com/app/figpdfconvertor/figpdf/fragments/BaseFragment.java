package com.app.figpdfconvertor.figpdf.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.figpdfconvertor.figpdf.funnelss.AnalyticsManager;

import java.util.Collections;

public class BaseFragment extends Fragment {

    private boolean hasUserInteracted = false;

    public boolean hasUserInteracted() { return hasUserInteracted; }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Automatic screen_view logging
        AnalyticsManager.INSTANCE.logEvent("screen_view",
                Collections.singletonMap("screen", getClass().getSimpleName()));
    }

    // Call this from UI interactions like button click, scroll, etc.
    protected void userInteracted() {
        hasUserInteracted = true;
    }

    // Call this when fragment is being removed / user navigates away
    protected void trackExit() {
        String exitType = hasUserInteracted ? "good_exit" : "bad_exit";
        AnalyticsManager.INSTANCE.logEvent(exitType,
                Collections.singletonMap("screen", getClass().getSimpleName()));
        AnalyticsManager.INSTANCE.logFunnelStep(exitType,
                Collections.singletonMap("screen", getClass().getSimpleName()));
    }

    @Override
    public void onPause() {
        super.onPause();
        // Track exit automatically if fragment is being removed
        if (isRemoving() || getActivity().isFinishing()) {
            trackExit();
        }
    }
}
