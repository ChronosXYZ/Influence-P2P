package io.github.chronosx88.influence.models;

import java.io.Serializable;

public class ChatMember implements Serializable {
    private String username;
    private String peerID;

    public ChatMember(String username, String peerID) {
        this.username = username;
        this.peerID = peerID;
    }

    public String getUsername() {
        return username;
    }

    public String getPeerID() {
        return peerID;
    }
}
