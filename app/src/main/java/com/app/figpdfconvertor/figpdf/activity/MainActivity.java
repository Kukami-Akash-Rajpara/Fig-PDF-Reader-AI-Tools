package com.app.figpdfconvertor.figpdf.activity;

import static com.app.figpdfconvertor.figpdf.ads.ConstantKt.checkCountry;
import static com.app.figpdfconvertor.figpdf.ads.ConstantKt.getGoogleMobileAdsConsentManager;
import static com.app.figpdfconvertor.figpdf.ads.ConstantKt.isNetworkAvailable;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.ads.GoogleMobileAdsConsentManager;
import com.app.figpdfconvertor.figpdf.databinding.ActivityMainBinding;
import com.app.figpdfconvertor.figpdf.funnelss.AnalyticsManager;
import com.app.figpdfconvertor.figpdf.utils.BottomNavHelper;
import com.app.figpdfconvertor.figpdf.utils.InAppUpdate;
import com.app.figpdfconvertor.figpdf.utils.MyApp;
import com.app.figpdfconvertor.figpdf.utils.MyUtils;
import com.google.android.ump.FormError;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import np.com.susanthapa.curved_bottom_navigation.CbnMenuItem;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private ActivityResultLauncher<String> requestNotificationPermissionLauncher;
    private InAppUpdate inAppUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtils.fullScreenLightStatusBar(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainLay, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (isNetworkAvailable(this)) {
            try {
                if (checkCountry()) {
                    getGoogleMobileAdsConsentManager().gatherConsent(this, new GoogleMobileAdsConsentManager.OnConsentGatheringCompleteListener() {
                        @Override
                        public void consentGatheringComplete(FormError error) {
                            if (error == null) {
                                // If consent is required → only load when allowed
                                if (getGoogleMobileAdsConsentManager().isConsentRequired()) {
                                    if (getGoogleMobileAdsConsentManager().canRequestAds()) {
                                        MyApp.getInstance().loadPreferredAds();
                                    } else {
                                        Log.e("Consent", "Consent required but not granted.");
                                    }
                                } else {
                                    // ✅ If consent is NOT required → always load ads
                                    MyApp.getInstance().loadPreferredAds();
                                }
                            } else {
                                Log.e("Consent", "Consent gathering failed: " + error.getMessage());
                            }
                        }
                    });
                }else{
                    MyApp.getInstance().loadPreferredAds();
                }
            } catch (Exception e) {
                // progressBar.setVisibility(View.GONE);
                e.printStackTrace();
            }
        } else {
            // progressBar.setVisibility(View.GONE);
        }

        // AnalyticsManager.INSTANCE.logEvent("home_viewed", null);
        AnalyticsManager.INSTANCE.logFunnelStep("home_viewed", null);
        CbnMenuItem[] menuItems = BottomNavHelper.getMenuItems();

        setupBackPressed();

        inAppUpdate = new InAppUpdate(MainActivity.this);
        inAppUpdate.checkForAppUpdate();

        // Get NavController from the NavHostFragment
        NavController navController = NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment));

        // Set menu items with default active tab (e.g., activeIndex = 2 for Home)
        int activeIndex = 0;
        binding.navView.setMenuItems(menuItems, activeIndex);
        requestNotificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                    } else {
                    }
                }
        );

        // Check and request permission
        checkNotificationPermission();

        // Link navigation controller
        binding.navView.setupWithNavController(navController);

        //click event to handle the menu
        final int[] DESTS = {
                R.id.homeFragment,
                R.id.communityFragment,
                R.id.historyFragment,
                R.id.settingsFragment
        };

        binding.navView.setOnMenuItemClickListener(new Function2<CbnMenuItem, Integer, Unit>() {
            @Override
            public Unit invoke(CbnMenuItem item, Integer index) {
                if (index >= 0 && index < DESTS.length) {
                    navController.navigate(DESTS[index]);

                    String tabName;
                    switch (index) {
                        case 0:
                            tabName = "home_tab";
                            break;
                        case 1:
                            tabName = "community_tab";
                            break;
                        case 2:
                            tabName = "history_tab";
                            break;
                        case 3:
                            tabName = "settings_tab";
                            break;
                        default:
                            tabName = "home_tab";
                    }
                    AnalyticsManager.INSTANCE.logEvent("tab_changed",
                            java.util.Collections.singletonMap("tab", tabName));

                    AnalyticsManager.INSTANCE.logFunnelStep("tab_changed",
                            java.util.Collections.singletonMap("tab", tabName));
                }

                return Unit.INSTANCE; //  Kotlin Unit, not boolean
            }
        });
    }
    private void checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        inAppUpdate.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        inAppUpdate.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        inAppUpdate.onActivityResult(requestCode, resultCode);
    }

    private AlertDialog exitDialog;

    private void setupBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Get current tab index
                int currentIndex = binding.navView.getSelectedIndex(); // get current tab

                if (currentIndex == 0) {
                    // Home tab → show exit dialog
                    showExitDialog(MainActivity.this);
                } else {
                    // Any other tab → switch to Home tab
                    binding.navView.onMenuItemClick(0); // index 0 = Home tab

                    // Navigate to HomeFragment if using NavController
                    NavController navController = NavHostFragment.findNavController(
                            getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)
                    );
                    navController.navigate(R.id.homeFragment);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }





    public void showExitDialog(Context context) {
        if (exitDialog != null && exitDialog.isShowing()) return; // avoid multiple dialogs

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_exit, null);

        RelativeLayout cancelBtn = dialogView.findViewById(R.id.cancelButton);
        RelativeLayout okBtn = dialogView.findViewById(R.id.okButton);

        exitDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (exitDialog.getWindow() != null) {
            exitDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        cancelBtn.setOnClickListener(v -> exitDialog.dismiss());

        okBtn.setOnClickListener(v -> {
            trackExit();
            finish();
            exitDialog.dismiss();
        });

        exitDialog.show();
    }
}