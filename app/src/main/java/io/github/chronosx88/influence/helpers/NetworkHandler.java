package io.github.chronosx88.influence.helpers;

import com.google.gson.Gson;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import io.github.chronosx88.influence.contracts.observer.INetworkObserver;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.models.ChatMember;
import io.github.chronosx88.influence.models.JoinChatMessage;
import io.github.chronosx88.influence.models.NewChatRequestMessage;

public class NetworkHandler implements INetworkObserver {
    private final static String LOG_TAG = "NetworkHandler";
    private static Gson gson = new Gson();
    private static PeerDHT peerDHT = AppHelper.getPeerDHT();
    private static KeyPairManager keyPairManager = new KeyPairManager();

    public NetworkHandler() {
        AppHelper.getObservable().register(this);
    }

    @Override
    public void handleEvent(Object object) {
        // Empty
    }



    public static void handlePendingChatRequests() {
        Map<Number640, Data> pendingChats = P2PUtils.get(AppHelper.getPeerID() + "_pendingChats");
        if (pendingChats != null) {
            for (Map.Entry<Number640, Data> entry : pendingChats.entrySet()) {
                NewChatRequestMessage newChatRequestMessage = null;
                try {
                    newChatRequestMessage = gson.fromJson((String) entry.getValue().object(), NewChatRequestMessage.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ChatMember chatMember = new ChatMember(AppHelper.getPeerID(), AppHelper.getPeerID());
                Data putData = null;
                try {
                    putData = new Data(gson.toJson(chatMember)).protectEntry(keyPairManager.openMainKeyPair());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                P2PUtils.put(newChatRequestMessage.getChatID() + "_members", AppHelper.getPeerID(), putData);

                LocalDBWrapper.createChatEntry(
                        newChatRequestMessage.getChatID(),
                        newChatRequestMessage.getUsername(),
                        newChatRequestMessage.getChatID() + "_metadata",
                        newChatRequestMessage.getChatID() + "_members",
                        newChatRequestMessage.getChunkID()
                );

                P2PUtils.remove(AppHelper.getPeerID() + "_pendingChats", newChatRequestMessage.getChatID());
                String messageID = UUID.randomUUID().toString();
                try {
                    P2PUtils.put(newChatRequestMessage.getChatID() + "_messages", messageID, new Data(gson.toJson(new JoinChatMessage(AppHelper.getPeerID(), AppHelper.getUsername() == null ? AppHelper.getPeerID() : AppHelper.getUsername(), newChatRequestMessage.getChatID(), System.currentTimeMillis()))).protectEntry(keyPairManager.openMainKeyPair()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ObservableUtils.notifyUI(UIActions.SUCCESSFUL_CREATE_CHAT);
            }
        }
    }

    public static void handleNewChat(String chatID) {
        Data newChat = P2PUtils.get(AppHelper.getPeerID() + "_pendingChats", chatID);
        if (newChat != null) {
            NewChatRequestMessage newChatRequestMessage = null;
            try {
                newChatRequestMessage = gson.fromJson((String) newChat.object(), NewChatRequestMessage.class);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ChatMember chatMember = new ChatMember(AppHelper.getPeerID(), AppHelper.getPeerID());
            Data putData = null;
            try {
                putData = new Data(gson.toJson(chatMember)).protectEntry(keyPairManager.openMainKeyPair());
            } catch (IOException e) {
                e.printStackTrace();
            }

            P2PUtils.put(newChatRequestMessage.getChatID() + "_members", AppHelper.getPeerID(), putData);

            LocalDBWrapper.createChatEntry(
                    newChatRequestMessage.getChatID(),
                    newChatRequestMessage.getUsername(),
                    newChatRequestMessage.getChatID() + "_metadata",
                    newChatRequestMessage.getChatID() + "_members",
                    newChatRequestMessage.getChunkID()
            );

            P2PUtils.remove(AppHelper.getPeerID() + "_pendingChats", newChatRequestMessage.getChatID());
            String messageID = UUID.randomUUID().toString();
            try {
                P2PUtils.put(newChatRequestMessage.getChatID() + "_messages", messageID, new Data(gson.toJson(new JoinChatMessage(AppHelper.getPeerID(), AppHelper.getUsername() == null ? AppHelper.getPeerID() : AppHelper.getUsername(), newChatRequestMessage.getChatID(), System.currentTimeMillis()))).protectEntry(keyPairManager.openMainKeyPair()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ObservableUtils.notifyUI(UIActions.SUCCESSFUL_CREATE_CHAT);
        }
    }
}