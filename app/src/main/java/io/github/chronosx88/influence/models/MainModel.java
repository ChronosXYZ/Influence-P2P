package io.github.chronosx88.influence.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.nat.FutureNAT;
import net.tomp2p.nat.FutureRelayNAT;
import net.tomp2p.nat.PeerBuilderNAT;
import net.tomp2p.nat.PeerNAT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.relay.tcp.TCPRelayClientConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import io.github.chronosx88.influence.contracts.MainModelContract;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.MessageActions;
import io.github.chronosx88.influence.helpers.StorageMVStore;

public class MainModel implements MainModelContract {
    private SharedPreferences preferences;
    private Number160 peerID;
    private PeerDHT peerDHT;
    private Context context;

    public MainModel() {
        this.context = AppHelper.getContext();
        this.preferences = context.getSharedPreferences("io.github.chronosx88.influence_preferences", context.MODE_PRIVATE);
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
                InetAddress bootstrapAddress = null;

                peerDHT = new PeerBuilderDHT(
                        new PeerBuilder(peerID)
                                .ports(7243)
                                .behindFirewall()
                                .start()
                )
                        .storage(new StorageMVStore(peerID, context.getFilesDir()))
                        .start();
                PeerNAT peerNAT = new PeerBuilderNAT(peerDHT.peer()).start();
                try {
                    String bootstrapIP = this.preferences.getString("bootstrapAddress", null);
                    if(bootstrapIP == null) {
                        throw new NullPointerException();
                    }
                    bootstrapAddress = Inet4Address.getByName(bootstrapIP);
                } catch (NullPointerException e) {
                    try {
                        AppHelper.getObservable().notifyObservers(new JSONObject()
                                .put("action", MessageActions.BOOTSTRAP_NOT_SPECIFIED));
                        peerDHT.shutdown();
                        return;
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                } catch (UnknownHostException e) {
                    try {
                        AppHelper.getObservable().notifyObservers(new JSONObject()
                                .put("action", MessageActions.NETWORK_ERROR));
                        peerDHT.shutdown();
                        return;
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }

                FutureDiscover futureDiscover = peerDHT.peer().discover().inetAddress(bootstrapAddress).ports(7243).start();
                FutureNAT futureNAT = peerNAT.startSetupPortforwarding(futureDiscover);
                FutureRelayNAT futureRelayNAT = peerNAT.startRelay(new TCPRelayClientConfig(), futureDiscover, futureNAT);
                futureRelayNAT.awaitUninterruptibly();

                if(futureDiscover.isSuccess()) {
                    Log.d("MainModel", "# Success discover! Your IP: " + futureDiscover.peerAddress());
                } else {
                    Log.d("MainModel", "# Error when discovering: " + futureDiscover.failedReason());
                    try {
                        AppHelper.getObservable().notifyObservers(new JSONObject()
                                .put("action", MessageActions.PORT_FORWARDING_ERROR));
                        peerDHT.shutdown();
                        return;
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }

                if(futureNAT.isSuccess()) {
                    Log.d("MainModel", "# Success NAT discover! Your IP: " + futureNAT.peerAddress());
                } else {
                    try {
                        Log.d("MainModel", "# Error when discovering: " + futureNAT.failedReason());
                        AppHelper.getObservable().notifyObservers(new JSONObject()
                                .put("action", MessageActions.PORT_FORWARDING_ERROR));
                        peerDHT.shutdown();
                        return;
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }

                if(futureRelayNAT.isSuccess()) {
                    Log.d("MainModel", "# Success discover with relay!");
                } else {
                    try {
                        Log.d("MainModel", "# Error when discovering: " + futureRelayNAT.failedReason());
                        AppHelper.getObservable().notifyObservers(new JSONObject()
                                .put("action", MessageActions.PORT_FORWARDING_ERROR));
                        peerDHT.shutdown();
                        return;
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }

                FutureBootstrap futureBootstrap = peerDHT.peer().bootstrap().inetAddress(bootstrapAddress).ports(7243).start();
                futureBootstrap.awaitUninterruptibly();
                if(futureBootstrap.isSuccess()) {
                    try {
                        AppHelper.getObservable().notifyObservers(new JSONObject()
                                .put("action", MessageActions.BOOTSTRAP_SUCCESS));
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
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
