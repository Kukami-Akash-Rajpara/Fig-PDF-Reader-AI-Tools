package com.app.figpdfconvertor.figpdf.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.funnelss.AnalyticsManager;
import com.app.figpdfconvertor.figpdf.utils.LocaleHelper;
import com.app.figpdfconvertor.figpdf.utils.MyApp;
import com.app.figpdfconvertor.figpdf.utils.NetworkReceiver;
import com.app.figpdfconvertor.figpdf.utils.NetworkUtils;
import com.app.figpdfconvertor.figpdf.utils.UtilsDialog;

import java.lang.ref.WeakReference;
import java.util.Collections;

public class BaseActivity extends AppCompatActivity {
    private boolean hasUserInteracted = false;
    public void userExplored() {
        hasUserInteracted = true;

        // Set global flag too
        ((MyApp)getApplication()).setUserHasExplored(true);
    }
    // <-- Add this getter
    public boolean hasUserInteracted() {
        return hasUserInteracted;
    }
    private WeakReference<Activity> mActivityReference;
    private NetworkReceiver networkReceiver;
    private Dialog noInternetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mActivityReference = new WeakReference<>(this);

        networkReceiver = new NetworkReceiver(isConnected -> {
            if (isConnected) {
                if (noInternetDialog != null && noInternetDialog.isShowing()) {
                    noInternetDialog.dismiss();
                }
            } else {
                showNoInternetDialog();
            }
        });

        if (!NetworkUtils.isInternetAvailable(this)) {
            showNoInternetDialog();
        }
        AnalyticsManager.INSTANCE.logEvent("screen_view",
                Collections.singletonMap("screen", getClass().getSimpleName()));
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userExplored(); // any touch, click, scroll counts
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
        if (isFinishing()) {
            trackExit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityReference.clear();
        mActivityReference = null;
    }

    private void showNoInternetDialog() {
        Activity activity = mActivityReference.get();
        if (activity == null || activity.isFinishing()) return;

        if (noInternetDialog != null && noInternetDialog.isShowing()) {
            return;
        }

        noInternetDialog = UtilsDialog.generateDialogFull(this, R.layout.dialog_no_internet);
        if (noInternetDialog != null) {
            noInternetDialog.setCancelable(false);

            noInternetDialog.findViewById(R.id.btnRetry).setOnClickListener(v -> {
                if (NetworkUtils.isInternetAvailable(this)) {
                    noInternetDialog.dismiss();
                } else {
                    Toast.makeText(this, "Please check again", Toast.LENGTH_SHORT).show();
                }
            });

            noInternetDialog.show();
        }
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(LocaleHelper.onAttach(context));
    }

    protected void trackExit() {
        boolean goodExit = ((MyApp)getApplication()).hasUserExplored();
        String exitType = goodExit ? "good_exit" : "bad_exit";

        AnalyticsManager.INSTANCE.logEvent(exitType,
                java.util.Collections.singletonMap("screen", getClass().getSimpleName()));

        AnalyticsManager.INSTANCE.logFunnelStep(exitType,
                java.util.Collections.singletonMap("screen", getClass().getSimpleName()));
    }
}
