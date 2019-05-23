package io.github.chronosx88.influence.helpers;

import android.app.Application;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import androidx.multidex.MultiDexApplication;
import androidx.room.Room;

import com.instacart.library.truetime.TrueTime;

import java.io.IOException;

import io.github.chronosx88.influence.LoginCredentials;
import io.github.chronosx88.influence.XMPPConnection;

/**
 * Extended Application class which designed for centralized getting various objects from anywhere in the application.
 */
public class AppHelper extends MultiDexApplication {
    private static Application instance;
    public final static String APP_NAME = "Influence";
    public final static String DEFAULT_NTP_SERVER = "0.europe.pool.ntp.org";

    private static String jid;
    private static RoomHelper chatDB;
    private static SharedPreferences preferences;
    private static XMPPConnection xmppConnection;
    private static LoginCredentials currentLoginCredentials;
    private static Handler mainUIThreadHandler;
    private static ServiceConnection serviceConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        mainUIThreadHandler = new Handler(Looper.getMainLooper());
        initChatDB();
        preferences = PreferenceManager.getDefaultSharedPreferences(instance);
        initTrueTime();
        loadLoginCredentials();
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static String getJid() { return jid; }

    public static void setJid(String jid1) { jid = jid1; }

    public static RoomHelper getChatDB() { return chatDB; }

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    public static XMPPConnection getXmppConnection() {
        return xmppConnection;
    }

    public static void setXmppConnection(XMPPConnection xmppConnection) {
        AppHelper.xmppConnection = xmppConnection;
    }

    private static void loadLoginCredentials() {
        currentLoginCredentials = new LoginCredentials();
        String jid = preferences.getString("jid", null);
        String password = preferences.getString("pass", null);
        if(jid != null && password != null) {
            String username = jid.split("@")[0];
            String jabberHost = jid.split("@")[1];
            currentLoginCredentials.username = username;
            currentLoginCredentials.jabberHost = jabberHost;
            currentLoginCredentials.password = password;
        }
        AppHelper.setJid(currentLoginCredentials.username + "@" + currentLoginCredentials.jabberHost);
    }

    private static void initTrueTime() {
        new Thread(() -> {
            boolean isTrueTimeIsOn = false;
            while(!isTrueTimeIsOn) {
                try {
                    TrueTime.build().withNtpHost(DEFAULT_NTP_SERVER).initialize();
                    isTrueTimeIsOn = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initChatDB() {
        chatDB = Room.databaseBuilder(getApplicationContext(), RoomHelper.class, "chatDB")
                .allowMainThreadQueries()
                .build();
    }

    public static Handler getMainUIThread() {
        return mainUIThreadHandler;
    }

    public static ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public static void setServiceConnection(ServiceConnection serviceConnection) {
        AppHelper.serviceConnection = serviceConnection;
    }
}