package io.github.chronosx88.influence.models;

import java.io.Serializable;

/**
 * Абстрактный класс-модель для любых сообщений, которые передаются по DHT-сети
 */
public class BasicNetworkMessage implements Serializable {
    private int action;
    private String messageID;
    private String senderID;
    private String username;
    private long timestamp;

    public BasicNetworkMessage() {
        //
    }

    public BasicNetworkMessage(int action, String messageID, String senderID, String username, long timestamp) {
        this.action = action;
        this.senderID = senderID;
        this.username = username;
        this.messageID = messageID;
        this.timestamp = timestamp;
    }

    public int getAction() {
        return action;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getUsername() {
        return username;
    }

    public String getMessageID() {
        return messageID;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
