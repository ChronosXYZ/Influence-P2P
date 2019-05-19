package io.github.chronosx88.influence.helpers;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.tomp2p.dht.PeerDHT;

import androidx.multidex.MultiDexApplication;
import androidx.room.Room;

import com.instacart.library.truetime.TrueTime;

import java.io.IOException;

import io.github.chronosx88.influence.observable.MainObservable;

/**
 * Extended Application class which designed for centralized getting various objects from anywhere in the application.
 */

public class AppHelper extends MultiDexApplication {
    private static Application instance;
    private static MainObservable observable;
    public final static String APP_NAME = "Influence";

    private static String jid;
    private static RoomHelper chatDB;
    private static SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        observable = new MainObservable();
        chatDB = Room.databaseBuilder(getApplicationContext(), RoomHelper.class, "chatDB")
                    .allowMainThreadQueries()
                    .build();
        preferences = PreferenceManager.getDefaultSharedPreferences(instance);
        new Thread(() -> {
            try {
                TrueTime.build().initialize();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static MainObservable getObservable() { return observable; }

    public static String getJid() { return jid; }

    public static void setJid(String jid1) { jid = jid1; }

    public static RoomHelper getChatDB() { return chatDB; }

    public static SharedPreferences getPreferences() {
        return preferences;
    }
}