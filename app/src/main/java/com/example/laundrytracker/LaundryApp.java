package com.example.laundrytracker;

import android.app.Application;
import androidx.annotation.NonNull;
import com.example.laundrytracker.util.AppLogger;
import com.google.android.material.color.DynamicColors;

public class LaundryApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
        
        AppLogger.init(this);
        
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            AppLogger.e("Crash", "Uncaught exception on thread " + thread.getName(), throwable);
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }
}
