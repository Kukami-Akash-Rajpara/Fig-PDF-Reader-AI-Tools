package com.app.figpdfconvertor.figpdf.utils;

import static android.app.Activity.RESULT_CANCELED;

import android.app.Activity;
import android.content.IntentSender;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.app.figpdfconvertor.figpdf.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

public class InAppUpdate {

    private final Activity parentActivity;
    private final AppUpdateManager appUpdateManager;
    private final int appUpdateType = AppUpdateType.IMMEDIATE; // Forced update
    private final int MY_REQUEST_CODE = 500;

    public InAppUpdate(Activity activity) {
        this.parentActivity = activity;
        appUpdateManager = AppUpdateManagerFactory.create(parentActivity);
    }

    InstallStateUpdatedListener stateUpdatedListener = installState -> {
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackBarForCompleteUpdate();
        }
    };

    public void checkForAppUpdate() {
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(info -> {
            boolean isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE;
            boolean isUpdateAllowed = info.isUpdateTypeAllowed(appUpdateType);

            if (isUpdateAvailable && isUpdateAllowed) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            info,
                            appUpdateType,
                            parentActivity,
                            MY_REQUEST_CODE
                    );
                } catch (IntentSender.SendIntentException e) {
                    // Log.e(TAG, "checkForAppUpdate: " + e.getMessage());
                }
            }
        });
        appUpdateManager.registerListener(stateUpdatedListener);
    }

    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode == RESULT_CANCELED) {
                System.out.println("Update canceled by user");
            } else if (resultCode != AppCompatActivity.RESULT_OK) {
                checkForAppUpdate();
            }
        }
    }

    private void popupSnackBarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        parentActivity.findViewById(R.id.mainLay),
                        "The latest update is now downloaded",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> {
            if (appUpdateManager != null) {
                appUpdateManager.completeUpdate();
            }
        });
        snackbar.setActionTextColor(ContextCompat.getColor(parentActivity, R.color.white));
        snackbar.show();
    }

    public void onResume() {
        if (appUpdateManager != null) {
            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(info -> {
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackBarForCompleteUpdate();
                }
            });
        }
    }

    public void onDestroy() {
        if (appUpdateManager != null) {
            appUpdateManager.unregisterListener(stateUpdatedListener);
        }
    }
}