package io.github.chronosx88.influence.models;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;

/**
 * Абстрактный класс-модель для любых сообщений, которые передаются по DHT-сети
 */
public class BasicNetworkMessage implements Serializable {
    private int action;
    private String senderID;
    private PeerAddress senderPeerAddress;

    public BasicNetworkMessage() {
        //
    }

    public BasicNetworkMessage(int action, String senderID, PeerAddress senderPeerAddress) {
        this.action = action;
        this.senderID = senderID;
        this.senderPeerAddress = senderPeerAddress;
    }

    public int getAction() {
        return action;
    }

    public String getSenderID() {
        return senderID;
    }

    public PeerAddress getSenderPeerAddress() {
        return senderPeerAddress;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public void setSenderPeerAddress(PeerAddress senderPeerAddress) {
        this.senderPeerAddress = senderPeerAddress;
    }
}
