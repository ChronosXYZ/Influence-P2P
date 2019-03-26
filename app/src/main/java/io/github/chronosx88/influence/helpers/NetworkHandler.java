package io.github.chronosx88.influence.helpers;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FuturePing;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.github.chronosx88.influence.contracts.observer.NetworkObserver;
import io.github.chronosx88.influence.helpers.actions.NetworkActions;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.models.NewChatRequestMessage;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

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
        FutureGet futureGetPendingChats = peerDHT
                .get(Number160.createHash(AppHelper.getPeerID() + "_pendingChats"))
                .all()
                .start()
                .awaitUninterruptibly();
        if(!futureGetPendingChats.isEmpty()) {
            for(Map.Entry<Number640, Data> entry : futureGetPendingChats.dataMap().entrySet()) {
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
                        FuturePut put = peerDHT.put(Number160.createHash(newChatRequestMessage.getSenderID() + "_pendingAcceptedChats"))
                                .data(Number160.createHash(newChatRequestReply.getChatID()), new Data(gson.toJson(newChatRequestReply)))
                                .start()
                                .awaitUninterruptibly();
                        if(put.isSuccess()) {
                            Log.i(LOG_TAG, "# Successfully put message SUCCESSFULLY_CREATE_CHAT in " + newChatRequestMessage.getSenderID() + "_pendingAcceptedChats, because receiver is offline.");
                        } else {
                            Log.e(LOG_TAG, "# Failed to put message SUCCESSFULLY_CREATE_CHAT in " + newChatRequestMessage.getSenderID() + "_pendingAcceptedChats. Reason: " + put.failedReason());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    peerDHT.remove(Number160.createHash(AppHelper.getPeerID() + "_pendingChats"))
                            .contentKey(Number160.createHash(newChatRequestMessage.getChatID()))
                            .start()
                            .awaitUninterruptibly();
                }

                ObservableUtils.notifyUI(UIActions.NEW_CHAT);
            }
        }
    }

    public static void handlePendingAcceptedChats() {
        FutureGet futureGetPendingAcceptedChats = peerDHT
                .get(Number160.createHash(AppHelper.getPeerID() + "_pendingAcceptedChats"))
                .all()
                .start()
                .awaitUninterruptibly();
        if(!futureGetPendingAcceptedChats.isEmpty()) {
            for(Map.Entry<Number640, Data> entry : futureGetPendingAcceptedChats.dataMap().entrySet()) {
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

                peerDHT.remove(Number160.createHash(AppHelper.getPeerID() + "_pendingAcceptedChats"))
                        .contentKey(Number160.createHash(newChatRequestMessage.getChatID()))
                        .start()
                        .awaitUninterruptibly();

                ObservableUtils.notifyUI(UIActions.NEW_CHAT);
            }
        }
    }
}
