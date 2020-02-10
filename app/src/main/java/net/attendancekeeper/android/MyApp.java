package net.attendancekeeper.android;

import android.app.Application;

import com.beardedhen.androidbootstrap.TypefaceProvider;

public class MyApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        TypefaceProvider.registerDefaultIconSets();
    }
}
