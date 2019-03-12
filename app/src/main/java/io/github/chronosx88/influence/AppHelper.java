package io.github.chronosx88.influence;

import android.app.Application;
import android.content.Context;

import io.github.chronosx88.influence.observable.MainObservable;

/**
 * Extended Application class which designed for getting Context from anywhere in the application.
 */

public class AppHelper extends Application {
    private static Application instance;
    private static MainObservable observable;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        observable = new MainObservable();
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static MainObservable getObservable() { return observable; }
}