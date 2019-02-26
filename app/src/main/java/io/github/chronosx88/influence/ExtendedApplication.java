package io.github.chronosx88.influence;

import android.app.Application;
import android.content.Context;

/**
 * Extended Application class which designed for getting Context from anywhere in the application.
 */

public class ExtendedApplication extends Application {
    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }
}