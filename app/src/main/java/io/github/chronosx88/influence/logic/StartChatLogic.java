package io.github.chronosx88.influence.logic;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FuturePing;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.UUID;

import io.github.chronosx88.influence.contracts.startchat.StartChatLogicContract;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.KeyPairManager;
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
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", UIActions.PEER_NOT_EXIST);
                AppHelper.getObservable().notifyUIObservers(jsonObject);
                return;
            }
            PeerAddress recipientPeerAddress = getPublicProfile(peerID).getPeerAddress();

            FuturePing ping = peerDHT.peer().ping().peerAddress(recipientPeerAddress).start().awaitUninterruptibly();
            if(ping.isSuccess()) {
                peerDHT.peer().sendDirect(recipientPeerAddress).object(gson.toJson(new NewChatRequestMessage(AppHelper.getPeerID(), peerDHT.peerAddress()))).start();
            } else {
                try {
                    NewChatRequestMessage newChatRequestMessage = new NewChatRequestMessage(AppHelper.getPeerID(), peerDHT.peerAddress());
                    FuturePut futurePut = peerDHT
                            .put(Number160.createHash(peerID))
                            .data(Number160.createHash(UUID.randomUUID().toString()), new Data(gson.toJson(newChatRequestMessage))
                                    .protectEntry(keyPairManager.openMainKeyPair())).start().awaitUninterruptibly();
                    if(futurePut.isSuccess()) {
                        Log.i(LOG_TAG, "# Create new offline chat request is successful! ChatID: " + newChatRequestMessage.getChatID());
                    } else {
                        Log.e(LOG_TAG, "# Failed to create chat: " + futurePut.failedReason());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private PublicUserProfile getPublicProfile(String peerID) {
        PublicUserProfile publicProfile = null;
        FutureGet futureGetProfile = peerDHT.get(Number160.createHash(peerID + "_profile")).start().awaitUninterruptibly();
        if (!futureGetProfile.isEmpty()) {
            String jsonString = null;
            try {
                jsonString = (String) futureGetProfile.data().object();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                publicProfile = gson.fromJson((String) futureGetProfile.data().object(), PublicUserProfile.class);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return publicProfile;
        }
        return null;
    }
}
