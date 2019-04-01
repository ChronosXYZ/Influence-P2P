package io.github.chronosx88.influence.models;

import java.io.Serializable;

import io.github.chronosx88.influence.helpers.actions.NetworkActions;

public class TextMessage extends BasicNetworkMessage implements Serializable {
    private String chatID; // Chat ID
    private String text; // Message text
    private boolean isRead; // Message Read Indicator

    public TextMessage(String senderID, String messageID, String chatID, String username, long timestamp, String text, boolean isRead) {
        super(NetworkActions.TEXT_MESSAGE, messageID, senderID, username, timestamp);
        this.chatID = chatID;
        this.text = text;
        this.isRead = isRead;
    }

    public String getChatID() {
        return chatID;
    }

    public String getText() {
        return text;
    }

    public boolean isRead() {
        return isRead;
    }
}
