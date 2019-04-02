package io.github.chronosx88.influence.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.tomp2p.connection.RSASignatureFactory;
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
import net.tomp2p.replication.AutoReplication;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageDisk;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import io.github.chronosx88.influence.contracts.mainactivity.IMainLogicContract;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.DSAKey;
import io.github.chronosx88.influence.helpers.JVMShutdownHook;
import io.github.chronosx88.influence.helpers.KeyPairManager;
import io.github.chronosx88.influence.helpers.NetworkHandler;
import io.github.chronosx88.influence.helpers.P2PUtils;
import io.github.chronosx88.influence.helpers.StorageMVStore;
import io.github.chronosx88.influence.helpers.StorageMapDB;
import io.github.chronosx88.influence.helpers.actions.NetworkActions;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.models.PublicUserProfile;

public class MainLogic implements IMainLogicContract {
    private static final String LOG_TAG = "MainLogic";

    private SharedPreferences preferences;
    private Number160 peerID;
    private PeerDHT peerDHT;
    private Context context;
    private InetAddress bootstrapAddress = null;
    private PeerAddress bootstrapPeerAddress = null;
    private Gson gson;
    private AutoReplication replication;
    private KeyPairManager keyPairManager;
    private Thread checkNewChatsThread = null;

    public MainLogic() {
        this.context = AppHelper.getContext();
        this.preferences = context.getSharedPreferences("io.github.chronosx88.influence_preferences", context.MODE_PRIVATE);
        gson = new Gson();
        keyPairManager = new KeyPairManager();
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
                    AppHelper.getObservable().notifyUIObservers(jsonObject);
                    peerDHT.shutdown();
                    return;
                } catch (UnknownHostException e) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", UIActions.NETWORK_ERROR);
                    AppHelper.getObservable().notifyUIObservers(jsonObject);
                    peerDHT.shutdown();
                    return;
                }

                boolean discoveredExternalAddress = false;

                if(!discoverExternalAddress()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", UIActions.PORT_FORWARDING_ERROR);
                    AppHelper.getObservable().notifyUIObservers(jsonObject);
                } else {
                    discoveredExternalAddress = true;
                }

                if(!discoveredExternalAddress) {
                    if(!setupConnectionToRelay()) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", UIActions.RELAY_CONNECTION_ERROR);
                        AppHelper.getObservable().notifyUIObservers(jsonObject);
                        return;
                    }
                }

                if(!bootstrapPeer()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", UIActions.BOOTSTRAP_ERROR);
                    AppHelper.getObservable().notifyUIObservers(jsonObject);
                    return;
                }

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", UIActions.BOOTSTRAP_SUCCESS);
                AppHelper.getObservable().notifyUIObservers(jsonObject);
                AppHelper.storePeerID(preferences.getString("peerID", null));
                AppHelper.storePeerDHT(peerDHT);
                AppHelper.initNetworkHandler();
                setReceiveHandler();
                gson = new Gson();
                publicProfileToDHT();
                NetworkHandler.handlePendingChatRequests();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if(checkNewChatsThread == null) {
                            checkNewChatsThread = new Thread(NetworkHandler::handlePendingChatRequests);
                            checkNewChatsThread.start();
                        }
                        if(!checkNewChatsThread.isAlive()) {
                            checkNewChatsThread = new Thread(NetworkHandler::handlePendingChatRequests);
                            checkNewChatsThread.start();
                        }
                    }
                };
                Timer timer = new Timer();
                timer.schedule(timerTask, 1, 5000);
                replication = new AutoReplication(peerDHT.peer()).start();
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
            AppHelper.getObservable().notifyNetworkObservers(r);
            return null;
        });
    }

    @Override
    public void shutdownPeer() {
        new Thread(() -> {
            if(replication != null) {
                replication.shutdown().start();
            }
            peerDHT.peer().announceShutdown().start().awaitUninterruptibly();
            peerDHT.peer().shutdown();
        }).start();
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

    private void publicProfileToDHT() {
        KeyPair mainKeyPair = keyPairManager.openMainKeyPair();
        KeyFactory factory = null;
        try {
            factory = KeyFactory.getInstance("DSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        PublicUserProfile userProfile = null;
        try {
            DSAPublicKeySpec dsaKey = factory.getKeySpec(mainKeyPair.getPublic(), DSAPublicKeySpec.class);
            userProfile = new PublicUserProfile(AppHelper.getPeerID(), peerDHT.peerAddress(), new DSAKey(dsaKey.getQ(), dsaKey.getP(), dsaKey.getY(), dsaKey.getG()));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        Data serializedUserProfile = null;
        try {
            serializedUserProfile = new Data(gson.toJson(userProfile))
                    .protectEntry(mainKeyPair.getPrivate())
                    .sign(keyPairManager.getKeyPair("mainSigningKeyPair"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(LOG_TAG, P2PUtils.put(AppHelper.getPeerID() + "_profile", null, serializedUserProfile) ? "# Profile successfully published!" : "# Profile publishing failed!");
    }
}
