package io.github.chronosx88.influence.models;

import java.io.Serializable;
import java.util.UUID;

import io.github.chronosx88.influence.helpers.actions.NetworkActions;

public class JoinChatMessage extends BasicNetworkMessage implements Serializable {
    private String chatID;

    public JoinChatMessage(String senderID, String username, String chatID, long timestamp) {
        super(NetworkActions.JOIN_CHAT, UUID.randomUUID().toString(), senderID, username, timestamp);
        this.chatID = chatID;
    }

    public String getChatID() {
        return chatID;
    }
}
