package io.github.chronosx88.influence.models;

import java.io.Serializable;

import io.github.chronosx88.influence.helpers.actions.NetworkActions;

public class NewChatRequestMessage extends BasicNetworkMessage implements Serializable {
    private String chatID;
    private int chunkID;

    public NewChatRequestMessage(String messageID, String chatID, String senderID, String username, long timestamp, int chunkID) {
        super(NetworkActions.CREATE_CHAT, messageID, senderID, username, timestamp);
        this.chatID = chatID;
        this.chunkID = chunkID;
    }

    public String getChatID() {
        return chatID;
    }

    public int getChunkID() {
        return chunkID;
    }
}
