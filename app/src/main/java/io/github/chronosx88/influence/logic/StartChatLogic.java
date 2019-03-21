package io.github.chronosx88.influence.logic;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FuturePing;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import io.github.chronosx88.influence.contracts.startchat.StartChatLogicContract;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.PrepareData;
import io.github.chronosx88.influence.helpers.Serializer;
import io.github.chronosx88.influence.helpers.actions.NetworkActions;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.observable.MainObservable;

public class StartChatLogic implements StartChatLogicContract {
    private PeerDHT peerDHT;
    private Gson gson;

    public StartChatLogic() {
        peerDHT = AppHelper.getPeerDHT();
        gson = new Gson();
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
            FuturePing ping = peerDHT.peer().ping().peerAddress(peerAddress).start().awaitUninterruptibly();
            if(ping.isSuccess()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", NetworkActions.START_CHAT);
                jsonObject.addProperty("chatID", UUID.randomUUID().toString());
                // TODO: Append public key to new chat request (for encryption)
                jsonObject.addProperty("senderAddress", PrepareData.prepareToStore(peerDHT.peerAddress()));
                peerDHT.peer().sendDirect(peerAddress).object(gson.toJson(jsonObject)).start();
            } else {
                // TODO: put chat entry to "*peerID*_newChats". That peer later will fetch this new chats
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
