package com.app.figpdfconvertor.figpdf.utils;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

public abstract class DoubleClickListener implements View.OnClickListener {

    private static final long DEFAULT_DEBOUNCE_INTERVAL = 1000L; // 1 second debounce interval
    private final long debounceInterval;
    private boolean enabled = true;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public DoubleClickListener() {
        this(DEFAULT_DEBOUNCE_INTERVAL);
    }

    public DoubleClickListener(long debounceInterval) {
        this.debounceInterval = debounceInterval;
    }

    @Override
    public final void onClick(View v) {
        if (enabled) {
            enabled = false;
            handler.postDelayed(() -> enabled = true, debounceInterval);
            performClick(v);
        }
    }

    public abstract void performClick(View v);
}
