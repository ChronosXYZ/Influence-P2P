package io.github.chronosx88.influence.helpers;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.Map;

import io.github.chronosx88.influence.contracts.observer.NetworkObserver;
import io.github.chronosx88.influence.helpers.actions.NetworkActions;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.models.NewChatRequestMessage;
import io.github.chronosx88.influence.models.SendMessage;
import io.github.chronosx88.influence.models.SuccessfullySentMessage;

public class NetworkHandler implements NetworkObserver {
    private final static String LOG_TAG = "NetworkHandler";
    private static Gson gson = new Gson();
    private static PeerDHT peerDHT = AppHelper.getPeerDHT();
    private static KeyPairManager keyPairManager = new KeyPairManager();

    public NetworkHandler() {
        AppHelper.getObservable().register(this);
    }

    @Override
    public void handleEvent(Object object) {
        new Thread(() -> {
            switch (getMessageAction((String) object)) {
                case NetworkActions.CREATE_CHAT: {
                    NewChatRequestMessage newChatRequestMessage = gson.fromJson((String) object, NewChatRequestMessage.class);
                    LocalDBWrapper.createChatEntry(newChatRequestMessage.getChatID(), newChatRequestMessage.getSenderID(), newChatRequestMessage.getSenderPeerAddress());
                    handleIncomingChatRequest(newChatRequestMessage.getChatID(), newChatRequestMessage.getSenderPeerAddress());
                    ObservableUtils.notifyUI(UIActions.NEW_CHAT);
                    break;
                }

                case NetworkActions.SUCCESSFULL_CREATE_CHAT: {
                    NewChatRequestMessage newChatRequestMessage = gson.fromJson((String) object, NewChatRequestMessage.class);
                    LocalDBWrapper.createChatEntry(newChatRequestMessage.getChatID(), newChatRequestMessage.getSenderID(), newChatRequestMessage.getSenderPeerAddress());
                    ObservableUtils.notifyUI(UIActions.SUCCESSFUL_CREATE_CHAT);
                    break;
                }

                case NetworkActions.NEW_MESSAGE: {
                    SendMessage sendMessage = gson.fromJson((String) object, SendMessage.class);
                    long messageID = LocalDBWrapper.createMessageEntry(sendMessage.getMessageType(), sendMessage.getChatID(), sendMessage.getSenderID(), sendMessage.getText());
                    ObservableUtils.notifyUI(UIActions.MESSAGE_RECEIVED, messageID);
                    sendMessageReceived(sendMessage);
                    break;
                }

                case NetworkActions.MESSAGE_SENT: {
                    SuccessfullySentMessage successfullySentMessage = gson.fromJson((String) object, SuccessfullySentMessage.class);
                    LocalDBWrapper.updateChatEntry(successfullySentMessage.getMessageID(), true);
                }
            }
        }).start();
    }

    public static int getMessageAction(String json) {
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        return jsonObject.get("action").getAsInt();
    }


    private void handleIncomingChatRequest(String chatID, PeerAddress chatStarterAddress) {
        NewChatRequestMessage newChatRequestMessage = new NewChatRequestMessage(chatID, AppHelper.getPeerID(), peerDHT.peerAddress());
        newChatRequestMessage.setAction(NetworkActions.SUCCESSFULL_CREATE_CHAT);
        AppHelper.getPeerDHT().peer().sendDirect(chatStarterAddress).object(gson.toJson(newChatRequestMessage)).start().awaitUninterruptibly();
    }

    public static void handlePendingChats() {
        Map<Number640, Data> pendingChats = P2PUtils.get(AppHelper.getPeerID() + "_pendingChats");
        if(pendingChats != null) {
            for(Map.Entry<Number640, Data> entry : pendingChats.entrySet()) {
                NewChatRequestMessage newChatRequestMessage = null;
                try {
                    newChatRequestMessage = gson.fromJson((String) entry.getValue().object(), NewChatRequestMessage.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                LocalDBWrapper.createChatEntry(
                        newChatRequestMessage.getChatID(),
                        newChatRequestMessage.getSenderID(),
                        newChatRequestMessage.getSenderPeerAddress()
                );

                NewChatRequestMessage newChatRequestReply = new NewChatRequestMessage(newChatRequestMessage.getChatID(), AppHelper.getPeerID(), peerDHT.peerAddress());
                newChatRequestReply.setAction(NetworkActions.SUCCESSFULL_CREATE_CHAT);
                if(P2PUtils.ping(newChatRequestMessage.getSenderPeerAddress())) {
                    peerDHT
                            .peer()
                            .sendDirect(newChatRequestMessage.getSenderPeerAddress())
                            .object(gson.toJson(newChatRequestReply))
                            .start()
                            .awaitUninterruptibly();
                } else {
                    try {
                        if(P2PUtils.put(newChatRequestMessage.getSenderID() + "_pendingAcceptedChats", newChatRequestReply.getChatID(), new Data(gson.toJson(newChatRequestReply)))) {
                            Log.i(LOG_TAG, "# Successfully put message SUCCESSFULLY_CREATE_CHAT in " + newChatRequestMessage.getSenderID() + "_pendingAcceptedChats, because receiver is offline.");
                        } else {
                            Log.e(LOG_TAG, "# Failed to put message SUCCESSFULLY_CREATE_CHAT in " + newChatRequestMessage.getSenderID() + "_pendingAcceptedChats.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    peerDHT.remove(Number160.createHash(AppHelper.getPeerID() + "_pendingChats"))
                            .contentKey(Number160.createHash(newChatRequestMessage.getChatID()))
                            .start()
                            .awaitUninterruptibly();
                }

                ObservableUtils.notifyUI(UIActions.SUCCESSFUL_CREATE_CHAT);
            }
        }
    }

    public static void handlePendingAcceptedChats() {
       Map<Number640, Data> pendingAcceptedChats = P2PUtils.get(AppHelper.getPeerID() + "_pendingAcceptedChats");
        if(pendingAcceptedChats != null) {
            for(Map.Entry<Number640, Data> entry : pendingAcceptedChats.entrySet()) {
                NewChatRequestMessage newChatRequestMessage = null;

                try {
                    newChatRequestMessage = gson.fromJson((String) entry.getValue().object(), NewChatRequestMessage.class);
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }

                /*LocalDBWrapper.createChatEntry(
                        newChatRequestMessage.getMessageID(),
                        newChatRequestMessage.getSenderID(),
                        newChatRequestMessage.getSenderPeerAddress()
                );*/

                Log.i(LOG_TAG, "Chat " + newChatRequestMessage.getChatID() + " successfully accepted!");

                peerDHT.remove(Number160.createHash(AppHelper.getPeerID() + "_pendingAcceptedChats"))
                        .contentKey(Number160.createHash(newChatRequestMessage.getChatID()))
                        .start()
                        .awaitUninterruptibly();

                ObservableUtils.notifyUI(UIActions.SUCCESSFUL_CREATE_CHAT);
            }
        }
    }

    private void sendMessageReceived(SendMessage sendMessage) {
        P2PUtils.send(sendMessage.getSenderPeerAddress(), gson.toJson(new SuccessfullySentMessage(AppHelper.getPeerID(), AppHelper.getPeerDHT().peerAddress(), sendMessage.getMessageID())));
    }
}
