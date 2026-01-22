package com.inout.app;

import android.app.Application;

import com.inout.app.utils.FirebaseManager;

/**
 * The custom Application class for InOut.
 * This is the entry point of the application process.
 * Its main responsibility is to initialize components that are needed globally,
 * specifically our dynamic Firebase configuration.
 */
public class InOutApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize our custom FirebaseManager.
        // This manager will attempt to load a stored Firebase configuration (encrypted)
        // if one exists, allowing dynamic project switching.
        FirebaseManager.initialize(this);
    }
}