package io.github.chronosx88.influence.logic;

import android.util.Log;

import com.google.gson.Gson;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.helpers.KeyPairManager;
import io.github.chronosx88.influence.helpers.LocalDBWrapper;
import io.github.chronosx88.influence.helpers.ObservableUtils;
import io.github.chronosx88.influence.helpers.P2PUtils;
import io.github.chronosx88.influence.helpers.actions.UIActions;
import io.github.chronosx88.influence.models.ChatMetadata;
import io.github.chronosx88.influence.models.NewChatRequestMessage;
import io.github.chronosx88.influence.models.PublicUserProfile;

public class StartChatLogic implements CoreContracts.IStartChatLogicContract {
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
    public void sendStartChatMessage(String username) {
        if(peerDHT == null) {
            ObservableUtils.notifyUI(UIActions.NODE_IS_OFFLINE);
            return;
        }

        new Thread(() -> {
            String peerID = getPeerIDByUsername(username);
            if(peerID == null) {
                ObservableUtils.notifyUI(UIActions.PEER_NOT_EXIST);
                return;
            }
            PublicUserProfile recipientPublicProfile = getPublicProfile(peerID);
            if(recipientPublicProfile == null) {
                ObservableUtils.notifyUI(UIActions.PEER_NOT_EXIST);
                return;
            }

            NewChatRequestMessage newChatRequestMessage = new NewChatRequestMessage(UUID.randomUUID().toString(), UUID.randomUUID().toString(), AppHelper.getPeerID(), AppHelper.getUsername(), System.currentTimeMillis(), 0);
            try {
                if(P2PUtils.put(peerID + "_pendingChats", newChatRequestMessage.getChatID(), new Data(gson.toJson(newChatRequestMessage)))) {
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
