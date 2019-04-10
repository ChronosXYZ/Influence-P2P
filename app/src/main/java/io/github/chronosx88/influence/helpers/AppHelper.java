package io.github.chronosx88.influence.helpers;

import android.app.Application;
import android.content.Context;

import net.tomp2p.dht.PeerDHT;

import androidx.multidex.MultiDexApplication;
import androidx.room.Room;
import io.github.chronosx88.influence.observable.MainObservable;

/**
 * Extended Application class which designed for getting various objects from anywhere in the application.
 */

public class AppHelper extends MultiDexApplication {
    private static Application instance;
    private static MainObservable observable;
    private static String peerID;
    private static PeerDHT peerDHT;
    private static RoomHelper chatDB;
    private static NetworkHandler networkHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        observable = new MainObservable();
        chatDB = Room.databaseBuilder(getApplicationContext(), RoomHelper.class, "chatDB")
                    .allowMainThreadQueries()
                    .build();
    }

    public static void storePeerID(String peerID1) { peerID = peerID1; }

    public static void storePeerDHT(PeerDHT peerDHT1) { peerDHT = peerDHT1; }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static MainObservable getObservable() { return observable; }

    public static String getPeerID() { return peerID; }

    public static PeerDHT getPeerDHT() { return peerDHT; }

    public static RoomHelper getChatDB() { return chatDB; }

    public static void initNetworkHandler() { networkHandler = new NetworkHandler(); }
}