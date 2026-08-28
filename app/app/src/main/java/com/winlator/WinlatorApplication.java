package com.winlator;

import android.app.Application;
import com.winlator.core.AppCrashHandler;

public class WinlatorApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize global crash & logcat capture handler
        AppCrashHandler.init(this);
    }
}
