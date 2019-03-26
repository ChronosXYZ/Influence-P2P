package io.github.chronosx88.influence.logic;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FuturePing;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import io.github.chronosx88.influence.contracts.startchat.StartChatLogicContract;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.KeyPairManager;
import io.github.chronosx88.influence.helpers.LocalDBWrapper;
import io.github.chronosx88.influence.helpers.NetworkHandler;
import io.github.chronosx88.influence.helpers.ObservableUtils;
import io.github.chronosx88.influence.helpers.P2PUtils;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.models.NewChatRequestMessage;
import io.github.chronosx88.influence.models.PublicUserProfile;

public class StartChatLogic implements StartChatLogicContract {
    private PeerDHT peerDHT;
    private Gson gson;
    private KeyPairManager keyPairManager;
    private final static String LOG_TAG = "StartChatLogic";

    public StartChatLogic() {
        peerDHT = AppHelper.getPeerDHT();
        gson = new Gson();
        keyPairManager = new KeyPairManager();
    }

    @Override
    public void sendStartChatMessage(String peerID) {
        new Thread(() -> {
            PublicUserProfile recipientPublicProfile = getPublicProfile(peerID);
            if(recipientPublicProfile == null) {
                ObservableUtils.notifyUI(UIActions.PEER_NOT_EXIST);
                return;
            }
            PeerAddress recipientPeerAddress = getPublicProfile(peerID).getPeerAddress();

            NewChatRequestMessage newChatRequestMessage = new NewChatRequestMessage(UUID.randomUUID().toString(), AppHelper.getPeerID(), peerDHT.peerAddress());
            if(P2PUtils.ping(recipientPeerAddress)) {
                peerDHT.peer().sendDirect(recipientPeerAddress).object(gson.toJson(newChatRequestMessage)).start().awaitUninterruptibly();
            } else {
                try {
                    if(P2PUtils.put(peerID + "_pendingChats", newChatRequestMessage.getChatID(), new Data(gson.toJson(newChatRequestMessage)))) {
                        Log.i(LOG_TAG, "# Create new offline chat request is successful! ChatID: " + newChatRequestMessage.getChatID());
                    } else {
                        Log.e(LOG_TAG, "# Failed to create offline chat request. ChatID: " + newChatRequestMessage.getChatID());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            LocalDBWrapper.createChatEntry(newChatRequestMessage.getChatID(), peerID, recipientPeerAddress);
            ObservableUtils.notifyUI(UIActions.NEW_CHAT);
        }).start();
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
}
