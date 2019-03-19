package io.github.chronosx88.influence.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.nat.FutureRelayNAT;
import net.tomp2p.nat.PeerBuilderNAT;
import net.tomp2p.nat.PeerNAT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.relay.tcp.TCPRelayClientConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import io.github.chronosx88.influence.contracts.main.MainLogicContract;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.NetworkActions;
import io.github.chronosx88.influence.helpers.StorageMVStore;
import io.github.chronosx88.influence.helpers.UIActions;
import io.github.chronosx88.influence.observable.MainObservable;

public class MainLogic implements MainLogicContract {
    private static final String LOG_TAG = "MainLogic";

    private SharedPreferences preferences;
    private Number160 peerID;
    private PeerDHT peerDHT;
    private Context context;
    private InetAddress bootstrapAddress = null;
    private PeerAddress bootstrapPeerAddress = null;
    private Gson gson;

    public MainLogic() {
        this.context = AppHelper.getContext();
        this.preferences = context.getSharedPreferences("io.github.chronosx88.influence_preferences", context.MODE_PRIVATE);
        gson = new Gson();
    }

    @Override
    public void initPeer() {
        org.apache.log4j.BasicConfigurator.configure();

        if(checkFirstRun()) {
            SharedPreferences.Editor editor = preferences.edit();
            String uuid = UUID.randomUUID().toString();
            editor.putString("peerID", uuid);
            editor.apply();
        }

        peerID = Number160.createHash(preferences.getString("peerID", null));

        new Thread(() -> {
            try {
                peerDHT = new PeerBuilderDHT(
                        new PeerBuilder(peerID)
                                .ports(7243)
                                .start()
                )
                        .storage(new StorageMVStore(peerID, context.getFilesDir()))
                        .start();
                try {
                    String bootstrapIP = this.preferences.getString("bootstrapAddress", null);
                    if(bootstrapIP == null) {
                        throw new NullPointerException();
                    }
                    bootstrapAddress = Inet4Address.getByName(bootstrapIP);
                } catch (NullPointerException e) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", UIActions.BOOTSTRAP_NOT_SPECIFIED);
                    AppHelper.getObservable().notifyObservers(jsonObject, MainObservable.UI_ACTIONS_CHANNEL);
                    peerDHT.shutdown();
                    return;
                } catch (UnknownHostException e) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", UIActions.NETWORK_ERROR);
                    AppHelper.getObservable().notifyObservers(jsonObject, MainObservable.UI_ACTIONS_CHANNEL);
                    peerDHT.shutdown();
                    return;
                }

                if(!discoverExternalAddress()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", UIActions.PORT_FORWARDING_ERROR);
                    AppHelper.getObservable().notifyObservers(jsonObject, MainObservable.UI_ACTIONS_CHANNEL);
                }

                if(!setupConnectionToRelay()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", UIActions.RELAY_CONNECTION_ERROR);
                    AppHelper.getObservable().notifyObservers(jsonObject, MainObservable.UI_ACTIONS_CHANNEL);
                    return;
                }

                if(!bootstrapPeer()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", UIActions.BOOTSTRAP_ERROR);
                    AppHelper.getObservable().notifyObservers(jsonObject, MainObservable.UI_ACTIONS_CHANNEL);
                    return;
                }

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", UIActions.BOOTSTRAP_SUCCESS);
                AppHelper.getObservable().notifyObservers(jsonObject, MainObservable.UI_ACTIONS_CHANNEL);
                AppHelper.storePeerID(preferences.getString("peerID", null));
                AppHelper.storePeerDHT(peerDHT);
                setReceiveHandler();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean bootstrapPeer() {
        FutureBootstrap futureBootstrap = peerDHT.peer().bootstrap().inetAddress(bootstrapAddress).ports(7243).start();
        futureBootstrap.awaitUninterruptibly();
        if(futureBootstrap.isSuccess()) {
            Log.i("MainLogic", "# Successfully bootstrapped to " + bootstrapAddress.toString());
            return true;
        } else {
            Log.e("MainLogic", "# Cannot bootstrap to " + bootstrapAddress.toString() + ". Reason: " + futureBootstrap.failedReason());
            return false;
        }
    }

    private boolean discoverExternalAddress() {
        FutureDiscover futureDiscover = peerDHT
                .peer()
                .discover()
                .inetAddress(bootstrapAddress)
                .ports(7243)
                .start();
        futureDiscover.awaitUninterruptibly();
        bootstrapPeerAddress = futureDiscover.reporter();
        if(futureDiscover.isSuccess()) {
            Log.i(LOG_TAG, "# Success discover! Your external IP: " + futureDiscover.peerAddress().toString());
            return true;
        } else {
            Log.e(LOG_TAG, "# Failed to discover my external IP. Reason: " + futureDiscover.failedReason());
            return false;
        }
    }

    private boolean setupConnectionToRelay() {
        PeerNAT peerNat = new PeerBuilderNAT(peerDHT.peer()).start();
        FutureRelayNAT futureRelayNAT = peerNat.startRelay(new TCPRelayClientConfig(), bootstrapPeerAddress).awaitUninterruptibly();
        if (futureRelayNAT.isSuccess()) {
            Log.i(LOG_TAG, "# Successfully connected to relay node.");
            return true;
        } else {
            Log.e(LOG_TAG, "# Cannot connect to relay node. Reason: " + futureRelayNAT.failedReason());
            return false;
        }
    }

    private void setReceiveHandler() {
        AppHelper.getPeerDHT().peer().objectDataReply((s, r) -> {
            Log.i(LOG_TAG, "# Incoming message: " + r);
            JSONObject incomingObject = new JSONObject((String) r);
            if(incomingObject.getInt("action") == NetworkActions.PING) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", NetworkActions.PONG);
                return gson.toJson(jsonObject);
            }
            AppHelper.getObservable().notifyObservers(new JsonParser().parse((String) r).getAsJsonObject(), MainObservable.OTHER_ACTIONS_CHANNEL);
            return null;
        });
    }

    @Override
    public void shutdownPeer() {
        peerDHT.shutdown();
    }

    private boolean checkFirstRun() {
        if (preferences.getBoolean("firstRun", true)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("firstRun", false);
            editor.apply();
            return true;
        }
        return false;
    }
}
