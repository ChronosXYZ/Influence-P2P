package io.github.chronosx88.influence.helpers;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import net.tomp2p.dht.PeerDHT;

import androidx.multidex.MultiDexApplication;
import androidx.room.Room;
import io.github.chronosx88.influence.observable.MainObservable;

/**
 * Extended Application class which designed for centralized getting various objects from anywhere in the application.
 */

public class AppHelper extends MultiDexApplication {
    private static Application instance;
    private static MainObservable observable;
    private static String peerID;
    private static PeerDHT peerDHT;
    private static RoomHelper chatDB;
    private static NetworkHandler networkHandler;
    private static String username = "";
    private static SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        observable = new MainObservable();
        chatDB = Room.databaseBuilder(getApplicationContext(), RoomHelper.class, "chatDB")
                    .allowMainThreadQueries()
                    .build();
        preferences = getApplicationContext().getSharedPreferences("io.github.chronosx88.influence_preferences", MODE_PRIVATE);
    }

    public static void storePeerID(String peerID1) { peerID = peerID1; }

    public static void updateUsername(String username1) { username = username1; }

    public static void storePeerDHT(PeerDHT peerDHT1) { peerDHT = peerDHT1; }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static MainObservable getObservable() { return observable; }

    public static String getPeerID() { return peerID; }

    public static String getUsername() { return username; }

    public static PeerDHT getPeerDHT() { return peerDHT; }

    public static RoomHelper getChatDB() { return chatDB; }

    public static void initNetworkHandler() { networkHandler = new NetworkHandler(); }

    public static SharedPreferences getPreferences() {
        return preferences;
    }
}