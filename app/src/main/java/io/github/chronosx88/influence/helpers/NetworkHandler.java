package io.github.chronosx88.influence.helpers;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.PeerAddress;

import java.util.List;

import io.github.chronosx88.influence.contracts.observer.NetworkObserver;
import io.github.chronosx88.influence.helpers.actions.NetworkActions;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.models.NewChatRequestMessage;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

public class NetworkHandler implements NetworkObserver {
    private final static String LOG_TAG = "NetworkHandler";
    private Gson gson;
    private PeerDHT peerDHT;

    public NetworkHandler() {
        gson = new Gson();
        peerDHT = AppHelper.getPeerDHT();
        AppHelper.getObservable().register(this);
    }

    @Override
    public void handleEvent(Object object) {
        new Thread(() -> {
            switch (getMessageAction((String) object)) {
                case NetworkActions.CREATE_CHAT: {
                    NewChatRequestMessage newChatRequestMessage = gson.fromJson((String) object, NewChatRequestMessage.class);
                    createChatEntry(newChatRequestMessage.getChatID(), newChatRequestMessage.getChatID(), newChatRequestMessage.getSenderPeerAddress());
                    handleIncomingChat(newChatRequestMessage.getChatID(), newChatRequestMessage.getSenderPeerAddress());
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", UIActions.NEW_CHAT);
                    AppHelper.getObservable().notifyUIObservers(jsonObject);
                    break;
                }

                case NetworkActions.SUCCESSFULL_CREATE_CHAT: {
                    NewChatRequestMessage newChatRequestMessage = gson.fromJson((String) object, NewChatRequestMessage.class);
                    createChatEntry(newChatRequestMessage.getChatID(), newChatRequestMessage.getSenderID(), newChatRequestMessage.getSenderPeerAddress());
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", UIActions.NEW_CHAT);
                    AppHelper.getObservable().notifyUIObservers(jsonObject);
                    break;
                }
            }
        }).start();
    }

    private int getMessageAction(String json) {
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        return jsonObject.get("action").getAsInt();
    }

    private void createChatEntry(String chatID, String name, PeerAddress peerAddress) {
        List<ChatEntity> chatEntities = AppHelper.getChatDB().chatDao().getChatByChatID(chatID);
        if (chatEntities.size() > 0) {
            Log.e(LOG_TAG, "Failed to create chat " + chatID + " because chat exists!");
            return;
        }
        AppHelper.getChatDB().chatDao().addChat(new ChatEntity(chatID, name, "", Serializer.serialize(peerAddress)));
    }

    private void handleIncomingChat(String chatID, PeerAddress chatStarterAddress) {
        NewChatRequestMessage newChatRequestMessage = new NewChatRequestMessage(AppHelper.getPeerID(), peerDHT.peerAddress());
        newChatRequestMessage.setChatID(chatID);
        newChatRequestMessage.setAction(NetworkActions.SUCCESSFULL_CREATE_CHAT);
        AppHelper.getPeerDHT().peer().sendDirect(chatStarterAddress).object(gson.toJson(newChatRequestMessage)).start().awaitUninterruptibly();
    }
}
