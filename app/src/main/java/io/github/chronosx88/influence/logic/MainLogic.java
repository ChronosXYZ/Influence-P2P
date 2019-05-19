package io.github.chronosx88.influence.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.ChannelClientConfiguration;
import net.tomp2p.connection.ChannelServerConfiguration;
import net.tomp2p.connection.Ports;
import net.tomp2p.connection.RSASignatureFactory;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.Storage;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.nat.FutureRelayNAT;
import net.tomp2p.nat.PeerBuilderNAT;
import net.tomp2p.nat.PeerNAT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.relay.tcp.TCPRelayClientConfig;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.storage.Data;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.KeyPairManager;
import io.github.chronosx88.influence.helpers.LocalDBWrapper;
import io.github.chronosx88.influence.helpers.NetworkHandler;
import io.github.chronosx88.influence.helpers.ObservableUtils;
import io.github.chronosx88.influence.helpers.StorageBerkeleyDB;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.models.ChatMetadata;
import io.github.chronosx88.influence.models.NewChatRequestMessage;
import io.github.chronosx88.influence.models.PublicUserProfile;

public class MainLogic implements CoreContracts.IMainLogicContract {
    private static final String LOG_TAG = MainLogic.class.getName();

    private SharedPreferences preferences;
    private Number160 peerID;
    private PeerDHT peerDHT;
    private Context context;
    private InetAddress bootstrapAddress = null;
    private PeerAddress bootstrapPeerAddress = null;
    private Gson gson;
    private IndirectReplication replication;
    private KeyPairManager keyPairManager;
    private Thread checkNewChatsThread = null;
    private Storage storage;

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
                File dhtDBEnv = new File(context.getFilesDir(), "dhtDBEnv");
                if(!dhtDBEnv.exists())
                    dhtDBEnv.mkdirs();
                Storage storage = new StorageBerkeleyDB(peerID, dhtDBEnv, new RSASignatureFactory());
                this.storage = storage;
                peerDHT = new PeerBuilderDHT(
                        new PeerBuilder(peerID)
                                .ports(7243)
                                .channelClientConfiguration(createChannelClientConfig())
                                .channelServerConfiguration(createChannelServerConfig())
                                .start()
                )
                        .storage(storage)
                        .start();
                Runtime.getRuntime().addShutdownHook(new JVMShutdownHook(storage));
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

                if(discoverExternalAddress()) {
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
                AppHelper.updateUsername(preferences.getString("username", null));
                AppHelper.storePeerDHT(peerDHT);
                AppHelper.initNetworkHandler();
                //setReceiveHandler();
                gson = new Gson();
                publicProfileToDHT();
                SettingsLogic.Companion.publishUsername(AppHelper.getUsername(), AppHelper.getUsername());
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
                replication = new IndirectReplication(peerDHT).start();
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
                replication.shutdown();
            }
            if(peerDHT != null) {
                peerDHT.peer().announceShutdown().start().awaitUninterruptibly();
                peerDHT.peer().shutdown().awaitUninterruptibly();
            }
            storage.close();
            System.exit(0);
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
        PublicUserProfile userProfile = new PublicUserProfile(AppHelper.getUsername(), peerDHT.peerAddress());
        Data serializedUserProfile = null;
        try {
            serializedUserProfile = new Data(gson.toJson(userProfile))
                    .protectEntry(mainKeyPair.getPrivate());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(LOG_TAG, P2PUtils.put(AppHelper.getPeerID() + "_profile", null, serializedUserProfile, mainKeyPair) ? "# Profile successfully published!" : "# Profile publishing failed!");
    }

    private ChannelClientConfiguration createChannelClientConfig() {
        ChannelClientConfiguration channelClientConfiguration = new ChannelClientConfiguration();
        channelClientConfiguration.bindings(new Bindings());
        channelClientConfiguration.maxPermitsPermanentTCP(250);
        channelClientConfiguration.maxPermitsTCP(250);
        channelClientConfiguration.maxPermitsUDP(250);
        channelClientConfiguration.pipelineFilter(new PeerBuilder.DefaultPipelineFilter());
        channelClientConfiguration.signatureFactory(new RSASignatureFactory());
        channelClientConfiguration.senderTCP((new InetSocketAddress(0)).getAddress());
        channelClientConfiguration.senderUDP((new InetSocketAddress(0)).getAddress());
        channelClientConfiguration.byteBufPool(false);
        return channelClientConfiguration;
    }

    private ChannelServerConfiguration createChannelServerConfig() {
        ChannelServerConfiguration channelServerConfiguration = new ChannelServerConfiguration();
        channelServerConfiguration.bindings(new Bindings());
        //these two values may be overwritten in the peer builder
        channelServerConfiguration.ports(new Ports(Ports.DEFAULT_PORT, Ports.DEFAULT_PORT));
        channelServerConfiguration.portsForwarding(new Ports(Ports.DEFAULT_PORT, Ports.DEFAULT_PORT));
        channelServerConfiguration.behindFirewall(false);
        channelServerConfiguration.pipelineFilter(new PeerBuilder.DefaultPipelineFilter());
        channelServerConfiguration.signatureFactory(new RSASignatureFactory());
        channelServerConfiguration.byteBufPool(false);
        return channelServerConfiguration;
    }

    @Override
    public void sendStartChatMessage(@NotNull String username) {
        if(AppHelper.getPeerDHT() == null) {
            ObservableUtils.notifyUI(UIActions.NODE_IS_OFFLINE);
            return;
        }

        String companionPeerID = getPeerIDByUsername(username);
        if(companionPeerID == null) {
            ObservableUtils.notifyUI(UIActions.PEER_NOT_EXIST);
            return;
        }
        PublicUserProfile recipientPublicProfile = getPublicProfile(companionPeerID);
        if(recipientPublicProfile == null) {
            ObservableUtils.notifyUI(UIActions.PEER_NOT_EXIST);
            return;
        }

        NewChatRequestMessage newChatRequestMessage = new NewChatRequestMessage(UUID.randomUUID().toString(), UUID.randomUUID().toString(), AppHelper.getPeerID(), AppHelper.getUsername(), System.currentTimeMillis(), 0);
        try {
            if(P2PUtils.put(companionPeerID + "_pendingChats", newChatRequestMessage.getChatID(), new Data(gson.toJson(newChatRequestMessage)))) {
                Log.i(LOG_TAG, "# Create new offline chat request is successful! ChatID: " + newChatRequestMessage.getChatID());
            } else {
                Log.e(LOG_TAG, "# Failed to create offline chat request. ChatID: " + newChatRequestMessage.getChatID());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> admins = new ArrayList<>();
        admins.add(AppHelper.getPeerID());
        Data data = null;
        try {
            data = new Data(gson.toJson(new ChatMetadata(username, admins, new ArrayList<>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        data.protectEntry(keyPairManager.openMainKeyPair());
        P2PUtils.put(newChatRequestMessage.getChatID() + "_metadata", null, data);
        LocalDBWrapper.createChatEntry(newChatRequestMessage.getChatID(), username, newChatRequestMessage.getChatID() + "_metadata", newChatRequestMessage.getChatID() + "_members", 0);
        ObservableUtils.notifyUI(UIActions.NEW_CHAT);
    }

    private PublicUserProfile getPublicProfile(String peerID) {
        PublicUserProfile publicProfile = null;
        Map<Number640, Data> data = P2PUtils.get(peerID + "_profile");
        if (data != null && data.size() == 1) {
            try {
                publicProfile = gson.fromJson((String) data.values().iterator().next().object(), PublicUserProfile.class);
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
            return publicProfile;
        }
        return null;
    }

    private String getPeerIDByUsername(String username) {
        Map<Number640, Data> usernameMap = P2PUtils.get(username);
        if(usernameMap == null) {
            return null;
        }
        try {
            return (String) usernameMap.values().iterator().next().object();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
