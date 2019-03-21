package io.github.chronosx88.influence.logic;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FuturePing;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import io.github.chronosx88.influence.contracts.startchat.StartChatLogicContract;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.KeyPairManager;
import io.github.chronosx88.influence.helpers.PrepareData;
import io.github.chronosx88.influence.helpers.Serializer;
import io.github.chronosx88.influence.helpers.actions.NetworkActions;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.observable.MainObservable;

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
            JsonObject recipientPublicProfile = getPublicProfile(peerID);
            if(recipientPublicProfile == null) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", UIActions.PEER_NOT_EXIST);
                AppHelper.getObservable().notifyObservers(jsonObject, MainObservable.UI_ACTIONS_CHANNEL);
                return;
            }
            String peerAddressString = recipientPublicProfile.get("peerAddress").getAsString();
            PeerAddress peerAddress = Serializer.deserializeObject(new String(Base64.decode(peerAddressString, Base64.URL_SAFE), StandardCharsets.UTF_8));
            String chatID = UUID.randomUUID().toString();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", NetworkActions.START_CHAT);
            jsonObject.addProperty("chatID", chatID);
            jsonObject.addProperty("senderID", AppHelper.getPeerID());
            // TODO: Append public key to new chat request (for encryption)
            jsonObject.addProperty("senderAddress", PrepareData.prepareToStore(peerDHT.peerAddress()));

            FuturePing ping = peerDHT.peer().ping().peerAddress(peerAddress).start().awaitUninterruptibly();
            if(ping.isSuccess()) {
                peerDHT.peer().sendDirect(peerAddress).object(gson.toJson(jsonObject)).start();
            } else {
                try {
                    FuturePut futurePut = peerDHT
                            .put(Number160.createHash(peerID))
                            .data(Number160.createHash(UUID.randomUUID().toString()), new Data(gson.toJson(jsonObject))
                                    .protectEntry(keyPairManager.openMainKeyPair())).start().awaitUninterruptibly();
                    if(futurePut.isSuccess()) {
                        Log.i(LOG_TAG, "# Create new offline chat request is successful! ChatID: " + chatID);
                    } else {
                        Log.e(LOG_TAG, "# Failed to create chat: " + futurePut.failedReason());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private JsonObject getPublicProfile(String peerID) {
        JsonObject publicProfile;
        FutureGet futureGetProfile = peerDHT.get(Number160.createHash(peerID + "_profile")).start().awaitUninterruptibly();
        if (futureGetProfile.isSuccess()) {
            String jsonString = null;
            try {
                jsonString = (String) futureGetProfile.data().object();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            publicProfile = new JsonParser().parse(jsonString).getAsJsonObject();
            return publicProfile;
        }
        return null;
    }
}
