package io.github.chronosx88.influence.helpers;

import android.app.Application;
import android.content.Context;

import net.tomp2p.dht.PeerDHT;

import io.github.chronosx88.influence.observable.MainObservable;

/**
 * Extended Application class which designed for getting Context from anywhere in the application.
 */

public class AppHelper extends Application {
    private static Application instance;
    private static MainObservable observable;
    private static String peerID;
    private static PeerDHT peerDHT;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        observable = new MainObservable();
    }

    public static void storePeerID(String peerID1) { peerID = peerID1; }

    public static void storePeerDHT(PeerDHT peerDHT1) { peerDHT = peerDHT1; }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static MainObservable getObservable() { return observable; }

    public static String getPeerID() { return peerID; }

    public static PeerDHT getPeerDHT() { return peerDHT; }
}