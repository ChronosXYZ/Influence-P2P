package io.github.chronosx88.influence.helpers;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import net.tomp2p.dht.PeerDHT;

import androidx.multidex.MultiDexApplication;
import androidx.room.Room;

import com.instacart.library.truetime.TrueTime;

import java.io.IOException;

import io.github.chronosx88.influence.notificationSystem.NotificationSystem;
import io.github.chronosx88.influence.observable.MainObservable;
import rice.environment.Environment;
import rice.pastry.PastryNode;

/**
 * Extended Application class which designed for centralized getting various objects from anywhere in the application.
 */

public class AppHelper extends MultiDexApplication {
    private static Application instance;
    private static MainObservable observable;
    private static String peerID;
    private static PeerDHT peerDHT;
    private static RoomHelper chatDB;
    private static String username = "";
    private static SharedPreferences preferences;
    private static PastryNode pastryNode;
    private static Environment pastryEnvironment;
    private static NotificationSystem notificationSystem;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        observable = new MainObservable();
        chatDB = Room.databaseBuilder(getApplicationContext(), RoomHelper.class, "chatDB")
                    .allowMainThreadQueries()
                    .build();
        preferences = getApplicationContext().getSharedPreferences("io.github.chronosx88.influence_preferences", MODE_PRIVATE);
        new Thread(() -> {
            try {
                TrueTime.build().initialize();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
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

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    public static void storePastryNode(PastryNode node) { pastryNode = node; }

    public static PastryNode getPastryNode() { return pastryNode; }

    public static void storePastryEnvironment(Environment env) {
        pastryEnvironment = env;
    }

    public static Environment getPastryEnvironment() {
        return pastryEnvironment;
    }

    public static void storeNotificationSystem(NotificationSystem system) {
        notificationSystem = system;
    }

    public static NotificationSystem getNotificationSystem() {
        return notificationSystem;
    }
}