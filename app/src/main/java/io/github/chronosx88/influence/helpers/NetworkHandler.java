package io.github.chronosx88.influence.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.PeerAddress;

import io.github.chronosx88.influence.contracts.observer.Observer;
import io.github.chronosx88.influence.helpers.actions.NetworkActions;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;
import io.github.chronosx88.influence.observable.MainObservable;

public class NetworkHandler implements Observer {
    private Gson gson;
    private PeerDHT peerDHT;

    public NetworkHandler() {
        gson = new Gson();
        peerDHT = AppHelper.getPeerDHT();
        AppHelper.getObservable().register(this, MainObservable.OTHER_ACTIONS_CHANNEL);
    }

    @Override
    public void handleEvent(JsonObject object) {
        new Thread(() -> {
            switch (object.get("action").getAsInt()) {
                case NetworkActions.START_CHAT: {
                    String chatStarterPlainAddress = object.get("senderAddress").getAsString();
                    createChatEntry(object.get("chatID").getAsString(), object.get("senderID").getAsString(), chatStarterPlainAddress);
                    handleIncomingChat(object.get("chatID").getAsString(), PrepareData.prepareFromStore(chatStarterPlainAddress));
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", UIActions.NEW_CHAT);
                    AppHelper.getObservable().notifyObservers(jsonObject, MainObservable.UI_ACTIONS_CHANNEL);
                    break;
                }

                case NetworkActions.SUCCESSFULL_CREATE_CHAT: {
                    createChatEntry(object.get("chatID").getAsString(), object.get("senderID").getAsString(), object.get("senderAddress").getAsString());
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", UIActions.NEW_CHAT);
                    AppHelper.getObservable().notifyObservers(jsonObject, MainObservable.UI_ACTIONS_CHANNEL);
                    break;
                }
            }
        }).start();
    }

    private void createChatEntry(String chatID, String name, String peerAddress) {
        AppHelper.getChatDB().chatDao().addChat(new ChatEntity(chatID, name, peerAddress, ""));
    }

    private void handleIncomingChat(String chatID, PeerAddress chatStarterAddress) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", NetworkActions.SUCCESSFULL_CREATE_CHAT);
        jsonObject.addProperty("chatID", chatID);
        jsonObject.addProperty("senderID", AppHelper.getPeerID());
        jsonObject.addProperty("senderAddress", PrepareData.prepareToStore(peerDHT.peerAddress()));
        AppHelper.getPeerDHT().peer().sendDirect(chatStarterAddress).object(gson.toJson(jsonObject)).start().awaitUninterruptibly();
    }
}
