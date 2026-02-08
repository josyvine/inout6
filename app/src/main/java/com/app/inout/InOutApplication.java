package com.inout.app;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.inout.app.utils.FirebaseManager;

/**
 * The custom Application class for InOut.
 * This is the entry point of the application process.
 * Its main responsibility is to initialize components that are needed globally,
 * specifically our dynamic Firebase configuration and AdMob SDK.
 */
public class InOutApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize our custom FirebaseManager.
        // This manager will attempt to load a stored Firebase configuration (encrypted)
        // if one exists, allowing dynamic project switching.
        FirebaseManager.initialize(this);

        // NEW: Initialize Google Mobile Ads SDK for Banner Ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                // SDK Initialized successfully
            }
        });
    }
}