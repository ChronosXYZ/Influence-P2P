package io.github.chronosx88.influence.models;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;

import io.github.chronosx88.influence.helpers.actions.NetworkActions;

public class SendMessage extends BasicNetworkMessage implements Serializable {
    private long messageID;
    private long timestamp;
    private int messageType;
    private String chatID;
    private String text;

    public SendMessage(String senderID, PeerAddress senderPeerAddress, long messageID,long timestamp, int messageType, String chatID, String text) {
        super(NetworkActions.NEW_MESSAGE, senderID, senderPeerAddress);
        this.messageID = messageID;
        this.timestamp = timestamp;
        this.messageType = messageType;
        this.chatID = chatID;
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getMessageType() {
        return messageType;
    }

    public String getChatID() {
        return chatID;
    }

    public String getText() {
        return text;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getMessageID() {
        return messageID;
    }

    public void setMessageID(long messageID) {
        this.messageID = messageID;
    }
}
